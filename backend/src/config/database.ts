import mongoose from 'mongoose';
import config from './config';

/**
 * Connect to MongoDB database
 */
const connectDB = async (): Promise<void> => {
  try {
    const conn = await mongoose.connect(config.mongoUri);
    console.log(`MongoDB Connected: ${conn.connection.host}`);
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : String(error);
    console.error(`Error connecting to MongoDB: ${errorMessage}`);
    throw error;
  }
};

export default connectDB;
