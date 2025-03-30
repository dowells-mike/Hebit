import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import { User, ProductivityMetrics } from '../models';
import { AuthRequest } from '../types';

/**
 * @desc    Get user profile
 * @route   GET /api/users/profile
 * @access  Private
 */
export const getUserProfile = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  // Get user profile without password
  const user = await User.findById(userId).select('-password');
  
  if (!user) {
    throw new AppError('User not found', 404);
  }
  
  res.status(200).json(user);
});

/**
 * @desc    Update user profile
 * @route   PUT /api/users/profile
 * @access  Private
 */
export const updateUserProfile = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  // Find the user
  const user = await User.findById(userId);
  
  if (!user) {
    throw new AppError('User not found', 404);
  }
  
  // Don't allow updates to sensitive fields
  const allowedUpdates = ['name', 'username', 'bio', 'avatarUrl', 'coverPhotoUrl', 'timezone'];
  const updates: { [key: string]: any } = {};
  
  Object.keys(req.body).forEach(key => {
    if (allowedUpdates.includes(key)) {
      updates[key] = req.body[key];
    }
  });
  
  // If no allowed updates, return the current user
  if (Object.keys(updates).length === 0) {
    return res.status(200).json(user);
  }
  
  // Apply updates
  const updatedUser = await User.findByIdAndUpdate(
    userId,
    { $set: updates },
    { new: true, runValidators: true }
  ).select('-password');
  
  res.status(200).json(updatedUser);
});

/**
 * @desc    Update user settings
 * @route   PUT /api/users/settings
 * @access  Private
 */
export const updateUserSettings = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  // Find the user
  const user = await User.findById(userId);
  
  if (!user) {
    throw new AppError('User not found', 404);
  }
  
  // Validate settings object
  if (!req.body.settings || typeof req.body.settings !== 'object') {
    throw new AppError('Settings object is required', 400);
  }
  
  // Update settings
  const updatedUser = await User.findByIdAndUpdate(
    userId,
    { $set: { settings: req.body.settings } },
    { new: true, runValidators: true }
  ).select('-password');
  
  res.status(200).json(updatedUser);
});

/**
 * @desc    Update notification preferences
 * @route   PUT /api/users/notifications
 * @access  Private
 */
export const updateNotificationPreferences = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  // Find the user
  const user = await User.findById(userId);
  
  if (!user) {
    throw new AppError('User not found', 404);
  }
  
  // Validate notification preferences
  if (!req.body.notificationPreferences || typeof req.body.notificationPreferences !== 'object') {
    throw new AppError('Notification preferences object is required', 400);
  }
  
  // Update settings.notificationPreferences
  const updatedUser = await User.findByIdAndUpdate(
    userId,
    { $set: { 'settings.notificationPreferences': req.body.notificationPreferences } },
    { new: true, runValidators: true }
  ).select('-password');
  
  res.status(200).json(updatedUser);
});

/**
 * @desc    Get user productivity statistics
 * @route   GET /api/users/productivity-stats
 * @access  Private
 */
export const getProductivityStats = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  // Default to last 30 days if no date range is provided
  const endDate = req.query.endDate ? new Date(req.query.endDate as string) : new Date();
  const startDate = req.query.startDate 
    ? new Date(req.query.startDate as string) 
    : new Date(endDate.getTime() - 30 * 24 * 60 * 60 * 1000); // 30 days ago
  
  // Validate dates
  if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
    throw new AppError('Invalid date format', 400);
  }
  
  // Get productivity metrics for date range
  const metrics = await ProductivityMetrics.find({
    user: userId,
    date: { $gte: startDate, $lte: endDate }
  }).sort({ date: 1 });
  
  // Calculate aggregate statistics
  const stats = {
    totalTasksCompleted: 0,
    totalFocusTime: 0,
    averageProductivityScore: 0,
    bestDay: null as any,
    worstDay: null as any,
    dailyStats: metrics.map(m => ({
      date: m.date,
      tasksCompleted: m.tasksCompleted,
      focusTime: m.focusTime,
      productivityScore: m.productivityScore
    }))
  };
  
  // Process metrics
  if (metrics.length > 0) {
    // Calculate totals
    metrics.forEach(m => {
      stats.totalTasksCompleted += m.tasksCompleted;
      stats.totalFocusTime += m.focusTime;
    });
    
    // Calculate average productivity score
    const totalScore = metrics.reduce((sum, m) => sum + m.productivityScore, 0);
    stats.averageProductivityScore = totalScore / metrics.length;
    
    // Find best and worst days
    let bestScore = -1;
    let worstScore = 101; // Higher than max possible score
    
    metrics.forEach(m => {
      if (m.productivityScore > bestScore) {
        bestScore = m.productivityScore;
        stats.bestDay = {
          date: m.date,
          score: m.productivityScore,
          tasksCompleted: m.tasksCompleted
        };
      }
      
      if (m.productivityScore < worstScore) {
        worstScore = m.productivityScore;
        stats.worstDay = {
          date: m.date,
          score: m.productivityScore,
          tasksCompleted: m.tasksCompleted
        };
      }
    });
  }
  
  // Update user productivity metrics in user model
  await User.findByIdAndUpdate(userId, {
    $set: {
      'productivity.completionRate': stats.averageProductivityScore
    }
  });
  
  res.status(200).json(stats);
});

/**
 * @desc    Update peak productivity hours
 * @route   PUT /api/users/productivity-hours
 * @access  Private
 */
export const updateProductivityHours = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  // Find the user
  const user = await User.findById(userId);
  
  if (!user) {
    throw new AppError('User not found', 404);
  }
  
  // Validate peak hours array
  if (!req.body.peakHours || !Array.isArray(req.body.peakHours)) {
    throw new AppError('Peak hours array is required', 400);
  }
  
  // Validate each hour is between 0-23
  for (const hour of req.body.peakHours) {
    if (typeof hour !== 'number' || hour < 0 || hour > 23) {
      throw new AppError('Peak hours must be numbers between 0 and 23', 400);
    }
  }
  
  // Update productivity.peakHours
  const updatedUser = await User.findByIdAndUpdate(
    userId,
    { $set: { 'productivity.peakHours': req.body.peakHours } },
    { new: true, runValidators: true }
  ).select('-password');
  
  res.status(200).json(updatedUser);
}); 