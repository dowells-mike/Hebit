import express from 'express';
import * as productivityController from '../controllers/productivityMetricsController';
import { protect } from '../middleware/auth';

const router = express.Router();

// Protect all routes
router.use(protect);

// Routes
router.route('/')
  .get(productivityController.getProductivityMetrics);

router.route('/:date')
  .get(productivityController.getDailyProductivityMetrics);

router.post('/focus', productivityController.trackFocusTime);
router.post('/rating', productivityController.submitDailyRating);

// This could be protected with additional middleware for admin-only access
router.post('/generate', productivityController.generateDailyMetrics);

export default router; 