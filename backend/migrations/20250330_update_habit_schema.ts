import { Migration } from './migrationRunner';
import { MongoClient } from 'mongodb';
import dotenv from 'dotenv';

// Load environment variables
dotenv.config();

// MongoDB connection
async function getDb() {
  const mongoUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/hebit';
  if (!mongoUri) {
    throw new Error('MONGODB_URI not defined in environment');
  }
  
  const client = await MongoClient.connect(mongoUri);
  return { client, db: client.db() };
}

const migration: Migration = {
  name: '20250330_update_habit_schema',
  
  up: async (): Promise<void> => {
    console.log('Running migration: 20250330_update_habit_schema');
    const { client, db } = await getDb();
    
    try {
      // 1. Add metadata fields to all habits
      await db.collection('habits').updateMany(
        { 'metadata.contextPatterns': { $exists: false } },
        { 
          $set: { 
            'metadata.contextPatterns': {
              location: [],
              precedingActivities: [],
              followingActivities: []
            },
            'metadata.successRate': 0,
            'metadata.averageCompletionTime': ''
          } 
        }
      );
      
      // 2. Ensure all habits have streakData field
      await db.collection('habits').updateMany(
        { 'streakData': { $exists: false } },
        { 
          $set: { 
            'streakData': {
              current: 0,
              longest: 0,
              lastCompleted: null
            }
          } 
        }
      );
      
      // 3. Ensure all habits have completionHistory array
      await db.collection('habits').updateMany(
        { 'completionHistory': { $exists: false } },
        { $set: { 'completionHistory': [] } }
      );

      // 4. Update habit frequency config structure
      await db.collection('habits').updateMany(
        { 'frequencyConfig': { $exists: false } },
        {
          $set: {
            'frequencyConfig': {
              daysOfWeek: [],
              datesOfMonth: [],
              timesPerPeriod: 1
            }
          }
        }
      );

      // 5. Add successCriteria field if missing
      await db.collection('habits').updateMany(
        { 'successCriteria': { $exists: false } },
        {
          $set: {
            'successCriteria': {
              type: 'boolean',
              target: null,
              unit: '',
              minimumThreshold: null
            }
          }
        }
      );
      
      console.log('Habit schema migration completed successfully');
    } finally {
      await client.close();
    }
  },
  
  down: async (): Promise<void> => {
    console.log('Rolling back migration: 20250330_update_habit_schema');
    const { client, db } = await getDb();
    
    try {
      // Remove the newly added metadata fields
      await db.collection('habits').updateMany(
        {},
        {
          $unset: {
            'metadata.contextPatterns': '',
            'metadata.successRate': '',
            'metadata.averageCompletionTime': ''
          }
        }
      );
      
      console.log('Rollback completed successfully');
    } finally {
      await client.close();
    }
  }
};

export default migration; 