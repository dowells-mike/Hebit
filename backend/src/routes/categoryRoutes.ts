import express from 'express';
import {
  getCategories,
  createCategory,
  updateCategory,
  deleteCategory,
} from '../controllers/categoryController';
import { protect } from '../middleware/auth'; // Import authentication middleware

const router = express.Router();

// Apply protect middleware to all category routes
router.use(protect);

// Define routes
router.route('/')
  .get(getCategories)
  .post(createCategory);

router.route('/:id')
  .put(updateCategory)
  .delete(deleteCategory);

export default router; 