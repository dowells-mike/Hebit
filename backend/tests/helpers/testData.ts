import mongoose from 'mongoose';
import { User, Task, Habit, Goal, Category, ProductivityMetrics } from '../../src/models';
import jwt from 'jsonwebtoken';

// Helper to create test users
export const createTestUser = async (customData = {}) => {
  const userData = {
    name: 'Test User',
    email: `test.${Date.now()}@example.com`,
    password: 'password123',
    settings: {
      theme: 'system',
      startScreen: 'dashboard',
      notificationPreferences: {
        tasks: true,
        habits: true,
        goals: true,
        system: true
      },
      privacySettings: {
        shareActivity: false,
        allowSuggestions: true
      }
    },
    productivity: {
      peakHours: [9, 10, 11, 14, 15], // Default productive hours
      completionRate: 0
    },
    ...customData
  };

  const user = await User.create(userData);
  return user;
};

// Helper to generate auth token for a user
export const generateAuthToken = (user: any) => {
  const secretKey = process.env.JWT_SECRET || 'test_jwt_secret';
  const tokenPayload = { id: user._id };
  const tokenOptions = { expiresIn: process.env.JWT_EXPIRATION || '1h' };
  
  // @ts-ignore - Ignoring type checks for jwt.sign as it's causing overload issues
  const token = jwt.sign(tokenPayload, secretKey, tokenOptions);
  return token;
};

// Helper to create a test task
export const createTestTask = async (userId: mongoose.Types.ObjectId | string, customData = {}) => {
  const taskData = {
    user: userId,
    title: 'Test Task',
    description: 'Test Description',
    priority: 'medium',
    status: 'todo',
    dueDate: new Date(Date.now() + 24 * 60 * 60 * 1000), // Tomorrow
    completed: false,
    metadata: {
      completionContext: {
        timeOfDay: null,
        dayOfWeek: null,
        location: null
      },
      timeSpent: 0,
      lastModifiedField: null
    },
    ...customData
  };

  const task = await Task.create(taskData);
  return task;
};

// Helper to create a test habit
export const createTestHabit = async (userId: mongoose.Types.ObjectId | string, customData = {}) => {
  const habitData = {
    user: userId,
    title: 'Test Habit',
    description: 'Test Description',
    frequency: 'daily',
    streak: 0,
    status: 'active',
    difficulty: 'medium',
    impact: 3,
    completionHistory: [],
    metadata: {
      lastModifiedField: null
    },
    ...customData
  };

  const habit = await Habit.create(habitData);
  return habit;
};

// Helper to create a test goal
export const createTestGoal = async (userId: mongoose.Types.ObjectId | string, customData = {}) => {
  const goalData = {
    user: userId,
    title: 'Test Goal',
    description: 'Test Description',
    status: 'not_started',
    priority: 3,
    difficulty: 3,
    progress: 0,
    timePeriod: 'medium_term',
    startDate: new Date(),
    targetDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), // 30 days from now
    milestones: [],
    checkIns: [],
    metadata: {
      lastModifiedField: null
    },
    ...customData
  };

  const goal = await Goal.create(goalData);
  return goal;
};

// Helper to create a test category
export const createTestCategory = async (userId: mongoose.Types.ObjectId | string, customData = {}) => {
  const categoryData = {
    user: userId,
    name: 'Test Category',
    color: '#FF5733',
    ...customData
  };

  const category = await Category.create(categoryData);
  return category;
};

// Helper to create test productivity metrics
export const createTestProductivityMetrics = async (userId: mongoose.Types.ObjectId | string, customData = {}) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const metricsData = {
    user: userId,
    date: today,
    tasksCompleted: 0,
    tasksCreated: 0,
    habitsCompleted: 0,
    habitsCreated: 0,
    goalsCompleted: 0,
    goalsCreated: 0,
    habitCompletionRate: 0,
    focusTime: 0,
    productivityScore: 0,
    dayRating: null,
    ...customData
  };

  const metrics = await ProductivityMetrics.create(metricsData);
  return metrics;
}; 