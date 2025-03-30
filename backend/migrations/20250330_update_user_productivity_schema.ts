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
  name: '20250330_update_user_productivity_schema',
  
  up: async (): Promise<void> => {
    console.log('Running migration: 20250330_update_user_productivity_schema');
    const { client, db } = await getDb();
    
    try {
      // 1. Add productivity field if it doesn't exist
      await db.collection('users').updateMany(
        { 'productivity': { $exists: false } },
        {
          $set: {
            'productivity': {
              peakHours: [],
              preferredWorkDays: [],
              focusDuration: 0,
              completionRate: 0
            }
          }
        }
      );

      // 2. Add notification preferences if they don't exist
      await db.collection('users').updateMany(
        { 'settings.notificationPreferences': { $exists: false } },
        {
          $set: {
            'settings.notificationPreferences': {
              tasks: true,
              habits: true,
              goals: true,
              system: true
            }
          }
        }
      );

      // 3. Add privacy settings if they don't exist
      await db.collection('users').updateMany(
        { 'settings.privacySettings': { $exists: false } },
        {
          $set: {
            'settings.privacySettings': {
              shareActivity: false,
              allowSuggestions: true
            }
          }
        }
      );

      // 4. Add timezone field with default value
      await db.collection('users').updateMany(
        { 'timezone': { $exists: false } },
        {
          $set: {
            'timezone': 'UTC'
          }
        }
      );

      // 5. Add auth providers field
      await db.collection('users').updateMany(
        { 'authProviders': { $exists: false } },
        {
          $set: {
            'authProviders': {
              email: true,
              google: false,
              biometric: false
            }
          }
        }
      );
      
      console.log('User productivity schema migration completed successfully');
    } finally {
      await client.close();
    }
  },
  
  down: async (): Promise<void> => {
    console.log('Rolling back migration: 20250330_update_user_productivity_schema');
    const { client, db } = await getDb();
    
    try {
      // Remove the added productivity fields
      await db.collection('users').updateMany(
        {},
        {
          $unset: {
            'productivity.peakHours': '',
            'productivity.preferredWorkDays': '',
            'productivity.focusDuration': '',
            'productivity.completionRate': ''
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