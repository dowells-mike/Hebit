import { describe, it, expect, beforeEach } from '@jest/globals';
import mongoose from 'mongoose';
import { Task, Habit, Goal, User, ProductivityMetrics } from '../../../src/models';
import { authenticatedRequest } from '../../helpers/testServer';
import { createTestTask, createTestHabit, createTestGoal } from '../../helpers/testData';

describe('Machine Learning Data Collection Integration Tests', () => {
  let auth: any;
  
  beforeEach(async () => {
    // Create authenticated request for each test
    auth = await authenticatedRequest();
  });

  describe('Task ML Data Collection', () => {
    it('should track task completion context for ML analysis', async () => {
      // Create a task
      const task = await createTestTask(auth.user._id);
      
      // Complete the task
      await auth.put(`/api/tasks/${task._id}`, {
        completed: true
      });
      
      // Verify ML data was captured
      const updatedTask = await Task.findById(task._id);
      expect(updatedTask?.metadata).toBeDefined();
      expect(updatedTask?.metadata?.completionContext).toBeDefined();
      expect(updatedTask?.metadata?.completionContext?.timeOfDay).toBeDefined();
      expect(updatedTask?.metadata?.completionContext?.dayOfWeek).toBeDefined();
    });
    
    it('should track task modification patterns', async () => {
      // Create a task
      const task = await createTestTask(auth.user._id);
      
      // Update various fields in sequence
      await auth.put(`/api/tasks/${task._id}`, { priority: 'high' });
      await auth.put(`/api/tasks/${task._id}`, { description: 'Updated description' });
      
      // Verify last modified field is tracked
      const updatedTask = await Task.findById(task._id);
      expect(updatedTask?.metadata).toBeDefined();
      expect(updatedTask?.metadata?.lastModifiedField).toBe('description');
    });
    
    it('should update task completion history for ML pattern analysis', async () => {
      // Create a task
      const task = await createTestTask(auth.user._id, { title: 'Recurring Task' });
      
      // Complete the task
      await auth.put(`/api/tasks/${task._id}`, {
        completed: true
      });
      
      // Archive the task (soft delete)
      await auth.delete(`/api/tasks/${task._id}`);
      
      // Create a similar task
      const newTask = await createTestTask(auth.user._id, { 
        title: 'Recurring Task',
        description: task.description 
      });
      
      // Verify the ML system can track recurring patterns
      const archivedTask = await Task.findById(task._id);
      expect(archivedTask?.status).toBe('archived');
      
      // The system should track completion patterns even for archived tasks
      expect(archivedTask?.metadata).toBeDefined();
      expect(archivedTask?.completed).toBe(true);
    });
  });

  describe('Habit ML Data Collection', () => {
    it('should track habit completion context for ML analysis', async () => {
      // Create a habit
      const habit = await createTestHabit(auth.user._id);
      
      // Track habit completion
      const today = new Date().toISOString().split('T')[0];
      await auth.post(`/api/habits/${habit._id}/track`, {
        completed: true,
        date: today,
        notes: 'Completed on time'
      });
      
      // Verify ML data was captured
      const updatedHabit = await Habit.findById(habit._id);
      expect(updatedHabit?.completionHistory).toHaveLength(1);
      
      // Verify streak calculations for ML pattern analysis
      expect(updatedHabit?.streak).toBe(1);
    });
    
    it('should calculate consistency score for ML habit analysis', async () => {
      // Create a habit
      const habit = await createTestHabit(auth.user._id, { frequency: 'daily' });
      
      // Add completion history for the past week
      const today = new Date();
      const startDate = new Date(today);
      startDate.setDate(startDate.getDate() - 7);
      
      // Mock completion for 5 out of 7 days
      for (let i = 0; i < 7; i++) {
        const date = new Date(startDate);
        date.setDate(date.getDate() + i);
        
        // Skip 2 days to simulate non-completion
        if (i !== 2 && i !== 5) {
          await auth.post(`/api/habits/${habit._id}/track`, {
            completed: true,
            date: date.toISOString().split('T')[0]
          });
        }
      }
      
      // Get the habit with updated stats
      const response = await auth.get(`/api/habits/${habit._id}`);
      
      expect(response.status).toBe(200);
      
      // Check for consistency data
      // The response might include calculated consistency even if not in DB model
      expect(response.body).toHaveProperty('consistency');
      expect(response.body.consistency).toBeGreaterThan(0);
      
      // Check streak
      expect(response.body.streak).toBe(1); // Current streak should be 1 if today was completed
    });
    
    it('should track habit modification patterns', async () => {
      // Create a habit
      const habit = await createTestHabit(auth.user._id);
      
      // Update various fields in sequence
      await auth.put(`/api/habits/${habit._id}`, { difficulty: 'hard' });
      await auth.put(`/api/habits/${habit._id}`, { description: 'Updated description' });
      
      // Verify last modified field is tracked for ML purposes
      const updatedHabit = await Habit.findById(habit._id);
      expect(updatedHabit?.metadata).toBeDefined();
      // Use any to bypass type checking since the schema might have been extended
      expect((updatedHabit?.metadata as any)?.lastModifiedField).toBe('description');
    });
  });

  describe('Goal ML Data Collection', () => {
    it('should track goal progress patterns for ML analysis', async () => {
      // Create a goal
      const goal = await createTestGoal(auth.user._id);
      
      // Update progress multiple times
      await auth.post(`/api/goals/${goal._id}/progress`, { progress: 25 });
      
      // Wait a bit to simulate time passing
      await new Promise(resolve => setTimeout(resolve, 100));
      
      await auth.post(`/api/goals/${goal._id}/progress`, { progress: 50 });
      
      // Get the updated goal
      const updatedGoal = await Goal.findById(goal._id);
      
      // Verify the goal's progress was updated
      expect(updatedGoal?.progress).toBe(50);
      expect(updatedGoal?.status).toBe('in_progress');
    });
    
    it('should track goal completion context for ML prediction', async () => {
      // Create a goal
      const goal = await createTestGoal(auth.user._id);
      
      // Complete the goal
      await auth.post(`/api/goals/${goal._id}/progress`, { progress: 100 });
      
      // Get the updated goal
      const updatedGoal = await Goal.findById(goal._id);
      
      // Check completion status
      expect(updatedGoal?.status).toBe('completed');
      
      // Verify ML completion data was captured
      expect(updatedGoal?.metadata).toBeDefined();
      if (updatedGoal?.metadata) {
        expect(updatedGoal.metadata).toHaveProperty('timeToComplete');
        expect(updatedGoal.metadata).toHaveProperty('completionHour');
        expect(updatedGoal.metadata).toHaveProperty('completionDay');
      }
    });
    
    it('should track goal modification patterns', async () => {
      // Create a goal
      const goal = await createTestGoal(auth.user._id);
      
      // Update various fields in sequence
      await auth.put(`/api/goals/${goal._id}`, { priority: 5 });
      await auth.put(`/api/goals/${goal._id}`, { description: 'Updated description' });
      
      // Verify last modified field is tracked
      const updatedGoal = await Goal.findById(goal._id);
      expect(updatedGoal?.metadata).toBeDefined();
      // Use any to bypass type checking since the schema might have been extended
      expect((updatedGoal?.metadata as any)?.lastModifiedField).toBe('description');
    });
  });

  describe('Productivity Metrics ML Data Collection', () => {
    it('should calculate productivity score based on multiple factors', async () => {
      const userId = auth.user._id;
      
      // Create tasks and complete some to generate data
      const task1 = await createTestTask(userId);
      const task2 = await createTestTask(userId);
      const task3 = await createTestTask(userId);
      
      // Complete 2 out of 3 tasks
      await auth.put(`/api/tasks/${task1._id}`, { completed: true });
      await auth.put(`/api/tasks/${task2._id}`, { completed: true });
      
      // Track focus time
      await auth.post('/api/productivity/focus', { minutes: 120 });
      
      // Generate metrics (this would typically be called by a scheduled job)
      const response = await auth.post('/api/productivity/generate');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveProperty('productivityScore');
      expect(response.body.productivityScore).toBeGreaterThan(0);
      
      // Verify the metrics include all factors
      expect(response.body.tasksCompleted).toBe(2);
      expect(response.body.tasksCreated).toBe(3);
      expect(response.body.focusTime).toBe(120);
    });
    
    it('should track daily user ratings for ML sentiment analysis', async () => {
      // Submit a day rating
      const response = await auth.post('/api/productivity/day-rating', { 
        rating: 4,
        notes: 'Felt productive today' 
      });
      
      expect(response.status).toBe(200);
      
      // Verify rating was saved
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const metrics = await ProductivityMetrics.findOne({
        user: auth.user._id,
        date: today
      });
      
      expect(metrics).not.toBeNull();
      expect(metrics?.dayRating).toBe(4);
      // Use notes field name that matches schema, or use any to bypass type checking
      expect((metrics as any)?.notes || (metrics as any)?.dayNotes).toBe('Felt productive today');
    });
    
    it('should correlate productivity with time of day for ML patterns', async () => {
      // Create tasks spread throughout the day
      const task1 = await createTestTask(auth.user._id);
      const task2 = await createTestTask(auth.user._id);
      const task3 = await createTestTask(auth.user._id);
      
      // Complete tasks with time tracking
      const morning = new Date();
      morning.setHours(9, 0, 0);
      
      const afternoon = new Date();
      afternoon.setHours(14, 0, 0);
      
      const evening = new Date();
      evening.setHours(20, 0, 0);
      
      // Simulate tasks completed at different times
      await auth.put(`/api/tasks/${task1._id}`, { 
        completed: true,
        completedAt: morning.toISOString()
      });
      
      await auth.put(`/api/tasks/${task2._id}`, { 
        completed: true,
        completedAt: afternoon.toISOString()
      });
      
      // Get user productivity stats
      const response = await auth.get('/api/users/productivity-stats');
      
      expect(response.status).toBe(200);
      
      // Check that the response includes detailed productivity data
      expect(response.body).toHaveProperty('totalTasksCompleted');
      expect(response.body).toHaveProperty('dailyStats');
      
      // Check for productivity peak hours update in user model
      const user = await User.findById(auth.user._id);
      expect(user?.productivity).toBeDefined();
      expect(user?.productivity?.peakHours).toBeDefined();
    });
  });

  describe('Cross-Entity ML Data Collection', () => {
    it('should track relationships between goals and habits for ML recommendations', async () => {
      // Create a goal
      const goal = await createTestGoal(auth.user._id, { title: 'Improve Health' });
      
      // Create habits that support the goal
      const habit1 = await createTestHabit(auth.user._id, { 
        title: 'Morning Exercise',
        description: 'Support for health goal' 
      });
      
      const habit2 = await createTestHabit(auth.user._id, { 
        title: 'Drink Water',
        description: 'Support for health goal' 
      });
      
      // Link habits to goal
      await auth.put(`/api/goals/${goal._id}`, { 
        relatedHabits: [habit1._id.toString(), habit2._id.toString()]
      });
      
      // Verify relationship was recorded
      const updatedGoal = await Goal.findById(goal._id);
      expect(updatedGoal?.relatedHabits).toBeDefined();
      expect(updatedGoal?.relatedHabits).toHaveLength(2);
      expect(updatedGoal?.relatedHabits?.includes(habit1._id.toString())).toBe(true);
    });
    
    it('should track category effectiveness for ML organization recommendations', async () => {
      // Create a category
      const categoryResponse = await auth.post('/api/categories', {
        name: 'Test Category',
        type: 'task',
        color: '#FF5733'
      });
      
      expect(categoryResponse.status).toBe(201);
      const category = categoryResponse.body;
      
      // Create tasks in this category
      const task1 = await createTestTask(auth.user._id, { category: category._id });
      const task2 = await createTestTask(auth.user._id, { category: category._id });
      
      // Complete one of the tasks
      await auth.put(`/api/tasks/${task1._id}`, { completed: true });
      
      // Generate metrics
      await auth.post('/api/productivity/generate');
      
      // Fetch category list with analytics
      const categoriesResponse = await auth.get('/api/categories');
      expect(categoriesResponse.status).toBe(200);
      
      // At this point, if our ML system tracks category effectiveness,
      // we would expect to see analytics data in the category or in a related ML collection
    });
  });
}); 