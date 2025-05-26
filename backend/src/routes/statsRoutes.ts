import express from 'express';
import * as statsController from '../controllers/statsController';
import { protect } from '../middleware/auth';

const router = express.Router();

// Protect all routes
router.use(protect);

// Endpoint to get aggregated task statistics
// Query params: period (e.g., 'today', 'week', 'month', 'all'), startDate (ISO_DATE), endDate (ISO_DATE)
router.get('/tasks', statsController.getTaskStatistics);

// Endpoint to get productivity score
// Query params: period (e.g., 'today', 'week', 'month'), startDate (ISO_DATE), endDate (ISO_DATE)
router.get('/productivity-score', statsController.getProductivityScore);

// Endpoint to get historical scores for trends
// Query params: periodType (e.g., 'daily', 'weekly'), count (number of periods)
router.get('/score-history', statsController.getScoreHistory);

export default router; 