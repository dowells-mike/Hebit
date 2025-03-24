import express from 'express';
import * as taskController from '../controllers/taskController';
import * as mockTaskController from '../controllers/mockTaskController';
import { protect } from '../middleware/auth';

// Use mock controllers directly for now
const controller = mockTaskController;

const router = express.Router();

// Protect all routes
router.use(protect);

// Routes
router.route('/')
  .get(controller.getTasks)
  .post(controller.createTask);

router.route('/:id')
  .get(controller.getTaskById)
  .put(controller.updateTask)
  .delete(controller.deleteTask);

router.patch('/:id/complete', controller.toggleTaskCompletion);

export default router;
