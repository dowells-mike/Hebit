import mongoose from 'mongoose';
import { Task, User } from '../models';

/**
 * ML Service - Responsible for collecting, processing and utilizing ML data
 */

interface CompletionContext {
  timeOfDay: number;
  dayOfWeek: number;
  completedAt: Date;
  monthDay: number;
  month: number;
}

/**
 * Collects context data when a task is completed
 * This data will be used to train ML models for task recommendations
 */
export const collectTaskCompletionContext = async (taskId: string, userId: string) => {
  try {
    const now = new Date();
    
    // Extract context features
    const context: CompletionContext = {
      timeOfDay: now.getHours(),
      dayOfWeek: now.getDay(),
      completedAt: now,
      monthDay: now.getDate(),
      month: now.getMonth() + 1,
    };
    
    // Update task with ML metadata
    await Task.findByIdAndUpdate(taskId, {
      $set: {
        'metadata.completionContext': context,
        'metadata.lastModifiedField': 'completed'
      }
    });
    
    // Update user productivity patterns
    await updateUserProductivityPatterns(userId, context);
    
    return context;
  } catch (error) {
    console.error('Error collecting task completion context:', error);
    throw error;
  }
};

/**
 * Update user's productivity patterns based on task completion
 */
const updateUserProductivityPatterns = async (userId: string, context: CompletionContext) => {
  try {
    const user = await User.findById(userId);
    if (!user) return;
    
    // Initialize productivity object if it doesn't exist
    if (!user.productivity) {
      user.productivity = {
        peakHours: {},
        peakDays: {},
        taskCompletionCount: 0
      };
    }
    
    // Ensure nested objects exist
    if (!user.productivity.peakHours) {
      user.productivity.peakHours = {};
    }
    
    if (!user.productivity.peakDays) {
      user.productivity.peakDays = {};
    }
    
    // Update peak hours data
    const hourKey = context.timeOfDay.toString();
    const currentHourCount = user.productivity.peakHours[hourKey] || 0;
    user.productivity.peakHours[hourKey] = currentHourCount + 1;
    
    // Update peak days data
    const dayKey = context.dayOfWeek.toString();
    const currentDayCount = user.productivity.peakDays[dayKey] || 0;
    user.productivity.peakDays[dayKey] = currentDayCount + 1;
    
    // Increment total tasks completed
    user.productivity.taskCompletionCount = (user.productivity.taskCompletionCount || 0) + 1;
    
    await user.save();
  } catch (error) {
    console.error('Error updating user productivity patterns:', error);
    // Don't rethrow - this is a background operation
  }
};

interface TaskData {
  category?: string;
  priority?: string;
  title?: string;
  description?: string;
}

/**
 * Calculate task duration estimates based on user's history
 * This is a simple example of using collected data for predictions
 */
export const estimateTaskDuration = async (taskData: TaskData, userId: string) => {
  try {
    // Find similar completed tasks by this user
    const similarTasks = await Task.find({
      user: userId,
      completed: true,
      ...(taskData.category ? { category: taskData.category } : {})
    }).limit(10);
    
    if (similarTasks.length === 0) {
      return { 
        estimatedMinutes: 30, // Default estimate
        confidence: 'low'
      };
    }
    
    // Calculate average duration of similar tasks
    const durations: number[] = similarTasks
      .map(task => {
        // Skip if we don't have both creation and completion timestamps
        if (!task.createdAt) return null;
        
        let completionDate: Date | null = null;
        
        // Try to get completion date from different sources
        if (task.metadata?.completionContext?.completedAt) {
          completionDate = new Date(task.metadata.completionContext.completedAt);
        } else if (task.completedAt) {
          completionDate = new Date(task.completedAt);
        } else if (task.updatedAt) {
          completionDate = new Date(task.updatedAt);
        }
            
        if (!completionDate) return null;
          
        const created = new Date(task.createdAt);
        return (completionDate.getTime() - created.getTime()) / (1000 * 60); // minutes
      })
      .filter((duration): duration is number => 
        duration !== null && duration > 0 && duration < 24 * 60
      ); // Filter out nulls and outliers
    
    if (durations.length === 0) {
      return { 
        estimatedMinutes: 30, // Default estimate
        confidence: 'low'
      };
    }
    
    const avgDuration = durations.reduce((sum, val) => sum + val, 0) / durations.length;
    
    return {
      estimatedMinutes: Math.round(avgDuration),
      confidence: durations.length > 5 ? 'medium' : 'low'
    };
  } catch (error) {
    console.error('Error estimating task duration:', error);
    return { 
      estimatedMinutes: 30, // Default estimate
      confidence: 'low'
    };
  }
};

/**
 * Recommend the best time of day for a user to complete a task
 * based on their historical productivity patterns
 */
export const recommendTaskTime = async (userId: string) => {
  try {
    const user = await User.findById(userId);
    if (!user || !user.productivity || !user.productivity.peakHours) {
      return { hour: 9, confidence: 'low' }; // Default to 9 AM
    }
    
    // Find the hour with the most task completions
    const peakHours = user.productivity.peakHours;
    let bestHour = '9'; // Default
    let maxCompletions = 0;
    
    Object.entries(peakHours).forEach(([hour, count]) => {
      if (typeof count === 'number' && count > maxCompletions) {
        maxCompletions = count;
        bestHour = hour;
      }
    });
    
    const hourNum = parseInt(bestHour, 10);
    
    return {
      hour: isNaN(hourNum) ? 9 : hourNum,
      confidence: maxCompletions > 10 ? 'high' : maxCompletions > 5 ? 'medium' : 'low'
    };
  } catch (error) {
    console.error('Error recommending task time:', error);
    return { hour: 9, confidence: 'low' };
  }
};

/**
 * Export task completion data for ML training
 * This would typically be called by a scheduled job
 */
export const exportTaskDataForTraining = async () => {
  try {
    // Get all completed tasks with context data
    const tasks = await Task.find({
      completed: true,
      'metadata.completionContext': { $exists: true }
    }).populate('user', 'id');
    
    // Transform into ML training format
    const trainingData = tasks.map(task => {
      const taskObj = task.toObject();
      return {
        userId: taskObj.user,
        taskId: taskObj._id,
        title: taskObj.title,
        description: taskObj.description,
        category: taskObj.category,
        priority: taskObj.priority,
        completionHour: taskObj.metadata?.completionContext?.timeOfDay,
        completionDay: taskObj.metadata?.completionContext?.dayOfWeek,
        duration: taskObj.metadata?.estimatedDuration || 0,
        // Add more features as needed
      };
    });
    
    return trainingData;
    
    // In a real implementation, you would save this data to a file or database
    // for later use in training ML models
  } catch (error) {
    console.error('Error exporting task data for training:', error);
    throw error;
  }
};
