# ML Functionality in Productivity App

This document provides an overview of the machine learning (ML) functionality in the productivity app and how to use it.

## Current ML Features

The app currently provides the following ML-based features:

1. **Task Completion Context Collection**: When tasks are completed, the system automatically collects contextual data (time of day, day of week, etc.) to learn user productivity patterns.

2. **Productivity Pattern Recognition**: The system analyzes when users are most productive based on task completion patterns.

3. **Task Duration Estimation**: The system can estimate how long a task will take based on similar completed tasks.

4. **Optimal Time Recommendations**: The system can recommend the best time of day to complete tasks based on historical productivity patterns.

5. **Task Prioritization**: The system can recommend which tasks to work on next based on due dates, priorities, and productivity patterns.

## API Endpoints

The following API endpoints are available:

- `GET /api/tasks/recommendations/next`: Get recommended next tasks to work on
- `GET /api/tasks/recommendations/optimal-time`: Get the optimal time of day to complete tasks
- `GET /api/tasks/stats/duration-estimate`: Get an estimated duration for a task

## Data Collection

The ML system collects the following data:

- Task completion context (time, day of week)
- User productivity patterns (peak hours, peak days)
- Task properties (category, priority, complexity)
- Task completion durations

## Exporting Training Data

To export task data for ML training:

```bash
npm run export:ml-data
```

This will create a JSON file in the `ml-data` directory with training data formatted for ML model training.

## ML Implementation Status

The current ML implementation is a basic version that focuses on data collection and simple pattern recognition. Future versions will include:

- More sophisticated ML models using TensorFlow or similar libraries
- Personalized recommendations based on more complex patterns
- Schedule optimization
- External ML platform integration

## Adding New ML Features

When adding new ML features, follow these steps:

1. Create data collection functions in the `mlService.ts` file
2. Add appropriate controller functions in the relevant controller files
3. Add API endpoints in the route files
4. Update this documentation

## Privacy Considerations

- All ML data is tied to the user's account and not shared with other users
- Users can disable ML features in their settings
- Data is processed in a privacy-preserving manner 