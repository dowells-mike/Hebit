// Jest setup file
const mongoose = require('mongoose');
require('dotenv').config({ path: '.env.test' });

// Set test environment variables
process.env.JWT_SECRET = 'test_jwt_secret';
process.env.JWT_EXPIRATION = '1h';
process.env.MONGODB_URI = 'mongodb://localhost:27017/hebit_test';
process.env.NODE_ENV = 'test';

// Global setup - connect to MongoDB
beforeAll(async () => {
  console.log('Connecting to MongoDB for testing...');
  try {
    await mongoose.connect(process.env.MONGODB_URI);
    console.log('Connected to MongoDB for testing');
  } catch (error) {
    console.error('MongoDB connection error:', error);
    throw error;
  }
}, 30000);

// Global teardown - disconnect from MongoDB
afterAll(async () => {
  console.log('Cleaning up MongoDB connection...');
  try {
    await mongoose.connection.close();
    console.log('MongoDB connection closed');
  } catch (error) {
    console.error('Error closing MongoDB connection:', error);
    throw error;
  }
}, 30000);

// Clean up database before each test
beforeEach(async () => {
  if (mongoose.connection.readyState === 1) {
    const collections = mongoose.connection.collections;
    for (const key in collections) {
      const collection = collections[key];
      await collection.deleteMany({});
    }
  }
}); 