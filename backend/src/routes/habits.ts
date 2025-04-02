import express from 'express';
import * as habitController from '../controllers/habitController';
import { protect } from '../middleware/auth';

const router = express.Router();

// Protect all routes
router.use(protect);

// Special routes must come before generic routes
router.get('/today', habitController.getTodaysHabits);

// Routes
router.route('/')
  .get(habitController.getHabits)
  .post(habitController.createHabit);

router.route('/:id')
  .get(habitController.getHabitById)
  .put(habitController.updateHabit)
  .delete(habitController.deleteHabit);

router.post('/:id/track', habitController.trackHabit);
router.get('/:id/stats', habitController.getHabitStats);

export default router;
