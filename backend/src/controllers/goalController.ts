import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import Goal from '../models/Goal';
import { AuthRequest } from '../types';

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
  
  // Execute the query with filters and sort by recently created
  const goals = await Goal.find(filter).sort({ createdAt: -1 });
  
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
  
  const goal = await Goal.create({
    ...req.body,
    user: userId,
    progress: 0,
    status: req.body.status || 'not_started'
  });
  
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
  
  // Update the goal
  const updatedGoal = await Goal.findByIdAndUpdate(
    goalId,
    req.body,
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
  
  await Goal.findByIdAndDelete(goalId);
  
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
  const { progress } = req.body;
  
  if (progress === undefined) {
    throw new AppError('Progress is required', 400);
  }
  
  if (progress < 0 || progress > 100) {
    throw new AppError('Progress must be between 0 and 100', 400);
  }
  
  const goal = await Goal.findOne({ _id: goalId, user: userId });
  
  if (!goal) {
    throw new AppError('Goal not found', 404);
  }
  
  // Update progress
  goal.progress = progress;
  
  // Update status based on progress
  if (progress === 100) {
    goal.status = 'completed';
  } else if (progress > 0) {
    goal.status = 'in_progress';
  }
  
  await goal.save();
  
  res.status(200).json(goal);
});
