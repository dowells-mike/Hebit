import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import Task from '../models/Task';
import { AuthRequest } from '../types';

/**
 * @desc    Get all tasks for a user
 * @route   GET /api/tasks
 * @access  Private
 */
export const getTasks = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  // Build the filter object
  const filter: any = { user: userId };
  
  // Add filters based on query parameters
  if (req.query.completed) {
    filter.completed = req.query.completed === 'true';
  }
  
  if (req.query.priority) {
    filter.priority = req.query.priority;
  }
  
  // Execute the query with filters and sort by recently created
  const tasks = await Task.find(filter).sort({ createdAt: -1 });
  
  res.status(200).json(tasks);
});

/**
 * @desc    Get a task by ID
 * @route   GET /api/tasks/:id
 * @access  Private
 */
export const getTaskById = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const taskId = req.params.id;
  
  const task = await Task.findOne({ _id: taskId, user: userId });
  
  if (!task) {
    throw new AppError('Task not found', 404);
  }
  
  res.status(200).json(task);
});

/**
 * @desc    Create a new task
 * @route   POST /api/tasks
 * @access  Private
 */
export const createTask = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  if (!req.body.title) {
    throw new AppError('Title is required', 400);
  }
  
  const task = await Task.create({
    ...req.body,
    user: userId
  });
  
  res.status(201).json(task);
});

/**
 * @desc    Update a task
 * @route   PUT /api/tasks/:id
 * @access  Private
 */
export const updateTask = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const taskId = req.params.id;
  
  const task = await Task.findOne({ _id: taskId, user: userId });
  
  if (!task) {
    throw new AppError('Task not found', 404);
  }
  
  // Update the task
  const updatedTask = await Task.findByIdAndUpdate(
    taskId,
    req.body,
    { new: true, runValidators: true }
  );
  
  res.status(200).json(updatedTask);
});

/**
 * @desc    Delete a task
 * @route   DELETE /api/tasks/:id
 * @access  Private
 */
export const deleteTask = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const taskId = req.params.id;
  
  const task = await Task.findOne({ _id: taskId, user: userId });
  
  if (!task) {
    throw new AppError('Task not found', 404);
  }
  
  await Task.findByIdAndDelete(taskId);
  
  res.status(200).json({ success: true });
});

/**
 * @desc    Toggle task completion
 * @route   PATCH /api/tasks/:id/complete
 * @access  Private
 */
export const toggleTaskCompletion = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const taskId = req.params.id;
  
  const task = await Task.findOne({ _id: taskId, user: userId });
  
  if (!task) {
    throw new AppError('Task not found', 404);
  }
  
  task.completed = !task.completed;
  await task.save();
  
  res.status(200).json(task);
});
