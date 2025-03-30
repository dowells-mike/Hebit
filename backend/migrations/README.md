# Database Migrations for Hebit App

This directory contains database migration scripts for the Hebit productivity application. These scripts are designed to safely migrate existing data in the MongoDB database to new schemas and structures as the application evolves.

## MongoDB Connection

The migrations use the MongoDB connection string from the `.env` file in the backend directory. Make sure the `MONGODB_URI` environment variable is set correctly:

```
MONGODB_URI=mongodb://localhost:27017/hebit
```

## Migration Structure

Each migration script follows a standard format with `up` and `down` methods:
- The `up` method applies changes to move the database forward
- The `down` method reverses changes to roll back if needed

## Available Migrations

1. **20250330_update_habit_schema.ts**
   - Updates the Habit schema with ML data collection fields
   - Adds metadata fields for context patterns and success metrics
   - Ensures all habits have streak data and completion history

2. **20250330_update_user_productivity_schema.ts**
   - Updates the User schema with productivity tracking fields
   - Adds notification preferences and privacy settings
   - Sets default values for timezone and authentication providers

3. **20250330_setup_achievement_collections.ts**
   - Initializes the achievement system
   - Creates default achievements
   - Sets up user-achievement relationships

4. **20250330_create_productivity_metrics.ts**
   - Creates the productivity metrics collection
   - Sets up appropriate indexes
   - Initializes empty metrics for existing users

5. **20250330_update_task_schema.ts**
   - Updates the Task schema with ML data collection fields
   - Adds completion analytics for time estimation improvement
   - Adds priority analytics for ML-based task prioritization
   - Creates fields for task clustering and relationships

6. **20250330_update_goal_schema.ts**
   - Updates the Goal schema with ML prediction fields
   - Adds milestone tracking for progress analysis
   - Adds sentiment tracking for motivation analysis
   - Creates fields for goal-habit relationships
   - Sets up indexes for ML-based goal recommendations

## Running Migrations

To run the migrations, use the following npm scripts:

```bash
# Create a new migration
npm run migration:create migration-name

# Run all pending migrations
npm run migration:up

# Roll back the most recent migration
npm run migration:down
```

Before running migrations, make sure to build the TypeScript files:

```bash
# Build TypeScript migration files
npm run build:migrations
```

## Migration Best Practices

1. **Data Safety**: Migrations should always be written to preserve existing data
2. **Idempotency**: A migration should be safe to run multiple times (check if changes are needed before applying)
3. **Backward Compatibility**: Ensure that the application works with both old and new data formats during transition
4. **Testing**: Test migrations on a copy of production data before applying to production
5. **Documentation**: Document what each migration does and any special considerations

## Troubleshooting

If a migration fails, check the error message and fix the underlying issue. Then run the migration again. 

If you need to roll back a migration, use the `migration:down` command. Note that some data changes might not be fully reversible if application logic has already operated on the new schema. 