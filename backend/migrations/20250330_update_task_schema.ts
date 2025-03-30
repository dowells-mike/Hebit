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
  name: '20250330_update_task_schema',
  
  up: async (): Promise<void> => {
    console.log('Running migration: 20250330_update_task_schema');
    const { client, db } = await getDb();
    
    try {
      // 1. Add ML metadata fields to all tasks
      await db.collection('tasks').updateMany(
        { 'metadata': { $exists: false } },
        { 
          $set: { 
            'metadata': {
              completionPatterns: {
                timeOfDay: [],
                dayOfWeek: [],
                location: []
              },
              estimatedVsActualTime: {
                accuracy: 0,
                averageDeviation: 0
              },
              postponementCount: 0,
              focusSessionsRequired: 0,
              tags: []
            }
          } 
        }
      );
      
      // 2. Add completion analytics fields
      await db.collection('tasks').updateMany(
        { 'completionAnalytics': { $exists: false } },
        { 
          $set: { 
            'completionAnalytics': {
              estimatedDuration: null,
              actualDuration: null,
              startedAt: null,
              completionStreak: 0,
              focusSessions: []
            }
          } 
        }
      );
      
      // 3. Add priority analytics for ML recommendations
      await db.collection('tasks').updateMany(
        { 'priorityAnalytics': { $exists: false } },
        { 
          $set: { 
            'priorityAnalytics': {
              originalPriority: null,
              changes: [],
              mlSuggested: false,
              mlConfidence: 0
            }
          } 
        }
      );

      // 4. Add related tasks field for task clustering
      await db.collection('tasks').updateMany(
        { 'relatedTasks': { $exists: false } },
        { 
          $set: { 
            'relatedTasks': []
          } 
        }
      );

      // 5. Create index for task analytics
      await db.collection('tasks').createIndex({ 'metadata.tags': 1 });
      await db.collection('tasks').createIndex({ 'completionAnalytics.completionStreak': 1 });
      
      console.log('Task schema migration completed successfully');
    } finally {
      await client.close();
    }
  },
  
  down: async (): Promise<void> => {
    console.log('Rolling back migration: 20250330_update_task_schema');
    const { client, db } = await getDb();
    
    try {
      // Remove the newly added fields
      await db.collection('tasks').updateMany(
        {},
        {
          $unset: {
            'metadata': '',
            'completionAnalytics': '',
            'priorityAnalytics': '',
            'relatedTasks': ''
          }
        }
      );
      
      // Drop the created indexes
      await db.collection('tasks').dropIndex('metadata.tags_1');
      await db.collection('tasks').dropIndex('completionAnalytics.completionStreak_1');
      
      console.log('Rollback completed successfully');
    } finally {
      await client.close();
    }
  }
};

export default migration; 