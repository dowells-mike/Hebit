import fs from 'fs';
import path from 'path';
import mongoose from 'mongoose';
import dotenv from 'dotenv';
import { MongoClient, Document, WithId } from 'mongodb';

// Load environment variables
dotenv.config();

// Interface for migration metadata
interface MigrationMeta {
  name: string;
  appliedAt: Date;
}

// Interface for a migration script
export interface Migration {
  name: string;
  up: () => Promise<void>;
  down: () => Promise<void>;
}

// Migration collection name
const MIGRATION_COLLECTION = 'migrations';

export async function connectToMongo(): Promise<MongoClient> {
  const mongoUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/hebit';
  
  if (!mongoUri) {
    throw new Error('MONGODB_URI not defined in environment');
  }
  
  try {
    const client = await MongoClient.connect(mongoUri);
    console.log('Connected to MongoDB for migrations');
    return client;
  } catch (error) {
    console.error('Failed to connect to MongoDB', error);
    throw error;
  }
}

// Run all pending migrations
export async function runMigrations(): Promise<void> {
  console.log('Starting migrations...');
  const client = await connectToMongo();
  const db = client.db();
  
  try {
    // Create migrations collection if it doesn't exist
    const collections = await db.listCollections({ name: MIGRATION_COLLECTION }).toArray();
    if (collections.length === 0) {
      await db.createCollection(MIGRATION_COLLECTION);
      console.log(`Created ${MIGRATION_COLLECTION} collection`);
    }
    
    // Get list of applied migrations
    const migrationsCollection = db.collection(MIGRATION_COLLECTION);
    const migrationDocs = await migrationsCollection.find().toArray();
    
    // Properly map MongoDB documents to our MigrationMeta interface
    const appliedMigrations: MigrationMeta[] = migrationDocs.map(doc => ({
      name: doc.name as string,
      appliedAt: doc.appliedAt as Date
    }));
    
    const appliedMigrationNames = new Set(appliedMigrations.map(m => m.name));
    
    // Get all migration files
    const migrationsDir = path.join(__dirname);
    const migrationFiles = fs.readdirSync(migrationsDir)
      .filter(file => {
        return file.endsWith('.js') && 
               file !== 'migrationRunner.js' && 
               file !== 'createMigration.js' &&
               file !== 'README.md' &&
               !file.includes('.test.js');
      })
      .sort(); // Sort to ensure consistent order
    
    // Run pending migrations
    for (const file of migrationFiles) {
      const migrationName = path.basename(file, '.js');
      
      if (!appliedMigrationNames.has(migrationName)) {
        console.log(`Running migration: ${migrationName}`);
        
        // Import and run the migration
        const migration = require(path.join(migrationsDir, file)).default as Migration;
        
        try {
          await migration.up();
          
          // Record the migration
          await migrationsCollection.insertOne({
            name: migrationName,
            appliedAt: new Date()
          });
          
          console.log(`Migration ${migrationName} applied successfully`);
        } catch (error) {
          console.error(`Migration ${migrationName} failed:`, error);
          throw error;
        }
      } else {
        console.log(`Migration ${migrationName} already applied, skipping`);
      }
    }
    
    console.log('All migrations completed successfully');
  } finally {
    await client.close();
    console.log('MongoDB connection closed');
  }
}

// Rollback the last applied migration
export async function rollbackLastMigration(): Promise<void> {
  console.log('Rolling back last migration...');
  const client = await connectToMongo();
  const db = client.db();
  
  try {
    const migrationsCollection = db.collection(MIGRATION_COLLECTION);
    
    // Get the last applied migration
    const lastMigrationDocs = await migrationsCollection
      .find()
      .sort({ appliedAt: -1 })
      .limit(1)
      .toArray();
    
    if (lastMigrationDocs.length === 0) {
      console.log('No migrations to roll back');
      return;
    }
    
    const lastMigration = lastMigrationDocs[0];
    const migrationName = lastMigration.name as string;
    console.log(`Rolling back migration: ${migrationName}`);
    
    // Import the migration
    const migrationPath = path.join(__dirname, `${migrationName}.js`);
    const migration = require(migrationPath).default as Migration;
    
    try {
      await migration.down();
      
      // Remove from applied migrations
      await migrationsCollection.deleteOne({ name: migrationName });
      
      console.log(`Migration ${migrationName} rolled back successfully`);
    } catch (error) {
      console.error(`Failed to roll back migration ${migrationName}:`, error);
      throw error;
    }
  } finally {
    await client.close();
    console.log('MongoDB connection closed');
  }
}

// If this script is run directly
if (require.main === module) {
  const action = process.argv[2];
  
  if (action === 'up') {
    runMigrations()
      .then(() => process.exit(0))
      .catch(error => {
        console.error('Migration failed:', error);
        process.exit(1);
      });
  } else if (action === 'down') {
    rollbackLastMigration()
      .then(() => process.exit(0))
      .catch(error => {
        console.error('Rollback failed:', error);
        process.exit(1);
      });
  } else {
    console.log('Usage: node migrationRunner.js [up|down]');
    process.exit(1);
  }
} 