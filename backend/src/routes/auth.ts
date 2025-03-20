import express from 'express';
import * as authController from '../controllers/authController';
import { protect } from '../middleware/auth';

// Use real auth controller
const controller = authController;

const router = express.Router();

// Public routes
router.post('/register', controller.register);
router.post('/login', controller.login);
router.post('/refresh-token', controller.refreshToken);

// Protected routes
router.get('/profile', protect, controller.getProfile);
router.put('/profile', protect, controller.updateProfile);

export default router;
