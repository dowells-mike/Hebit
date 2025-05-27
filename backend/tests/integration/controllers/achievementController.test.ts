import { describe, it, expect, beforeEach } from '@jest/globals';
import mongoose from 'mongoose';
import { Achievement, UserAchievement } from '../../../src/models';
import { authenticatedRequest, request } from '../../helpers/testServer';
import { createTestAchievement, createTestUserAchievement, createTestUser } from '../../helpers/testData';

describe('Achievement Controller Integration Tests', () => {
  let auth: any;
  let testUser: any;
  
  beforeEach(async () => {
    // Create authenticated request for each test
    const authResult = await authenticatedRequest();
    auth = authResult; // This contains the request methods with token
    testUser = authResult.user; // This is the user object
  });
  
  // GET /api/achievements - Get all public achievements
  describe('GET /api/achievements', () => {
    it('should return an empty array if no achievements exist', async () => {
      const response = await request.get('/api/achievements'); // Use non-authenticated request
      expect(response.status).toBe(200);
      expect(response.body).toEqual([]);
    });

    it('should return all non-secret achievements', async () => {
      await createTestAchievement({ name: 'Public Achievement 1', secret: false });
      await createTestAchievement({ name: 'Public Achievement 2', secret: false });
      await createTestAchievement({ name: 'Secret Achievement 1', secret: true });

      const response = await request.get('/api/achievements');
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(2);
      expect(response.body.some((ach: any) => ach.name === 'Public Achievement 1')).toBe(true);
      expect(response.body.some((ach: any) => ach.name === 'Public Achievement 2')).toBe(true);
      expect(response.body.some((ach: any) => ach.name === 'Secret Achievement 1')).toBe(false);
    });

    it('should not return secret achievements', async () => {
      await createTestAchievement({ name: 'Super Secret Achievement', secret: true });
      await createTestAchievement({ name: 'Another Secret', secret: true });

      const response = await request.get('/api/achievements');
      expect(response.status).toBe(200);
      expect(response.body).toEqual([]);
    });
    
    it('should return achievements with correct structure', async () => {
      const achData = {
        name: 'Well Structured Achievement',
        description: 'A very descriptive text.',
        category: 'goals',
        points: 25,
        icon: 'goal_icon.svg',
        criteria: { type: 'event_based', source: 'user_activity', targetValue: 1, conditionDetails: { eventName: 'USER_SIGNUP' } },
        rarity: 'epic',
        secret: false,
      };
      const createdAch = await createTestAchievement(achData);

      const response = await request.get('/api/achievements');
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(1);
      const returnedAch = response.body[0];

      expect(returnedAch._id).toBe(createdAch._id.toString());
      expect(returnedAch.name).toBe(achData.name);
      expect(returnedAch.description).toBe(achData.description);
      expect(returnedAch.category).toBe(achData.category);
      expect(returnedAch.points).toBe(achData.points);
      expect(returnedAch.icon).toBe(achData.icon);
      expect(returnedAch.criteria.type).toBe(achData.criteria.type);
      expect(returnedAch.criteria.source).toBe(achData.criteria.source);
      expect(returnedAch.criteria.targetValue).toBe(achData.criteria.targetValue);
      expect(returnedAch.criteria.conditionDetails.eventName).toBe(achData.criteria.conditionDetails.eventName);
      expect(returnedAch.rarity).toBe(achData.rarity);
      expect(returnedAch.secret).toBe(false);
    });
  });
  
  // GET /api/achievements/my - Get my achievements (protected)
  describe('GET /api/achievements/my', () => {
    it('should return 401 if not authenticated', async () => {
      const response = await request.get('/api/achievements/my'); // Use non-authenticated request
      expect(response.status).toBe(401);
    });

    it('should return an empty array if no achievements exist in the system', async () => {
      const response = await auth.get('/api/achievements/my');
      expect(response.status).toBe(200);
      expect(response.body).toEqual([]);
    });

    it('should return all achievements with user-specific progress (even if no UserAchievement record exists yet)', async () => {
      const ach1 = await createTestAchievement({ name: 'Public Ach 1', secret: false, points: 10 });
      await createTestAchievement({ name: 'Secret Ach 1 (Unearned)', secret: true, points: 20 });
      const ach3 = await createTestAchievement({ name: 'Public Ach 2', secret: false, points: 30 });

      await createTestUserAchievement(testUser._id, ach3._id, { progress: 50, earned: false });

      const response = await auth.get('/api/achievements/my');
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(2);

      const returnedAch1 = response.body.find((a: any) => a.name === 'Public Ach 1');
      expect(returnedAch1).toBeDefined();
      expect(returnedAch1.name).toBe(ach1.name);
      expect(returnedAch1.progress).toBe(0);
      expect(returnedAch1.earned).toBe(false);
      expect(returnedAch1.seenByUser).toBe(false);

      const returnedAch3 = response.body.find((a: any) => a.name === 'Public Ach 2');
      expect(returnedAch3).toBeDefined();
      expect(returnedAch3.name).toBe(ach3.name);
      expect(returnedAch3.progress).toBe(50);
      expect(returnedAch3.earned).toBe(false);
    });

    it('should include earned secret achievements', async () => {
      const secretAch = await createTestAchievement({ name: 'Top Secret Achievement', secret: true });
      await createTestUserAchievement(testUser._id, secretAch._id, { progress: 100, earned: true, earnedAt: new Date() });

      const response = await auth.get('/api/achievements/my');
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(1);
      const returnedSecretAch = response.body[0];
      expect(returnedSecretAch.name).toBe(secretAch.name);
      expect(returnedSecretAch.earned).toBe(true);
      expect(returnedSecretAch.secret).toBe(true);
    });

    it('should filter out unearned secret achievements', async () => {
      await createTestAchievement({ name: 'Visible Public', secret: false });
      await createTestAchievement({ name: 'Hidden Secret Unearned', secret: true });

      const response = await auth.get('/api/achievements/my');
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(1);
      expect(response.body.some((a: any) => a.name === 'Visible Public')).toBe(true);
      expect(response.body.some((a: any) => a.name === 'Hidden Secret Unearned')).toBe(false);
    });

    it('should correctly reflect seenByUser status', async () => {
      const ach = await createTestAchievement({ name: 'Seen Test Ach', secret: false });
      await createTestUserAchievement(testUser._id, ach._id, { progress: 100, earned: true, seenByUser: true, earnedAt: new Date() });

      const response = await auth.get('/api/achievements/my');
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(1);
      expect(response.body[0].name).toBe(ach.name);
      expect(response.body[0].earned).toBe(true);
      expect(response.body[0].seenByUser).toBe(true);
    });

     it('should return multiple achievements with varied progress and secret status correctly', async () => {
      await createTestAchievement({ name: 'Public Unearned', secret: false });
      const publicAchEarnedUnseen = await createTestAchievement({ name: 'Public Earned Unseen', secret: false });
      const publicAchEarnedSeen = await createTestAchievement({ name: 'Public Earned Seen', secret: false });
      await createTestAchievement({ name: 'Secret Unearned', secret: true });
      const secretAchEarnedUnseen = await createTestAchievement({ name: 'Secret Earned Unseen', secret: true });
      const secretAchEarnedSeen = await createTestAchievement({ name: 'Secret Earned Seen', secret: true });

      await createTestUserAchievement(testUser._id, publicAchEarnedUnseen._id, { progress: 100, earned: true, earnedAt: new Date(), seenByUser: false });
      await createTestUserAchievement(testUser._id, publicAchEarnedSeen._id, { progress: 100, earned: true, earnedAt: new Date(), seenByUser: true });
      await createTestUserAchievement(testUser._id, secretAchEarnedUnseen._id, { progress: 100, earned: true, earnedAt: new Date(), seenByUser: false });
      await createTestUserAchievement(testUser._id, secretAchEarnedSeen._id, { progress: 100, earned: true, earnedAt: new Date(), seenByUser: true });

      const response = await auth.get('/api/achievements/my');
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(5);

      const findByName = (name: string) => response.body.find((a: any) => a.name === name);

      expect(findByName('Public Unearned').earned).toBe(false);
      expect(findByName('Public Earned Unseen').earned).toBe(true);
      expect(findByName('Public Earned Unseen').seenByUser).toBe(false);
      expect(findByName('Public Earned Seen').earned).toBe(true);
      expect(findByName('Public Earned Seen').seenByUser).toBe(true);
      expect(findByName('Secret Unearned')).toBeUndefined();
      expect(findByName('Secret Earned Unseen').earned).toBe(true);
      expect(findByName('Secret Earned Unseen').secret).toBe(true);
      expect(findByName('Secret Earned Unseen').seenByUser).toBe(false);
      expect(findByName('Secret Earned Seen').earned).toBe(true);
      expect(findByName('Secret Earned Seen').secret).toBe(true);
      expect(findByName('Secret Earned Seen').seenByUser).toBe(true);
    });
  });
  
  // GET /api/achievements/earned - Get my earned achievements (protected)
  describe('GET /api/achievements/earned', () => {
    it('should return 401 if not authenticated', async () => {
      const response = await request.get('/api/achievements/earned');
      expect(response.status).toBe(401);
    });

    it('should return an empty array if the user has no earned achievements', async () => {
      await createTestAchievement({ name: 'Unearned Public 1' });
      const secretUnearned = await createTestAchievement({ name: 'Unearned Secret 1', secret: true });
      await createTestUserAchievement(testUser._id, secretUnearned._id, { progress: 50, earned: false });

      const response = await auth.get('/api/achievements/earned');
      expect(response.status).toBe(200);
      expect(response.body).toEqual([]);
    });

    it('should return only earned achievements (public and secret)', async () => {
      const publicAchUnearned = await createTestAchievement({ name: 'Public Unearned', secret: false });
      const publicAchEarned = await createTestAchievement({ name: 'Public Earned', secret: false });
      const secretAchUnearned = await createTestAchievement({ name: 'Secret Unearned', secret: true });
      const secretAchEarned = await createTestAchievement({ name: 'Secret Earned', secret: true });

      await createTestUserAchievement(testUser._id, publicAchUnearned._id, { progress: 50, earned: false });
      await createTestUserAchievement(testUser._id, publicAchEarned._id, { progress: 100, earned: true, earnedAt: new Date(Date.now() - 10000) });
      await createTestUserAchievement(testUser._id, secretAchEarned._id, { progress: 100, earned: true, earnedAt: new Date() });

      const response = await auth.get('/api/achievements/earned');
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(2);
      expect(response.body.some((a: any) => a.achievement.name === 'Public Earned')).toBe(true);
      expect(response.body.some((a: any) => a.achievement.name === 'Secret Earned')).toBe(true);
      expect(response.body.every((a: any) => a.earned === true)).toBe(true);
    });

    it('should return full UserAchievement details and sort by earnedAt descending', async () => {
      const ach1 = await createTestAchievement({ name: 'Older Earned', points: 5 });
      const ach2 = await createTestAchievement({ name: 'Newer Earned', points: 10, secret: true });
      const ach3 = await createTestAchievement({ name: 'Middle Earned', points: 15 });

      const dateOlder = new Date('2023-01-01T10:00:00.000Z');
      const dateMiddle = new Date('2023-01-01T12:00:00.000Z');
      const dateNewer = new Date('2023-01-01T14:00:00.000Z');

      await createTestUserAchievement(testUser._id, ach1._id, { progress: 100, earned: true, earnedAt: dateOlder, seenByUser: true });
      await createTestUserAchievement(testUser._id, ach3._id, { progress: 100, earned: true, earnedAt: dateMiddle, seenByUser: false });
      await createTestUserAchievement(testUser._id, ach2._id, { progress: 100, earned: true, earnedAt: dateNewer, seenByUser: false });
      
      const achUnearned = await createTestAchievement({ name: 'Never Earned' });
      await createTestUserAchievement(testUser._id, achUnearned._id, { progress: 10, earned: false });

      const response = await auth.get('/api/achievements/earned');
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(3);

      expect(response.body[0].achievement.name).toBe('Newer Earned');
      expect(new Date(response.body[0].earnedAt).toISOString()).toBe(dateNewer.toISOString());
      expect(response.body[1].achievement.name).toBe('Middle Earned');
      expect(new Date(response.body[1].earnedAt).toISOString()).toBe(dateMiddle.toISOString());
      expect(response.body[2].achievement.name).toBe('Older Earned');
      expect(new Date(response.body[2].earnedAt).toISOString()).toBe(dateOlder.toISOString());

      const newestEarned = response.body[0];
      expect(newestEarned.user).toBe(testUser._id.toString());
      expect(newestEarned.achievement._id).toBe(ach2._id.toString());
      expect(newestEarned.achievement.name).toBe(ach2.name);
      expect(newestEarned.achievement.points).toBe(ach2.points);
      expect(newestEarned.achievement.secret).toBe(true);
      expect(newestEarned.progress).toBe(100);
      expect(newestEarned.earned).toBe(true);
      expect(newestEarned.seenByUser).toBe(false);
    });
  });
  
  // POST /api/achievements/my/:userAchievementId/seen - Mark achievement as seen (protected)
  describe('POST /api/achievements/my/:userAchievementId/seen', () => {
    it('should return 401 if not authenticated', async () => {
      const response = await request.post('/api/achievements/my/someUserAchievementId/seen');
      expect(response.status).toBe(401);
    });

    it('should return 404 if UserAchievement does not exist', async () => {
      const nonExistentId = new mongoose.Types.ObjectId();
      const response = await auth.post(`/api/achievements/my/${nonExistentId}/seen`);
      expect(response.status).toBe(404);
    });

    it('should return 403 if UserAchievement belongs to another user', async () => {
      const otherUser = await createTestUser({ email: 'other@example.com', name: 'Other User' });
      const achievement = await createTestAchievement({ name: 'Shared Ach' });
      const uaOtherUser = await createTestUserAchievement(otherUser._id, achievement._id, { earned: true, earnedAt: new Date() });

      const response = await auth.post(`/api/achievements/my/${uaOtherUser._id}/seen`);
      expect(response.status).toBe(403);
    });

    it('should return 400 if UserAchievement is not earned yet', async () => {
      const achievement = await createTestAchievement({ name: 'Not Earned Ach' });
      const uaNotEarned = await createTestUserAchievement(testUser._id, achievement._id, { earned: false, progress: 50 });

      const response = await auth.post(`/api/achievements/my/${uaNotEarned._id}/seen`);
      expect(response.status).toBe(400);
      expect(response.body.message).toContain('Achievement not earned yet');
    });

    it('should mark an earned, unseen UserAchievement as seen and return it', async () => {
      const achievement = await createTestAchievement({ name: 'To Be Seen Ach' });
      const uaUnseen = await createTestUserAchievement(testUser._id, achievement._id, { earned: true, earnedAt: new Date(), seenByUser: false });

      const response = await auth.post(`/api/achievements/my/${uaUnseen._id}/seen`);
      expect(response.status).toBe(200);
      expect(response.body.seenByUser).toBe(true);
      expect(response.body._id).toBe(uaUnseen._id.toString());
      expect(response.body.user).toBe(testUser._id.toString());

      // Verify in DB
      const uaInDb = await UserAchievement.findById(uaUnseen._id);
      expect(uaInDb?.seenByUser).toBe(true);
    });

    it('should not change seenByUser if already true (idempotency)', async () => {
      const achievement = await createTestAchievement({ name: 'Already Seen Ach' });
      const uaSeen = await createTestUserAchievement(testUser._id, achievement._id, { earned: true, earnedAt: new Date(), seenByUser: true });

      const response = await auth.post(`/api/achievements/my/${uaSeen._id}/seen`);
      expect(response.status).toBe(200);
      expect(response.body.seenByUser).toBe(true);

      // Verify in DB (still true, and no other changes like timestamps if not intended)
      const uaInDb = await UserAchievement.findById(uaSeen._id);
      expect(uaInDb?.seenByUser).toBe(true);
    });

    it('should return 400 for invalid userAchievementId format', async () => {
        const response = await auth.post('/api/achievements/my/invalidObjectId/seen');
        expect(response.status).toBe(400); // Assuming controller or middleware handles invalid ObjectId
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
      await createTestUserAchievement(auth.user._id, achievement._id, { progress: 50 });
      
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
      await createTestUserAchievement(auth.user._id, achievement._id, { progress: 50 });
      
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