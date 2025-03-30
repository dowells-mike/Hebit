import express from 'express';
import * as achievementController from '../controllers/achievementController';
import { protect } from '../middleware/auth';

const router = express.Router();

// Protect all routes
router.use(protect);

// Routes
router.route('/')
  .get(achievementController.getAchievements)
  .post(achievementController.createAchievement); // Admin only in production

router.route('/earned')
  .get(achievementController.getEarnedAchievements);

router.route('/category/:category')
  .get(achievementController.getAchievementsByCategory);

router.route('/check')
  .post(achievementController.checkAchievementProgress);

router.route('/:id')
  .put(achievementController.updateAchievement) // Admin only in production
  .delete(achievementController.deleteAchievement); // Admin only in production

export default router; 