import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import { Goal, ProductivityMetrics } from '../models';
import { AuthRequest, GoalDocument } from '../types';

/**
 * @desc    Get all goals for a user
 * @route   GET /api/goals
 * @access  Private
 */
export const getGoals = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  // Build the filter object
  const filter: any = { user: userId };
  
  // Add filter by status if provided
  if (req.query.status) {
    filter.status = req.query.status;
  }
  
  // Add filter by category if provided
  if (req.query.category) {
    filter.category = req.query.category;
  }
  
  // Add filter by priority if provided
  if (req.query.priority) {
    filter.priority = req.query.priority;
  }
  
  // Date range filter
  if (req.query.fromDate || req.query.toDate) {
    filter.targetDate = {};
    
    if (req.query.fromDate) {
      filter.targetDate.$gte = new Date(req.query.fromDate as string);
    }
    
    if (req.query.toDate) {
      filter.targetDate.$lte = new Date(req.query.toDate as string);
    }
  }
  
  // Time period filter
  if (req.query.timePeriod) {
    filter.timePeriod = req.query.timePeriod;
  }
  
  // Default to not showing archived goals
  if (req.query.showArchived !== 'true') {
    filter.status = { $ne: 'archived' };
  }
  
  // Sort options
  let sort: any = { createdAt: -1 }; // Default sort
  
  if (req.query.sort) {
    const sortField = req.query.sort as string;
    const sortDirection = req.query.sortDir === 'asc' ? 1 : -1;
    sort = { [sortField]: sortDirection };
  }
  
  // Execute the query with filters and sort
  const goals = await Goal.find(filter).sort(sort);
  
  res.status(200).json(goals);
});

/**
 * @desc    Get a goal by ID
 * @route   GET /api/goals/:id
 * @access  Private
 */
export const getGoalById = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const goalId = req.params.id;
  
  const goal = await Goal.findOne({ _id: goalId, user: userId });
  
  if (!goal) {
    throw new AppError('Goal not found', 404);
  }
  
  res.status(200).json(goal);
});

/**
 * @desc    Create a new goal
 * @route   POST /api/goals
 * @access  Private
 */
export const createGoal = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  if (!req.body.title) {
    throw new AppError('Title is required', 400);
  }
  
  // Default values for enhanced fields
  const goalData = {
    ...req.body,
    user: userId,
    progress: 0,
    status: req.body.status || 'not_started',
    priority: req.body.priority || 3, // Medium priority by default
    difficulty: req.body.difficulty || 3, // Medium difficulty by default
    milestones: req.body.milestones || [],
    checkIns: []
  };
  
  const goal = await Goal.create(goalData);
  
  // Update daily metrics for goal creation count
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  await ProductivityMetrics.findOneAndUpdate(
    { user: userId, date: today },
    { $inc: { goalsCreated: 1 } },
    { upsert: true, new: true }
  );
  
  res.status(201).json(goal);
});

/**
 * @desc    Update a goal
 * @route   PUT /api/goals/:id
 * @access  Private
 */
export const updateGoal = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const goalId = req.params.id;
  
  const goal = await Goal.findOne({ _id: goalId, user: userId });
  
  if (!goal) {
    throw new AppError('Goal not found', 404);
  }
  
  // Track which field is being modified for ML purposes
  const updates = { ...req.body };
  
  // Don't allow changing user
  delete updates.user;
  
  // Update the metadata with last modified field
  const changedFields = Object.keys(updates);
  if (changedFields.length > 0) {
    updates.metadata = {
      ...(goal.metadata || {}),
      lastModifiedField: changedFields[0]
    };
  }
  
  // Update the goal
  const updatedGoal = await Goal.findByIdAndUpdate(
    goalId,
    updates,
    { new: true, runValidators: true }
  );
  
  res.status(200).json(updatedGoal);
});

/**
 * @desc    Delete a goal
 * @route   DELETE /api/goals/:id
 * @access  Private
 */
export const deleteGoal = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const goalId = req.params.id;
  
  const goal = await Goal.findOne({ _id: goalId, user: userId });
  
  if (!goal) {
    throw new AppError('Goal not found', 404);
  }
  
  // Soft delete - mark as archived
  if (req.query.permanent !== 'true') {
    await Goal.findByIdAndUpdate(
      goalId,
      { status: 'archived' },
      { new: true }
    );
  } else {
    // Hard delete
    await Goal.findByIdAndDelete(goalId);
  }
  
  res.status(200).json({ success: true });
});

/**
 * @desc    Update goal progress
 * @route   PATCH /api/goals/:id/progress
 * @access  Private
 */
export const updateProgress = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const goalId = req.params.id;
  const { progress, note } = req.body;
  
  if (progress === undefined) {
    throw new AppError('Progress is required', 400);
  }
  
  if (progress < 0 || progress > 100) {
    throw new AppError('Progress must be between 0 and 100', 400);
  }
  
  const goal = await Goal.findOne({ _id: goalId, user: userId }).exec();
  
  if (!goal) {
    throw new AppError('Goal not found', 404);
  }
  
  const previousProgress = goal.progress;
  
  // Update progress
  goal.progress = progress;
  
  // Add to check-ins for tracking progress
  const checkIn = {
    date: new Date(),
    notes: note || '',
    progressUpdate: progress,
    blockers: []
  };
  
  if (!goal.checkIns) {
    // If checkIns is undefined, create a new array with the initial checkIn
    goal.set('checkIns', [checkIn]);
  } else {
    // Otherwise push to the existing array
    goal.checkIns.push(checkIn);
  }
  
  // Update status based on progress
  if (progress === 100 && goal.status !== 'completed') {
    goal.status = 'completed';
    const completedAt = new Date();
    
    // Update productivity metrics for goal completion
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    await ProductivityMetrics.findOneAndUpdate(
      { user: userId, date: today },
      { $inc: { goalsCompleted: 1 } },
      { upsert: true, new: true }
    );
    
    // Record completion context for ML using the metadata object
    if (!goal.metadata) {
      goal.metadata = {};
    }
    
    // Track completion times and patterns
    goal.set('metadata.timeToComplete', completedAt.getTime() - goal.createdAt.getTime());
    goal.set('metadata.completionHour', completedAt.getHours());
    goal.set('metadata.completionDay', completedAt.getDay());
    
  } else if (progress > 0 && progress < 100) {
    goal.status = 'in_progress';
  }
  
  await goal.save();
  
  res.status(200).json(goal);
});

/**
 * @desc    Add a milestone to a goal
 * @route   POST /api/goals/:id/milestones
 * @access  Private
 */
export const addMilestone = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const goalId = req.params.id;
  const { title, description, dueDate, completed } = req.body;
  
  if (!title) {
    throw new AppError('Milestone title is required', 400);
  }
  
  const goal = await Goal.findOne({ _id: goalId, user: userId }).exec();
  
  if (!goal) {
    throw new AppError('Goal not found', 404);
  }
  
  // Create new milestone
  const newMilestone = {
    id: new Date().getTime().toString(), // Simple ID generation
    title,
    description: description || undefined,
    dueDate: dueDate ? new Date(dueDate) : undefined,
    completed: !!completed,
    completedAt: completed ? new Date() : undefined
  };
  
  // Add milestone to array, ensuring the array exists
  if (!goal.milestones) {
    // If milestones is undefined, create it with the new milestone
    goal.set('milestones', [newMilestone]);
  } else {
    // Otherwise push to the existing array
    goal.milestones.push(newMilestone);
  }
  
  await goal.save();
  
  res.status(201).json(goal);
});

/**
 * @desc    Update a milestone
 * @route   PUT /api/goals/:id/milestones/:milestoneIndex
 * @access  Private
 */
export const updateMilestone = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const goalId = req.params.id;
  const milestoneIndex = parseInt(req.params.milestoneIndex);
  
  const { title, description, dueDate, completed } = req.body;
  
  const goal = await Goal.findOne({ _id: goalId, user: userId }).exec();
  
  if (!goal) {
    throw new AppError('Goal not found', 404);
  }
  
  // Ensure milestones array exists and index is valid
  if (!goal.milestones || !goal.milestones[milestoneIndex]) {
    throw new AppError('Milestone not found', 404);
  }
  
  // Update milestone properties
  if (title !== undefined) {
    goal.milestones[milestoneIndex].title = title;
  }
  
  if (description !== undefined) {
    goal.milestones[milestoneIndex].description = description;
  }
  
  if (dueDate !== undefined) {
    goal.milestones[milestoneIndex].dueDate = new Date(dueDate);
  }
  
  if (completed !== undefined) {
    goal.milestones[milestoneIndex].completed = completed;
    
    if (completed && !goal.milestones[milestoneIndex].completedAt) {
      goal.milestones[milestoneIndex].completedAt = new Date();
    } else if (!completed) {
      goal.milestones[milestoneIndex].completedAt = undefined;
    }
  }
  
  await goal.save();
  
  // Recalculate goal progress based on milestones
  if (goal.milestones && goal.milestones.length > 0) {
    const completedMilestones = goal.milestones.filter(m => m.completed).length;
    const newProgress = Math.round((completedMilestones / goal.milestones.length) * 100);
    
    // Only update if different from current progress
    if (newProgress !== goal.progress) {
      // Update progress directly rather than calling updateProgress again
      goal.progress = newProgress;
      
      // Update status based on progress
      if (newProgress === 100 && goal.status !== 'completed') {
        goal.status = 'completed';
      } else if (newProgress > 0 && newProgress < 100) {
        goal.status = 'in_progress';
      }
      
      await goal.save();
    }
  }
  
  res.status(200).json(goal);
});

/**
 * @desc    Get goal statistics
 * @route   GET /api/goals/stats
 * @access  Private
 */
export const getGoalStats = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  // Count goals by status
  const statusCounts = await Goal.aggregate([
    { $match: { user: userId } },
    { $group: { _id: '$status', count: { $sum: 1 } } }
  ]);
  
  // Format status counts
  const statusMap: Record<string, number> = {};
  statusCounts.forEach(item => {
    if (item._id) {
      statusMap[item._id] = item.count;
    }
  });
  
  // Get completion rate
  const totalGoals = await Goal.countDocuments({ user: userId });
  const completedGoals = await Goal.countDocuments({ user: userId, status: 'completed' });
  const completionRate = totalGoals > 0 ? (completedGoals / totalGoals) * 100 : 0;
  
  // Get average time to complete goals (in days)
  // Use a more robust query using metadata that contains completion times
  const completedGoals2Weeks = await Goal.countDocuments({ 
    user: userId, 
    status: 'completed',
    updatedAt: { $gte: new Date(Date.now() - 14 * 24 * 60 * 60 * 1000) }
  });
  
  // Get goals by time period
  const timePeriodCounts = await Goal.aggregate([
    { $match: { user: userId } },
    { $group: { _id: '$timePeriod', count: { $sum: 1 } } }
  ]);
  
  // Format time period counts
  const timePeriodMap: Record<string, number> = {};
  timePeriodCounts.forEach(item => {
    if (item._id) {
      timePeriodMap[item._id] = item.count;
    }
  });
  
  // Get goals by priority
  const priorityCounts = await Goal.aggregate([
    { $match: { user: userId } },
    { $group: { _id: '$priority', count: { $sum: 1 } } }
  ]);
  
  // Format priority counts
  const priorityMap: Record<string, number> = {};
  priorityCounts.forEach(item => {
    if (item._id) {
      priorityMap[item._id] = item.count;
    }
  });
  
  res.status(200).json({
    totalGoals,
    completedGoals,
    completionRate,
    completedLast2Weeks: completedGoals2Weeks,
    statusCounts: statusMap,
    timePeriodCounts: timePeriodMap,
    priorityCounts: priorityMap
  });
});
