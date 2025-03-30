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

// Interface for productivity metrics
interface ProductivityMetrics {
  user: string;
  date: Date;
  tasksCompleted: number;
  tasksCreated: number;
  habitCompletionRate: number;
  goalProgress: Array<{
    goalId: string;
    progress: number;
  }>;
  focusTime: number;
  productivityScore: number;
  dayRating: number | null;
  createdAt: Date;
  updatedAt: Date;
}

const migration: Migration = {
  name: '20250330_create_productivity_metrics',
  
  up: async (): Promise<void> => {
    console.log('Running migration: 20250330_create_productivity_metrics');
    const { client, db } = await getDb();
    
    try {
      // 1. Create the productivity metrics collection if needed
      let collections = await db.listCollections({ name: 'productivitymetrics' }).toArray();
      if (collections.length === 0) {
        await db.createCollection('productivitymetrics');
        console.log('Created productivitymetrics collection');
        
        // Create indexes
        const productivityMetricsCollection = db.collection('productivitymetrics');
        await productivityMetricsCollection.createIndex({ user: 1, date: 1 }, { unique: true });
        await productivityMetricsCollection.createIndex({ date: -1 });
        await productivityMetricsCollection.createIndex({ productivityScore: -1 });
        console.log('Created indexes for productivitymetrics collection');
      }
      
      // 2. Create initial metrics entries for existing users (for today)
      const users = await db.collection('users').find({}, { projection: { _id: 1 } }).toArray();
      console.log(`Processing ${users.length} users for initial productivity metrics`);
      
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      // Check for users without metrics for today
      for (const user of users) {
        const userId = user._id.toString();
        
        const existingMetrics = await db.collection('productivitymetrics').findOne({
          user: userId,
          date: today
        });
        
        if (!existingMetrics) {
          // Create initial empty metrics for today
          const initialMetrics: ProductivityMetrics = {
            user: userId,
            date: today,
            tasksCompleted: 0,
            tasksCreated: 0,
            habitCompletionRate: 0,
            goalProgress: [],
            focusTime: 0,
            productivityScore: 0,
            dayRating: null,
            createdAt: new Date(),
            updatedAt: new Date()
          };
          
          await db.collection('productivitymetrics').insertOne(initialMetrics);
          console.log(`Created initial productivity metrics for user ${userId}`);
        }
      }
      
      console.log('Productivity metrics migration completed successfully');
    } finally {
      await client.close();
    }
  },
  
  down: async (): Promise<void> => {
    console.log('Rolling back migration: 20250330_create_productivity_metrics');
    const { client, db } = await getDb();
    
    try {
      // Only drop the collection if it was created in this migration
      // We'll use a careful approach and only remove records created today
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      await db.collection('productivitymetrics').deleteMany({
        createdAt: { $gte: today }
      });
      
      console.log('Removed productivity metrics created today');
      console.log('Rollback completed successfully');
    } finally {
      await client.close();
    }
  }
};

export default migration; 