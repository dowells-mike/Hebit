import fs from 'fs';
import path from 'path';

// Check if migration name is provided
if (process.argv.length < 3) {
  console.error('Please provide a migration name');
  console.log('Example: npm run migration:create add-user-preferences');
  process.exit(1);
}

// Get the migration name and format it
const rawName = process.argv[2];
const timestamp = new Date().toISOString().replace(/[-:\.T]/g, '').slice(0, 14);
const migrationName = `${timestamp}_${rawName.replace(/[^a-z0-9]/gi, '_').toLowerCase()}`;

// Migration file template
const migrationTemplate = `import { Migration } from './migrationRunner';
import { MongoClient } from 'mongodb';
import dotenv from 'dotenv';

// Load environment variables
dotenv.config();

// MongoDB connection
async function getDb() {
  const mongoUri = process.env.MONGO_URI || '';
  if (!mongoUri) {
    throw new Error('MONGO_URI not defined in environment');
  }
  
  const client = await MongoClient.connect(mongoUri);
  return { client, db: client.db() };
}

const migration: Migration = {
  name: '${migrationName}',
  
  up: async (): Promise<void> => {
    console.log('Running migration: ${migrationName}');
    const { client, db } = await getDb();
    
    try {
      // TODO: Implement your migration logic here
      // Example: Add a new field with default value to all documents in a collection
      // await db.collection('users').updateMany({}, { $set: { newField: 'defaultValue' } });
      
      console.log('Migration completed successfully');
    } finally {
      await client.close();
    }
  },
  
  down: async (): Promise<void> => {
    console.log('Rolling back migration: ${migrationName}');
    const { client, db } = await getDb();
    
    try {
      // TODO: Implement your rollback logic here
      // Example: Remove the field that was added in the up method
      // await db.collection('users').updateMany({}, { $unset: { newField: '' } });
      
      console.log('Rollback completed successfully');
    } finally {
      await client.close();
    }
  }
};

export default migration;
`;

// Write migration file
const migrationsDir = path.join(__dirname);
const filePath = path.join(migrationsDir, `${migrationName}.ts`);

fs.writeFileSync(filePath, migrationTemplate);

console.log(`Migration file created: ${filePath}`); 