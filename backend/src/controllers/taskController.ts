import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import { Task, ProductivityMetrics } from '../models';
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
  
  if (req.query.status) {
    filter.status = req.query.status;
  }
  
  if (req.query.priority) {
    filter.priority = req.query.priority;
  }
  
  if (req.query.category) {
    filter.category = req.query.category;
  }
  
  if (req.query.parentTaskId) {
    filter.parentTaskId = req.query.parentTaskId;
  } else if (req.query.showRootOnly === 'true') {
    // Only show root tasks (no parent)
    filter.parentTaskId = { $exists: false };
  }
  
  // Date range filter
  if (req.query.fromDate || req.query.toDate) {
    filter.dueDate = {};
    
    if (req.query.fromDate) {
      filter.dueDate.$gte = new Date(req.query.fromDate as string);
    }
    
    if (req.query.toDate) {
      filter.dueDate.$lte = new Date(req.query.toDate as string);
    }
  }
  
  // Default to not showing deleted tasks
  if (req.query.showDeleted !== 'true') {
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
  const tasks = await Task.find(filter).sort(sort);
  
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
  
  // Get sub-tasks if they exist
  const subTasks = await Task.find({ parentTaskId: taskId, user: userId });
  
  // Return task with sub-tasks
  res.status(200).json({
    ...task.toObject(),
    subTasks
  });
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
  
  // Default values for enhanced fields
  const taskData = {
    ...req.body,
    user: userId,
    status: req.body.status || 'todo',
    effort: req.body.effort || 3, // Medium effort by default
    complexity: req.body.complexity || 3 // Medium complexity by default
  };
  
  // Create the task
  const task = await Task.create(taskData);
  
  // Update daily metrics for task creation count
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  await ProductivityMetrics.findOneAndUpdate(
    { user: userId, date: today },
    { $inc: { tasksCreated: 1 } },
    { upsert: true, new: true }
  );
  
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
  
  // Track which field is being modified for ML purposes
  const updates = { ...req.body };
  
  // Don't allow changing user
  delete updates.user;
  
  // Update the metadata with last modified field
  const changedFields = Object.keys(updates);
  if (changedFields.length > 0) {
    updates.metadata = {
      ...(task.metadata || {}),
      lastModifiedField: changedFields[0]
    };
  }
  
  // If completing the task, set completedAt
  if (updates.completed && !task.completed) {
    updates.completedAt = new Date();
    updates.status = 'completed';
    
    // Record completion context for ML
    updates.metadata = {
      ...(updates.metadata || {}),
      completionContext: {
        timeOfDay: new Date().getHours(),
        dayOfWeek: new Date().getDay()
      }
    };
    
    // Update productivity metrics
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    await ProductivityMetrics.findOneAndUpdate(
      { user: userId, date: today },
      { $inc: { tasksCompleted: 1 } },
      { upsert: true, new: true }
    );
  }
  
  // Update the task
  const updatedTask = await Task.findByIdAndUpdate(
    taskId,
    updates,
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
  
  // Soft delete - mark as archived
  if (req.query.permanent !== 'true') {
    await Task.findByIdAndUpdate(
      taskId,
      { status: 'archived' },
      { new: true }
    );
  } else {
    // Hard delete
    await Task.findByIdAndDelete(taskId);
  }
  
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
  
  // Toggle completion
  const updates: any = {
    completed: !task.completed,
    status: !task.completed ? 'completed' : 'todo'
  };
  
  // Set completedAt if completing, otherwise clear it
  if (!task.completed) {
    updates.completedAt = new Date();
    
    // Record completion context for ML
    updates.metadata = {
      ...(task.metadata || {}),
      completionContext: {
        timeOfDay: new Date().getHours(),
        dayOfWeek: new Date().getDay()
      }
    };
    
    // Update productivity metrics
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    await ProductivityMetrics.findOneAndUpdate(
      { user: userId, date: today },
      { $inc: { tasksCompleted: 1 } },
      { upsert: true, new: true }
    );
  } else {
    updates.completedAt = null;
    
    // Decrement task completion count from productivity metrics
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    await ProductivityMetrics.findOneAndUpdate(
      { user: userId, date: today },
      { $inc: { tasksCompleted: -1 } },
      { upsert: true, new: true }
    );
  }
  
  const updatedTask = await Task.findByIdAndUpdate(
    taskId,
    updates,
    { new: true }
  );
  
  res.status(200).json(updatedTask);
});

/**
 * @desc    Get sub-tasks for a parent task
 * @route   GET /api/tasks/:id/subtasks
 * @access  Private
 */
export const getSubTasks = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const parentTaskId = req.params.id;
  
  // Check if parent task exists
  const parentTask = await Task.findOne({ _id: parentTaskId, user: userId });
  
  if (!parentTask) {
    throw new AppError('Parent task not found', 404);
  }
  
  // Get sub-tasks
  const subTasks = await Task.find({ parentTaskId, user: userId }).sort({ createdAt: -1 });
  
  res.status(200).json(subTasks);
});
