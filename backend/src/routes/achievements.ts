import express from 'express';
import {
  getAllAchievements,
  getUserAchievements,
  markAchievementSeen
} from '../controllers/achievementController';
import { protect as authMiddleware } from '../middleware/auth';

const router = express.Router();

// @route   GET /api/achievements
// @desc    Get all defined achievements
// @access  Private
router.get('/', authMiddleware, getAllAchievements);

// @route   GET /api/achievements/user/:userId (or /api/achievements/user/me)
// @desc    Get a specific user's achievements and progress
// @access  Private
router.get('/user/:userId', authMiddleware, getUserAchievements);

// @route   POST /api/achievements/user-achievements/:userAchievementId/seen
// @desc    Mark a specific user achievement as seen
// @access  Private
router.post('/user-achievements/:userAchievementId/seen', authMiddleware, markAchievementSeen);

export default router; 