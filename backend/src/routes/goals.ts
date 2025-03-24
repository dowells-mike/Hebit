import express from 'express';
import * as goalController from '../controllers/goalController';
import * as mockGoalController from '../controllers/mockGoalController';
import { protect } from '../middleware/auth';

// Use mock controllers directly for now
const controller = mockGoalController;

const router = express.Router();

// Protect all routes
router.use(protect);

// Routes
router.route('/')
  .get(controller.getGoals)
  .post(controller.createGoal);

router.route('/:id')
  .get(controller.getGoalById)
  .put(controller.updateGoal)
  .delete(controller.deleteGoal);

router.patch('/:id/progress', controller.updateProgress);
router.post('/:id/tasks', controller.linkTasksToGoal);
router.post('/:id/habits', controller.linkHabitsToGoal);

export default router;
