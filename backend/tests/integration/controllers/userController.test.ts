import { describe, it, expect, beforeEach } from '@jest/globals';
import mongoose from 'mongoose';
import { User, ProductivityMetrics } from '../../../src/models';
import { authenticatedRequest } from '../../helpers/testServer';
import { createTestProductivityMetrics } from '../../helpers/testData';

describe('User Controller Integration Tests', () => {
  let auth: any;
  
  beforeEach(async () => {
    // Create authenticated request for each test
    auth = await authenticatedRequest();
  });
  
  describe('GET /api/users/profile', () => {
    it('should return the user profile', async () => {
      const response = await auth.get('/api/users/profile');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveProperty('_id', auth.user._id.toString());
      expect(response.body).toHaveProperty('name', auth.user.name);
      expect(response.body).toHaveProperty('email', auth.user.email);
      expect(response.body).not.toHaveProperty('password');
    });
  });
  
  describe('PUT /api/users/profile', () => {
    it('should update user profile', async () => {
      const updateData = {
        name: 'Updated Name',
        bio: 'This is my updated bio',
        avatarUrl: 'https://example.com/avatar.jpg'
      };
      
      const response = await auth.put('/api/users/profile', updateData);
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveProperty('name', updateData.name);
      expect(response.body).toHaveProperty('bio', updateData.bio);
      expect(response.body).toHaveProperty('avatarUrl', updateData.avatarUrl);
      
      // Verify user was updated in database
      const user = await User.findById(auth.user._id);
      expect(user?.name).toBe(updateData.name);
      expect(user?.bio).toBe(updateData.bio);
    });
    
    it('should not update disallowed fields', async () => {
      const originalEmail = auth.user.email;
      
      const updateData = {
        email: 'newemail@example.com',
        password: 'newpassword',
        isAdmin: true
      };
      
      const response = await auth.put('/api/users/profile', updateData);
      
      expect(response.status).toBe(200);
      expect(response.body).not.toHaveProperty('email', updateData.email);
      
      // Verify email was not updated in database
      const user = await User.findById(auth.user._id);
      expect(user?.email).toBe(originalEmail);
    });
  });
  
  describe('PUT /api/users/settings', () => {
    it('should update user settings', async () => {
      const newSettings = {
        theme: 'dark',
        startScreen: 'tasks',
        notificationPreferences: {
          tasks: true,
          habits: false,
          goals: true,
          system: false
        },
        privacySettings: {
          shareActivity: true,
          allowSuggestions: false
        }
      };
      
      const response = await auth.put('/api/users/settings', { settings: newSettings });
      
      expect(response.status).toBe(200);
      expect(response.body.settings).toEqual(newSettings);
      
      // Verify settings were updated in database
      const user = await User.findById(auth.user._id);
      expect(user?.settings).toEqual(newSettings);
    });
    
    it('should reject invalid settings object', async () => {
      const response = await auth.put('/api/users/settings', { notASettingsObject: true });
      
      expect(response.status).toBe(400);
    });
  });
  
  describe('PUT /api/users/notifications', () => {
    it('should update notification preferences', async () => {
      const notificationPreferences = {
        tasks: false,
        habits: false,
        goals: false,
        system: true
      };
      
      const response = await auth.put('/api/users/notifications', { notificationPreferences });
      
      expect(response.status).toBe(200);
      expect(response.body.settings.notificationPreferences).toEqual(notificationPreferences);
      
      // Verify notification preferences were updated in database
      const user = await User.findById(auth.user._id);
      expect(user?.settings?.notificationPreferences).toEqual(notificationPreferences);
    });
    
    it('should reject invalid notification preferences object', async () => {
      const response = await auth.put('/api/users/notifications', { notPreferences: true });
      
      expect(response.status).toBe(400);
    });
  });
  
  describe('GET /api/users/productivity-stats', () => {
    it('should return productivity statistics for date range', async () => {
      const userId = auth.user._id;
      
      // Create productivity metrics for multiple days
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const yesterday = new Date(today);
      yesterday.setDate(yesterday.getDate() - 1);
      
      const twoDaysAgo = new Date(yesterday);
      twoDaysAgo.setDate(twoDaysAgo.getDate() - 1);
      
      // Create metrics for today
      await createTestProductivityMetrics(userId, {
        date: today,
        tasksCompleted: 5,
        focusTime: 120,
        productivityScore: 85
      });
      
      // Create metrics for yesterday
      await createTestProductivityMetrics(userId, {
        date: yesterday,
        tasksCompleted: 3,
        focusTime: 90,
        productivityScore: 70
      });
      
      // Create metrics for two days ago
      await createTestProductivityMetrics(userId, {
        date: twoDaysAgo,
        tasksCompleted: 1,
        focusTime: 30,
        productivityScore: 40
      });
      
      // Get productivity stats
      const response = await auth.get('/api/users/productivity-stats');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveProperty('totalTasksCompleted', 9);
      expect(response.body).toHaveProperty('totalFocusTime', 240);
      expect(response.body).toHaveProperty('averageProductivityScore', 65);
      expect(response.body).toHaveProperty('bestDay');
      expect(response.body).toHaveProperty('worstDay');
      expect(response.body).toHaveProperty('dailyStats');
      expect(response.body.dailyStats).toHaveLength(3);
      
      // Check best day has highest score
      expect(response.body.bestDay.score).toBe(85);
      
      // Check worst day has lowest score
      expect(response.body.worstDay.score).toBe(40);
      
      // Verify user productivity completion rate was updated
      const user = await User.findById(userId);
      expect(user?.productivity?.completionRate).toBe(65);
    });
    
    it('should respect date range parameters', async () => {
      const userId = auth.user._id;
      
      // Create metrics for multiple days
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const yesterday = new Date(today);
      yesterday.setDate(yesterday.getDate() - 1);
      
      const twoDaysAgo = new Date(yesterday);
      twoDaysAgo.setDate(twoDaysAgo.getDate() - 1);
      
      await createTestProductivityMetrics(userId, {
        date: today,
        tasksCompleted: 5,
        productivityScore: 85
      });
      
      await createTestProductivityMetrics(userId, {
        date: yesterday,
        tasksCompleted: 3,
        productivityScore: 70
      });
      
      await createTestProductivityMetrics(userId, {
        date: twoDaysAgo,
        tasksCompleted: 1,
        productivityScore: 40
      });
      
      // Request only yesterday's data
      const startDate = yesterday.toISOString().split('T')[0];
      const endDate = yesterday.toISOString().split('T')[0];
      
      const response = await auth.get(`/api/users/productivity-stats?startDate=${startDate}&endDate=${endDate}`);
      
      expect(response.status).toBe(200);
      expect(response.body.dailyStats).toHaveLength(1);
      expect(response.body.totalTasksCompleted).toBe(3);
      expect(response.body.averageProductivityScore).toBe(70);
    });
  });
  
  describe('PUT /api/users/productivity-hours', () => {
    it('should update peak productivity hours', async () => {
      const peakHours = [8, 9, 10, 14, 15, 16];
      
      const response = await auth.put('/api/users/productivity-hours', { peakHours });
      
      expect(response.status).toBe(200);
      expect(response.body.productivity.peakHours).toEqual(peakHours);
      
      // Verify peak hours were updated in database
      const user = await User.findById(auth.user._id);
      expect(user?.productivity?.peakHours).toEqual(peakHours);
    });
    
    it('should reject invalid peak hours', async () => {
      // Hours outside valid range
      const response1 = await auth.put('/api/users/productivity-hours', { 
        peakHours: [8, 9, 24] 
      });
      
      expect(response1.status).toBe(400);
      
      // Not an array
      const response2 = await auth.put('/api/users/productivity-hours', { 
        peakHours: 'not an array' 
      });
      
      expect(response2.status).toBe(400);
    });
  });
}); 