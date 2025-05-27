import express from 'express';
import { getAllPublicAchievements, getMyAchievements, markUserAchievementAsSeen, getEarnedAchievements } from '../controllers/achievementController';
import { protect } from '../middleware/auth'; // Corrected path

const router = express.Router();

// @route   GET /api/achievements
// @desc    Get all publicly available achievements
// @access  Public (or adjust middleware if needed)
router.get('/', getAllPublicAchievements);

// @route   GET /api/achievements/my
// @desc    Get all achievements for the authenticated user (earned and in-progress)
// @access  Private
router.get('/my', protect, getMyAchievements);

// @route   GET /api/achievements/earned
// @desc    Get all earned achievements for the authenticated user
// @access  Private
router.get('/earned', protect, getEarnedAchievements);

// @route   POST /api/achievements/my/:userAchievementId/seen
// @desc    Mark a specific user achievement as seen
// @access  Private
router.post('/my/:userAchievementId/seen', protect, markUserAchievementAsSeen);

// Future route for marking as seen:
// router.post('/my/:userAchievementId/seen', protect, markUserAchievementAsSeen);

export default router; 