import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import mongoose from 'mongoose';
import { errorHandler } from './middleware/errorHandler';

// Routes
import authRoutes from './routes/auth';
import taskRoutes from './routes/tasks';
import habitRoutes from './routes/habits';
import goalRoutes from './routes/goals';

dotenv.config();

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/tasks', taskRoutes);
app.use('/api/habits', habitRoutes);
app.use('/api/goals', goalRoutes);

// Health check route
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'ok', message: 'Hebit API is running' });
});

// Default route
app.get('/', (req, res) => {
  res.status(200).json({ 
    message: 'Welcome to Hebit API',
    version: '1.0.0',
    documentation: '/api/docs' // For future API documentation
  });
});

// Error handling middleware
app.use(errorHandler);

// Set development mode for our project
process.env.NODE_ENV = process.env.NODE_ENV || 'development';

// Connect to MongoDB
import connectDB from './config/database';

// Connect to MongoDB database
connectDB().then(() => {
  console.log('Connected to MongoDB');
}).catch((err) => {
  console.error('Failed to connect to MongoDB', err);
  process.exit(1);  // Exit with failure
});

// Start server with fallback ports
const startServer = (port = 5000) => {
  try {
    const server = app.listen(port, () => {
      console.log(`Server running on port ${port} in ${process.env.NODE_ENV || 'development'} mode`);
    });
    
    server.on('error', (e: any) => {
      if (e.code === 'EADDRINUSE') {
        console.log(`Port ${port} is already in use, trying port ${port + 1}...`);
        startServer(port + 1);
      } else {
        console.error('Server error:', e);
      }
    });
  } catch (error) {
    console.error('Failed to start server:', error);
  }
};

// Try to start server with port from env or default 5000
const PORT = parseInt(process.env.PORT || '5000', 10);
startServer(PORT);

export default app;
