import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import { Task, ProductivityMetrics, User } from '../models';
import { AuthRequest } from '../types';
import * as mlService from '../services/mlService';

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
    
    // Use ML service to collect task completion context
    try {
      // Ensure userId is a string
      if (userId) {
        const mlContext = await mlService.collectTaskCompletionContext(taskId, userId.toString());
        
        // Add the ML context to the updates
        updates.metadata = {
          ...(updates.metadata || {}),
          completionContext: mlContext
        };
      }
    } catch (error) {
      console.error('ML data collection error:', error);
      // Continue with update even if ML processing fails
    }
    
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
  
  // First, find the task
  const task = await Task.findOne({ _id: taskId, user: userId });
  
  if (!task) {
    throw new AppError('Task not found', 404);
  }
  
  // Determine current completion state and NEW state
  const currentlyCompleted = Boolean(task.completed);
  const newCompletionState = !currentlyCompleted;
  
  console.log(`Task toggle - ID: ${taskId}`);
  console.log(`  Current state: completed=${currentlyCompleted}, status=${task.status}`);
  console.log(`  New state: completed=${newCompletionState}, status=${newCompletionState ? 'completed' : 'todo'}`);
  
  // Prepare update object
  const updates: any = {
    completed: newCompletionState,
    status: newCompletionState ? 'completed' : 'todo'
  };
  
  // If completing, add completion metadata
  if (newCompletionState) {
    // Mark as completed
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
    // Mark as incomplete
    updates.completedAt = null;
    
    // Decrement task completion count
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    await ProductivityMetrics.findOneAndUpdate(
      { user: userId, date: today },
      { $inc: { tasksCompleted: -1 } },
      { upsert: true, new: true }
    );
  }
  
  // Perform update with explicit options
  const updatedTask = await Task.findByIdAndUpdate(
    taskId,
    { $set: updates },
    { 
      new: true,      // Return the updated document
      runValidators: true  // Run schema validators
    }
  );
  
  console.log(`Task after update - completed=${updatedTask?.completed}, status=${updatedTask?.status}`);
  
  // Send response
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

/**
 * @desc    Get priority tasks for a user
 * @route   GET /api/tasks/priority
 * @access  Private
 */
export const getPriorityTasks = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const limit = req.query.limit ? parseInt(req.query.limit as string) : 5;
  
  // Get high priority tasks that aren't completed
  const priorityTasks = await Task.find({ 
    user: userId, 
    priority: 'high',
    completed: false,
    status: { $ne: 'archived' }
  })
  .sort({ dueDate: 1 }) // Sort by due date ascending (soonest first)
  .limit(limit);
  
  // If not enough high priority tasks, get medium priority ones
  if (priorityTasks.length < limit) {
    const mediumPriorityTasks = await Task.find({
      user: userId,
      priority: 'medium',
      completed: false,
      status: { $ne: 'archived' }
    })
    .sort({ dueDate: 1 })
    .limit(limit - priorityTasks.length);
    
    priorityTasks.push(...mediumPriorityTasks);
  }
  
  res.status(200).json({
    tasks: priorityTasks,
    total: priorityTasks.length,
    page: 1,
    per_page: limit
  });
});

/**
 * @desc    Get recommended next task based on ML
 * @route   GET /api/tasks/recommendations/next
 * @access  Private
 */
export const getRecommendedNextTask = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  if (!userId) {
    throw new AppError('User ID is required', 400);
  }
  
  // Get all incomplete tasks for the user
  const tasks = await Task.find({
    user: userId,
    completed: false,
    status: { $ne: 'archived' }
  });
  
  if (tasks.length === 0) {
    return res.status(200).json({
      message: 'No tasks available for recommendation',
      tasks: []
    });
  }
  
  // Get the user's productivity patterns for recommendation
  const user = await User.findById(userId);
  const peakHours = user?.productivity?.peakHours || {};
  
  // Simple recommendation logic - prioritize by:
  // 1. Due date (closer dates first)
  // 2. Priority (high to low)
  // 3. If current hour is a peak productivity hour, prioritize more complex tasks
  const currentHour = new Date().getHours().toString();
  const isPeakHour = peakHours[currentHour] && peakHours[currentHour] > 3;
  
  // Sort tasks
  const sortedTasks = [...tasks].sort((a, b) => {
    // Due date comparison - null dates go to the end
    if (a.dueDate && !b.dueDate) return -1;
    if (!a.dueDate && b.dueDate) return 1;
    if (a.dueDate && b.dueDate) {
      const dateComparison = new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime();
      if (dateComparison !== 0) return dateComparison;
    }
    
    // Priority comparison (high to low)
    const priorityMap: any = { high: 3, medium: 2, low: 1 };
    const priorityA = priorityMap[a.priority] || 2;
    const priorityB = priorityMap[b.priority] || 2;
    if (priorityA !== priorityB) return priorityB - priorityA;
    
    // If peak hour, prioritize complex tasks
    if (isPeakHour) {
      const complexityA = a.complexity || 3;
      const complexityB = b.complexity || 3;
      if (complexityA !== complexityB) return complexityB - complexityA;
    }
    
    return 0;
  });
  
  // Return top 3 recommended tasks
  const recommendedTasks = sortedTasks.slice(0, 3);
  
  res.status(200).json({
    message: isPeakHour 
      ? 'Current hour is a peak productivity time for you' 
      : 'Based on your task priorities and deadlines',
    isPeakProductivityTime: isPeakHour,
    tasks: recommendedTasks
  });
});

/**
 * @desc    Get optimal time to work on tasks
 * @route   GET /api/tasks/recommendations/optimal-time
 * @access  Private
 */
export const getOptimalTaskTime = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  if (!userId) {
    throw new AppError('User ID is required', 400);
  }
  
  // Use ML service to recommend optimal time
  const recommendation = await mlService.recommendTaskTime(userId.toString());
  
  res.status(200).json({
    optimalHour: recommendation.hour,
    confidence: recommendation.confidence,
    message: `Your most productive time is around ${recommendation.hour}:00`
  });
});

/**
 * @desc    Get estimated duration for a task
 * @route   GET /api/tasks/stats/duration-estimate
 * @access  Private
 */
export const getTaskDurationEstimate = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  if (!userId) {
    throw new AppError('User ID is required', 400);
  }
  
  // Get task data from query params
  const taskData = {
    category: req.query.category as string,
    priority: req.query.priority as string,
    title: req.query.title as string,
    description: req.query.description as string
  };
  
  // Use ML service to estimate task duration
  const estimate = await mlService.estimateTaskDuration(taskData, userId.toString());
  
  res.status(200).json({
    estimatedMinutes: estimate.estimatedMinutes,
    confidence: estimate.confidence,
    message: `This task will take approximately ${estimate.estimatedMinutes} minutes to complete`
  });
});
