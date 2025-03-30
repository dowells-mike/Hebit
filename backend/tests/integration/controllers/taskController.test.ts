import { describe, it, expect, beforeEach } from '@jest/globals';
import mongoose from 'mongoose';
import { Task, ProductivityMetrics } from '../../../src/models';
import { authenticatedRequest } from '../../helpers/testServer';
import { createTestTask } from '../../helpers/testData';

describe('Task Controller Integration Tests', () => {
  let auth: any;
  
  beforeEach(async () => {
    // Create authenticated request for each test
    auth = await authenticatedRequest();
  });
  
  describe('GET /api/tasks', () => {
    it('should return empty array when no tasks exist', async () => {
      const response = await auth.get('/api/tasks');
      
      expect(response.status).toBe(200);
      expect(response.body).toEqual([]);
    });
    
    it('should return all tasks for the authenticated user', async () => {
      // Create some tasks for the user
      await createTestTask(auth.user._id, { title: 'Task 1' });
      await createTestTask(auth.user._id, { title: 'Task 2' });
      await createTestTask(auth.user._id, { title: 'Task 3' });
      
      const response = await auth.get('/api/tasks');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(3);
      expect(response.body[0]).toHaveProperty('title');
      expect(response.body[0]).toHaveProperty('status');
      expect(response.body[0]).toHaveProperty('user', auth.user._id.toString());
    });
    
    it('should not return tasks for other users', async () => {
      // Create another authenticated request (different user)
      const auth2 = await authenticatedRequest();
      
      // Create tasks for first user
      await createTestTask(auth.user._id, { title: 'Task for user 1' });
      
      // Create tasks for second user
      await createTestTask(auth2.user._id, { title: 'Task for user 2' });
      
      // Get tasks for first user
      const response = await auth.get('/api/tasks');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(1);
      expect(response.body[0].title).toBe('Task for user 1');
    });
    
    it('should filter tasks by status', async () => {
      // Create tasks with different statuses
      await createTestTask(auth.user._id, { title: 'Task 1', status: 'todo' });
      await createTestTask(auth.user._id, { title: 'Task 2', status: 'in_progress' });
      await createTestTask(auth.user._id, { title: 'Task 3', status: 'completed' });
      
      const response = await auth.get('/api/tasks?status=completed');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(1);
      expect(response.body[0].title).toBe('Task 3');
      expect(response.body[0].status).toBe('completed');
    });
  });
  
  describe('POST /api/tasks', () => {
    it('should create a new task', async () => {
      const taskData = {
        title: 'New Task',
        description: 'Task Description',
        priority: 'high',
        dueDate: new Date().toISOString()
      };
      
      const response = await auth.post('/api/tasks', taskData);
      
      expect(response.status).toBe(201);
      expect(response.body).toMatchObject({
        title: taskData.title,
        description: taskData.description,
        priority: taskData.priority,
        user: auth.user._id.toString()
      });
      
      // Verify task was created in database
      const taskInDb = await Task.findById(response.body._id);
      expect(taskInDb).not.toBeNull();
      expect(taskInDb?.title).toBe(taskData.title);
      
      // Check that productivity metrics were updated
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const metrics = await ProductivityMetrics.findOne({
        user: auth.user._id,
        date: today
      });
      
      expect(metrics).not.toBeNull();
      expect(metrics?.tasksCreated).toBe(1);
    });
    
    it('should return error for invalid task data', async () => {
      const response = await auth.post('/api/tasks', {});
      
      expect(response.status).toBe(400);
      expect(response.body).toHaveProperty('message');
    });
  });
  
  describe('PUT /api/tasks/:id', () => {
    it('should update task', async () => {
      // Create a task
      const task = await createTestTask(auth.user._id);
      
      // Update the task
      const updateData = {
        title: 'Updated Title',
        description: 'Updated Description',
        priority: 'high'
      };
      
      const response = await auth.put(`/api/tasks/${task._id}`, updateData);
      
      expect(response.status).toBe(200);
      expect(response.body).toMatchObject(updateData);
      
      // Verify update in database
      const updatedTask = await Task.findById(task._id);
      expect(updatedTask?.title).toBe(updateData.title);
    });
    
    it('should mark task as completed and update metrics', async () => {
      // Create a task
      const task = await createTestTask(auth.user._id);
      
      // Update task to completed
      const response = await auth.put(`/api/tasks/${task._id}`, {
        completed: true
      });
      
      expect(response.status).toBe(200);
      expect(response.body.completed).toBe(true);
      expect(response.body.status).toBe('completed');
      expect(response.body).toHaveProperty('completedAt');
      
      // Check that task completion was recorded in productivity metrics
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const metrics = await ProductivityMetrics.findOne({
        user: auth.user._id,
        date: today
      });
      
      expect(metrics).not.toBeNull();
      expect(metrics?.tasksCompleted).toBe(1);
    });
    
    it('should not update task of another user', async () => {
      // Create another authenticated request (different user)
      const auth2 = await authenticatedRequest();
      
      // Create a task for the second user
      const task = await createTestTask(auth2.user._id);
      
      // Try to update the task as first user
      const response = await auth.put(`/api/tasks/${task._id}`, {
        title: 'Updated Title'
      });
      
      expect(response.status).toBe(404);
    });
  });
  
  describe('DELETE /api/tasks/:id', () => {
    it('should soft delete a task by updating status to archived', async () => {
      // Create a task
      const task = await createTestTask(auth.user._id);
      
      // Delete the task
      const response = await auth.delete(`/api/tasks/${task._id}`);
      
      expect(response.status).toBe(200);
      
      // Verify the task is marked as archived but not actually deleted
      const archivedTask = await Task.findById(task._id);
      expect(archivedTask).not.toBeNull();
      expect(archivedTask?.status).toBe('archived');
    });
  });
  
  // Test for ML data collection
  describe('ML Data Collection', () => {
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
  });
}); 