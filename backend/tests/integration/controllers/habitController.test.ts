import { describe, it, expect, beforeEach } from '@jest/globals';
import mongoose from 'mongoose';
import { Habit, ProductivityMetrics } from '../../../src/models';
import { authenticatedRequest } from '../../helpers/testServer';
import { createTestHabit } from '../../helpers/testData';

describe('Habit Controller Integration Tests', () => {
  let auth: any;
  
  beforeEach(async () => {
    // Create authenticated request for each test
    auth = await authenticatedRequest();
  });
  
  describe('GET /api/habits', () => {
    it('should return empty array when no habits exist', async () => {
      const response = await auth.get('/api/habits');
      
      expect(response.status).toBe(200);
      expect(response.body).toEqual([]);
    });
    
    it('should return all habits for a user', async () => {
      const userId = auth.user._id;
      
      // Create 3 habits
      await createTestHabit(userId, { title: 'Daily Meditation', frequency: 'daily' });
      await createTestHabit(userId, { title: 'Weekly Review', frequency: 'weekly' });
      await createTestHabit(userId, { title: 'Monthly Planning', frequency: 'monthly' });
      
      const response = await auth.get('/api/habits');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(3);
      expect(response.body[0].title).toBeDefined();
      expect(response.body[0].frequency).toBeDefined();
    });
    
    it('should filter habits by frequency', async () => {
      const userId = auth.user._id;
      
      // Create habits with different frequencies
      await createTestHabit(userId, { title: 'Daily Meditation', frequency: 'daily' });
      await createTestHabit(userId, { title: 'Daily Exercise', frequency: 'daily' });
      await createTestHabit(userId, { title: 'Weekly Review', frequency: 'weekly' });
      
      const response = await auth.get('/api/habits?frequency=daily');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(2);
      expect(response.body[0].frequency).toBe('daily');
      expect(response.body[1].frequency).toBe('daily');
    });
    
    it('should filter habits by category', async () => {
      const userId = auth.user._id;
      
      // Create habits with different categories
      await createTestHabit(userId, { title: 'Meditation', category: 'health' });
      await createTestHabit(userId, { title: 'Reading', category: 'personal' });
      await createTestHabit(userId, { title: 'Exercise', category: 'health' });
      
      const response = await auth.get('/api/habits?category=health');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(2);
      expect(response.body[0].category).toBe('health');
      expect(response.body[1].category).toBe('health');
    });
    
    it('should not show archived habits by default', async () => {
      const userId = auth.user._id;
      
      // Create active and archived habits
      await createTestHabit(userId, { title: 'Active Habit', status: 'active' });
      await createTestHabit(userId, { title: 'Archived Habit', status: 'archived' });
      
      const response = await auth.get('/api/habits');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(1);
      expect(response.body[0].title).toBe('Active Habit');
    });
    
    it('should sort habits as requested', async () => {
      const userId = auth.user._id;
      
      // Create habits with different creation dates
      const habit1 = await createTestHabit(userId, { title: 'Habit 1', difficulty: 'easy' });
      const habit2 = await createTestHabit(userId, { title: 'Habit 2', difficulty: 'medium' });
      const habit3 = await createTestHabit(userId, { title: 'Habit 3', difficulty: 'hard' });
      
      // Sort by difficulty ascending
      const response = await auth.get('/api/habits?sort=difficulty&sortDir=asc');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(3);
      expect(response.body[0].difficulty).toBe('easy');
      expect(response.body[1].difficulty).toBe('medium');
      expect(response.body[2].difficulty).toBe('hard');
    });
  });
  
  describe('GET /api/habits/:id', () => {
    it('should return a habit by ID', async () => {
      const userId = auth.user._id;
      
      // Create a habit
      const habit = await createTestHabit(userId, { 
        title: 'Daily Meditation', 
        frequency: 'daily',
        difficulty: 'medium'
      });
      
      const response = await auth.get(`/api/habits/${habit._id}`);
      
      expect(response.status).toBe(200);
      expect(response.body._id).toBe(habit._id.toString());
      expect(response.body.title).toBe('Daily Meditation');
      expect(response.body.frequency).toBe('daily');
      expect(response.body.difficulty).toBe('medium');
    });
    
    it('should return 404 for non-existent habit', async () => {
      const fakeId = new mongoose.Types.ObjectId();
      const response = await auth.get(`/api/habits/${fakeId}`);
      
      expect(response.status).toBe(404);
    });
  });
  
  describe('POST /api/habits', () => {
    it('should create a new habit', async () => {
      const habitData = {
        title: 'Daily Meditation',
        frequency: 'daily',
        description: 'Meditate for 10 minutes daily',
        category: 'health',
        difficulty: 'easy',
        impact: 4
      };
      
      const response = await auth.post('/api/habits', habitData);
      
      expect(response.status).toBe(201);
      expect(response.body.title).toBe(habitData.title);
      expect(response.body.frequency).toBe(habitData.frequency);
      expect(response.body.description).toBe(habitData.description);
      expect(response.body.category).toBe(habitData.category);
      expect(response.body.difficulty).toBe(habitData.difficulty);
      expect(response.body.impact).toBe(habitData.impact);
      expect(response.body.streak).toBe(0);
      expect(response.body.user).toBe(auth.user._id.toString());
      
      // Verify habit was created in database
      const habit = await Habit.findById(response.body._id);
      expect(habit).not.toBeNull();
      expect(habit?.title).toBe(habitData.title);
      
      // Check that productivity metrics were updated
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const metrics = await ProductivityMetrics.findOne({
        user: auth.user._id,
        date: today
      });
      
      expect(metrics).not.toBeNull();
      expect((metrics as any).habitsCreated).toBe(1);
    });
    
    it('should reject habit creation without title', async () => {
      const invalidHabit = {
        frequency: 'daily'
      };
      
      const response = await auth.post('/api/habits', invalidHabit);
      
      expect(response.status).toBe(400);
    });
    
    it('should reject habit creation without frequency', async () => {
      const invalidHabit = {
        title: 'Daily Meditation'
      };
      
      const response = await auth.post('/api/habits', invalidHabit);
      
      expect(response.status).toBe(400);
    });
    
    it('should use default values when not provided', async () => {
      const minimalHabit = {
        title: 'Daily Meditation',
        frequency: 'daily'
      };
      
      const response = await auth.post('/api/habits', minimalHabit);
      
      expect(response.status).toBe(201);
      expect(response.body.difficulty).toBe('medium'); // Default value
      expect(response.body.impact).toBe(3); // Default value
      expect(response.body.status).toBe('active'); // Default value
    });
  });
  
  describe('PUT /api/habits/:id', () => {
    it('should update a habit', async () => {
      const userId = auth.user._id;
      
      // Create a habit
      const habit = await createTestHabit(userId, { 
        title: 'Daily Meditation', 
        frequency: 'daily',
        difficulty: 'medium'
      });
      
      // Update the habit
      const updates = {
        title: 'Morning Meditation',
        difficulty: 'hard',
        impact: 5
      };
      
      const response = await auth.put(`/api/habits/${habit._id}`, updates);
      
      expect(response.status).toBe(200);
      expect(response.body.title).toBe(updates.title);
      expect(response.body.difficulty).toBe(updates.difficulty);
      expect(response.body.impact).toBe(updates.impact);
      expect(response.body.frequency).toBe('daily'); // Unchanged
      
      // Verify update in database
      const updatedHabit = await Habit.findById(habit._id);
      expect(updatedHabit?.title).toBe(updates.title);
      expect(updatedHabit?.difficulty).toBe(updates.difficulty);
    });
    
    it('should return 404 for updating non-existent habit', async () => {
      const fakeId = new mongoose.Types.ObjectId();
      const response = await auth.put(`/api/habits/${fakeId}`, { title: 'Updated Title' });
      
      expect(response.status).toBe(404);
    });
    
    it('should track metadata of modified fields', async () => {
      const userId = auth.user._id;
      
      // Create a habit
      const habit = await createTestHabit(userId, { title: 'Daily Meditation' });
      
      // Update the title
      const response = await auth.put(`/api/habits/${habit._id}`, { title: 'Morning Meditation' });
      
      expect(response.status).toBe(200);
      expect(response.body.metadata).toBeDefined();
      expect(response.body.metadata.lastModifiedField).toBe('title');
    });
  });
  
  describe('DELETE /api/habits/:id', () => {
    it('should soft-delete (archive) a habit by default', async () => {
      const userId = auth.user._id;
      
      // Create a habit
      const habit = await createTestHabit(userId, { title: 'Daily Meditation' });
      
      // Delete the habit (soft delete by default)
      const response = await auth.delete(`/api/habits/${habit._id}`);
      
      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
      
      // Verify habit was archived, not deleted
      const archivedHabit = await Habit.findById(habit._id);
      expect(archivedHabit).not.toBeNull();
      expect((archivedHabit as any).status).toBe('archived');
    });
    
    it('should permanently delete a habit when specified', async () => {
      const userId = auth.user._id;
      
      // Create a habit
      const habit = await createTestHabit(userId, { title: 'Daily Meditation' });
      
      // Delete the habit permanently
      const response = await auth.delete(`/api/habits/${habit._id}?permanent=true`);
      
      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
      
      // Verify habit was deleted
      const deletedHabit = await Habit.findById(habit._id);
      expect(deletedHabit).toBeNull();
    });
    
    it('should return 404 for deleting non-existent habit', async () => {
      const fakeId = new mongoose.Types.ObjectId();
      const response = await auth.delete(`/api/habits/${fakeId}`);
      
      expect(response.status).toBe(404);
    });
  });
  
  describe('POST /api/habits/:id/track', () => {
    it('should track habit completion', async () => {
      const userId = auth.user._id;
      
      // Create a habit
      const habit = await createTestHabit(userId, { title: 'Daily Meditation' });
      
      // Track completion for today
      const today = new Date();
      const trackData = {
        completed: true,
        date: today.toISOString(),
        notes: 'Great session today'
      };
      
      const response = await auth.post(`/api/habits/${habit._id}/track`, trackData);
      
      expect(response.status).toBe(200);
      expect(response.body.completionHistory).toHaveLength(1);
      expect(response.body.completionHistory[0].completed).toBe(true);
      expect(response.body.completionHistory[0].notes).toBe(trackData.notes);
      expect(response.body.streak).toBe(1); // First completion creates streak of 1
      
      // Verify productivity metrics were updated
      const metricDate = new Date(today);
      metricDate.setHours(0, 0, 0, 0);
      
      const metrics = await ProductivityMetrics.findOne({
        user: auth.user._id,
        date: metricDate
      });
      
      expect(metrics).not.toBeNull();
      expect((metrics as any).habitsCompleted).toBe(1);
    });
    
    it('should update existing completion entry for the same date', async () => {
      const userId = auth.user._id;
      
      // Create a habit
      const habit = await createTestHabit(userId, { title: 'Daily Meditation' });
      
      // Track completion for today
      const today = new Date();
      
      // First mark as completed
      await auth.post(`/api/habits/${habit._id}/track`, {
        completed: true,
        date: today.toISOString()
      });
      
      // Then mark as not completed with notes
      const updateData = {
        completed: false,
        date: today.toISOString(),
        notes: 'Didn\'t have time today'
      };
      
      const response = await auth.post(`/api/habits/${habit._id}/track`, updateData);
      
      expect(response.status).toBe(200);
      expect(response.body.completionHistory).toHaveLength(1);
      expect(response.body.completionHistory[0].completed).toBe(false);
      expect(response.body.completionHistory[0].notes).toBe(updateData.notes);
      expect(response.body.streak).toBe(0); // Streak reset to 0
      
      // Verify productivity metrics were updated (decremented)
      const metricDate = new Date(today);
      metricDate.setHours(0, 0, 0, 0);
      
      const metrics = await ProductivityMetrics.findOne({
        user: auth.user._id,
        date: metricDate
      });
      
      expect(metrics).not.toBeNull();
      expect((metrics as any).habitsCompleted).toBe(0); // Decremented from 1 to 0
    });
    
    it('should return 400 for future dates', async () => {
      const userId = auth.user._id;
      
      // Create a habit
      const habit = await createTestHabit(userId, { title: 'Daily Meditation' });
      
      // Try to track for future date
      const futureDate = new Date();
      futureDate.setDate(futureDate.getDate() + 1);
      
      const trackData = {
        completed: true,
        date: futureDate.toISOString()
      };
      
      const response = await auth.post(`/api/habits/${habit._id}/track`, trackData);
      
      expect(response.status).toBe(400);
    });
    
    it('should require completed status and date', async () => {
      const userId = auth.user._id;
      
      // Create a habit
      const habit = await createTestHabit(userId, { title: 'Daily Meditation' });
      
      // Missing completed
      const response1 = await auth.post(`/api/habits/${habit._id}/track`, {
        date: new Date().toISOString()
      });
      
      expect(response1.status).toBe(400);
      
      // Missing date
      const response2 = await auth.post(`/api/habits/${habit._id}/track`, {
        completed: true
      });
      
      expect(response2.status).toBe(400);
    });
  });
  
  describe('GET /api/habits/:id/stats', () => {
    it('should return habit statistics', async () => {
      const userId = auth.user._id;
      
      // Create a habit
      const habit = await createTestHabit(userId, { title: 'Daily Meditation' });
      
      // Add some completion history
      const today = new Date();
      
      // Complete today
      await auth.post(`/api/habits/${habit._id}/track`, {
        completed: true,
        date: today.toISOString()
      });
      
      // Complete yesterday
      const yesterday = new Date(today);
      yesterday.setDate(yesterday.getDate() - 1);
      
      await auth.post(`/api/habits/${habit._id}/track`, {
        completed: true,
        date: yesterday.toISOString()
      });
      
      // Not completed two days ago
      const twoDaysAgo = new Date(today);
      twoDaysAgo.setDate(twoDaysAgo.getDate() - 2);
      
      await auth.post(`/api/habits/${habit._id}/track`, {
        completed: false,
        date: twoDaysAgo.toISOString()
      });
      
      // Get statistics
      const response = await auth.get(`/api/habits/${habit._id}/stats`);
      
      expect(response.status).toBe(200);
      expect(response.body.completionRate).toBe(66.67); // 2 out of 3 days = ~66.67%
      expect(response.body.currentStreak).toBe(2); // Today and yesterday
      expect(response.body.longestStreak).toBe(2);
      expect(response.body.totalEntries).toBe(3);
      expect(response.body.completedEntries).toBe(2);
      expect(response.body.completionsByDay).toBeDefined();
      expect(response.body.completionsByTime).toBeDefined();
    });
    
    it('should return 404 for non-existent habit', async () => {
      const fakeId = new mongoose.Types.ObjectId();
      const response = await auth.get(`/api/habits/${fakeId}/stats`);
      
      expect(response.status).toBe(404);
    });
  });
}); 