import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import { Habit, ProductivityMetrics } from '../models';
import { AuthRequest, HabitDocument as HabitDataType } from '../types';
import mongoose from 'mongoose';
import eventEmitter from '../services/eventEmitter';

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
  
  // Pagination
  const page = parseInt(req.query.page as string) || 1;
  const perPage = parseInt(req.query.per_page as string) || 20;
  
  // Execute the query with filters and sort
  const habits = await Habit.find(filter).sort(sort);
  
  // Add completed_today field to each habit
  const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD format
  
  const habitsWithCompletedToday = habits.map(habit => {
    const habitObj = habit.toObject();
    // Check if the habit is completed today
    const completedToday = habit.completionHistory.some(
      (entry: any) => entry.date.toISOString().split('T')[0] === today && entry.completed
    );
    
    return {
      ...habitObj,
      completed_today: completedToday
    };
  });
  
  // Return in the same format as other habit endpoints
  res.status(200).json({
    habits: habitsWithCompletedToday,
    total: habits.length,
    page: page,
    per_page: perPage
  });
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
  
  // Check if the habit is completed today
  const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD format
  const completedToday = habit.completionHistory.some(
    (entry: any) => entry.date.toISOString().split('T')[0] === today && entry.completed
  );
  
  // Create a response object with the completed_today field
  const habitResponse = {
    ...habit.toObject(),
    completed_today: completedToday
  };
  
  res.status(200).json(habitResponse);
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
  
  let habitWasJustMarkedCompleted = false; // Flag to track if completion status changed to true
  
  if (existingEntryIndex !== -1) {
    const wasCompletedBefore = habit.completionHistory[existingEntryIndex].completed;
    habit.completionHistory[existingEntryIndex].completed = completed;
    habit.completionHistory[existingEntryIndex].notes = notes || habit.completionHistory[existingEntryIndex].notes;
    
    if (!wasCompletedBefore && completed) {
      habitWasJustMarkedCompleted = true;
      if (userId) {
        await updateProductivityMetrics(userId.toString(), completionDate, { habitsCompleted: 1 });
      }
    } else if (wasCompletedBefore && !completed) {
      if (userId) {
        await updateProductivityMetrics(userId.toString(), completionDate, { habitsCompleted: -1 });
      }
    }
  } else {
    habit.completionHistory.push({
      date: completionDate,
      completed,
      notes
    });
    if (completed) {
      habitWasJustMarkedCompleted = true;
      if (userId) {
        await updateProductivityMetrics(userId.toString(), completionDate, { habitsCompleted: 1 });
      }
    }
  }
  
  // Emit HABIT_COMPLETED_AT event if the habit was just marked as completed
  if (habitWasJustMarkedCompleted) {
    eventEmitter.emit('HABIT_COMPLETED_AT', {
      userId: habit.user.toString(),
      habitId: habit._id.toString(),
      habitTitle: habit.title,
      completedAt: completionDate, // This is the exact date/time from the input or entry
      frequency: habit.frequency,
      category: habit.category // Assuming habit model has category, add if present
      // Consider adding other habit details if relevant for achievements
    });
    // console.log(`Event HABIT_COMPLETED_AT emitted for habit ${habit._id} at ${completionDate.toISOString()}`);
  }
  
  // Sort completion history by date (newest first for display, but calculation needs oldest first for some logic)
  habit.completionHistory.sort((a, b) => 
    new Date(b.date).getTime() - new Date(a.date).getTime()
  );
  
  // Calculate and update streak data (this emits HABIT_STREAK_UPDATED)
  await updateStreakDataAndSave(habit, completed, completionDate);
  
  // Calculate consistency score (percentage of completion over the last 30 days)
  const consistencyScore = calculateConsistency(habit);
  
  await habit.save();
  
  const response = habit.toObject() as any; 
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
  
  const habitObject = habit.toObject() as HabitDataType;
  
  // Calculate overall completion rate
  const totalEntries = habitObject.completionHistory.length;
  const completedEntries = habitObject.completionHistory.filter(entry => entry.completed).length;
  const completionRate = totalEntries > 0 ? (completedEntries / totalEntries) * 100 : 0;
  
  // Get longest streak using the new historical calculation
  const longestStreak = calculateAllHistoricalStreaks(habitObject);
  
  // Get current streak
  const currentStreakValue = calculateCurrentStreak(habitObject);
  
  // Get completion counts by day of week
  const completionsByDay = getCompletionsByDayOfWeek(habitObject);
  
  // Get completion counts by time of day
  const completionsByTime = getCompletionsByTimeOfDay(habitObject);
  
  // Calculate consistency
  const consistency = calculateConsistency(habitObject);
  
  res.status(200).json({
    completionRate,
    currentStreak: currentStreakValue, // Use the accurately calculated current streak
    longestStreak, // Use the new historical longest streak
    consistency,
    totalEntries,
    completedEntries,
    completionsByDay,
    completionsByTime,
    // oldStreak: habit.streak, // Keeping the old habit.streak for a moment if needed for comparison during debug
  });
});

/**
 * @desc    Get habits for today
 * @route   GET /api/habits/today
 * @access  Private
 */
export const getTodaysHabits = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const today = new Date();
  today.setHours(0, 0, 0, 0); // Normalize today to the start of the day for consistent comparisons
  
  const currentDayOfWeek = today.getDay(); // 0 (Sun) - 6 (Sat)
  const currentDateOfMonth = today.getDate(); // 1 - 31
  const todayISOString = today.toISOString().split('T')[0]; // YYYY-MM-DD
  
  console.log(`Getting today's habits for user ${userId}, date: ${todayISOString}, day of week: ${currentDayOfWeek}, date of month: ${currentDateOfMonth}`);

  const habitsQuery = { 
    user: userId,
    status: 'active' 
  };

  const allActiveHabits = await Habit.find(habitsQuery);
  console.log(`Found ${allActiveHabits.length} active habits for user`);

  const todaysHabits = allActiveHabits.filter(habit => {
    const freq = habit.frequency;
    const freqConfig = habit.frequencyConfig;

    if (freq === 'daily') {
      return true; // Daily habits are always due
    }

    if (freq === 'weekly') {
      if (freqConfig && freqConfig.daysOfWeek && freqConfig.daysOfWeek.includes(currentDayOfWeek)) {
        return true;
      }
      return false;
    }

    if (freq === 'monthly') {
      if (freqConfig && freqConfig.datesOfMonth && freqConfig.datesOfMonth.includes(currentDateOfMonth)) {
        return true;
      }
      // Handle 'lastDay' if it's a special value in datesOfMonth for monthly habits
      if (freqConfig && freqConfig.datesOfMonth && freqConfig.datesOfMonth.includes(-1)) { // Assuming -1 denotes last day of month
        const lastDayOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0).getDate();
        if (currentDateOfMonth === lastDayOfMonth) {
          return true;
        }
      }
      return false;
    }

    if (freq === 'specific_dates') {
      if (freqConfig && freqConfig.specificDates) {
        return freqConfig.specificDates.some(specificDate => {
          const d = new Date(specificDate);
          d.setHours(0,0,0,0);
          return d.toISOString().split('T')[0] === todayISOString;
        });
      }
      return false;
    }
    
    // If frequency is unknown or not handled, exclude it by default
    console.warn(`Habit ${habit._id} has unknown frequency ${freq} for today's check.`);
    return false; 
  });
  
  console.log(`Returning ${todaysHabits.length} habits for today after filtering`);
  
  const todaysHabitsWithCompletion = todaysHabits.map(habit => {
    const habitObj = habit.toObject();
    const completedToday = habit.completionHistory.some(
      (entry: any) => new Date(entry.date).toISOString().split('T')[0] === todayISOString && entry.completed
    );
    
    return {
      ...habitObj,
      completed_today: completedToday
    };
  });
  
  res.status(200).json({
    habits: todaysHabitsWithCompletion,
    total: todaysHabitsWithCompletion.length,
    page: 1, // Pagination might not be needed here, or adjust if so
    per_page: todaysHabitsWithCompletion.length
  });
});

/**
 * @desc    Mark a habit as skipped for a specific date
 * @route   POST /api/habits/:id/skip
 * @access  Private
 */
export const skipHabit = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const habitId = req.params.id;
  const { date, skipReason } = req.body;

  if (!date) {
    throw new AppError('Date is required for skipping a habit', 400);
  }

  const habit = await Habit.findOne({ _id: habitId, user: userId });

  if (!habit) {
    throw new AppError('Habit not found', 404);
  }

  const skipDate = new Date(date);
  skipDate.setHours(0,0,0,0); // Normalize to start of day

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  if (skipDate > today) {
    throw new AppError('Cannot skip habit for future dates', 400);
  }

  const existingEntryIndex = habit.completionHistory.findIndex(
    entry => new Date(entry.date).toDateString() === skipDate.toDateString()
  );

  if (existingEntryIndex !== -1) {
    const wasCompletedBefore = habit.completionHistory[existingEntryIndex].completed;
    habit.completionHistory[existingEntryIndex].completed = false;
    habit.completionHistory[existingEntryIndex].skipReason = skipReason || habit.completionHistory[existingEntryIndex].skipReason;
    // If it was completed and now skipped, decrement metrics
    if (wasCompletedBefore && userId) {
        await updateProductivityMetrics(userId.toString(), skipDate, { habitsCompleted: -1 });
    }
  } else {
    habit.completionHistory.push({
      date: skipDate,
      completed: false,
      notes: undefined, // Or keep notes if applicable for skips?
      skipReason: skipReason,
    });
    // Skipping does not increment habitsCompleted
  }

  habit.completionHistory.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());

  // Update streak data (skipping currently acts like a miss for daily streaks)
  await updateStreakDataAndSave(habit as mongoose.Document & HabitDataType, false, skipDate);

  // Placeholder for event emission
  // import eventEmitter from '../services/eventEmitter';
  // eventEmitter.emit('HABIT_SKIPPED', { habitId: habit._id, userId: habit.user, date: skipDate, reason: skipReason });
  // eventEmitter.emit('STREAK_UPDATED', { habitId: habit._id, userId: habit.user, streakData: habit.streakData });

  const response = habit.toObject() as any;
  // Optionally add consistency or other relevant info to response if needed
  res.status(200).json(response);
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

// Renamed from calculateCurrentDailyStreak and made more generic
const calculateCurrentStreak = (habit: HabitDataType): number => {
  if (!habit.completionHistory || habit.completionHistory.length === 0) {
    return 0;
  }

  const sortedHistory = [...habit.completionHistory].sort((a, b) => 
    new Date(a.date).getTime() - new Date(b.date).getTime()
  );
  
  if (habit.frequency === 'daily') {
    return calculateDailyStreakLogic(sortedHistory);
  } else if (habit.frequency === 'weekly') {
    // Pass relevant parts of freqConfig if needed by the specific logic
    return calculateWeeklyStreakLogic(sortedHistory, habit.frequencyConfig);
  } else if (habit.frequency === 'monthly') {
    return calculateMonthlyStreakLogic(sortedHistory, habit.frequencyConfig);
  }

  console.warn(`Unknown habit frequency: ${habit.frequency} for streak calculation.`);
  return 0; 
};

const calculateDailyStreakLogic = (sortedHistory: HabitDataType['completionHistory']): number => {
  let currentStreak = 0;
  let lastCompletionDate: Date | null = null;

  // Iterate from most recent to oldest to find the start of the current streak
  for (let i = sortedHistory.length - 1; i >= 0; i--) {
    const entry = sortedHistory[i];
    const entryDate = new Date(entry.date);
    entryDate.setHours(0, 0, 0, 0);

    if (entry.completed) {
      if (lastCompletionDate === null) { // This is the most recent completed entry
        currentStreak = 1;
      } else {
        const diffTime = lastCompletionDate.getTime() - entryDate.getTime();
        const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24));
        if (diffDays === 1) {
          currentStreak++;
        } else {
          break; // Streak broken
        }
      }
      lastCompletionDate = entryDate;
    } else {
      // If we are counting a streak (lastCompletionDate is not null) and hit an uncompleted day, streak ends.
      // If lastCompletionDate is null, it means we haven't found the first completed day of the potential current streak yet.
      if (lastCompletionDate !== null) {
          break; 
      }
    }
  }

  // Final check: if the most recent completion (stored in lastCompletionDate) 
  // is not today or yesterday, the streak is considered lost.
  if (lastCompletionDate) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
    const diffFromToday = Math.round((today.getTime() - lastCompletionDate.getTime()) / (1000 * 60 * 60 * 24));
    if (diffFromToday > 1) {
      currentStreak = 0;
    }
  } else {
    // No completed entries found at all, or the first one found wasn't completed.
    currentStreak = 0;
  }
  return currentStreak;
};

const getWeekBoundaries = (date: Date): { start: Date, end: Date } => {
  const d = new Date(date);
  d.setHours(0, 0, 0, 0);
  // Monday is 1 (if getDay() is 0 for Sunday), Sunday is 0 or 7.
  // Let's make Monday the start of the week (day 1), Sunday the end (day 0 or 7).
  // Date.prototype.getDay() returns 0 for Sunday, 1 for Monday, ..., 6 for Saturday.
  const dayOfWeek = d.getDay(); // 0 (Sun) - 6 (Sat)
  const diffToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek; // if Sunday (0), go back 6 days. If Monday (1), 0 days.
  const monday = new Date(d.setDate(d.getDate() + diffToMonday));
  
  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);
  sunday.setHours(23, 59, 59, 999); // End of Sunday

  return { start: monday, end: sunday };
};

const calculateWeeklyStreakLogic = (
  sortedHistory: HabitDataType['completionHistory'], 
  freqConfig?: HabitDataType['frequencyConfig']
): number => {
  if (!freqConfig || !freqConfig.timesPerPeriod || freqConfig.timesPerPeriod <= 0) {
    console.warn("Weekly streak calculation requires valid timesPerPeriod in freqConfig. Returning 0.");
    return 0;
  }
  if (sortedHistory.length === 0) return 0;

  const timesPerWeekTarget = freqConfig.timesPerPeriod;
  let currentStreak = 0;
  let lastSuccessfulWeekEndDate: Date | null = null;

  // Group completions by week
  const completionsByWeek: Map<string, { count: number, lastDate: Date }> = new Map();
  sortedHistory.forEach(entry => {
    if (entry.completed) {
      const entryDate = new Date(entry.date);
      const weekBoundaries = getWeekBoundaries(entryDate);
      const weekKey = weekBoundaries.start.toISOString().split('T')[0]; // Use Monday as week key

      const weekData = completionsByWeek.get(weekKey) || { count: 0, lastDate: entryDate };
      weekData.count++;
      if(entryDate > weekData.lastDate) weekData.lastDate = entryDate; // track last completion in that week
      completionsByWeek.set(weekKey, weekData);
    }
  });

  if (completionsByWeek.size === 0) return 0;

  // Get sorted list of week keys (Mondays) for weeks with completions
  const weekKeys = Array.from(completionsByWeek.keys()).sort((a, b) => new Date(b).getTime() - new Date(a).getTime()); // Newest week first
  
  for (const weekKey of weekKeys) {
    const weekData = completionsByWeek.get(weekKey)!;
    const currentWeekStart = new Date(weekKey);

    if (weekData.count >= timesPerWeekTarget) {
      // This week met the criteria
      if (lastSuccessfulWeekEndDate === null) { // This is the most recent successful week
      currentStreak = 1;
    } else {
        // Check if this successful week is consecutive to the last one
        const expectedPreviousWeekEnd = new Date(currentWeekStart);
        expectedPreviousWeekEnd.setDate(currentWeekStart.getDate() - 1); // Should be Sunday of previous week
        expectedPreviousWeekEnd.setHours(23,59,59,999);
        
        // Check if lastSuccessfulWeekEndDate is indeed the end of the immediately preceding week
        // Compare string dates to avoid timezone/ms issues with direct date object comparison for equality
        if (lastSuccessfulWeekEndDate.toISOString().split('T')[0] === expectedPreviousWeekEnd.toISOString().split('T')[0]) {
        currentStreak++;
      } else {
          break; // Streak broken
        }
      }
      const { end } = getWeekBoundaries(currentWeekStart); // get end of current successful week
      lastSuccessfulWeekEndDate = end;
    } else {
      // If a week in the history did not meet criteria, and we were in a streak, it's broken
      if (lastSuccessfulWeekEndDate !== null) {
        break;
      }
    }
  }

  // Final check: if the last successful week is not the current or previous week, streak is lost.
  if (lastSuccessfulWeekEndDate) {
    const today = new Date();
    const currentWeekBoundaries = getWeekBoundaries(today);
    const previousWeekBoundaries = getWeekBoundaries(new Date(today.setDate(today.getDate() - 7)));

    if (
      lastSuccessfulWeekEndDate.getTime() < previousWeekBoundaries.start.getTime() && 
      !(lastSuccessfulWeekEndDate.getTime() >= previousWeekBoundaries.start.getTime() && lastSuccessfulWeekEndDate.getTime() <= previousWeekBoundaries.end.getTime()) &&
      !(lastSuccessfulWeekEndDate.getTime() >= currentWeekBoundaries.start.getTime() && lastSuccessfulWeekEndDate.getTime() <= currentWeekBoundaries.end.getTime())
    ) {
         // If last successful week ended before the start of the previous week, streak is 0
        currentStreak = 0;
    }
  } else {
    currentStreak = 0; // No successful weeks
  }
  
  return currentStreak;
};

const getMonthBoundaries = (date: Date): { start: Date, end: Date } => {
  const d = new Date(date.getFullYear(), date.getMonth(), 1);
  d.setHours(0, 0, 0, 0);
  const endDate = new Date(date.getFullYear(), date.getMonth() + 1, 0);
  endDate.setHours(23, 59, 59, 999);
  return { start: d, end: endDate };
};

// Corrected calculateMonthlyStreakLogic (for CURRENT streak)
const calculateMonthlyStreakLogic = (
  sortedHistory: HabitDataType['completionHistory'], 
  freqConfig?: HabitDataType['frequencyConfig']
): number => {
  if (!freqConfig || !freqConfig.timesPerPeriod || freqConfig.timesPerPeriod <= 0) {
    // console.warn("Monthly streak calculation requires valid timesPerPeriod in freqConfig. Returning 0.");
    return 0;
  }
  if (sortedHistory.length === 0) return 0;

  const timesPerMonthTarget = freqConfig.timesPerPeriod;
  let currentStreakValue = 0; // Use a distinct variable name for this function's current streak count
  let lastSuccessfulPeriodEndDate: Date | null = null;

  const completionsByPeriod: Map<string, { count: number, lastDate: Date }> = new Map();
  sortedHistory.forEach(entry => {
    if (entry.completed) {
      const entryDate = new Date(entry.date);
      const periodBoundaries = getMonthBoundaries(entryDate);
      const periodKey = `${periodBoundaries.start.getFullYear()}-${String(periodBoundaries.start.getMonth() + 1).padStart(2, '0')}`;
      const periodData = completionsByPeriod.get(periodKey) || { count: 0, lastDate: entryDate };
      periodData.count++;
      if (entryDate > periodData.lastDate) periodData.lastDate = entryDate;
      completionsByPeriod.set(periodKey, periodData);
    }
  });

  if (completionsByPeriod.size === 0) return 0;

  const periodKeys = Array.from(completionsByPeriod.keys()).sort((a, b) => {
    const [yearA, monthA] = a.split('-').map(Number);
    const [yearB, monthB] = b.split('-').map(Number);
    if (yearB !== yearA) return yearB - yearA; // Newest period first
    return monthB - monthA;
  });
  
  for (const periodKey of periodKeys) {
    const periodData = completionsByPeriod.get(periodKey)!;
    const [year, monthNum] = periodKey.split('-').map(Number);
    const currentPeriodStart = new Date(year, monthNum - 1, 1);

    if (periodData.count >= timesPerMonthTarget) {
      if (lastSuccessfulPeriodEndDate === null) {
        currentStreakValue = 1;
      } else {
        const expectedPreviousPeriodEnd = new Date(currentPeriodStart);
        expectedPreviousPeriodEnd.setDate(currentPeriodStart.getDate() - 1);
        expectedPreviousPeriodEnd.setHours(23,59,59,999);
        if (lastSuccessfulPeriodEndDate.toISOString().split('T')[0] === expectedPreviousPeriodEnd.toISOString().split('T')[0]) {
          currentStreakValue++;
        } else {
          break; 
        }
      }
      lastSuccessfulPeriodEndDate = getMonthBoundaries(currentPeriodStart).end;
    } else {
      if (lastSuccessfulPeriodEndDate !== null) {
          break;
      }
    }
  }

  if (lastSuccessfulPeriodEndDate) {
    const today = new Date();
    const currentActualPeriodBoundaries = getMonthBoundaries(today);
    const tempPrevPeriod = new Date(today);
    tempPrevPeriod.setMonth(tempPrevPeriod.getMonth() - 1);
    const previousActualPeriodBoundaries = getMonthBoundaries(tempPrevPeriod);

    const isInCurrent = lastSuccessfulPeriodEndDate.getTime() >= currentActualPeriodBoundaries.start.getTime() && lastSuccessfulPeriodEndDate.getTime() <= currentActualPeriodBoundaries.end.getTime();
    const isInPrevious = lastSuccessfulPeriodEndDate.getTime() >= previousActualPeriodBoundaries.start.getTime() && lastSuccessfulPeriodEndDate.getTime() <= previousActualPeriodBoundaries.end.getTime();

    if (!isInCurrent && !isInPrevious) {
      currentStreakValue = 0;
    }
  } else {
    currentStreakValue = 0; 
  }
  return currentStreakValue;
};

const updateStreakDataAndSave = async (habitInstance: mongoose.Document & HabitDataType, wasCompleted: boolean, completionDate: Date) => {
  const habitObject = habitInstance.toObject() as HabitDataType;
  const currentStreak = calculateCurrentStreak(habitObject);

  habitInstance.set('streakData.current', currentStreak);

  const historicalLongest = calculateAllHistoricalStreaks(habitObject);
 
  const newLongest = Math.max(historicalLongest, currentStreak, (habitInstance.get('streakData.longest') || 0));
  habitInstance.set('streakData.longest', newLongest);

  if (wasCompleted) {
    habitInstance.set('streakData.lastCompleted', completionDate);
  }

  if (habitInstance.get('streak') !== undefined) {
    habitInstance.set('streak', currentStreak);
  }

  await habitInstance.save();

  // Emit event after streak data is saved
  const updatedHabitObject = habitInstance.toObject() as HabitDataType; // Get the latest state
  eventEmitter.emit('HABIT_STREAK_UPDATED', {
    userId: updatedHabitObject.user.toString(), // Ensure userId is a string if it's an ObjectId
    habitId: updatedHabitObject._id.toString(),
    streakData: updatedHabitObject.streakData,
    habitTitle: updatedHabitObject.title, // For easier identification in logs or notifications
    frequency: updatedHabitObject.frequency
    // Potentially add more habit data if commonly needed by achievements
  });

  // console.log(`Event HABIT_STREAK_UPDATED emitted for habit ${updatedHabitObject._id}`);
};

// Helper function to calculate streak (Original: still used by getHabitStats - NOW OBSOLETE due to getHabitStats update)
// const calculateStreak = (habit: HabitDataType) => { ... }; // This can now be removed

// Calculate all historical streaks (dispatcher)
const calculateAllHistoricalStreaks = (habit: HabitDataType): number => {
  if (!habit.completionHistory || habit.completionHistory.length === 0) return 0;

  const sortedHistory = [...habit.completionHistory].sort((a, b) => 
    new Date(a.date).getTime() - new Date(b.date).getTime() // Oldest first
  );

  let streaksArray: number[] = [];
  if (habit.frequency === 'daily') {
    streaksArray = calculateAllDailyStreaksLogic(sortedHistory);
  } else if (habit.frequency === 'weekly') {
    streaksArray = calculateAllWeeklyStreaksLogic(sortedHistory, habit.frequencyConfig);
  } else if (habit.frequency === 'monthly') {
    streaksArray = calculateAllMonthlyStreaksLogic(sortedHistory, habit.frequencyConfig);
  } else {
    console.warn(`Unknown habit frequency: ${habit.frequency} for calculating all historical streaks.`);
    return 0;
  }
  return Math.max(0, ...streaksArray);
};

const calculateAllDailyStreaksLogic = (sortedHistory: HabitDataType['completionHistory']): number[] => {
  const streaks: number[] = [];
  let currentStreakLength = 0;
  let lastDate: Date | null = null;
  
  for (let i = 0; i < sortedHistory.length; i++) {
    const entry = sortedHistory[i];
    const entryDate = new Date(entry.date);
    entryDate.setHours(0,0,0,0);
    
    if (entry.completed) {
      if (lastDate === null) { 
        currentStreakLength = 1;
      } else {
        const diffDays = Math.round((entryDate.getTime() - lastDate.getTime()) / (1000 * 60 * 60 * 24));
        if (diffDays === 1) {
          currentStreakLength++;
        } else { 
          if (currentStreakLength > 0) streaks.push(currentStreakLength);
          currentStreakLength = 1; 
        }
      }
      lastDate = entryDate;
    } else { 
      if (currentStreakLength > 0) {
        streaks.push(currentStreakLength);
      }
      currentStreakLength = 0;
      lastDate = null; 
    }
  }
  if (currentStreakLength > 0) streaks.push(currentStreakLength); 
  return streaks.length > 0 ? streaks : [0];
};

const calculateAllWeeklyStreaksLogic = (
  sortedHistory: HabitDataType['completionHistory'], 
  freqConfig?: HabitDataType['frequencyConfig']
): number[] => {
  if (!freqConfig || !freqConfig.timesPerPeriod || freqConfig.timesPerPeriod <= 0) {
    console.warn("Weekly streak calculation requires valid timesPerPeriod in freqConfig. Returning [0].");
    return [0];
  }
  if (sortedHistory.length === 0) return [0];

  const timesPerWeekTarget = freqConfig.timesPerPeriod;
  const streaks: number[] = [];
  let currentSuccessfulWeeksStreak = 0;
  let lastSuccessfulWeekEndDate: Date | null = null;

  const completionsByWeek: Map<string, { count: number, weekStart: Date, weekEnd: Date }> = new Map();
  sortedHistory.forEach(entry => {
    if (entry.completed) {
      const entryDate = new Date(entry.date);
      const weekBoundaries = getWeekBoundaries(entryDate);
      const weekKey = weekBoundaries.start.toISOString().split('T')[0];

      const weekData = completionsByWeek.get(weekKey) || { count: 0, weekStart: weekBoundaries.start, weekEnd: weekBoundaries.end };
      weekData.count++;
      completionsByWeek.set(weekKey, weekData);
    }
  });

  const activeWeekKeys = Array.from(completionsByWeek.keys()).sort((a, b) => new Date(a).getTime() - new Date(b).getTime()); 
  
  if (activeWeekKeys.length === 0) return [0]; // No weeks with any completions

  const firstHistoricalWeekStart = getWeekBoundaries(new Date(activeWeekKeys[0])).start;
  const lastHistoricalWeekStart = getWeekBoundaries(new Date(activeWeekKeys[activeWeekKeys.length - 1])).start;
  let currentIteratingWeekStart = new Date(firstHistoricalWeekStart);

  while(currentIteratingWeekStart.getTime() <= lastHistoricalWeekStart.getTime()){
    const weekKey = currentIteratingWeekStart.toISOString().split('T')[0];
    const weekData = completionsByWeek.get(weekKey);
    const currentWeekBoundaries = getWeekBoundaries(currentIteratingWeekStart);

    if (weekData && weekData.count >= timesPerWeekTarget) {
      if (lastSuccessfulWeekEndDate === null) {
        currentSuccessfulWeeksStreak = 1;
      } else {
        const expectedPreviousWeekEnd = new Date(currentWeekBoundaries.start);
        expectedPreviousWeekEnd.setDate(currentWeekBoundaries.start.getDate() - 1); 
        expectedPreviousWeekEnd.setHours(23,59,59,999);
        
        if (lastSuccessfulWeekEndDate.toISOString().split('T')[0] === expectedPreviousWeekEnd.toISOString().split('T')[0]) {
          currentSuccessfulWeeksStreak++;
        } else {
          if (currentSuccessfulWeeksStreak > 0) streaks.push(currentSuccessfulWeeksStreak);
          currentSuccessfulWeeksStreak = 1; 
        }
      }
      lastSuccessfulWeekEndDate = currentWeekBoundaries.end;
    } else {
      if (currentSuccessfulWeeksStreak > 0) {
        streaks.push(currentSuccessfulWeeksStreak);
      }
      currentSuccessfulWeeksStreak = 0;
      lastSuccessfulWeekEndDate = null; 
    }
    currentIteratingWeekStart.setDate(currentIteratingWeekStart.getDate() + 7);
  }

  if (currentSuccessfulWeeksStreak > 0) {
    streaks.push(currentSuccessfulWeeksStreak);
  }

  return streaks.length > 0 ? streaks : [0];
};

// Corrected calculateAllMonthlyStreaksLogic (for ALL historical streaks)
const calculateAllMonthlyStreaksLogic = (
  sortedHistory: HabitDataType['completionHistory'], 
  freqConfig?: HabitDataType['frequencyConfig']
): number[] => {
  if (!freqConfig || !freqConfig.timesPerPeriod || freqConfig.timesPerPeriod <= 0) {
    // console.warn("Monthly streak calculation requires valid timesPerPeriod in freqConfig for all streaks. Returning [0].");
    return [0];
  }
  if (sortedHistory.length === 0) return [0];

  const timesPerMonthTarget = freqConfig.timesPerPeriod;
  const streaks: number[] = [];
  let currentSuccessfulMonthsStreak = 0;
  let lastSuccessfulPeriodEndDate: Date | null = null;

  const completionsByPeriod: Map<string, { count: number, monthStart: Date, monthEnd: Date }> = new Map();
  sortedHistory.forEach(entry => {
    if (entry.completed) {
      const entryDate = new Date(entry.date);
      const periodBoundaries = getMonthBoundaries(entryDate);
      const periodKey = `${periodBoundaries.start.getFullYear()}-${String(periodBoundaries.start.getMonth() + 1).padStart(2, '0')}`;
      const periodData = completionsByPeriod.get(periodKey) || { count: 0, monthStart: periodBoundaries.start, monthEnd: periodBoundaries.end };
      periodData.count++;
      completionsByPeriod.set(periodKey, periodData);
    }
  });

  const activePeriodKeys = Array.from(completionsByPeriod.keys()).sort((a,b) => {
    const [yearA, monA] = a.split('-').map(Number);
    const [yearB, monB] = b.split('-').map(Number);
    if(yearA !== yearB) return yearA - yearB;
    return monA - monB;
  });

  if (activePeriodKeys.length === 0) return [0];

  const [firstYearStr, firstMonthStr] = activePeriodKeys[0].split('-');
  const firstHistoricalPeriodStart = getMonthBoundaries(new Date(parseInt(firstYearStr), parseInt(firstMonthStr) - 1, 1)).start;
  
  const [lastYearStr, lastMonthStr] = activePeriodKeys[activePeriodKeys.length-1].split('-');
  const lastHistoricalPeriodStart = getMonthBoundaries(new Date(parseInt(lastYearStr), parseInt(lastMonthStr) - 1, 1)).start;
  
  let currentIteratingPeriodStart = new Date(firstHistoricalPeriodStart);

  while(currentIteratingPeriodStart.getTime() <= lastHistoricalPeriodStart.getTime()){
    const periodKey = `${currentIteratingPeriodStart.getFullYear()}-${String(currentIteratingPeriodStart.getMonth() + 1).padStart(2, '0')}`;
    const periodData = completionsByPeriod.get(periodKey);
    const currentPeriodBoundaries = getMonthBoundaries(currentIteratingPeriodStart);

    if (periodData && periodData.count >= timesPerMonthTarget) {
      if (lastSuccessfulPeriodEndDate === null) {
        currentSuccessfulMonthsStreak = 1;
    } else {
        const expectedPreviousPeriodEnd = new Date(currentPeriodBoundaries.start);
        expectedPreviousPeriodEnd.setDate(currentPeriodBoundaries.start.getDate() - 1); 
        expectedPreviousPeriodEnd.setHours(23,59,59,999);
        
        if (lastSuccessfulPeriodEndDate.toISOString().split('T')[0] === expectedPreviousPeriodEnd.toISOString().split('T')[0]) {
          currentSuccessfulMonthsStreak++;
        } else { 
          if (currentSuccessfulMonthsStreak > 0) streaks.push(currentSuccessfulMonthsStreak);
          currentSuccessfulMonthsStreak = 1; 
        }
      }
      lastSuccessfulPeriodEndDate = currentPeriodBoundaries.end;
    } else { 
      if (currentSuccessfulMonthsStreak > 0) {
        streaks.push(currentSuccessfulMonthsStreak);
      }
      currentSuccessfulMonthsStreak = 0;
      lastSuccessfulPeriodEndDate = null; 
    }
    currentIteratingPeriodStart.setMonth(currentIteratingPeriodStart.getMonth() + 1);
    currentIteratingPeriodStart.setDate(1); 
  }

  if (currentSuccessfulMonthsStreak > 0) {
    streaks.push(currentSuccessfulMonthsStreak);
  }
  return streaks.length > 0 ? streaks : [0];
};

// Helper function to calculate consistency score
const calculateConsistency = (habit: HabitDataType): number => {
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
const getCompletionsByDayOfWeek = (habit: HabitDataType) => {
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
const getCompletionsByTimeOfDay = (habit: HabitDataType) => {
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
