import express from 'express';
import * as habitController from '../controllers/habitController';
import * as mockHabitController from '../controllers/mockHabitController';
import { protect } from '../middleware/auth';

// Use mock controllers directly for now
const controller = mockHabitController;

const router = express.Router();

// Protect all routes
router.use(protect);

// Routes
router.route('/')
  .get(controller.getHabits)
  .post(controller.createHabit);

router.route('/:id')
  .get(controller.getHabitById)
  .put(controller.updateHabit)
  .delete(controller.deleteHabit);

router.post('/:id/track', controller.trackHabit);
router.get('/:id/streak', controller.getHabitStreak);
router.post('/:id/complete', controller.completeHabit);

export default router;
