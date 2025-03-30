import express from 'express';
import * as userController from '../controllers/userController';
import { protect } from '../middleware/auth';

const router = express.Router();

// Protect all routes
router.use(protect);

// Profile routes
router.route('/profile')
  .get(userController.getUserProfile)
  .put(userController.updateUserProfile);

// Settings routes
router.put('/settings', userController.updateUserSettings);
router.put('/notifications', userController.updateNotificationPreferences);

// Productivity routes
router.get('/productivity-stats', userController.getProductivityStats);
router.put('/productivity-hours', userController.updateProductivityHours);

export default router; 