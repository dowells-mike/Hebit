import dotenv from 'dotenv';
import path from 'path';

// Load environment variables from .env file
dotenv.config();

// Check if MONGODB_URI is set
const mongoUri = process.env.MONGODB_URI;
if (!mongoUri) {
  console.error("FATAL ERROR: MONGODB_URI environment variable is not set.");
  process.exit(1); // Exit if the URI is not provided
}

const config = {
  env: process.env.NODE_ENV || 'development',
  port: process.env.PORT || 5000,
  // Use MONGODB_URI from environment ONLY.
  mongoUri: mongoUri,
  jwtSecret: process.env.JWT_SECRET || 'your_jwt_secret_here', // Keep a local default for JWT
  jwtExpiration: process.env.JWT_EXPIRATION || '7d',
  jwtRefreshExpiration: process.env.JWT_REFRESH_EXPIRATION || '30d',
  corsOrigins: process.env.CORS_ORIGINS ? process.env.CORS_ORIGINS.split(',') : ['http://localhost:3000'],
  fileUpload: {
    maxSize: parseInt(process.env.MAX_FILE_SIZE || '10485760', 10), // 10MB
    uploadDir: process.env.FILE_UPLOAD_DIR || 'uploads/'
  },
  mailService: {
    host: process.env.MAIL_HOST || 'smtp.mailtrap.io',
    port: parseInt(process.env.MAIL_PORT || '2525', 10),
    user: process.env.MAIL_USER || '',
    pass: process.env.MAIL_PASS || '',
    from: process.env.MAIL_FROM || 'noreply@hebit.app'
  }
};

export default config;