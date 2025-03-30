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

// Define interface for UserAchievement
interface UserAchievement {
  user: string;
  achievement: string;
  progress: number;
  earned: boolean;
  createdAt: Date;
  updatedAt: Date;
}

// Default achievement data for initial setup
const defaultAchievements = [
  {
    name: 'First Habit',
    description: 'Create your first habit',
    category: 'habits',
    points: 10,
    icon: 'award-star',
    criteria: {
      type: 'count',
      target: 1,
      criteria: 'habits.created'
    },
    rarity: 'common'
  },
  {
    name: 'Early Bird',
    description: 'Complete a task before 9 AM',
    category: 'tasks',
    points: 15,
    icon: 'sunrise',
    criteria: {
      type: 'time',
      target: 1,
      criteria: 'tasks.completedBefore9am'
    },
    rarity: 'common'
  },
  {
    name: 'Consistency Master',
    description: 'Maintain a 7-day streak on any habit',
    category: 'habits',
    points: 30,
    icon: 'calendar-check',
    criteria: {
      type: 'streak',
      target: 7,
      criteria: 'habits.streak'
    },
    rarity: 'rare'
  },
  {
    name: 'Goal Achiever',
    description: 'Complete your first goal',
    category: 'goals',
    points: 25,
    icon: 'target',
    criteria: {
      type: 'count',
      target: 1,
      criteria: 'goals.completed'
    },
    rarity: 'common'
  },
  {
    name: 'Productivity Champion',
    description: 'Achieve a productivity score of 90 or higher for 5 consecutive days',
    category: 'special',
    points: 50,
    icon: 'trophy',
    criteria: {
      type: 'complex',
      target: 5,
      criteria: 'productivity.score90plus.consecutive'
    },
    rarity: 'epic'
  }
];

const migration: Migration = {
  name: '20250330_setup_achievement_collections',
  
  up: async (): Promise<void> => {
    console.log('Running migration: 20250330_setup_achievement_collections');
    const { client, db } = await getDb();
    
    try {
      // 1. Create achievements collection if it doesn't exist
      const achievements = db.collection('achievements');
      const achievementsCount = await achievements.countDocuments();
      
      // If collection is empty, insert default achievements
      if (achievementsCount === 0) {
        console.log('Inserting default achievements...');
        await achievements.insertMany(defaultAchievements);
        console.log(`Inserted ${defaultAchievements.length} default achievements`);
      }
      
      // 2. Create initial user-achievement entries for existing users
      const users = await db.collection('users').find({}, { projection: { _id: 1 } }).toArray();
      
      // For each user and achievement, create a user-achievement document if it doesn't exist
      const achievementDocs = await achievements.find({}).toArray();
      
      console.log(`Processing ${users.length} users for achievement initialization`);
      
      for (const user of users) {
        const userId = user._id.toString();
        const existingUserAchievements = await db.collection('userachievements')
          .find({ user: userId })
          .toArray();
        
        // Create a map of existing achievements for this user
        const existingMap = new Map();
        for (const ua of existingUserAchievements) {
          existingMap.set(ua.achievement, true);
        }
        
        // Create entries for missing achievements
        const newUserAchievements: UserAchievement[] = [];
        for (const achievement of achievementDocs) {
          const achievementId = achievement._id.toString();
          if (!existingMap.has(achievementId)) {
            newUserAchievements.push({
              user: userId,
              achievement: achievementId,
              progress: 0,
              earned: false,
              createdAt: new Date(),
              updatedAt: new Date()
            });
          }
        }
        
        if (newUserAchievements.length > 0) {
          await db.collection('userachievements').insertMany(newUserAchievements);
          console.log(`Created ${newUserAchievements.length} achievement entries for user ${userId}`);
        }
      }
      
      console.log('Achievement collections setup completed successfully');
    } finally {
      await client.close();
    }
  },
  
  down: async (): Promise<void> => {
    console.log('Rolling back migration: 20250330_setup_achievement_collections');
    const { client, db } = await getDb();
    
    try {
      // We don't want to drop the collections, as that would delete user data
      // Instead, we'll just note that this migration was rolled back
      console.log('Note: This migration does not have a destructive rollback');
      console.log('Default achievements and user achievement entries have been preserved');
      
      console.log('Rollback completed');
    } finally {
      await client.close();
    }
  }
};

export default migration; 