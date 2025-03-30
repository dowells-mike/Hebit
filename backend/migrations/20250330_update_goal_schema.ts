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
  name: '20250330_update_goal_schema',
  
  up: async (): Promise<void> => {
    console.log('Running migration: 20250330_update_goal_schema');
    const { client, db } = await getDb();
    
    try {
      // 1. Add ML metadata fields to all goals
      await db.collection('goals').updateMany(
        { 'metadata': { $exists: false } },
        { 
          $set: { 
            'metadata': {
              completionPrediction: {
                predictedCompletionDate: null,
                confidence: 0,
                factorsAffecting: []
              },
              progressPatterns: {
                peakProgressDays: [],
                consistencyScore: 0,
                averageDailyProgress: 0
              },
              similarGoalsCompleted: [],
              difficulty: 'medium',
              userMotivationLevel: 50
            }
          } 
        }
      );
      
      // 2. Add milestone tracking for ML recommendations
      await db.collection('goals').updateMany(
        { 'milestones': { $exists: false } },
        { 
          $set: { 
            'milestones': []
          } 
        }
      );
      
      // 3. Add progress tracking analytics
      await db.collection('goals').updateMany(
        { 'progressHistory': { $exists: false } },
        { 
          $set: { 
            'progressHistory': []
          } 
        }
      );

      // 4. Add related habits for goal-habit connection
      await db.collection('goals').updateMany(
        { 'relatedHabits': { $exists: false } },
        { 
          $set: { 
            'relatedHabits': []
          } 
        }
      );

      // 5. Add sentiment tracking for user feelings about the goal
      await db.collection('goals').updateMany(
        { 'sentimentTracking': { $exists: false } },
        { 
          $set: { 
            'sentimentTracking': {
              currentSentiment: 'neutral',
              history: []
            }
          } 
        }
      );
      
      // 6. Create indexes for ML-based queries
      await db.collection('goals').createIndex({ 'metadata.difficulty': 1 });
      await db.collection('goals').createIndex({ 'metadata.userMotivationLevel': 1 });
      await db.collection('goals').createIndex({ 'metadata.progressPatterns.consistencyScore': 1 });
      
      console.log('Goal schema migration completed successfully');
    } finally {
      await client.close();
    }
  },
  
  down: async (): Promise<void> => {
    console.log('Rolling back migration: 20250330_update_goal_schema');
    const { client, db } = await getDb();
    
    try {
      // Remove the newly added fields
      await db.collection('goals').updateMany(
        {},
        {
          $unset: {
            'metadata': '',
            'milestones': '',
            'progressHistory': '',
            'relatedHabits': '',
            'sentimentTracking': ''
          }
        }
      );
      
      // Drop the created indexes
      await db.collection('goals').dropIndex('metadata.difficulty_1');
      await db.collection('goals').dropIndex('metadata.userMotivationLevel_1');
      await db.collection('goals').dropIndex('metadata.progressPatterns.consistencyScore_1');
      
      console.log('Rollback completed successfully');
    } finally {
      await client.close();
    }
  }
};

export default migration; 