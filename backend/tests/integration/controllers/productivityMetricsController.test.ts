import { describe, it, expect, beforeEach } from '@jest/globals';
import mongoose from 'mongoose';
import { ProductivityMetrics, Task, Habit, Goal } from '../../../src/models';
import { authenticatedRequest } from '../../helpers/testServer';
import { createTestTask, createTestHabit, createTestGoal, createTestProductivityMetrics } from '../../helpers/testData';

describe('Productivity Metrics Controller Integration Tests', () => {
  let auth: any;
  
  beforeEach(async () => {
    // Create authenticated request for each test
    auth = await authenticatedRequest();
  });
  
  describe('GET /api/productivity', () => {
    it('should return empty array when no metrics exist', async () => {
      const response = await auth.get('/api/productivity');
      
      expect(response.status).toBe(200);
      expect(response.body).toEqual([]);
    });
    
    it('should return metrics for date range', async () => {
      // Create metrics for different days
      const userId = auth.user._id;
      
      // Create metrics for 3 days ago
      const date1 = new Date();
      date1.setDate(date1.getDate() - 3);
      date1.setHours(0, 0, 0, 0);
      await createTestProductivityMetrics(userId, { 
        date: date1, 
        tasksCompleted: 5,
        habitCompletionRate: 80,
        focusTime: 120
      });
      
      // Create metrics for 2 days ago
      const date2 = new Date();
      date2.setDate(date2.getDate() - 2);
      date2.setHours(0, 0, 0, 0);
      await createTestProductivityMetrics(userId, { 
        date: date2, 
        tasksCompleted: 3,
        habitCompletionRate: 60,
        focusTime: 90
      });
      
      // Create metrics for yesterday
      const date3 = new Date();
      date3.setDate(date3.getDate() - 1);
      date3.setHours(0, 0, 0, 0);
      await createTestProductivityMetrics(userId, { 
        date: date3, 
        tasksCompleted: 7,
        habitCompletionRate: 100,
        focusTime: 150
      });
      
      // Get metrics for the last 7 days
      const response = await auth.get('/api/productivity');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(3);
      expect(response.body[0].date).toBeDefined();
      expect(response.body[0].tasksCompleted).toBeDefined();
      expect(response.body[0].habitCompletionRate).toBeDefined();
    });
  });
  
  describe('POST /api/productivity/focus', () => {
    it('should track focus time', async () => {
      const focusData = {
        minutes: 30
      };
      
      const response = await auth.post('/api/productivity/focus', focusData);
      
      expect(response.status).toBe(200);
      expect(response.body.focusTime).toBe(30);
      
      // Verify focus time was recorded in the database
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const metrics = await ProductivityMetrics.findOne({
        user: auth.user._id,
        date: today
      });
      
      expect(metrics).not.toBeNull();
      expect(metrics?.focusTime).toBe(30);
    });
    
    it('should accumulate focus time for the same day', async () => {
      // Track focus time twice
      await auth.post('/api/productivity/focus', { minutes: 30 });
      const response = await auth.post('/api/productivity/focus', { minutes: 45 });
      
      expect(response.status).toBe(200);
      expect(response.body.focusTime).toBe(75); // 30 + 45
      
      // Verify accumulated focus time in database
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const metrics = await ProductivityMetrics.findOne({
        user: auth.user._id,
        date: today
      });
      
      expect(metrics).not.toBeNull();
      expect(metrics?.focusTime).toBe(75);
    });
  });
  
  describe('POST /api/productivity/rating', () => {
    it('should record daily rating', async () => {
      const ratingData = {
        rating: 4
      };
      
      const response = await auth.post('/api/productivity/rating', ratingData);
      
      expect(response.status).toBe(200);
      expect(response.body.dayRating).toBe(4);
      
      // Verify rating was recorded in database
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const metrics = await ProductivityMetrics.findOne({
        user: auth.user._id,
        date: today
      });
      
      expect(metrics).not.toBeNull();
      expect(metrics?.dayRating).toBe(4);
    });
    
    it('should reject invalid ratings', async () => {
      const invalidRating = {
        rating: 6 // Rating should be 1-5
      };
      
      const response = await auth.post('/api/productivity/rating', invalidRating);
      
      expect(response.status).toBe(400);
    });
  });
  
  describe('ML Data Collection', () => {
    it('should calculate productivity score based on multiple factors', async () => {
      const userId = auth.user._id;
      
      // Create tasks, habits, and complete some of them to generate metrics
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
    
    it('should identify peak productivity periods', async () => {
      const userId = auth.user._id;
      
      // Create metrics for several days with different patterns
      const daysOfWeek = [0, 1, 2, 3, 4, 5, 6]; // Sunday to Saturday
      
      for (const day of daysOfWeek) {
        // Create date for each day of the week in the past
        const date = new Date();
        date.setDate(date.getDate() - (date.getDay() - day + 7) % 7);
        date.setHours(0, 0, 0, 0);
        
        // Set productivity score higher for Tuesday, Wednesday, Thursday
        let productivityScore = 50; // Default
        
        if (day >= 2 && day <= 4) {
          productivityScore = 85; // Higher for Tue, Wed, Thu
        }
        
        await createTestProductivityMetrics(userId, {
          date: date,
          productivityScore: productivityScore,
          tasksCompleted: day >= 2 && day <= 4 ? 8 : 3,
          focusTime: day >= 2 && day <= 4 ? 180 : 60
        });
      }
      
      // Get productivity insights (weekly analysis)
      const response = await auth.get('/api/productivity/insights');
      
      // Verify the API can identify the most productive days
      expect(response.status).toBe(200);
      expect(response.body).toHaveProperty('mostProductiveDays');
      expect(response.body.mostProductiveDays).toEqual(expect.arrayContaining([2, 3, 4])); // Tuesday, Wednesday, Thursday
    });
  });
}); 