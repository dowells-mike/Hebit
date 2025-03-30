import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import { Habit, ProductivityMetrics } from '../models';
import { AuthRequest, HabitDocument } from '../types';

/**
 * @desc    Get all habits for a user
 * @route   GET /api/habits
 * @access  Private
 */
export const getHabits = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  // Build the filter object
  const filter: any = { user: userId };
  
  // Add filter by frequency if provided
  if (req.query.frequency) {
    filter.frequency = req.query.frequency;
  }
  
  // Add filter by category if provided
  if (req.query.category) {
    filter.category = req.query.category;
  }
  
  // Status filter
  if (req.query.status) {
    filter.status = req.query.status;
  } else {
    // Default to not showing archived habits
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
  const habits = await Habit.find(filter).sort(sort);
  
  res.status(200).json(habits);
});

/**
 * @desc    Get a habit by ID
 * @route   GET /api/habits/:id
 * @access  Private
 */
export const getHabitById = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const habitId = req.params.id;
  
  const habit = await Habit.findOne({ _id: habitId, user: userId });
  
  if (!habit) {
    throw new AppError('Habit not found', 404);
  }
  
  res.status(200).json(habit);
});

/**
 * @desc    Create a new habit
 * @route   POST /api/habits
 * @access  Private
 */
export const createHabit = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  if (!req.body.title) {
    throw new AppError('Title is required', 400);
  }
  
  if (!req.body.frequency) {
    throw new AppError('Frequency is required', 400);
  }
  
  // Default values for enhanced fields
  const habitData = {
    ...req.body,
    user: userId,
    streak: 0,
    completionHistory: [],
    status: req.body.status || 'active',
    difficulty: req.body.difficulty || 'medium', // Medium difficulty by default
    impact: req.body.impact || 3 // Medium impact by default
  };
  
  const habit = await Habit.create(habitData);
  
  // Update daily metrics for habit creation count
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  await ProductivityMetrics.findOneAndUpdate(
    { user: userId, date: today },
    { $inc: { habitsCreated: 1 } },
    { upsert: true, new: true }
  );
  
  res.status(201).json(habit);
});

/**
 * @desc    Update a habit
 * @route   PUT /api/habits/:id
 * @access  Private
 */
export const updateHabit = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const habitId = req.params.id;
  
  const habit = await Habit.findOne({ _id: habitId, user: userId });
  
  if (!habit) {
    throw new AppError('Habit not found', 404);
  }
  
  // Track which field is being modified for ML purposes
  const updates = { ...req.body };
  
  // Don't allow changing user
  delete updates.user;
  
  // Update the metadata with last modified field
  const changedFields = Object.keys(updates);
  if (changedFields.length > 0) {
    updates.metadata = {
      ...(habit.metadata || {}),
      lastModifiedField: changedFields[0]
    };
  }
  
  // Update the habit
  const updatedHabit = await Habit.findByIdAndUpdate(
    habitId,
    updates,
    { new: true, runValidators: true }
  );
  
  res.status(200).json(updatedHabit);
});

/**
 * @desc    Delete a habit
 * @route   DELETE /api/habits/:id
 * @access  Private
 */
export const deleteHabit = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const habitId = req.params.id;
  
  const habit = await Habit.findOne({ _id: habitId, user: userId });
  
  if (!habit) {
    throw new AppError('Habit not found', 404);
  }
  
  // Soft delete - mark as archived
  if (req.query.permanent !== 'true') {
    await Habit.findByIdAndUpdate(
      habitId,
      { status: 'archived' },
      { new: true }
    );
  } else {
    // Hard delete
    await Habit.findByIdAndDelete(habitId);
  }
  
  res.status(200).json({ success: true });
});

/**
 * @desc    Track habit completion
 * @route   POST /api/habits/:id/track
 * @access  Private
 */
export const trackHabit = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const habitId = req.params.id;
  const { completed, date, notes } = req.body;
  
  if (completed === undefined) {
    throw new AppError('Completed status is required', 400);
  }
  
  if (!date) {
    throw new AppError('Date is required', 400);
  }
  
  const habit = await Habit.findOne({ _id: habitId, user: userId });
  
  if (!habit) {
    throw new AppError('Habit not found', 404);
  }
  
  const completionDate = new Date(date);
  const today = new Date();
  
  // Check if the date is in the future
  if (completionDate > today) {
    throw new AppError('Cannot track habit for future dates', 400);
  }
  
  // Check if there's already an entry for this date
  const existingEntryIndex = habit.completionHistory.findIndex(
    entry => new Date(entry.date).toDateString() === completionDate.toDateString()
  );
  
  // Completion time data for analytics
  const contextData = {
    timeOfDay: completionDate.getHours(),
    dayOfWeek: completionDate.getDay()
  };
  
  if (existingEntryIndex !== -1) {
    // Update existing entry
    const wasCompletedBefore = habit.completionHistory[existingEntryIndex].completed;
    habit.completionHistory[existingEntryIndex].completed = completed;
    habit.completionHistory[existingEntryIndex].notes = notes || habit.completionHistory[existingEntryIndex].notes;
    
    // Update productivity metrics
    if (!wasCompletedBefore && completed) {
      // Increment habit completion count
      await updateProductivityMetrics(userId.toString(), completionDate, { habitsCompleted: 1 });
    } else if (wasCompletedBefore && !completed) {
      // Decrement habit completion count
      await updateProductivityMetrics(userId.toString(), completionDate, { habitsCompleted: -1 });
    }
  } else {
    // Add new entry
    habit.completionHistory.push({
      date: completionDate,
      completed,
      notes
    });
    
    // Update productivity metrics
    if (completed) {
      await updateProductivityMetrics(userId.toString(), completionDate, { habitsCompleted: 1 });
    }
  }
  
  // Sort completion history by date (newest first)
  habit.completionHistory.sort((a, b) => 
    new Date(b.date).getTime() - new Date(a.date).getTime()
  );
  
  // Calculate streak
  habit.streak = calculateStreak(habit);
  
  // Calculate consistency score (percentage of completion over the last 30 days)
  const consistencyScore = calculateConsistency(habit);
  
  // Update additional streak data if using the enhanced model
  if (habit.streakData) {
    habit.streakData.current = habit.streak;
    
    // Update longest streak if current is higher
    if (habit.streak > (habit.streakData.longest || 0)) {
      habit.streakData.longest = habit.streak;
    }
    
    if (completed) {
      habit.streakData.lastCompleted = new Date();
    }
  }
  
  await habit.save();
  
  // Include consistency in response even if not stored in the model
  const response = habit.toObject();
  response.consistency = consistencyScore;
  
  res.status(200).json(response);
});

/**
 * @desc    Get habit statistics
 * @route   GET /api/habits/:id/stats
 * @access  Private
 */
export const getHabitStats = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const habitId = req.params.id;
  
  const habit = await Habit.findOne({ _id: habitId, user: userId });
  
  if (!habit) {
    throw new AppError('Habit not found', 404);
  }
  
  // Calculate overall completion rate
  const totalEntries = habit.completionHistory.length;
  const completedEntries = habit.completionHistory.filter(entry => entry.completed).length;
  const completionRate = totalEntries > 0 ? (completedEntries / totalEntries) * 100 : 0;
  
  // Get longest streak
  const allStreaks = calculateAllStreaks(habit);
  const longestStreak = Math.max(...allStreaks, 0);
  
  // Get completion counts by day of week
  const completionsByDay = getCompletionsByDayOfWeek(habit);
  
  // Get completion counts by time of day
  const completionsByTime = getCompletionsByTimeOfDay(habit);
  
  // Calculate consistency
  const consistency = calculateConsistency(habit);
  
  res.status(200).json({
    completionRate,
    currentStreak: habit.streak,
    longestStreak,
    consistency,
    totalEntries,
    completedEntries,
    completionsByDay,
    completionsByTime
  });
});

// Helper function to update productivity metrics
const updateProductivityMetrics = async (userId: string, date: Date, updates: any) => {
  const metricDate = new Date(date);
  metricDate.setHours(0, 0, 0, 0);
  
  await ProductivityMetrics.findOneAndUpdate(
    { user: userId, date: metricDate },
    { $inc: updates },
    { upsert: true, new: true }
  );
};

// Helper function to calculate streak
const calculateStreak = (habit: HabitDocument) => {
  if (habit.completionHistory.length === 0) return 0;
  
  // Sort completion history by date (oldest first) for streak calculation
  const sortedHistory = [...habit.completionHistory].sort((a, b) => 
    new Date(a.date).getTime() - new Date(b.date).getTime()
  );
  
  let currentStreak = 0;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  // Check if most recent entry is today or yesterday
  const mostRecentEntry = sortedHistory[sortedHistory.length - 1];
  const mostRecentDate = new Date(mostRecentEntry.date);
  mostRecentDate.setHours(0, 0, 0, 0);
  
  const diffDays = Math.floor((today.getTime() - mostRecentDate.getTime()) / (1000 * 60 * 60 * 24));
  
  // If most recent entry is more than yesterday and not completed, streak is 0
  if (diffDays > 1 || (diffDays === 1 && !mostRecentEntry.completed)) {
    return 0;
  }
  
  // Calculate streak from most recent to oldest
  for (let i = sortedHistory.length - 1; i >= 0; i--) {
    const entry = sortedHistory[i];
    
    if (!entry.completed) break;
    
    if (i === sortedHistory.length - 1) {
      currentStreak = 1;
    } else {
      const currentDate = new Date(entry.date);
      const nextDate = new Date(sortedHistory[i + 1].date);
      
      // Check if dates are consecutive
      const diffTime = Math.abs(nextDate.getTime() - currentDate.getTime());
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      
      if (diffDays === 1) {
        currentStreak++;
      } else {
        break;
      }
    }
  }
  
  return currentStreak;
};

// Helper function to calculate all streaks
const calculateAllStreaks = (habit: HabitDocument) => {
  if (habit.completionHistory.length === 0) return [0];
  
  // Sort completion history by date (oldest first)
  const sortedHistory = [...habit.completionHistory].sort((a, b) => 
    new Date(a.date).getTime() - new Date(b.date).getTime()
  );
  
  const streaks: number[] = [];
  let currentStreak = 0;
  
  for (let i = 0; i < sortedHistory.length; i++) {
    const entry = sortedHistory[i];
    
    if (entry.completed) {
      currentStreak++;
      
      if (i === sortedHistory.length - 1) {
        streaks.push(currentStreak);
      } else {
        const currentDate = new Date(entry.date);
        const nextDate = new Date(sortedHistory[i + 1].date);
        
        // Check if next date is consecutive
        const diffTime = Math.abs(nextDate.getTime() - currentDate.getTime());
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        
        if (diffDays > 1 || !sortedHistory[i + 1].completed) {
          streaks.push(currentStreak);
          currentStreak = 0;
        }
      }
    } else {
      currentStreak = 0;
    }
  }
  
  return streaks;
};

// Helper function to calculate consistency score
const calculateConsistency = (habit: HabitDocument): number => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  // Start date is 30 days ago
  const startDate = new Date(today);
  startDate.setDate(startDate.getDate() - 29); // Include today, so -29 instead of -30
  
  // Filter completion history to entries in the last 30 days
  const recentEntries = habit.completionHistory.filter((entry: any) => {
    const entryDate = new Date(entry.date);
    entryDate.setHours(0, 0, 0, 0);
    return entryDate >= startDate && entryDate <= today;
  });
  
  // Count completed entries
  const completedCount = recentEntries.filter((entry: any) => entry.completed).length;
  
  // For daily habits, ideal is 30 completions in 30 days
  // For weekly habits, ideal is 4-5 completions in 30 days
  // For monthly habits, ideal is 1 completion in 30 days
  let targetCompletions = 30; // Default for daily
  
  if (habit.frequency === 'weekly') {
    targetCompletions = 4; // Approximately 4 weeks in 30 days
  } else if (habit.frequency === 'monthly') {
    targetCompletions = 1;
  }
  
  // Calculate consistency as a percentage of target completions
  return Math.min(100, Math.round((completedCount / targetCompletions) * 100));
};

// Helper function to get completions by day of week
const getCompletionsByDayOfWeek = (habit: HabitDocument) => {
  const daysOfWeek = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  const completionsByDay = Array(7).fill(0);
  
  habit.completionHistory.forEach((entry: any) => {
    if (entry.completed) {
      const entryDate = new Date(entry.date);
      const dayOfWeek = entryDate.getDay();
      completionsByDay[dayOfWeek]++;
    }
  });
  
  return daysOfWeek.map((day, index) => ({
    day,
    count: completionsByDay[index]
  }));
};

// Helper function to get completions by time of day
const getCompletionsByTimeOfDay = (habit: HabitDocument) => {
  const timeRanges = [
    { name: 'Morning (5am-12pm)', count: 0 },
    { name: 'Afternoon (12pm-5pm)', count: 0 },
    { name: 'Evening (5pm-9pm)', count: 0 },
    { name: 'Night (9pm-5am)', count: 0 }
  ];
  
  habit.completionHistory.forEach((entry: any) => {
    if (entry.completed) {
      const entryDate = new Date(entry.date);
      const hour = entryDate.getHours();
      
      if (hour >= 5 && hour < 12) {
        timeRanges[0].count++;
      } else if (hour >= 12 && hour < 17) {
        timeRanges[1].count++;
      } else if (hour >= 17 && hour < 21) {
        timeRanges[2].count++;
      } else {
        timeRanges[3].count++;
      }
    }
  });
  
  return timeRanges;
};
