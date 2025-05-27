import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import { Achievement, UserAchievement } from '../models';
import { AuthRequest, AchievementDocument, UserAchievementDocument } from '../types';

/**
 * @desc    Get all defined achievements (public view)
 * @route   GET /api/achievements
 * @access  Public (or Private if all achievements are sensitive)
 */
export const getAllPublicAchievements = catchAsync(async (req: AuthRequest, res: Response) => {
  // For now, return non-secret achievements. 
  // Or, if this is an admin endpoint, return all.
  const achievements = await Achievement.find({ secret: { $ne: true } }).lean();
  res.status(200).json(achievements);
});

/**
 * @desc    Get all achievements for the authenticated user (earned and in-progress)
 * @route   GET /api/achievements/my
 * @access  Private
 */
export const getMyAchievements = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  if (!userId) {
    throw new AppError('User not authenticated', 401);
  }

  const allAchievements = await Achievement.find().lean() as AchievementDocument[];
  const userAchievements = await UserAchievement.find({ user: userId }).lean() as UserAchievementDocument[];

  const userAchievementsMap = new Map<string, UserAchievementDocument>();
  userAchievements.forEach(ua => userAchievementsMap.set(ua.achievement.toString(), ua));

  const myAchievementsData = allAchievements.map(ach => {
    const userAchInstance = userAchievementsMap.get(ach._id.toString());

    // If an achievement is secret and not yet earned (or no UserAchievement record exists), skip it.
    if (ach.secret && (!userAchInstance || !userAchInstance.earned)) {
      return null; 
    }

    return {
      ...ach, // Spread all properties of AchievementDocument
      userProgress: userAchInstance?.progress ?? 0,
      earned: userAchInstance?.earned ?? false,
      earnedAt: userAchInstance?.earnedAt ?? null,
      seenByUser: userAchInstance?.seenByUser ?? false, // Default to false if no UserAchievement
      userAchievementId: userAchInstance?._id?.toString() ?? null // ID of the UserAchievement document
    };
  }).filter(ach => ach !== null); // Filter out nulls (skipped secret achievements)

  res.status(200).json(myAchievementsData);
});

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

/**
 * @desc    Mark a user achievement as seen
 * @route   POST /api/achievements/my/:userAchievementId/seen
 * @access  Private
 */
export const markUserAchievementAsSeen = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const { userAchievementId } = req.params;

  if (!userId) {
    throw new AppError('User not authenticated', 401);
  }

  if (!userAchievementId) {
    throw new AppError('UserAchievement ID is required', 400);
  }

  const userAchievement = await UserAchievement.findById(userAchievementId);

  if (!userAchievement) {
    throw new AppError('User achievement record not found', 404);
  }

  // Ensure the achievement record belongs to the authenticated user
  if (userAchievement.user.toString() !== userId.toString()) {
    throw new AppError('Not authorized to update this achievement record', 403);
  }

  if (userAchievement.seenByUser) {
    // Optionally, return the record as is if already seen, or a specific message
    return res.status(200).json(userAchievement); // Already seen, no change
  }

  userAchievement.seenByUser = true;
  userAchievement.updatedAt = new Date(); // Manually update timestamp if not automatically handled by Mongoose on this specific field update path
  
  await userAchievement.save();

  // Optionally, populate the achievement details before sending back
  // For consistency, could return the same structure as getMyAchievements for a single item
  // However, for a simple POST to mark as seen, returning the updated UserAchievement is often sufficient.
  res.status(200).json(userAchievement);
}); 