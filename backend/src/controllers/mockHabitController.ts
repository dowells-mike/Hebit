import { Request, Response } from 'express';
import { AuthRequest } from '../types';
import { catchAsync, AppError } from '../middleware/errorHandler';

// In-memory habit store for development mode
const habits = new Map();
let lastHabitId = 0;

/**
 * @desc    Create a new habit
 * @route   POST /api/habits
 * @access  Private
 */
export const createHabit = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const { title, description, frequency, timeOfDay, daysOfWeek } = req.body;

  if (!title) {
    throw new AppError('Habit title is required', 400);
  }

  if (!frequency) {
    throw new AppError('Habit frequency is required', 400);
  }

  const habitId = (++lastHabitId).toString();
  const habit = {
    _id: habitId,
    user: userId,
    title,
    description: description || '',
    frequency: frequency, // 'daily' | 'weekly' | 'monthly'
    timeOfDay: timeOfDay || '',
    daysOfWeek: daysOfWeek || [],
    streak: 0,
    completionHistory: [],
    createdAt: new Date(),
    updatedAt: new Date()
  };

  habits.set(habitId, habit);

  res.status(201).json(habit);
});

/**
 * @desc    Get all habits for a user
 * @route   GET /api/habits
 * @access  Private
 */
export const getHabits = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const { frequency } = req.query;

  let userHabits = Array.from(habits.values()).filter(
    (habit: any) => habit.user === userId
  );

  // Apply filters if provided
  if (frequency) {
    userHabits = userHabits.filter((habit: any) => habit.frequency === frequency);
  }

  res.status(200).json(userHabits);
});

/**
 * @desc    Get a single habit by ID
 * @route   GET /api/habits/:id
 * @access  Private
 */
export const getHabitById = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const habitId = req.params.id;

  const habit = habits.get(habitId);

  if (!habit) {
    throw new AppError('Habit not found', 404);
  }

  if (habit.user !== userId) {
    throw new AppError('Not authorized to access this habit', 401);
  }

  res.status(200).json(habit);
});

/**
 * @desc    Update a habit
 * @route   PUT /api/habits/:id
 * @access  Private
 */
export const updateHabit = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const habitId = req.params.id;
  const { title, description, frequency, timeOfDay, daysOfWeek } = req.body;

  const habit = habits.get(habitId);

  if (!habit) {
    throw new AppError('Habit not found', 404);
  }

  if (habit.user !== userId) {
    throw new AppError('Not authorized to update this habit', 401);
  }

  if (title) habit.title = title;
  if (description !== undefined) habit.description = description;
  if (frequency) habit.frequency = frequency;
  if (timeOfDay !== undefined) habit.timeOfDay = timeOfDay;
  if (daysOfWeek) habit.daysOfWeek = daysOfWeek;
  habit.updatedAt = new Date();

  habits.set(habitId, habit);

  res.status(200).json(habit);
});

/**
 * @desc    Delete a habit
 * @route   DELETE /api/habits/:id
 * @access  Private
 */
export const deleteHabit = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const habitId = req.params.id;

  const habit = habits.get(habitId);

  if (!habit) {
    throw new AppError('Habit not found', 404);
  }

  if (habit.user !== userId) {
    throw new AppError('Not authorized to delete this habit', 401);
  }

  habits.delete(habitId);

  res.status(200).json({ message: 'Habit deleted successfully' });
});

/**
 * @desc    Mark a habit as completed for today
 * @route   POST /api/habits/:id/complete
 * @access  Private
 */
export const completeHabit = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const habitId = req.params.id;
  const { date = new Date().toISOString() } = req.body;

  const habit = habits.get(habitId);

  if (!habit) {
    throw new AppError('Habit not found', 404);
  }

  if (habit.user !== userId) {
    throw new AppError('Not authorized to update this habit', 401);
  }

  const completionDate = new Date(date);
  const today = new Date();
  
  // Only allow completion for today or past dates
  if (completionDate > today) {
    throw new AppError('Cannot complete habit for future dates', 400);
  }

  // Format date to YYYY-MM-DD for comparison
  const formatDate = (d: Date) => d.toISOString().split('T')[0];
  const completionDateFormatted = formatDate(completionDate);
  
  // Check if already completed for this date
  const alreadyCompleted = habit.completionHistory.some(
    (entry: any) => formatDate(new Date(entry.date)) === completionDateFormatted
  );

  if (alreadyCompleted) {
    throw new AppError('Habit already completed for this date', 400);
  }

  // Add to completion history
  habit.completionHistory.push({
    date: completionDate,
    completed: true
  });

  // Calculate streak
  // We're simplifying streak calculation - in a real app, this would be more complex
  // based on habit frequency and expected completion dates
  habit.streak += 1;

  habits.set(habitId, habit);

  res.status(200).json({
    message: 'Habit marked as completed',
    habit
  });
});

/**
 * @desc    Get habit streak stats
 * @route   GET /api/habits/:id/streak
 * @access  Private
 */
export const getHabitStreak = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const habitId = req.params.id;

  const habit = habits.get(habitId);

  if (!habit) {
    throw new AppError('Habit not found', 404);
  }

  if (habit.user !== userId) {
    throw new AppError('Not authorized to access this habit', 401);
  }

  // Calculate last 30 days completion
  const last30Days = [];
  const today = new Date();
  
  for (let i = 0; i < 30; i++) {
    const date = new Date(today);
    date.setDate(date.getDate() - i);
    const dateStr = date.toISOString().split('T')[0];
    
    const completed = habit.completionHistory.some(
      (entry: any) => new Date(entry.date).toISOString().split('T')[0] === dateStr
    );
    
    last30Days.push({
      date: dateStr,
      completed
    });
  }

  const streakStats = {
    currentStreak: habit.streak,
    last30Days: last30Days.reverse(), // Reverse to get chronological order
    totalCompletions: habit.completionHistory.length
  };

  res.status(200).json(streakStats);
});

/**
 * @desc    Alias for completeHabit for compatibility with routes
 * @route   POST /api/habits/:id/track
 * @access  Private
 */
export const trackHabit = completeHabit;
