import { Request, Response } from 'express';
import { AuthRequest } from '../types';
import { catchAsync, AppError } from '../middleware/errorHandler';

// In-memory task store for development mode
const tasks = new Map();
let lastTaskId = 0;

/**
 * @desc    Create a new task
 * @route   POST /api/tasks
 * @access  Private
 */
export const createTask = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const { title, description, priority, dueDate, category, tags } = req.body;

  if (!title) {
    throw new AppError('Task title is required', 400);
  }

  const taskId = (++lastTaskId).toString();
  const task = {
    _id: taskId,
    user: userId,
    title,
    description: description || '',
    completed: false,
    priority: priority || 'medium',
    dueDate: dueDate ? new Date(dueDate) : undefined,
    category: category || 'inbox',
    tags: tags || [],
    createdAt: new Date(),
    updatedAt: new Date()
  };

  tasks.set(taskId, task);

  res.status(201).json(task);
});

/**
 * @desc    Get all tasks for a user
 * @route   GET /api/tasks
 * @access  Private
 */
export const getTasks = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const { category, completed, priority } = req.query;

  let userTasks = Array.from(tasks.values()).filter(
    (task: any) => task.user === userId
  );

  // Apply filters if provided
  if (category) {
    userTasks = userTasks.filter((task: any) => task.category === category);
  }

  if (completed !== undefined) {
    const isCompleted = completed === 'true';
    userTasks = userTasks.filter((task: any) => task.completed === isCompleted);
  }

  if (priority) {
    userTasks = userTasks.filter((task: any) => task.priority === priority);
  }

  res.status(200).json(userTasks);
});

/**
 * @desc    Get a single task by ID
 * @route   GET /api/tasks/:id
 * @access  Private
 */
export const getTaskById = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const taskId = req.params.id;

  const task = tasks.get(taskId);

  if (!task) {
    throw new AppError('Task not found', 404);
  }

  if (task.user !== userId) {
    throw new AppError('Not authorized to access this task', 401);
  }

  res.status(200).json(task);
});

/**
 * @desc    Update a task
 * @route   PUT /api/tasks/:id
 * @access  Private
 */
export const updateTask = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const taskId = req.params.id;
  const { title, description, completed, priority, dueDate, category, tags } = req.body;

  const task = tasks.get(taskId);

  if (!task) {
    throw new AppError('Task not found', 404);
  }

  if (task.user !== userId) {
    throw new AppError('Not authorized to update this task', 401);
  }

  if (title) task.title = title;
  if (description !== undefined) task.description = description;
  if (completed !== undefined) task.completed = completed;
  if (priority) task.priority = priority;
  if (dueDate) task.dueDate = new Date(dueDate);
  if (category) task.category = category;
  if (tags) task.tags = tags;
  task.updatedAt = new Date();

  tasks.set(taskId, task);

  res.status(200).json(task);
});

/**
 * @desc    Delete a task
 * @route   DELETE /api/tasks/:id
 * @access  Private
 */
export const deleteTask = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const taskId = req.params.id;

  const task = tasks.get(taskId);

  if (!task) {
    throw new AppError('Task not found', 404);
  }

  if (task.user !== userId) {
    throw new AppError('Not authorized to delete this task', 401);
  }

  tasks.delete(taskId);

  res.status(200).json({ message: 'Task deleted successfully' });
});

/**
 * @desc    Toggle task completion status
 * @route   PATCH /api/tasks/:id/complete
 * @access  Private
 */
export const toggleTaskCompletion = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const taskId = req.params.id;

  const task = tasks.get(taskId);

  if (!task) {
    throw new AppError('Task not found', 404);
  }

  if (task.user !== userId) {
    throw new AppError('Not authorized to update this task', 401);
  }

  // Toggle completion status
  task.completed = !task.completed;
  task.updatedAt = new Date();

  tasks.set(taskId, task);

  res.status(200).json(task);
});
