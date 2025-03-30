import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import { Achievement, UserAchievement } from '../models';
import { AuthRequest } from '../types';

/**
 * @desc    Get all achievements
 * @route   GET /api/achievements
 * @access  Private
 */
export const getAchievements = catchAsync(async (req: AuthRequest, res: Response) => {
  // Get all achievements
  const achievements = await Achievement.find().sort({ category: 1, points: 1 });
  
  // Get user's progress for these achievements
  const userId = req.user?._id;
  const userAchievements = await UserAchievement.find({ user: userId });
  
  // Map progress to achievements
  const achievementsWithProgress = achievements.map(achievement => {
    const userAchievement = userAchievements.find(
      ua => ua.achievement.toString() === achievement._id.toString()
    );
    
    return {
      ...achievement.toObject(),
      progress: userAchievement ? userAchievement.progress : 0,
      earned: userAchievement ? userAchievement.earned : false,
      earnedAt: userAchievement ? userAchievement.earnedAt : null
    };
  });
  
  res.status(200).json(achievementsWithProgress);
});

/**
 * @desc    Get achievements by category
 * @route   GET /api/achievements/category/:category
 * @access  Private
 */
export const getAchievementsByCategory = catchAsync(async (req: AuthRequest, res: Response) => {
  const { category } = req.params;
  
  // Validate category
  if (!['tasks', 'habits', 'goals', 'special'].includes(category)) {
    throw new AppError('Invalid category', 400);
  }
  
  // Get achievements in this category
  const achievements = await Achievement.find({ category }).sort({ points: 1 });
  
  // Get user's progress for these achievements
  const userId = req.user?._id;
  const userAchievements = await UserAchievement.find({ user: userId });
  
  // Map progress to achievements
  const achievementsWithProgress = achievements.map(achievement => {
    const userAchievement = userAchievements.find(
      ua => ua.achievement.toString() === achievement._id.toString()
    );
    
    return {
      ...achievement.toObject(),
      progress: userAchievement ? userAchievement.progress : 0,
      earned: userAchievement ? userAchievement.earned : false,
      earnedAt: userAchievement ? userAchievement.earnedAt : null
    };
  });
  
  res.status(200).json(achievementsWithProgress);
});

/**
 * @desc    Get user's earned achievements
 * @route   GET /api/achievements/earned
 * @access  Private
 */
export const getEarnedAchievements = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  // Find user achievements that are earned
  const userAchievements = await UserAchievement.find({
    user: userId,
    earned: true
  }).sort({ earnedAt: -1 });
  
  // Get full achievement details
  const achievementIds = userAchievements.map(ua => ua.achievement);
  const achievements = await Achievement.find({ _id: { $in: achievementIds } });
  
  // Combine the data
  const earnedAchievements = userAchievements.map(userAchievement => {
    const achievement = achievements.find(
      a => a._id.toString() === userAchievement.achievement.toString()
    );
    
    return {
      ...achievement?.toObject(),
      earnedAt: userAchievement.earnedAt
    };
  });
  
  res.status(200).json(earnedAchievements);
});

/**
 * @desc    Check achievement progress
 * @route   POST /api/achievements/check
 * @access  Private
 */
export const checkAchievementProgress = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  // In a real application, this would be more sophisticated
  // It would check various types of progress based on user actions
  // For simplicity, we'll just update a specific achievement if provided
  
  if (!req.body.achievementId) {
    throw new AppError('Achievement ID is required', 400);
  }
  
  const { achievementId, progress } = req.body;
  
  // Validate achievement exists
  const achievement = await Achievement.findById(achievementId);
  if (!achievement) {
    throw new AppError('Achievement not found', 404);
  }
  
  // Find or create user achievement progress
  let userAchievement = await UserAchievement.findOne({
    user: userId,
    achievement: achievementId
  });
  
  if (!userAchievement) {
    userAchievement = await UserAchievement.create({
      user: userId,
      achievement: achievementId,
      progress: progress || 0,
      earned: false
    });
  } else if (progress !== undefined) {
    // Update progress if provided
    userAchievement.progress = Math.min(100, progress);
    
    // Check if achievement is earned
    if (userAchievement.progress >= 100 && !userAchievement.earned) {
      userAchievement.earned = true;
      userAchievement.earnedAt = new Date();
    }
    
    await userAchievement.save();
  }
  
  // Return updated achievement with progress
  res.status(200).json({
    ...achievement.toObject(),
    progress: userAchievement.progress,
    earned: userAchievement.earned,
    earnedAt: userAchievement.earnedAt
  });
});

/**
 * @desc    Create a new achievement (admin only)
 * @route   POST /api/achievements
 * @access  Private/Admin
 */
export const createAchievement = catchAsync(async (req: AuthRequest, res: Response) => {
  // In production, this would be admin-only
  
  // Validate required fields
  if (!req.body.name || !req.body.description || !req.body.category || 
      !req.body.points || !req.body.icon || !req.body.criteria) {
    throw new AppError('Please provide all required fields', 400);
  }
  
  // Create achievement
  const achievement = await Achievement.create(req.body);
  
  res.status(201).json(achievement);
});

/**
 * @desc    Update an achievement (admin only)
 * @route   PUT /api/achievements/:id
 * @access  Private/Admin
 */
export const updateAchievement = catchAsync(async (req: AuthRequest, res: Response) => {
  // In production, this would be admin-only
  const achievementId = req.params.id;
  
  const achievement = await Achievement.findById(achievementId);
  if (!achievement) {
    throw new AppError('Achievement not found', 404);
  }
  
  // Update achievement
  const updatedAchievement = await Achievement.findByIdAndUpdate(
    achievementId,
    req.body,
    { new: true, runValidators: true }
  );
  
  res.status(200).json(updatedAchievement);
});

/**
 * @desc    Delete an achievement (admin only)
 * @route   DELETE /api/achievements/:id
 * @access  Private/Admin
 */
export const deleteAchievement = catchAsync(async (req: AuthRequest, res: Response) => {
  // In production, this would be admin-only
  const achievementId = req.params.id;
  
  const achievement = await Achievement.findById(achievementId);
  if (!achievement) {
    throw new AppError('Achievement not found', 404);
  }
  
  // Delete achievement and all related user progress
  await Achievement.findByIdAndDelete(achievementId);
  await UserAchievement.deleteMany({ achievement: achievementId });
  
  res.status(200).json({ success: true });
}); 