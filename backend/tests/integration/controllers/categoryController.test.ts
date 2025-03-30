import { describe, it, expect, beforeEach } from '@jest/globals';
import mongoose from 'mongoose';
import { Category } from '../../../src/models';
import { authenticatedRequest } from '../../helpers/testServer';
import { createTestCategory } from '../../helpers/testData';

describe('Category Controller Integration Tests', () => {
  let auth: any;
  
  beforeEach(async () => {
    // Create authenticated request for each test
    auth = await authenticatedRequest();
  });
  
  describe('GET /api/categories', () => {
    it('should return empty array when no categories exist', async () => {
      const response = await auth.get('/api/categories');
      
      expect(response.status).toBe(200);
      expect(response.body).toEqual([]);
    });
    
    it('should return all categories for a user', async () => {
      const userId = auth.user._id;
      
      // Create multiple categories
      await createTestCategory(userId, { name: 'Work', type: 'task', color: '#FF5733' });
      await createTestCategory(userId, { name: 'Personal', type: 'habit', color: '#33FF57' });
      await createTestCategory(userId, { name: 'Health', type: 'goal', color: '#5733FF' });
      
      const response = await auth.get('/api/categories');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(3);
      expect(response.body[0]).toHaveProperty('name');
      expect(response.body[0]).toHaveProperty('type');
      expect(response.body[0]).toHaveProperty('color');
    });
    
    it('should filter categories by type', async () => {
      const userId = auth.user._id;
      
      // Create categories with different types
      await createTestCategory(userId, { name: 'Work', type: 'task', color: '#FF5733' });
      await createTestCategory(userId, { name: 'Personal', type: 'habit', color: '#33FF57' });
      await createTestCategory(userId, { name: 'Health', type: 'task', color: '#5733FF' });
      
      const response = await auth.get('/api/categories?type=task');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(2);
      expect(response.body[0].type).toBe('task');
      expect(response.body[1].type).toBe('task');
    });
    
    it('should filter categories by isDefault', async () => {
      const userId = auth.user._id;
      
      // Create default and custom categories
      await createTestCategory(userId, { name: 'Default Category', isDefault: true });
      await createTestCategory(userId, { name: 'Custom Category', isDefault: false });
      
      const response = await auth.get('/api/categories?isDefault=true');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(1);
      expect(response.body[0].isDefault).toBe(true);
      expect(response.body[0].name).toBe('Default Category');
    });
  });
  
  describe('GET /api/categories/:id', () => {
    it('should return a category by ID', async () => {
      const userId = auth.user._id;
      
      // Create a category
      const category = await createTestCategory(userId, { 
        name: 'Test Category', 
        type: 'task',
        color: '#FF5733'
      });
      
      const response = await auth.get(`/api/categories/${category._id}`);
      
      expect(response.status).toBe(200);
      expect(response.body._id).toBe(category._id.toString());
      expect(response.body.name).toBe('Test Category');
      expect(response.body.type).toBe('task');
      expect(response.body.color).toBe('#FF5733');
    });
    
    it('should return 404 for non-existent category', async () => {
      const fakeId = new mongoose.Types.ObjectId();
      const response = await auth.get(`/api/categories/${fakeId}`);
      
      expect(response.status).toBe(404);
    });
  });
  
  describe('POST /api/categories', () => {
    it('should create a new category', async () => {
      const categoryData = {
        name: 'New Category',
        type: 'task',
        color: '#3355FF',
        icon: 'icon-work'
      };
      
      const response = await auth.post('/api/categories', categoryData);
      
      expect(response.status).toBe(201);
      expect(response.body.name).toBe(categoryData.name);
      expect(response.body.type).toBe(categoryData.type);
      expect(response.body.color).toBe(categoryData.color);
      expect(response.body.icon).toBe(categoryData.icon);
      expect(response.body.user).toBe(auth.user._id.toString());
      expect(response.body).toHaveProperty('order');
      
      // Verify category was created in database
      const category = await Category.findById(response.body._id);
      expect(category).not.toBeNull();
      expect(category?.name).toBe(categoryData.name);
    });
    
    it('should assign incrementing order values', async () => {
      const userId = auth.user._id;
      
      // Create first category
      const response1 = await auth.post('/api/categories', {
        name: 'First Category',
        type: 'task'
      });
      
      // Create second category
      const response2 = await auth.post('/api/categories', {
        name: 'Second Category',
        type: 'task'
      });
      
      expect(response1.body.order).toBe(0);
      expect(response2.body.order).toBe(1);
    });
    
    it('should reject category creation without name', async () => {
      const response = await auth.post('/api/categories', {
        type: 'task',
        color: '#FF5733'
      });
      
      expect(response.status).toBe(400);
    });
    
    it('should reject category creation without type', async () => {
      const response = await auth.post('/api/categories', {
        name: 'No Type Category',
        color: '#FF5733'
      });
      
      expect(response.status).toBe(400);
    });
  });
  
  describe('PUT /api/categories/:id', () => {
    it('should update a category', async () => {
      const userId = auth.user._id;
      
      // Create a category
      const category = await createTestCategory(userId, { 
        name: 'Old Name',
        type: 'task',
        color: '#FF5733'
      });
      
      // Update the category
      const updateData = {
        name: 'New Name',
        color: '#3355FF',
        icon: 'icon-updated'
      };
      
      const response = await auth.put(`/api/categories/${category._id}`, updateData);
      
      expect(response.status).toBe(200);
      expect(response.body.name).toBe(updateData.name);
      expect(response.body.color).toBe(updateData.color);
      expect(response.body.icon).toBe(updateData.icon);
      expect(response.body.type).toBe('task'); // Unchanged
      
      // Verify update in database
      const updatedCategory = await Category.findById(category._id);
      expect(updatedCategory?.name).toBe(updateData.name);
      expect(updatedCategory?.color).toBe(updateData.color);
    });
    
    it('should return 404 for updating non-existent category', async () => {
      const fakeId = new mongoose.Types.ObjectId();
      const response = await auth.put(`/api/categories/${fakeId}`, { name: 'Updated Name' });
      
      expect(response.status).toBe(404);
    });
  });
  
  describe('DELETE /api/categories/:id', () => {
    it('should delete a category', async () => {
      const userId = auth.user._id;
      
      // Create a category
      const category = await createTestCategory(userId, { 
        name: 'To Delete',
        type: 'task',
        isDefault: false
      });
      
      // Delete the category
      const response = await auth.delete(`/api/categories/${category._id}`);
      
      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
      
      // Verify category was deleted from database
      const deletedCategory = await Category.findById(category._id);
      expect(deletedCategory).toBeNull();
    });
    
    it('should not delete default categories', async () => {
      const userId = auth.user._id;
      
      // Create a default category
      const category = await createTestCategory(userId, { 
        name: 'Default Category',
        type: 'task',
        isDefault: true
      });
      
      // Try to delete the default category
      const response = await auth.delete(`/api/categories/${category._id}`);
      
      expect(response.status).toBe(400);
      
      // Verify category was not deleted
      const stillExistsCategory = await Category.findById(category._id);
      expect(stillExistsCategory).not.toBeNull();
    });
    
    it('should return 404 for deleting non-existent category', async () => {
      const fakeId = new mongoose.Types.ObjectId();
      const response = await auth.delete(`/api/categories/${fakeId}`);
      
      expect(response.status).toBe(404);
    });
  });
  
  describe('PATCH /api/categories/reorder', () => {
    it('should reorder categories', async () => {
      const userId = auth.user._id;
      
      // Create multiple categories
      const category1 = await createTestCategory(userId, { name: 'Category 1', order: 0 });
      const category2 = await createTestCategory(userId, { name: 'Category 2', order: 1 });
      const category3 = await createTestCategory(userId, { name: 'Category 3', order: 2 });
      
      // Reorder the categories
      const reorderData = {
        categories: [
          { id: category1._id.toString(), order: 2 },
          { id: category2._id.toString(), order: 0 },
          { id: category3._id.toString(), order: 1 }
        ]
      };
      
      const response = await auth.patch('/api/categories/reorder', reorderData);
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(3);
      
      // Categories should be returned in order
      expect(response.body[0].name).toBe('Category 2');
      expect(response.body[0].order).toBe(0);
      
      expect(response.body[1].name).toBe('Category 3');
      expect(response.body[1].order).toBe(1);
      
      expect(response.body[2].name).toBe('Category 1');
      expect(response.body[2].order).toBe(2);
      
      // Verify order changes in database
      const updatedCategory1 = await Category.findById(category1._id);
      const updatedCategory2 = await Category.findById(category2._id);
      const updatedCategory3 = await Category.findById(category3._id);
      
      expect(updatedCategory1?.order).toBe(2);
      expect(updatedCategory2?.order).toBe(0);
      expect(updatedCategory3?.order).toBe(1);
    });
    
    it('should reject reorder without categories array', async () => {
      const response = await auth.patch('/api/categories/reorder', {});
      
      expect(response.status).toBe(400);
    });
  });
}); 