import { Request, Response } from 'express';
import jwt from 'jsonwebtoken';
import { catchAsync, AppError } from '../middleware/errorHandler';
import { AuthRequest } from '../types';

// In-memory users store for development mode
const users = new Map();
let lastId = 0;

// Generate JWT
const generateToken = (id: string): string => {
  const secret = process.env.JWT_SECRET || 'your_jwt_secret_here';
  return jwt.sign({ id }, secret);
};

/**
 * @desc    Register a new user
 * @route   POST /api/auth/register
 * @access  Public
 */
export const register = catchAsync(async (req: Request, res: Response) => {
  const { name, email, password } = req.body;

  // Check if user already exists
  if (Array.from(users.values()).some(user => (user as any).email === email)) {
    throw new AppError('User already exists with this email', 400);
  }

  // Create new user
  const userId = (++lastId).toString();
  const user = {
    _id: userId,
    name,
    email,
    password, // In a real app, this would be hashed
    isAdmin: false,
    createdAt: new Date()
  };

  users.set(userId, user);

  // Generate token
  const token = generateToken(userId);

  res.status(201).json({
    token,
    user: {
      id: user._id,
      name: user.name,
      email: user.email,
      isAdmin: user.isAdmin,
      createdAt: user.createdAt
    }
  });
});

/**
 * @desc    Login user
 * @route   POST /api/auth/login
 * @access  Public
 */
export const login = catchAsync(async (req: Request, res: Response) => {
  const { email, password } = req.body;

  // Check if email and password are provided
  if (!email || !password) {
    throw new AppError('Please provide email and password', 400);
  }

  // Find user by email
  const user = Array.from(users.values()).find((u: any) => u.email === email);
  if (!user) {
    throw new AppError('Invalid credentials', 401);
  }

  // Check if password matches
  if (user.password !== password) {
    throw new AppError('Invalid credentials', 401);
  }

  // Generate token
  const token = generateToken(user._id);

  res.status(200).json({
    token,
    user: {
      id: user._id,
      name: user.name,
      email: user.email,
      isAdmin: user.isAdmin,
      createdAt: user.createdAt
    }
  });
});

/**
 * @desc    Get current user profile
 * @route   GET /api/auth/profile
 * @access  Private
 */
export const getProfile = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  
  const user = users.get(userId);
  if (!user) {
    throw new AppError('User not found', 404);
  }

  res.status(200).json({
    id: user._id,
    name: user.name,
    email: user.email,
    isAdmin: user.isAdmin,
    createdAt: user.createdAt
  });
});

/**
 * @desc    Update user profile
 * @route   PUT /api/auth/profile
 * @access  Private
 */
export const updateProfile = catchAsync(async (req: AuthRequest, res: Response) => {
  const userId = req.user?._id;
  const { name, email, password } = req.body;
  
  const user = users.get(userId);
  if (!user) {
    throw new AppError('User not found', 404);
  }

  // Update fields
  if (name) user.name = name;
  if (email) user.email = email;
  if (password) user.password = password;

  users.set(userId, user);

  res.status(200).json({
    id: user._id,
    name: user.name,
    email: user.email,
    isAdmin: user.isAdmin,
    createdAt: user.createdAt
  });
});
