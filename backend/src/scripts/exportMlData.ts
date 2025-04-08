import mongoose from 'mongoose';
import fs from 'fs';
import path from 'path';
import dotenv from 'dotenv';
import * as mlService from '../services/mlService';

// Load environment variables
dotenv.config();

/**
 * Script to export ML training data
 * 
 * This script connects to the database, extracts task data formatted for ML training,
 * and saves it to a JSON file that can be used for model training.
 */
async function exportMlData() {
  try {
    // Connect to MongoDB
    const mongoUri = process.env.MONGO_URI || 'mongodb://localhost:27017/productivity_app';
    await mongoose.connect(mongoUri);
    console.log('Connected to database');
    
    // Get training data using the ML service
    console.log('Exporting task data for ML training...');
    const trainingData = await mlService.exportTaskDataForTraining();
    
    // Create output directory if it doesn't exist
    const outputDir = path.join(__dirname, '../../ml-data');
    if (!fs.existsSync(outputDir)) {
      fs.mkdirSync(outputDir, { recursive: true });
    }
    
    // Generate timestamp for the filename
    const timestamp = new Date().toISOString().replace(/:/g, '-').replace(/\..+/, '');
    const outputFile = path.join(outputDir, `task-training-data-${timestamp}.json`);
    
    // Write data to file
    fs.writeFileSync(outputFile, JSON.stringify(trainingData, null, 2));
    
    console.log(`Exported ${trainingData.length} records to ${outputFile}`);
    console.log('Export completed successfully');
    
    // Disconnect from database
    await mongoose.disconnect();
    console.log('Disconnected from database');
    
  } catch (error) {
    console.error('Error exporting ML data:', error);
    process.exit(1);
  }
}

// Run the export function
exportMlData(); 