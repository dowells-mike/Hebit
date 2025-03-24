import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import Habit from '../models/Habit';
import { AuthRequest } from '../types';

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
  
  // Execute the query with filters and sort by recently created
  const habits = await Habit.find(filter).sort({ createdAt: -1 });
  
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
  
  const habit = await Habit.create({
    ...req.body,
    user: userId,
    streak: 0,
    completionHistory: []
  });
  
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
  
  // Update the habit
  const updatedHabit = await Habit.findByIdAndUpdate(
    habitId,
    req.body,
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
  
  await Habit.findByIdAndDelete(habitId);
  
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
  const { completed, date } = req.body;
  
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
  
  if (existingEntryIndex !== -1) {
    // Update existing entry
    habit.completionHistory[existingEntryIndex].completed = completed;
  } else {
    // Add new entry
    habit.completionHistory.push({
      date: completionDate,
      completed
    });
  }
  
  // Sort completion history by date (newest first)
  habit.completionHistory.sort((a, b) => 
    new Date(b.date).getTime() - new Date(a.date).getTime()
  );
  
  // Calculate streak
  let currentStreak = 0;
  
  if (completed) {
    currentStreak = 1;
    
    // Calculate streak based on consecutive completed days
    for (let i = 1; i < habit.completionHistory.length; i++) {
      if (!habit.completionHistory[i].completed) {
        break;
      }
      
      const currentDate = new Date(habit.completionHistory[i - 1].date);
      const prevDate = new Date(habit.completionHistory[i].date);
      
      // Check if dates are consecutive
      const diffTime = Math.abs(currentDate.getTime() - prevDate.getTime());
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      
      if (diffDays === 1) {
        currentStreak++;
      } else {
        break;
      }
    }
  }
  
  habit.streak = currentStreak;
  await habit.save();
  
  res.status(200).json(habit);
});
