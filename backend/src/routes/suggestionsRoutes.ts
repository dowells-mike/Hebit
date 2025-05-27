import express from 'express';
import { getTaskSuggestions } from '../controllers/suggestionsController';
import { protect } from '../middleware/auth'; // Corrected relative path

const router = express.Router();

// @route   GET /api/suggestions/tasks
// @desc    Get task suggestions for the logged-in user
// @access  Private
router.route('/tasks').get(protect, getTaskSuggestions);

export default router; 