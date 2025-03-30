import express from 'express';
import * as goalController from '../controllers/goalController';
import { protect } from '../middleware/auth';

const router = express.Router();

// Protect all routes
router.use(protect);

// Routes
router.route('/')
  .get(goalController.getGoals)
  .post(goalController.createGoal);

router.route('/:id')
  .get(goalController.getGoalById)
  .put(goalController.updateGoal)
  .delete(goalController.deleteGoal);

router.patch('/:id/progress', goalController.updateProgress);

// Milestone routes
router.post('/:id/milestones', goalController.addMilestone);
router.put('/:id/milestones/:milestoneIndex', goalController.updateMilestone);

// Stats route
router.get('/stats', goalController.getGoalStats);

export default router;
