import express from 'express';
import * as taskController from '../controllers/taskController';
import { protect } from '../middleware/auth';

const router = express.Router();

// Protect all routes
router.use(protect);

// Special routes must come before generic routes
router.get('/priority', taskController.getPriorityTasks);

// Routes
router.route('/')
  .get(taskController.getTasks)
  .post(taskController.createTask);

router.route('/:id')
  .get(taskController.getTaskById)
  .put(taskController.updateTask)
  .delete(taskController.deleteTask);

router.patch('/:id/complete', taskController.toggleTaskCompletion);
router.get('/:id/subtasks', taskController.getSubTasks);

// ML recommendation routes
router.get('/recommendations/next', taskController.getRecommendedNextTask);
router.get('/recommendations/optimal-time', taskController.getOptimalTaskTime);
router.get('/stats/duration-estimate', taskController.getTaskDurationEstimate);

export default router;
