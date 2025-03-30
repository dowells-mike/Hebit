import { describe, it, expect, beforeEach } from '@jest/globals';
import mongoose from 'mongoose';
import { User } from '../../../src/models';
import { request, authenticatedRequest } from '../../helpers/testServer';
import { createTestUser } from '../../helpers/testData';

describe('Auth Controller Integration Tests', () => {
  describe('POST /api/auth/register', () => {
    it('should register a new user', async () => {
      const userData = {
        name: 'Test Register',
        email: 'register.test@example.com',
        password: 'password123'
      };
      
      const response = await request.post('/api/auth/register').send(userData);
      
      expect(response.status).toBe(201);
      expect(response.body).toHaveProperty('token');
      expect(response.body).toHaveProperty('refreshToken');
      expect(response.body).toHaveProperty('user');
      expect(response.body.user).toHaveProperty('name', userData.name);
      expect(response.body.user).toHaveProperty('email', userData.email);
      expect(response.body.user).not.toHaveProperty('password');
      
      // Verify user was created in database
      const user = await User.findOne({ email: userData.email });
      expect(user).not.toBeNull();
      expect(user?.name).toBe(userData.name);
    });
    
    it('should not register user with existing email', async () => {
      // First create a user
      const existingUser = await createTestUser({
        email: 'existing.user@example.com'
      });
      
      // Try to register with the same email
      const userData = {
        name: 'Duplicate Email',
        email: 'existing.user@example.com',
        password: 'password123'
      };
      
      const response = await request.post('/api/auth/register').send(userData);
      
      expect(response.status).toBe(400);
      expect(response.body).toHaveProperty('message');
    });
    
    it('should reject registration with missing fields', async () => {
      // Missing name
      const response1 = await request.post('/api/auth/register').send({
        email: 'test@example.com',
        password: 'password123'
      });
      
      expect(response1.status).toBe(400);
      
      // Missing email
      const response2 = await request.post('/api/auth/register').send({
        name: 'Test User',
        password: 'password123'
      });
      
      expect(response2.status).toBe(400);
      
      // Missing password
      const response3 = await request.post('/api/auth/register').send({
        name: 'Test User',
        email: 'test@example.com'
      });
      
      expect(response3.status).toBe(400);
    });
  });
  
  describe('POST /api/auth/login', () => {
    it('should login with valid credentials', async () => {
      // Create a user
      const user = await createTestUser({
        email: 'login.test@example.com',
        password: 'password123'
      });
      
      // Login with created user
      const loginData = {
        email: 'login.test@example.com',
        password: 'password123'
      };
      
      const response = await request.post('/api/auth/login').send(loginData);
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveProperty('token');
      expect(response.body).toHaveProperty('refreshToken');
      expect(response.body).toHaveProperty('user');
      expect(response.body.user).toHaveProperty('name', user.name);
      expect(response.body.user).toHaveProperty('email', user.email);
      expect(response.body.user).not.toHaveProperty('password');
    });
    
    it('should reject login with wrong password', async () => {
      // Create a user
      await createTestUser({
        email: 'wrong.password@example.com',
        password: 'correctpassword'
      });
      
      // Login with wrong password
      const loginData = {
        email: 'wrong.password@example.com',
        password: 'wrongpassword'
      };
      
      const response = await request.post('/api/auth/login').send(loginData);
      
      expect(response.status).toBe(401);
      expect(response.body).toHaveProperty('message');
    });
    
    it('should reject login with non-existent email', async () => {
      const loginData = {
        email: 'nonexistent@example.com',
        password: 'password123'
      };
      
      const response = await request.post('/api/auth/login').send(loginData);
      
      expect(response.status).toBe(401);
      expect(response.body).toHaveProperty('message');
    });
    
    it('should reject login with missing credentials', async () => {
      // Missing email
      const response1 = await request.post('/api/auth/login').send({
        password: 'password123'
      });
      
      expect(response1.status).toBe(400);
      
      // Missing password
      const response2 = await request.post('/api/auth/login').send({
        email: 'test@example.com'
      });
      
      expect(response2.status).toBe(400);
    });
  });
  
  describe('GET /api/auth/profile', () => {
    let auth: any;
    
    beforeEach(async () => {
      // Create authenticated request for each test
      auth = await authenticatedRequest();
    });
    
    it('should return user profile when authenticated', async () => {
      const response = await auth.get('/api/auth/profile');
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveProperty('id', auth.user._id.toString());
      expect(response.body).toHaveProperty('name', auth.user.name);
      expect(response.body).toHaveProperty('email', auth.user.email);
      expect(response.body).toHaveProperty('settings');
      expect(response.body).toHaveProperty('productivity');
      expect(response.body).not.toHaveProperty('password');
    });
    
    it('should reject access without authentication', async () => {
      // Request without auth token
      const response = await request.get('/api/auth/profile');
      
      expect(response.status).toBe(401);
    });
  });
  
  describe('PUT /api/auth/profile', () => {
    let auth: any;
    
    beforeEach(async () => {
      auth = await authenticatedRequest();
    });
    
    it('should update user profile', async () => {
      const updateData = {
        name: 'New Name',
        email: 'new.email@example.com',
        bio: 'Updated bio',
        timezone: 'America/New_York'
      };
      
      const response = await auth.put('/api/auth/profile', updateData);
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveProperty('name', updateData.name);
      expect(response.body).toHaveProperty('email', updateData.email);
      expect(response.body).toHaveProperty('bio', updateData.bio);
      expect(response.body).toHaveProperty('timezone', updateData.timezone);
      
      // Verify update in database
      const user = await User.findById(auth.user._id);
      expect(user?.name).toBe(updateData.name);
      expect(user?.email).toBe(updateData.email);
    });
    
    it('should reject email update if already used by another user', async () => {
      // First create another user
      const otherUser = await createTestUser({
        email: 'other.user@example.com'
      });
      
      // Try to update current user's email to other user's email
      const updateData = {
        email: 'other.user@example.com'
      };
      
      const response = await auth.put('/api/auth/profile', updateData);
      
      expect(response.status).toBe(400);
      expect(response.body).toHaveProperty('message');
    });
  });
  
  describe('POST /api/auth/refresh-token', () => {
    it('should generate a new access token with valid refresh token', async () => {
      // First register a user to get a refresh token
      const userData = {
        name: 'Refresh Token Test',
        email: 'refresh.token@example.com',
        password: 'password123'
      };
      
      const registerResponse = await request.post('/api/auth/register').send(userData);
      const userId = registerResponse.body.user.id;
      const refreshToken = registerResponse.body.refreshToken;
      
      // Now use the refresh token to get a new access token
      const refreshData = {
        refreshToken,
        userId
      };
      
      const response = await request.post('/api/auth/refresh-token').send(refreshData);
      
      expect(response.status).toBe(200);
      expect(response.body).toHaveProperty('token');
      
      // Verify the new token works by accessing a protected route
      const profileResponse = await request
        .get('/api/auth/profile')
        .set('Authorization', `Bearer ${response.body.token}`);
      
      expect(profileResponse.status).toBe(200);
    });
    
    it('should reject refresh without token', async () => {
      const response = await request.post('/api/auth/refresh-token').send({
        userId: new mongoose.Types.ObjectId().toString()
      });
      
      expect(response.status).toBe(400);
    });
    
    it('should reject refresh without user ID', async () => {
      const response = await request.post('/api/auth/refresh-token').send({
        refreshToken: 'somerefreshtoken'
      });
      
      expect(response.status).toBe(400);
    });
    
    it('should reject refresh with invalid user ID', async () => {
      const response = await request.post('/api/auth/refresh-token').send({
        refreshToken: 'somerefreshtoken',
        userId: new mongoose.Types.ObjectId().toString() // Non-existent user ID
      });
      
      expect(response.status).toBe(404);
    });
  });
}); 