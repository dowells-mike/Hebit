import { Request, Response } from 'express';
import { AuthRequest } from '../types';
import { catchAsync, AppError } from '../middleware/errorHandler';

// In-memory goal store for development mode
const goals = new Map();
let lastGoalId = 0;

/**
 * @desc    Create a new goal
 * @route   POST /api/goals
 * @access  Private
 */
export const createGoal = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const { title, description, targetDate, relatedTasks, relatedHabits } = req.body;

  if (!title) {
    throw new AppError('Goal title is required', 400);
  }

  const goalId = (++lastGoalId).toString();
  const goal = {
    _id: goalId,
    user: userId,
    title,
    description: description || '',
    targetDate: targetDate ? new Date(targetDate) : undefined,
    progress: 0,
    status: 'not_started', // 'not_started' | 'in_progress' | 'completed'
    relatedTasks: relatedTasks || [],
    relatedHabits: relatedHabits || [],
    createdAt: new Date(),
    updatedAt: new Date()
  };

  goals.set(goalId, goal);

  res.status(201).json(goal);
});

/**
 * @desc    Get all goals for a user
 * @route   GET /api/goals
 * @access  Private
 */
export const getGoals = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const { status } = req.query;

  let userGoals = Array.from(goals.values()).filter(
    (goal: any) => goal.user === userId
  );

  // Apply filters if provided
  if (status) {
    userGoals = userGoals.filter((goal: any) => goal.status === status);
  }

  res.status(200).json(userGoals);
});

/**
 * @desc    Get a single goal by ID
 * @route   GET /api/goals/:id
 * @access  Private
 */
export const getGoalById = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const goalId = req.params.id;

  const goal = goals.get(goalId);

  if (!goal) {
    throw new AppError('Goal not found', 404);
  }

  if (goal.user !== userId) {
    throw new AppError('Not authorized to access this goal', 401);
  }

  res.status(200).json(goal);
});

/**
 * @desc    Update a goal
 * @route   PUT /api/goals/:id
 * @access  Private
 */
export const updateGoal = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const goalId = req.params.id;
  const { title, description, targetDate, progress, status, relatedTasks, relatedHabits } = req.body;

  const goal = goals.get(goalId);

  if (!goal) {
    throw new AppError('Goal not found', 404);
  }

  if (goal.user !== userId) {
    throw new AppError('Not authorized to update this goal', 401);
  }

  if (title) goal.title = title;
  if (description !== undefined) goal.description = description;
  if (targetDate) goal.targetDate = new Date(targetDate);
  if (progress !== undefined) {
    goal.progress = Math.min(Math.max(progress, 0), 100); // Ensure between 0-100
    
    // Update status based on progress
    if (goal.progress === 0) {
      goal.status = 'not_started';
    } else if (goal.progress === 100) {
      goal.status = 'completed';
    } else {
      goal.status = 'in_progress';
    }
  }
  if (status) goal.status = status;
  if (relatedTasks) goal.relatedTasks = relatedTasks;
  if (relatedHabits) goal.relatedHabits = relatedHabits;
  
  goal.updatedAt = new Date();

  goals.set(goalId, goal);

  res.status(200).json(goal);
});

/**
 * @desc    Delete a goal
 * @route   DELETE /api/goals/:id
 * @access  Private
 */
export const deleteGoal = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const goalId = req.params.id;

  const goal = goals.get(goalId);

  if (!goal) {
    throw new AppError('Goal not found', 404);
  }

  if (goal.user !== userId) {
    throw new AppError('Not authorized to delete this goal', 401);
  }

  goals.delete(goalId);

  res.status(200).json({ message: 'Goal deleted successfully' });
});

/**
 * @desc    Update goal progress
 * @route   PUT /api/goals/:id/progress
 * @access  Private
 */
export const updateGoalProgress = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const goalId = req.params.id;
  const { progress } = req.body;

  if (progress === undefined) {
    throw new AppError('Progress value is required', 400);
  }

  const goal = goals.get(goalId);

  if (!goal) {
    throw new AppError('Goal not found', 404);
  }

  if (goal.user !== userId) {
    throw new AppError('Not authorized to update this goal', 401);
  }

  // Ensure progress is between 0 and 100
  goal.progress = Math.min(Math.max(parseInt(progress), 0), 100);
  
  // Update status based on progress
  if (goal.progress === 0) {
    goal.status = 'not_started';
  } else if (goal.progress === 100) {
    goal.status = 'completed';
  } else {
    goal.status = 'in_progress';
  }
  
  goal.updatedAt = new Date();

  goals.set(goalId, goal);

  res.status(200).json(goal);
});

/**
 * @desc    Link tasks to a goal
 * @route   POST /api/goals/:id/tasks
 * @access  Private
 */
export const linkTasksToGoal = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const goalId = req.params.id;
  const { taskIds } = req.body;

  if (!taskIds || !Array.isArray(taskIds)) {
    throw new AppError('Task IDs array is required', 400);
  }

  const goal = goals.get(goalId);

  if (!goal) {
    throw new AppError('Goal not found', 404);
  }

  if (goal.user !== userId) {
    throw new AppError('Not authorized to update this goal', 401);
  }

  // Add tasks to the goal
  goal.relatedTasks = [...new Set([...goal.relatedTasks, ...taskIds])];
  goal.updatedAt = new Date();

  goals.set(goalId, goal);

  res.status(200).json(goal);
});

/**
 * @desc    Link habits to a goal
 * @route   POST /api/goals/:id/habits
 * @access  Private
 */
export const linkHabitsToGoal = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const goalId = req.params.id;
  const { habitIds } = req.body;

  if (!habitIds || !Array.isArray(habitIds)) {
    throw new AppError('Habit IDs array is required', 400);
  }

  const goal = goals.get(goalId);

  if (!goal) {
    throw new AppError('Goal not found', 404);
  }

  if (goal.user !== userId) {
    throw new AppError('Not authorized to update this goal', 401);
  }

  // Add habits to the goal
  goal.relatedHabits = [...new Set([...goal.relatedHabits, ...habitIds])];
  goal.updatedAt = new Date();

  goals.set(goalId, goal);

  res.status(200).json(goal);
});

/**
 * @desc    Alias for updateGoalProgress for compatibility with routes
 * @route   PATCH /api/goals/:id/progress
 * @access  Private
 */
export const updateProgress = updateGoalProgress;
