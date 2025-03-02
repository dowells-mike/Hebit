import dotenv from 'dotenv';
import path from 'path';

// Load environment variables from .env file
dotenv.config();

const config = {
  env: process.env.NODE_ENV || 'development',
  port: process.env.PORT || 5000,
  mongoUri: process.env.MONGODB_URI || 'mongodb://localhost:27017/hebit',
  jwtSecret: process.env.JWT_SECRET || 'your_jwt_secret_here',
  jwtExpiration: process.env.JWT_EXPIRATION || '7d',
};

export default config;