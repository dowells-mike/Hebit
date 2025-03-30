import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import { ProductivityMetrics, Task, Habit, Goal } from '../models';
import { AuthRequest } from '../types';

/**
 * @desc    Get productivity metrics for a date range
 * @route   GET /api/productivity
 * @access  Private
 */
export const getProductivityMetrics = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  // Default to last 7 days if no date range is provided
  const endDate = req.query.endDate ? new Date(req.query.endDate as string) : new Date();
  const startDate = req.query.startDate 
    ? new Date(req.query.startDate as string)
    : new Date(endDate.getTime() - 7 * 24 * 60 * 60 * 1000); // 7 days ago
  
  // Validate dates
  if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
    throw new AppError('Invalid date format', 400);
  }
  
  // Find metrics in date range
  const metrics = await ProductivityMetrics.find({
    user: userId,
    date: { $gte: startDate, $lte: endDate }
  }).sort({ date: 1 });
  
  res.status(200).json(metrics);
});

/**
 * @desc    Get productivity metrics for a specific day
 * @route   GET /api/productivity/:date
 * @access  Private
 */
export const getDailyProductivityMetrics = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const dateParam = req.params.date;
  
  // Validate date format
  const date = new Date(dateParam);
  if (isNaN(date.getTime())) {
    throw new AppError('Invalid date format. Use YYYY-MM-DD', 400);
  }
  
  // Set to start of day
  date.setHours(0, 0, 0, 0);
  
  // Find or create metrics for this day
  let metrics = await ProductivityMetrics.findOne({
    user: userId,
    date
  });
  
  if (!metrics) {
    // If no metrics exist for this day, return empty data
    res.status(200).json({
      user: userId,
      date,
      tasksCompleted: 0,
      tasksCreated: 0,
      habitCompletionRate: 0,
      goalProgress: [],
      focusTime: 0,
      productivityScore: 0
    });
    return;
  }
  
  res.status(200).json(metrics);
});

/**
 * @desc    Track focus time
 * @route   POST /api/productivity/focus
 * @access  Private
 */
export const trackFocusTime = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  if (!req.body.minutes || typeof req.body.minutes !== 'number' || req.body.minutes <= 0) {
    throw new AppError('Valid focus time in minutes is required', 400);
  }
  
  const minutes = Math.round(req.body.minutes);
  const date = req.body.date ? new Date(req.body.date) : new Date();
  
  // Validate date
  if (isNaN(date.getTime())) {
    throw new AppError('Invalid date format', 400);
  }
  
  // Set to start of day
  date.setHours(0, 0, 0, 0);
  
  // Find or create metrics for this day
  let metrics = await ProductivityMetrics.findOne({
    user: userId,
    date
  });
  
  if (!metrics) {
    // Create new metrics
    metrics = await ProductivityMetrics.create({
      user: userId,
      date,
      focusTime: minutes
    });
  } else {
    // Update existing metrics
    metrics.focusTime += minutes;
    await metrics.save();
  }
  
  res.status(200).json(metrics);
});

/**
 * @desc    Submit daily rating
 * @route   POST /api/productivity/rating
 * @access  Private
 */
export const submitDailyRating = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  if (!req.body.rating || typeof req.body.rating !== 'number' || req.body.rating < 1 || req.body.rating > 5) {
    throw new AppError('Valid rating (1-5) is required', 400);
  }
  
  const rating = Math.round(req.body.rating);
  const date = req.body.date ? new Date(req.body.date) : new Date();
  
  // Validate date
  if (isNaN(date.getTime())) {
    throw new AppError('Invalid date format', 400);
  }
  
  // Set to start of day
  date.setHours(0, 0, 0, 0);
  
  // Find or create metrics for this day
  let metrics = await ProductivityMetrics.findOne({
    user: userId,
    date
  });
  
  if (!metrics) {
    // Create new metrics
    metrics = await ProductivityMetrics.create({
      user: userId,
      date,
      dayRating: rating
    });
  } else {
    // Update existing metrics
    metrics.dayRating = rating;
    await metrics.save();
  }
  
  res.status(200).json(metrics);
});

/**
 * @desc    Generate daily metrics (admin or scheduled job)
 * @route   POST /api/productivity/generate
 * @access  Private/Admin
 */
export const generateDailyMetrics = catchAsync(async (req: AuthRequest, res: Response) => {
  // This would typically be called by a scheduled job, but can also be triggered manually
  const date = req.body.date ? new Date(req.body.date) : new Date();
  date.setHours(0, 0, 0, 0);
  
  const userId = req.user?._id;
  
  // Get completed tasks for the day
  const endOfDay = new Date(date);
  endOfDay.setHours(23, 59, 59, 999);
  
  // Tasks completed today
  const tasksCompleted = await Task.countDocuments({
    user: userId,
    completedAt: { $gte: date, $lte: endOfDay }
  });
  
  // Tasks created today
  const tasksCreated = await Task.countDocuments({
    user: userId,
    createdAt: { $gte: date, $lte: endOfDay }
  });
  
  // Habit completion rate
  const habits = await Habit.find({
    user: userId,
    // Filter to habits that were active on this date
    startDate: { $lte: date },
    $or: [
      { endDate: { $gte: date } },
      { endDate: null }
    ]
  });
  
  let dueHabits = 0;
  let completedHabits = 0;
  
  // Count habits that were due on this day based on frequency
  habits.forEach(habit => {
    // Simple check - would be more complex in production
    const isDue = true; // Placeholder for actual frequency check logic
    
    if (isDue) {
      dueHabits++;
      
      // Check if habit was completed on this date
      const wasCompleted = habit.completionHistory.some(entry => {
        const entryDate = new Date(entry.date);
        return entryDate.toDateString() === date.toDateString() && entry.completed;
      });
      
      if (wasCompleted) {
        completedHabits++;
      }
    }
  });
  
  const habitCompletionRate = dueHabits === 0 ? 0 : (completedHabits / dueHabits) * 100;
  
  // Goal progress
  const goals = await Goal.find({
    user: userId,
    status: { $in: ['not_started', 'in_progress'] }
  });
  
  const goalProgress = goals.map(goal => ({
    goalId: goal._id,
    progress: goal.progress
  }));
  
  // Calculate productivity score (simplified version)
  // In a real app, this would be more sophisticated
  const taskScore = tasksCompleted * 10; // 10 points per completed task
  const habitScore = habitCompletionRate; // 0-100 points based on habit completion
  const productivityScore = Math.min(100, (taskScore + habitScore) / 2); // Average, max 100
  
  // Find or create productivity metrics for the day
  let metrics = await ProductivityMetrics.findOne({
    user: userId,
    date
  });
  
  if (metrics) {
    // Update existing metrics
    metrics.tasksCompleted = tasksCompleted;
    metrics.tasksCreated = tasksCreated;
    metrics.habitCompletionRate = habitCompletionRate;
    metrics.goalProgress = goalProgress;
    metrics.productivityScore = productivityScore;
    await metrics.save();
  } else {
    // Create new metrics
    metrics = await ProductivityMetrics.create({
      user: userId,
      date,
      tasksCompleted,
      tasksCreated,
      habitCompletionRate,
      goalProgress,
      productivityScore,
      focusTime: 0
    });
  }
  
  res.status(200).json(metrics);
}); 