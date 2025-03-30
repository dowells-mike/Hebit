import { describe, it, expect, beforeEach } from '@jest/globals';
import mongoose from 'mongoose';
import { Goal, ProductivityMetrics } from '../../../src/models';
import { authenticatedRequest } from '../../helpers/testServer';
import { createTestGoal } from '../../helpers/testData';

describe('Goal Controller Integration Tests', () => {
  let auth: any;
  
  beforeEach(async () => {
    // Create authenticated request for each test
    auth = await authenticatedRequest();
  });
  
  describe('GET /api/goals', () => {
    it('should return empty array when no goals exist', async () => {
      const response = await auth.get('/api/goals');
      
      expect(response.status).toBe(200);
      expect(response.body).toEqual([]);
    });
    
    it('should return all goals for a user', async () => {
      const userId = auth.user._id;
      
      // Create 3 goals
      await createTestGoal(userId, { title: 'Complete project', timePeriod: 'short_term' });
      await createTestGoal(userId, { title: 'Learn new language', timePeriod: 'medium_term' });
      await createTestGoal(userId, { title: 'Career change', timePeriod: 'long_term' });
      
      const response = await auth.get('/api/goals');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(3);
      expect(response.body[0].title).toBeDefined();
      expect(response.body[0].timePeriod).toBeDefined();
    });
    
    it('should filter goals by status', async () => {
      const userId = auth.user._id;
      
      // Create goals with different statuses
      await createTestGoal(userId, { title: 'Goal 1', status: 'not_started' });
      await createTestGoal(userId, { title: 'Goal 2', status: 'in_progress' });
      await createTestGoal(userId, { title: 'Goal 3', status: 'completed' });
      
      const response = await auth.get('/api/goals?status=in_progress');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(1);
      expect(response.body[0].status).toBe('in_progress');
    });
    
    it('should filter goals by category', async () => {
      const userId = auth.user._id;
      
      // Create goals with different categories
      await createTestGoal(userId, { title: 'Work goal', category: 'work' });
      await createTestGoal(userId, { title: 'Personal goal', category: 'personal' });
      await createTestGoal(userId, { title: 'Health goal', category: 'health' });
      
      const response = await auth.get('/api/goals?category=work');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(1);
      expect(response.body[0].category).toBe('work');
    });
    
    it('should filter goals by time period', async () => {
      const userId = auth.user._id;
      
      // Create goals with different time periods
      await createTestGoal(userId, { title: 'Short term goal', timePeriod: 'short_term' });
      await createTestGoal(userId, { title: 'Medium term goal', timePeriod: 'medium_term' });
      await createTestGoal(userId, { title: 'Long term goal', timePeriod: 'long_term' });
      
      const response = await auth.get('/api/goals?timePeriod=short_term');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(1);
      expect(response.body[0].timePeriod).toBe('short_term');
    });
    
    it('should not show archived goals by default', async () => {
      const userId = auth.user._id;
      
      // Create active and archived goals
      await createTestGoal(userId, { title: 'Active Goal', status: 'in_progress' });
      await createTestGoal(userId, { title: 'Archived Goal', status: 'archived' });
      
      const response = await auth.get('/api/goals');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(1);
      expect(response.body[0].title).toBe('Active Goal');
    });
    
    it('should show archived goals when requested', async () => {
      const userId = auth.user._id;
      
      // Create active and archived goals
      await createTestGoal(userId, { title: 'Active Goal', status: 'in_progress' });
      await createTestGoal(userId, { title: 'Archived Goal', status: 'archived' });
      
      const response = await auth.get('/api/goals?showArchived=true');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(2);
    });
  });
  
  describe('GET /api/goals/:id', () => {
    it('should return a goal by ID', async () => {
      const userId = auth.user._id;
      
      // Create a goal
      const goal = await createTestGoal(userId, { 
        title: 'Learn TypeScript', 
        description: 'Master TypeScript in 3 months',
        priority: 2
      });
      
      const response = await auth.get(`/api/goals/${goal._id}`);
      
      expect(response.status).toBe(200);
      expect(response.body._id).toBe(goal._id.toString());
      expect(response.body.title).toBe('Learn TypeScript');
      expect(response.body.description).toBe('Master TypeScript in 3 months');
      expect(response.body.priority).toBe(2);
    });
    
    it('should return 404 for non-existent goal', async () => {
      const fakeId = new mongoose.Types.ObjectId();
      const response = await auth.get(`/api/goals/${fakeId}`);
      
      expect(response.status).toBe(404);
    });
  });
  
  describe('POST /api/goals', () => {
    it('should create a new goal', async () => {
      const goalData = {
        title: 'Learn TypeScript',
        description: 'Master TypeScript in 3 months',
        category: 'education',
        priority: 2,
        difficulty: 4,
        timePeriod: 'medium_term',
        targetDate: new Date('2023-12-31').toISOString()
      };
      
      const response = await auth.post('/api/goals', goalData);
      
      expect(response.status).toBe(201);
      expect(response.body.title).toBe(goalData.title);
      expect(response.body.description).toBe(goalData.description);
      expect(response.body.category).toBe(goalData.category);
      expect(response.body.priority).toBe(goalData.priority);
      expect(response.body.difficulty).toBe(goalData.difficulty);
      expect(response.body.progress).toBe(0);
      expect(response.body.status).toBe('not_started');
      expect(response.body.user).toBe(auth.user._id.toString());
      
      // Verify goal was created in database
      const goal = await Goal.findById(response.body._id);
      expect(goal).not.toBeNull();
      expect(goal?.title).toBe(goalData.title);
      
      // Check that productivity metrics were updated
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const metrics = await ProductivityMetrics.findOne({
        user: auth.user._id,
        date: today
      });
      
      expect(metrics).not.toBeNull();
      expect((metrics as any).goalsCreated).toBe(1);
    });
    
    it('should reject goal creation without title', async () => {
      const invalidGoal = {
        description: 'Missing title'
      };
      
      const response = await auth.post('/api/goals', invalidGoal);
      
      expect(response.status).toBe(400);
    });
    
    it('should use default values when not provided', async () => {
      const minimalGoal = {
        title: 'Minimal Goal'
      };
      
      const response = await auth.post('/api/goals', minimalGoal);
      
      expect(response.status).toBe(201);
      expect(response.body.status).toBe('not_started'); // Default value
      expect(response.body.priority).toBe(3); // Default value
      expect(response.body.difficulty).toBe(3); // Default value
    });
  });
  
  describe('PUT /api/goals/:id', () => {
    it('should update a goal', async () => {
      const userId = auth.user._id;
      
      // Create a goal
      const goal = await createTestGoal(userId, { 
        title: 'Learn TypeScript', 
        description: 'Master TypeScript in 3 months',
        priority: 2
      });
      
      // Update the goal
      const updates = {
        title: 'Learn TypeScript & React',
        description: 'Master TypeScript and React in 6 months',
        priority: 1
      };
      
      const response = await auth.put(`/api/goals/${goal._id}`, updates);
      
      expect(response.status).toBe(200);
      expect(response.body.title).toBe(updates.title);
      expect(response.body.description).toBe(updates.description);
      expect(response.body.priority).toBe(updates.priority);
      
      // Verify update in database
      const updatedGoal = await Goal.findById(goal._id);
      expect(updatedGoal?.title).toBe(updates.title);
      expect(updatedGoal?.description).toBe(updates.description);
    });
    
    it('should return 404 for updating non-existent goal', async () => {
      const fakeId = new mongoose.Types.ObjectId();
      const response = await auth.put(`/api/goals/${fakeId}`, { title: 'Updated Title' });
      
      expect(response.status).toBe(404);
    });
    
    it('should track metadata of modified fields', async () => {
      const userId = auth.user._id;
      
      // Create a goal
      const goal = await createTestGoal(userId, { title: 'Original Goal' });
      
      // Update the title
      const response = await auth.put(`/api/goals/${goal._id}`, { title: 'Updated Goal' });
      
      expect(response.status).toBe(200);
      expect(response.body.metadata).toBeDefined();
      expect(response.body.metadata.lastModifiedField).toBe('title');
    });
  });
  
  describe('DELETE /api/goals/:id', () => {
    it('should soft-delete (archive) a goal by default', async () => {
      const userId = auth.user._id;
      
      // Create a goal
      const goal = await createTestGoal(userId, { title: 'Test Goal' });
      
      // Delete the goal (soft delete by default)
      const response = await auth.delete(`/api/goals/${goal._id}`);
      
      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
      
      // Verify goal was archived, not deleted
      const archivedGoal = await Goal.findById(goal._id);
      expect(archivedGoal).not.toBeNull();
      expect((archivedGoal as any).status).toBe('archived');
    });
    
    it('should permanently delete a goal when specified', async () => {
      const userId = auth.user._id;
      
      // Create a goal
      const goal = await createTestGoal(userId, { title: 'Test Goal' });
      
      // Delete the goal permanently
      const response = await auth.delete(`/api/goals/${goal._id}?permanent=true`);
      
      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
      
      // Verify goal was deleted
      const deletedGoal = await Goal.findById(goal._id);
      expect(deletedGoal).toBeNull();
    });
    
    it('should return 404 for deleting non-existent goal', async () => {
      const fakeId = new mongoose.Types.ObjectId();
      const response = await auth.delete(`/api/goals/${fakeId}`);
      
      expect(response.status).toBe(404);
    });
  });
  
  describe('PATCH /api/goals/:id/progress', () => {
    it('should update goal progress', async () => {
      const userId = auth.user._id;
      
      // Create a goal
      const goal = await createTestGoal(userId, { title: 'Test Goal', progress: 0 });
      
      // Update progress to 50%
      const updateData = {
        progress: 50,
        note: 'Making good progress'
      };
      
      const response = await auth.patch(`/api/goals/${goal._id}/progress`, updateData);
      
      expect(response.status).toBe(200);
      expect(response.body.progress).toBe(50);
      expect(response.body.status).toBe('in_progress');
      expect(response.body.checkIns).toHaveLength(1);
      expect(response.body.checkIns[0].notes).toBe('Making good progress');
      expect(response.body.checkIns[0].progressUpdate).toBe(50);
      
      // Verify update in database
      const updatedGoal = await Goal.findById(goal._id);
      expect(updatedGoal?.progress).toBe(50);
      expect(updatedGoal?.status).toBe('in_progress');
    });
    
    it('should mark goal as completed when progress reaches 100%', async () => {
      const userId = auth.user._id;
      
      // Create a goal
      const goal = await createTestGoal(userId, { title: 'Test Goal', progress: 75 });
      
      // Update progress to 100%
      const updateData = {
        progress: 100,
        note: 'Completed the goal'
      };
      
      const response = await auth.patch(`/api/goals/${goal._id}/progress`, updateData);
      
      expect(response.status).toBe(200);
      expect(response.body.progress).toBe(100);
      expect(response.body.status).toBe('completed');
      
      // Verify update in database
      const updatedGoal = await Goal.findById(goal._id);
      expect(updatedGoal?.progress).toBe(100);
      expect(updatedGoal?.status).toBe('completed');
      
      // Check that productivity metrics were updated
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const metrics = await ProductivityMetrics.findOne({
        user: auth.user._id,
        date: today
      });
      
      expect(metrics).not.toBeNull();
      expect((metrics as any).goalsCompleted).toBe(1);
    });
    
    it('should reject invalid progress values', async () => {
      const userId = auth.user._id;
      
      // Create a goal
      const goal = await createTestGoal(userId, { title: 'Test Goal' });
      
      // Try to update with invalid progress (negative)
      const response1 = await auth.patch(`/api/goals/${goal._id}/progress`, { progress: -10 });
      
      expect(response1.status).toBe(400);
      
      // Try to update with invalid progress (over 100)
      const response2 = await auth.patch(`/api/goals/${goal._id}/progress`, { progress: 110 });
      
      expect(response2.status).toBe(400);
    });
  });
  
  describe('POST /api/goals/:id/milestones', () => {
    it('should add a milestone to a goal', async () => {
      const userId = auth.user._id;
      
      // Create a goal
      const goal = await createTestGoal(userId, { title: 'Test Goal' });
      
      // Add milestone
      const milestoneData = {
        title: 'First milestone',
        description: 'Complete initial research',
        dueDate: new Date('2023-12-01').toISOString()
      };
      
      const response = await auth.post(`/api/goals/${goal._id}/milestones`, milestoneData);
      
      expect(response.status).toBe(201);
      expect(response.body.milestones).toHaveLength(1);
      expect(response.body.milestones[0].title).toBe(milestoneData.title);
      expect(response.body.milestones[0].description).toBe(milestoneData.description);
      expect(response.body.milestones[0].completed).toBe(false);
      
      // Verify in database
      const updatedGoal = await Goal.findById(goal._id);
      expect(updatedGoal?.milestones).toHaveLength(1);
    });
    
    it('should reject milestone without title', async () => {
      const userId = auth.user._id;
      
      // Create a goal
      const goal = await createTestGoal(userId, { title: 'Test Goal' });
      
      // Try to add milestone without title
      const response = await auth.post(`/api/goals/${goal._id}/milestones`, { 
        description: 'Missing title' 
      });
      
      expect(response.status).toBe(400);
    });
  });
  
  describe('PUT /api/goals/:id/milestones/:milestoneIndex', () => {
    it('should update a milestone', async () => {
      const userId = auth.user._id;
      
      // Create a goal with a milestone
      const goal = await createTestGoal(userId, { 
        title: 'Test Goal',
        milestones: [{
          id: '1234567890',
          title: 'Initial milestone',
          description: 'Original description',
          completed: false
        }]
      });
      
      // Update the milestone
      const updateData = {
        title: 'Updated milestone',
        description: 'New description',
        completed: true
      };
      
      const response = await auth.put(`/api/goals/${goal._id}/milestones/0`, updateData);
      
      expect(response.status).toBe(200);
      expect(response.body.milestones[0].title).toBe(updateData.title);
      expect(response.body.milestones[0].description).toBe(updateData.description);
      expect(response.body.milestones[0].completed).toBe(true);
      expect(response.body.milestones[0].completedAt).toBeDefined();
      
      // Verify goal progress updated based on milestone completion
      expect(response.body.progress).toBe(100); // 1/1 milestones completed = 100%
      expect(response.body.status).toBe('completed');
    });
    
    it('should return 404 for non-existent milestone', async () => {
      const userId = auth.user._id;
      
      // Create a goal with no milestones
      const goal = await createTestGoal(userId, { title: 'Test Goal' });
      
      // Try to update non-existent milestone
      const response = await auth.put(`/api/goals/${goal._id}/milestones/0`, { 
        title: 'Updated milestone' 
      });
      
      expect(response.status).toBe(404);
    });
  });
  
  describe('GET /api/goals/stats', () => {
    it('should return goal statistics', async () => {
      const userId = auth.user._id;
      
      // Create goals with different statuses, priorities, and time periods
      await createTestGoal(userId, { 
        title: 'Goal 1', 
        status: 'not_started',
        priority: 1,
        timePeriod: 'short_term'
      });
      
      await createTestGoal(userId, { 
        title: 'Goal 2', 
        status: 'in_progress',
        priority: 2,
        timePeriod: 'medium_term'
      });
      
      await createTestGoal(userId, { 
        title: 'Goal 3', 
        status: 'completed',
        priority: 3,
        timePeriod: 'long_term'
      });
      
      const response = await auth.get('/api/goals/stats');
      
      expect(response.status).toBe(200);
      expect(response.body.totalGoals).toBe(3);
      expect(response.body.completedGoals).toBe(1);
      expect(response.body.completionRate).toBe(33.33333333333333); // 1/3 = 33.33%
      
      // Check status counts
      expect(response.body.statusCounts).toBeDefined();
      expect(response.body.statusCounts.not_started).toBe(1);
      expect(response.body.statusCounts.in_progress).toBe(1);
      expect(response.body.statusCounts.completed).toBe(1);
      
      // Check time period counts
      expect(response.body.timePeriodCounts).toBeDefined();
      expect(response.body.timePeriodCounts.short_term).toBe(1);
      expect(response.body.timePeriodCounts.medium_term).toBe(1);
      expect(response.body.timePeriodCounts.long_term).toBe(1);
      
      // Check priority counts
      expect(response.body.priorityCounts).toBeDefined();
      expect(response.body.priorityCounts['1']).toBe(1);
      expect(response.body.priorityCounts['2']).toBe(1);
      expect(response.body.priorityCounts['3']).toBe(1);
    });
  });
}); 