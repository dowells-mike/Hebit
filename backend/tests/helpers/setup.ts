import dotenv from 'dotenv';
import mongoose from 'mongoose';

// Import Jest lifecycle methods directly
const jestGlobals = require('@jest/globals');
const { beforeAll, afterAll, beforeEach } = jestGlobals;

// Load environment variables
dotenv.config({ path: '.env.test' });

// Mock environment variables if not set
process.env.JWT_SECRET = process.env.JWT_SECRET || 'test_jwt_secret';
process.env.JWT_EXPIRATION = process.env.JWT_EXPIRATION || '1h';
process.env.MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/hebit_test';
process.env.NODE_ENV = 'test';

// Connect to MongoDB before tests
beforeAll(async () => {
  // Connect to MongoDB with unique name for test database
  await mongoose.connect(process.env.MONGODB_URI as string);
  console.log('Connected to MongoDB for testing');
}, 30000); // 30 second timeout

// Disconnect from MongoDB after tests
afterAll(async () => {
  // Close MongoDB connection
  await mongoose.connection.close();
  console.log('MongoDB connection closed');
}, 30000); // 30 second timeout

// Clean up database before each test
beforeEach(async () => {
  // Clean up collections before each test
  const collections = mongoose.connection.collections;
  for (const key in collections) {
    const collection = collections[key];
    await collection.deleteMany({});
  }
}); 