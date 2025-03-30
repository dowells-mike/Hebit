# Hebit Database Setup Guide

This guide explains how to set up and monitor the MongoDB database for the Hebit productivity application.

## Database Connection Details

The Hebit application uses MongoDB as its primary database.

- **Connection String**: `mongodb://localhost:27017/hebit`
- **Database Name**: `hebit`
- **Default Collections**:
  - `users` - User account information
  - `habits` - User habit tracking data
  - `tasks` - User tasks and to-dos
  - `goals` - User goals and milestones
  - `categories` - Categories for organizing habits, tasks, and goals
  - `achievements` - Achievement definitions
  - `userachievements` - User progress on achievements
  - `productivitymetrics` - Daily productivity tracking data

## Schema Design for ML Features

The database schema is designed to support machine learning features for the Hebit app:

### Habits Collection
- **Metadata** - Context patterns and success metrics
- **Streak Data** - Tracking of current and longest streaks
- **Completion History** - Detailed record of habit completion events

### Tasks Collection
- **Metadata** - Completion patterns and time estimation accuracy
- **Completion Analytics** - Data about when tasks are completed
- **Priority Analytics** - History of priority changes for ML recommendations
- **Related Tasks** - Connections between similar tasks for clustering

### Goals Collection
- **Metadata** - Progress patterns and completion predictions
- **Milestones** - Structured breakdown of goal progress
- **Progress History** - Time-series data of goal completion progress
- **Related Habits** - Connections between goals and supporting habits
- **Sentiment Tracking** - User motivation and emotional connection to goals

### User Collection
- **Productivity Fields** - User productivity patterns and preferences
- **Settings** - User preferences for notifications and privacy

### Productivity Metrics Collection
- **Daily Metrics** - Day-by-day productivity statistics
- **Focus Time** - Tracking of focused work periods
- **Task Completion** - Aggregated task completion statistics
- **Habit Completion** - Aggregated habit adherence statistics

## Setting Up MongoDB Locally

1. **Install MongoDB Community Edition**
   - Follow the [official MongoDB installation guide](https://docs.mongodb.com/manual/administration/install-community/) for your operating system.

2. **Start MongoDB Service**
   - On Windows, MongoDB is typically installed as a service that starts automatically
   - On Linux/macOS, use the following command:
     ```
     sudo systemctl start mongod
     ```

3. **Create the Hebit Database**
   - MongoDB will automatically create the database when the application first connects to it.

## Using MongoDB Compass

MongoDB Compass is a graphical user interface for MongoDB that makes it easy to explore and manipulate your data.

1. **Install MongoDB Compass**
   - Download from the [MongoDB website](https://www.mongodb.com/products/compass)

2. **Connect to the Hebit Database**
   - Open MongoDB Compass
   - Enter the connection string: `mongodb://localhost:27017/`
   - Click "Connect"
   - You should see the `hebit` database in the list

3. **Exploring Collections**
   - Click on the `hebit` database
   - You'll see all collections within the database
   - Click on a collection to view its documents

4. **Monitoring Database Changes**
   - MongoDB Compass provides real-time updates when watching collections
   - You can use the "Watch" button in the collection view to monitor changes

## Running Database Migrations

The application includes database migration scripts to safely update the database schema as the application evolves. To run the migrations:

1. Navigate to the backend directory:
   ```
   cd backend
   ```

2. Build the migration files:
   ```
   npm run build:migrations
   ```

3. Run the migrations:
   ```
   npm run migration:up
   ```

4. If needed, roll back the most recent migration:
   ```
   npm run migration:down
   ```

## Machine Learning Data Collection

The schema is designed to automatically collect data for ML features:

1. **User Productivity Patterns**
   - The system tracks when users are most productive
   - Data on which days and times show highest completion rates
   - Preferred work patterns based on actual app usage

2. **Task Completion Insights**
   - Analysis of estimated vs. actual completion time
   - Patterns in task postponement and prioritization
   - Contextual factors affecting task completion

3. **Habit Formation Analysis**
   - Streak data and consistency metrics
   - Environmental and timing factors that affect habit adherence
   - Correlations between related habits and goals

4. **Goal Achievement Predictions**
   - Progress patterns and completion likelihood
   - Factors that contribute to successful goal completion
   - Sentiment analysis on user's emotional connection to goals

## Database Backup and Restore

### Creating a Backup

```bash
mongodump --uri="mongodb://localhost:27017/hebit" --out=./backup/$(date +%Y-%m-%d)
```

### Restoring from Backup

```bash
mongorestore --uri="mongodb://localhost:27017/hebit" --dir=./backup/[backup-date]/hebit
```

## Troubleshooting

### Common Issues

1. **Connection Refused**
   - Ensure MongoDB service is running
   - Check if the port 27017 is accessible

2. **Authentication Failed**
   - Verify the connection string includes correct credentials if authentication is enabled

3. **Missing Collections**
   - Run the migrations to create the necessary collections
   - Check application logs for database initialization errors

### Getting Help

If you encounter issues with the database, check:
- MongoDB logs located at `/var/log/mongodb/mongod.log` (Linux/macOS) or in the MongoDB installation directory on Windows
- Application logs for database-related errors 