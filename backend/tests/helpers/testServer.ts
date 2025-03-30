import supertest from 'supertest';
import app from '../../src/server';
import { createTestUser, generateAuthToken } from './testData';

// Create a supertest instance for the app
export const request = supertest(app);

// Helper to create an authenticated request
export const authenticatedRequest = async () => {
  // Create a test user
  const user = await createTestUser();
  
  // Generate token
  const token = generateAuthToken(user);
  
  // Return authenticated request object and user
  return {
    user,
    token,
    get: (url: string) => request.get(url).set('Authorization', `Bearer ${token}`),
    post: (url: string, data?: any) => request.post(url).set('Authorization', `Bearer ${token}`).send(data),
    put: (url: string, data?: any) => request.put(url).set('Authorization', `Bearer ${token}`).send(data),
    delete: (url: string) => request.delete(url).set('Authorization', `Bearer ${token}`)
  };
}; 