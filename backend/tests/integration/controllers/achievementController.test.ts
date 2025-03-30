import { describe, it, expect, beforeEach } from '@jest/globals';
import mongoose from 'mongoose';
import { Achievement, UserAchievement } from '../../../src/models';
import { authenticatedRequest } from '../../helpers/testServer';

describe('Achievement Controller Integration Tests', () => {
  let auth: any;
  
  beforeEach(async () => {
    // Create authenticated request for each test
    auth = await authenticatedRequest();
  });
  
  // Helper function to create a test achievement
  const createTestAchievement = async (customData = {}) => {
    const achievementData = {
      name: 'Test Achievement',
      description: 'This is a test achievement',
      category: 'tasks',
      points: 100,
      icon: 'trophy',
      criteria: 'Complete 10 tasks',
      ...customData
    };
    
    const achievement = await Achievement.create(achievementData);
    return achievement;
  };
  
  // Helper function to create user achievement progress
  const createUserAchievementProgress = async (userId: mongoose.Types.ObjectId, achievementId: mongoose.Types.ObjectId, customData = {}) => {
    const progressData = {
      user: userId,
      achievement: achievementId,
      progress: 0,
      earned: false,
      ...customData
    };
    
    const userAchievement = await UserAchievement.create(progressData);
    return userAchievement;
  };
  
  describe('GET /api/achievements', () => {
    it('should return all achievements with user progress', async () => {
      // Create test achievements
      const achievement1 = await createTestAchievement({ name: 'Achievement 1', category: 'tasks' });
      const achievement2 = await createTestAchievement({ name: 'Achievement 2', category: 'habits' });
      const achievement3 = await createTestAchievement({ name: 'Achievement 3', category: 'goals' });
      
      // Create progress for one achievement
      await createUserAchievementProgress(auth.user._id, achievement1._id, { progress: 50 });
      
      // Complete one achievement
      await createUserAchievementProgress(auth.user._id, achievement2._id, { 
        progress: 100, 
        earned: true,
        earnedAt: new Date()
      });
      
      const response = await auth.get('/api/achievements');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(3);
      
      // Check that progress data is included
      const achievementWithProgress = response.body.find((a: any) => a._id === achievement1._id.toString());
      expect(achievementWithProgress).toBeDefined();
      expect(achievementWithProgress.progress).toBe(50);
      expect(achievementWithProgress.earned).toBe(false);
      
      // Check that earned achievement is marked correctly
      const earnedAchievement = response.body.find((a: any) => a._id === achievement2._id.toString());
      expect(earnedAchievement).toBeDefined();
      expect(earnedAchievement.progress).toBe(100);
      expect(earnedAchievement.earned).toBe(true);
      expect(earnedAchievement.earnedAt).toBeDefined();
      
      // Check that achievement with no progress has default values
      const noProgressAchievement = response.body.find((a: any) => a._id === achievement3._id.toString());
      expect(noProgressAchievement).toBeDefined();
      expect(noProgressAchievement.progress).toBe(0);
      expect(noProgressAchievement.earned).toBe(false);
      expect(noProgressAchievement.earnedAt).toBeNull();
    });
  });
  
  describe('GET /api/achievements/category/:category', () => {
    it('should return achievements filtered by category', async () => {
      // Create test achievements in different categories
      await createTestAchievement({ name: 'Task Achievement 1', category: 'tasks' });
      await createTestAchievement({ name: 'Task Achievement 2', category: 'tasks' });
      await createTestAchievement({ name: 'Habit Achievement', category: 'habits' });
      await createTestAchievement({ name: 'Goal Achievement', category: 'goals' });
      
      // Get tasks category achievements
      const response = await auth.get('/api/achievements/category/tasks');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(2);
      expect(response.body[0].category).toBe('tasks');
      expect(response.body[1].category).toBe('tasks');
    });
    
    it('should return 400 for invalid category', async () => {
      const response = await auth.get('/api/achievements/category/invalid');
      
      expect(response.status).toBe(400);
    });
  });
  
  describe('GET /api/achievements/earned', () => {
    it('should return only earned achievements', async () => {
      // Create test achievements
      const achievement1 = await createTestAchievement({ name: 'Achievement 1' });
      const achievement2 = await createTestAchievement({ name: 'Achievement 2' });
      const achievement3 = await createTestAchievement({ name: 'Achievement 3' });
      
      // Create progress: one in progress, two completed
      await createUserAchievementProgress(auth.user._id, achievement1._id, { progress: 50 });
      
      const earnedDate1 = new Date(Date.now() - 24 * 60 * 60 * 1000); // Yesterday
      await createUserAchievementProgress(auth.user._id, achievement2._id, { 
        progress: 100, 
        earned: true,
        earnedAt: earnedDate1
      });
      
      const earnedDate2 = new Date(); // Today
      await createUserAchievementProgress(auth.user._id, achievement3._id, { 
        progress: 100, 
        earned: true,
        earnedAt: earnedDate2
      });
      
      const response = await auth.get('/api/achievements/earned');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(2);
      
      // Check that only earned achievements are returned
      response.body.forEach((achievement: any) => {
        expect(achievement.earnedAt).toBeDefined();
      });
      
      // Check that most recently earned achievement is first
      expect(new Date(response.body[0].earnedAt).getTime()).toBeGreaterThan(
        new Date(response.body[1].earnedAt).getTime()
      );
    });
    
    it('should return empty array when no achievements are earned', async () => {
      const response = await auth.get('/api/achievements/earned');
      
      expect(response.status).toBe(200);
      expect(response.body).toEqual([]);
    });
  });
  
  describe('POST /api/achievements/check', () => {
    it('should create user achievement progress if it does not exist', async () => {
      // Create test achievement
      const achievement = await createTestAchievement();
      
      // Check progress
      const response = await auth.post('/api/achievements/check', {
        achievementId: achievement._id.toString(),
        progress: 25
      });
      
      expect(response.status).toBe(200);
      expect(response.body._id).toBe(achievement._id.toString());
      expect(response.body.progress).toBe(25);
      expect(response.body.earned).toBe(false);
      
      // Verify progress was recorded in database
      const userAchievement = await UserAchievement.findOne({
        user: auth.user._id,
        achievement: achievement._id
      });
      
      expect(userAchievement).not.toBeNull();
      expect(userAchievement?.progress).toBe(25);
    });
    
    it('should update existing progress', async () => {
      // Create test achievement
      const achievement = await createTestAchievement();
      
      // Create initial progress
      await createUserAchievementProgress(auth.user._id, achievement._id, { progress: 50 });
      
      // Update progress
      const response = await auth.post('/api/achievements/check', {
        achievementId: achievement._id.toString(),
        progress: 75
      });
      
      expect(response.status).toBe(200);
      expect(response.body.progress).toBe(75);
      
      // Verify progress was updated in database
      const userAchievement = await UserAchievement.findOne({
        user: auth.user._id,
        achievement: achievement._id
      });
      
      expect(userAchievement?.progress).toBe(75);
    });
    
    it('should mark achievement as earned when progress reaches 100', async () => {
      // Create test achievement
      const achievement = await createTestAchievement();
      
      // Update to 100% progress
      const response = await auth.post('/api/achievements/check', {
        achievementId: achievement._id.toString(),
        progress: 100
      });
      
      expect(response.status).toBe(200);
      expect(response.body.progress).toBe(100);
      expect(response.body.earned).toBe(true);
      expect(response.body.earnedAt).toBeDefined();
      
      // Verify achievement was marked as earned in database
      const userAchievement = await UserAchievement.findOne({
        user: auth.user._id,
        achievement: achievement._id
      });
      
      expect(userAchievement?.earned).toBe(true);
      expect(userAchievement?.earnedAt).toBeDefined();
    });
    
    it('should not allow progress over 100%', async () => {
      // Create test achievement
      const achievement = await createTestAchievement();
      
      // Try to set progress to 120%
      const response = await auth.post('/api/achievements/check', {
        achievementId: achievement._id.toString(),
        progress: 120
      });
      
      expect(response.status).toBe(200);
      expect(response.body.progress).toBe(100); // Capped at 100
      
      // Verify progress was capped in database
      const userAchievement = await UserAchievement.findOne({
        user: auth.user._id,
        achievement: achievement._id
      });
      
      expect(userAchievement?.progress).toBe(100);
    });
    
    it('should return 400 for missing achievement ID', async () => {
      const response = await auth.post('/api/achievements/check', {
        progress: 50
      });
      
      expect(response.status).toBe(400);
    });
    
    it('should return 404 for non-existent achievement', async () => {
      const fakeId = new mongoose.Types.ObjectId();
      
      const response = await auth.post('/api/achievements/check', {
        achievementId: fakeId.toString(),
        progress: 50
      });
      
      expect(response.status).toBe(404);
    });
  });
  
  describe('Admin Operations (POST, PUT, DELETE /api/achievements)', () => {
    it('should create a new achievement', async () => {
      const achievementData = {
        name: 'New Achievement',
        description: 'A newly created achievement',
        category: 'special',
        points: 200,
        icon: 'star',
        criteria: 'Special criteria'
      };
      
      const response = await auth.post('/api/achievements', achievementData);
      
      expect(response.status).toBe(201);
      expect(response.body.name).toBe(achievementData.name);
      expect(response.body.category).toBe(achievementData.category);
      expect(response.body.points).toBe(achievementData.points);
      
      // Verify achievement was created in database
      const achievement = await Achievement.findById(response.body._id);
      expect(achievement).not.toBeNull();
      expect(achievement?.name).toBe(achievementData.name);
    });
    
    it('should reject achievement creation without required fields', async () => {
      // Missing name
      const response1 = await auth.post('/api/achievements', {
        description: 'Missing required fields',
        category: 'special',
        points: 100,
        icon: 'star',
        criteria: 'Test criteria'
      });
      
      expect(response1.status).toBe(400);
      
      // Missing multiple fields
      const response2 = await auth.post('/api/achievements', {
        name: 'Incomplete Achievement'
      });
      
      expect(response2.status).toBe(400);
    });
    
    it('should update an achievement', async () => {
      // Create a test achievement
      const achievement = await createTestAchievement();
      
      // Update the achievement
      const updateData = {
        name: 'Updated Achievement',
        description: 'This description has been updated',
        points: 150
      };
      
      const response = await auth.put(`/api/achievements/${achievement._id}`, updateData);
      
      expect(response.status).toBe(200);
      expect(response.body.name).toBe(updateData.name);
      expect(response.body.description).toBe(updateData.description);
      expect(response.body.points).toBe(updateData.points);
      expect(response.body.category).toBe(achievement.category); // Unchanged field
      
      // Verify update in database
      const updatedAchievement = await Achievement.findById(achievement._id);
      expect(updatedAchievement?.name).toBe(updateData.name);
      expect(updatedAchievement?.points).toBe(updateData.points);
    });
    
    it('should return 404 for updating non-existent achievement', async () => {
      const fakeId = new mongoose.Types.ObjectId();
      const response = await auth.put(`/api/achievements/${fakeId}`, { name: 'Updated Name' });
      
      expect(response.status).toBe(404);
    });
    
    it('should delete an achievement and its user progress', async () => {
      // Create a test achievement
      const achievement = await createTestAchievement();
      
      // Create some user progress for this achievement
      await createUserAchievementProgress(auth.user._id, achievement._id, { progress: 50 });
      
      // Delete the achievement
      const response = await auth.delete(`/api/achievements/${achievement._id}`);
      
      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
      
      // Verify achievement was deleted from database
      const deletedAchievement = await Achievement.findById(achievement._id);
      expect(deletedAchievement).toBeNull();
      
      // Verify user progress was also deleted
      const userProgress = await UserAchievement.findOne({
        achievement: achievement._id
      });
      expect(userProgress).toBeNull();
    });
    
    it('should return 404 for deleting non-existent achievement', async () => {
      const fakeId = new mongoose.Types.ObjectId();
      const response = await auth.delete(`/api/achievements/${fakeId}`);
      
      expect(response.status).toBe(404);
    });
  });
}); 