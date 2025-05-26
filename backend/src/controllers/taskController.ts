import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import { Task, ProductivityMetrics, User, Category } from '../models';
import { AuthRequest, TaskDocument } from '../types';
import * as mlService from '../services/mlService';
import { calculateUpcomingOccurrences } from '../utils/recurrenceUtils';
import { HydratedDocument } from 'mongoose';

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
  const tasksMongo: HydratedDocument<TaskDocument>[] = await Task.find(filter).sort(sort);

  // Ensure all tasks have a category, and fix if not
  for (const task of tasksMongo) {
    if (!task.category) {
      let generalCategory = await Category.findOne({ user: userId, name: 'General', type: { $in: ['task', 'all'] } });
      if (!generalCategory) {
        const highestOrderCategory = await Category.findOne({ user: userId })
          .sort({ order: -1 })
          .select('order');
        const order = highestOrderCategory ? highestOrderCategory.order + 1 : 0;
        generalCategory = await Category.create({
          user: userId,
          name: 'General',
          color: '#808080', // Default grey
          type: 'task',
          isDefault: true,
          order: order
        });
      }
      task.category = generalCategory._id.toString();
      await task.save(); // Save the updated task to the database
    }
  }

  // For each recurring task, calculate upcoming occurrences
  const tasksWithOccurrences = await Promise.all(
    tasksMongo.map(async (taskDoc: HydratedDocument<TaskDocument>) => {
      const taskObject: TaskDocument = taskDoc.toObject(); // Explicitly type taskObject
      if (taskObject.recurrenceRule && taskObject.recurrenceStartDate) {
        (taskObject as any).upcomingOccurrences = calculateUpcomingOccurrences(taskObject, 5, new Date());
      }
      return taskObject; // Return the modified plain object
    })
  );
  
  res.status(200).json(tasksWithOccurrences);
});

/**
 * @desc    Get a task by ID
 * @route   GET /api/tasks/:id
 * @access  Private
 */
export const getTaskById = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const taskId = req.params.id;
  
  // taskFromDb is a Mongoose document (or null)
  const taskFromDb: HydratedDocument<TaskDocument> | null = await Task.findOne({ _id: taskId, user: userId });
  
  if (!taskFromDb) {
    throw new AppError('Task not found', 404);
  }
  
  // Convert to plain object for further processing and response
  const taskObject: TaskDocument = taskFromDb.toObject();

  // Get sub-tasks if they exist
  const subTasks = await Task.find({ parentTaskId: taskId, user: userId });

  let upcomingOccurrences: Date[] = [];
  // Use taskObject (TaskDocument) for calculateUpcomingOccurrences
  if (taskObject.recurrenceRule && taskObject.recurrenceStartDate) {
    upcomingOccurrences = calculateUpcomingOccurrences(taskObject, 5, new Date());
  }
  
  // Return task with sub-tasks and upcoming occurrences
  res.status(200).json({
    ...taskObject, // Spread the plain object
    subTasks,
    upcomingOccurrences
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

  let categoryId = req.body.category;
  if (!categoryId) {
    let generalCategory = await Category.findOne({ user: userId, name: 'General', type: { $in: ['task', 'all'] } });
    if (!generalCategory) {
      // Find the highest order value for the user's categories to ensure new one is last
      const highestOrderCategory = await Category.findOne({ user: userId })
        .sort({ order: -1 })
        .select('order');
      const order = highestOrderCategory ? highestOrderCategory.order + 1 : 0;

      generalCategory = await Category.create({
        user: userId,
        name: 'General',
        color: '#808080', // Grey color for General
        type: 'task', 
        isDefault: true,
        order: order
      });
    }
    categoryId = generalCategory._id.toString();
  }
  
  // Default values for enhanced fields
  const taskData = {
    ...req.body,
    user: userId,
    category: categoryId, // Use the determined categoryId
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
  console.log("updateTask controller - Received req.body:", req.body); // Log the entire request body
  const userId = req.user?._id;
  const taskId = req.params.id;
  
  const task = await Task.findOne({ _id: taskId, user: userId });
  
  if (!task) {
    throw new AppError('Task not found', 404);
  }
  
  const { metadata: requestMetadata, ...otherUpdates } = req.body;
  const updates: any = { ...otherUpdates }; 

  // Explicitly map due_date from request to dueDate for the update
  if (req.body.due_date) {
    updates.dueDate = req.body.due_date;
    delete updates.due_date; // Remove the snake_case version if it was spread
  }

  // Process remindersRequest
  if (req.body.remindersRequest && Array.isArray(req.body.remindersRequest)) {
    updates.reminders = req.body.remindersRequest.map((r: any) => {
      const reminder: any = { type: r.type };
      if (r.type === 'absolute' && r.absolute_time) {
        reminder.absoluteTime = new Date(r.absolute_time);
      } else if (r.type === 'relative' && r.offset_minutes !== undefined) {
        reminder.offsetMinutes = r.offset_minutes;
      }
      return reminder;
    });
    delete updates.remindersRequest; // Remove the original snake_case version
  }

  // Initialize taskMetadata safely, using existing task.metadata or an empty object
  let taskMetadata: any = task.metadata ? { ...task.metadata } : {};

  // Merge requestMetadata if it exists
  if (requestMetadata) {
    taskMetadata = { ...taskMetadata, ...requestMetadata };
  }
  
  // Don't allow changing user
  delete updates.user;
  
  const changedFields = Object.keys(updates);
  if (changedFields.length > 0 && changedFields[0] !== 'metadata' && 
      !['recurrenceRule', 'recurrenceStartDate', 'recurrenceExceptions', 'reminders'].includes(changedFields[0])) {
    taskMetadata.lastModifiedField = changedFields[0];
  }
  
  if (updates.completed && !task.completed) {
    updates.completedAt = new Date();
    updates.status = 'completed';
    
    try {
      if (userId) {
        const mlContext = await mlService.collectTaskCompletionContext(taskId, userId.toString());
        taskMetadata.completionContext = mlContext;
      }
    } catch (error) {
      console.error('ML data collection error:', error);
    }
    
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    await ProductivityMetrics.findOneAndUpdate(
      { user: userId, date: today },
      { $inc: { tasksCompleted: 1 } },
      { upsert: true, new: true }
    );
  }
  
  // Assign the consolidated metadata to updates
  // only if it's not empty or if original metadata existed, to avoid sending empty object if not needed
  if (Object.keys(taskMetadata).length > 0 || task.metadata) {
    updates.metadata = taskMetadata;
  }

  // Recurrence is handled directly if present in req.body as updates.recurrence

  const updatedTask = await Task.findByIdAndUpdate(
    taskId,
    updates, // updates contains otherUpdates, and potentially updates.metadata
    { new: true, runValidators: true }
  );
  
  // Send specific fields to ensure progress is included if available on updatedTask
  if (updatedTask) {
    res.status(200).json({
      _id: updatedTask._id,
      title: updatedTask.title,
      description: updatedTask.description,
      completed: updatedTask.completed,
      completedAt: updatedTask.completedAt,
      priority: updatedTask.priority,
      progress: updatedTask.progress, // Explicitly include progress
      dueDate: updatedTask.dueDate,
      status: updatedTask.status,
      category: updatedTask.category,
      parentTaskId: updatedTask.parentTaskId,
      tags: updatedTask.tags,
      recurrenceRule: updatedTask.recurrenceRule,
      recurrenceStartDate: updatedTask.recurrenceStartDate,
      recurrenceExceptions: updatedTask.recurrenceExceptions,
      reminders: updatedTask.reminders,
      effort: updatedTask.effort,
      complexity: updatedTask.complexity,
      attachments: updatedTask.attachments,
      metadata: updatedTask.metadata,
      createdAt: updatedTask.createdAt,
      updatedAt: updatedTask.updatedAt,
      user: updatedTask.user // Ensure user is also sent if needed by client DTO mapping
    });
  } else {
    // Should not happen if findByIdAndUpdate was successful with an existing task
    throw new AppError('Failed to retrieve updated task details', 500);
  }
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
  
  // Ensure all tasks have a category, and fix if not
  for (const task of priorityTasks) {
    if (!task.category) {
      let generalCategory = await Category.findOne({ user: userId, name: 'General', type: { $in: ['task', 'all'] } });
      if (!generalCategory) {
        const highestOrderCategory = await Category.findOne({ user: userId })
          .sort({ order: -1 })
          .select('order');
        const order = highestOrderCategory ? highestOrderCategory.order + 1 : 0;
        generalCategory = await Category.create({
          user: userId,
          name: 'General',
          color: '#808080', // Default grey
          type: 'task',
          isDefault: true,
          order: order
        });
      }
      task.category = generalCategory._id.toString();
      await (task as HydratedDocument<TaskDocument>).save(); // Save the updated task to the database
    }
  }
  
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
    
    // Ensure all medium priority tasks also have a category
    for (const task of mediumPriorityTasks) {
      if (!task.category) {
        let generalCategory = await Category.findOne({ user: userId, name: 'General', type: { $in: ['task', 'all'] } });
        if (!generalCategory) {
          const highestOrderCategory = await Category.findOne({ user: userId })
            .sort({ order: -1 })
            .select('order');
          const order = highestOrderCategory ? highestOrderCategory.order + 1 : 0;
          generalCategory = await Category.create({
            user: userId,
            name: 'General',
            color: '#808080', // Default grey
            type: 'task',
            isDefault: true,
            order: order
          });
        }
        task.category = generalCategory._id.toString();
        await (task as HydratedDocument<TaskDocument>).save(); // Save the updated task to the database
      }
    }
    
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
