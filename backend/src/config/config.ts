import dotenv from 'dotenv';
import path from 'path';

// Load environment variables from .env file
dotenv.config();

// Construct the Atlas connection string
const ATLAS_URI = "mongodb+srv://mike:Clashroyale1@cluster0.mhauups.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

const config = {
  env: process.env.NODE_ENV || 'development',
  port: process.env.PORT || 5000,
  // Use MONGODB_URI from environment if set, otherwise default to the Atlas URI
  mongoUri: process.env.MONGODB_URI || ATLAS_URI,
  jwtSecret: process.env.JWT_SECRET || 'your_jwt_secret_here', // Keep a local default for JWT, but MONGO_URI now defaults to Atlas
  jwtExpiration: process.env.JWT_EXPIRATION || '7d',
  jwtRefreshExpiration: process.env.JWT_REFRESH_EXPIRATION || '30d',
  corsOrigins: process.env.CORS_ORIGINS ? process.env.CORS_ORIGINS.split(',') : ['http://localhost:3000'], // Keep local default for CORS during dev
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