import { Request, Response } from 'express';
import jwt from 'jsonwebtoken';
import crypto from 'crypto';
import { catchAsync, AppError } from '../middleware/errorHandler';
import { User } from '../models';
import { AuthRequest } from '../types';
import config from '../config/config';

// Generate access token
const generateAccessToken = (userId: string): string => {
  // Use 'as string' type assertion to ensure TypeScript treats it as a string
  const secret = config.jwtSecret as string;
  return jwt.sign({ id: userId }, secret, { expiresIn: '7d' });
};

// Generate refresh token - a random string that we will later associate with a user
const generateRefreshToken = (): string => {
  return crypto.randomBytes(40).toString('hex');
};

/**
 * @desc    Register a new user
 * @route   POST /api/auth/register
 * @access  Public
 */
export const register = catchAsync(async (req: Request, res: Response) => {
  const { name, email, password, username } = req.body;

  // Validate required fields
  if (!name || !email || !password) {
    throw new AppError('Please provide all required fields', 400);
  }

  // Check if user already exists
  const userExists = await User.findOne({ email });
  if (userExists) {
    throw new AppError('User already exists with this email', 400);
  }

  // Check if username is taken (if provided)
  if (username) {
    const usernameExists = await User.findOne({ username });
    if (usernameExists) {
      throw new AppError('Username is already taken', 400);
    }
  }

  // Create new user with default settings
  const user = await User.create({
    name,
    email,
    password, // Password will be hashed by the model pre-save hook
    username,
    settings: {
      theme: 'system',
      startScreen: 'dashboard',
      notificationPreferences: {
        tasks: true,
        habits: true,
        goals: true,
        system: true
      },
      privacySettings: {
        shareActivity: false,
        allowSuggestions: true
      }
    },
    productivity: {
      peakHours: [9, 10, 11, 14, 15], // Default productive hours
      completionRate: 0
    },
    lastLogin: new Date()
  });

  // Generate tokens
  const accessToken = generateAccessToken(user._id.toString());
  const refreshToken = generateRefreshToken();

  // We would store refresh token in the db in a real implementation
  // For now, we'll just send it to the client

  // Create user object for response without password
  const userResponse = {
    id: user._id,
    name: user.name,
    email: user.email,
    username: user.username,
    settings: user.settings,
    isAdmin: user.isAdmin,
    createdAt: user.createdAt
  };

  res.status(201).json({
    token: accessToken,
    refreshToken,
    user: userResponse
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
  const user = await User.findOne({ email }).select('+password');
  if (!user) {
    throw new AppError('Invalid credentials', 401);
  }

  // Check if password matches
  const isPasswordMatch = await user.comparePassword(password);
  if (!isPasswordMatch) {
    throw new AppError('Invalid credentials', 401);
  }

  // Update last login timestamp
  user.lastLogin = new Date();
  await user.save();

  // Generate tokens
  const accessToken = generateAccessToken(user._id.toString());
  const refreshToken = generateRefreshToken();

  // We would store refresh token in the db in a real implementation
  // For now, we'll just send it to the client

  // Create user object for response without password
  const userResponse = {
    id: user._id,
    name: user.name,
    email: user.email,
    username: user.username,
    avatarUrl: user.avatarUrl,
    settings: user.settings,
    isAdmin: user.isAdmin,
    createdAt: user.createdAt
  };

  res.status(200).json({
    token: accessToken,
    refreshToken,
    user: userResponse
  });
});

/**
 * @desc    Get current user profile
 * @route   GET /api/auth/profile
 * @access  Private
 */
export const getProfile = catchAsync(async (req: AuthRequest, res: Response) => {
  if (!req.user || !req.user._id) {
    throw new AppError('User not authenticated', 401);
  }

  const userId = req.user._id;
  
  const user = await User.findById(userId);
  if (!user) {
    throw new AppError('User not found', 404);
  }

  // Return comprehensive user profile without sensitive information
  res.status(200).json({
    id: user._id,
    name: user.name,
    email: user.email,
    username: user.username,
    bio: user.bio,
    avatarUrl: user.avatarUrl,
    coverPhotoUrl: user.coverPhotoUrl,
    timezone: user.timezone,
    settings: user.settings,
    productivity: user.productivity,
    isAdmin: user.isAdmin,
    lastLogin: user.lastLogin,
    createdAt: user.createdAt
  });
});

/**
 * @desc    Update user profile
 * @route   PUT /api/auth/profile
 * @access  Private
 */
export const updateProfile = catchAsync(async (req: AuthRequest, res: Response) => {
  if (!req.user || !req.user._id) {
    throw new AppError('User not authenticated', 401);
  }

  const userId = req.user._id;
  const { name, email, username, bio, avatarUrl, coverPhotoUrl, timezone, password } = req.body;
  
  const user = await User.findById(userId);
  if (!user) {
    throw new AppError('User not found', 404);
  }

  // Update fields if provided
  if (name) user.name = name;
  
  if (email) {
    // Check if email is already in use by someone else
    const existingUser = await User.findOne({ email });
    if (existingUser && existingUser._id.toString() !== userId.toString()) {
      throw new AppError('Email is already in use', 400);
    }
    user.email = email;
  }
  
  if (username) {
    // Check if username is already in use by someone else
    const existingUser = await User.findOne({ username });
    if (existingUser && existingUser._id.toString() !== userId.toString()) {
      throw new AppError('Username is already in use', 400);
    }
    user.username = username;
  }
  
  if (bio !== undefined) user.bio = bio;
  if (avatarUrl !== undefined) user.avatarUrl = avatarUrl;
  if (coverPhotoUrl !== undefined) user.coverPhotoUrl = coverPhotoUrl;
  if (timezone !== undefined) user.timezone = timezone;
  if (password) user.password = password;

  const updatedUser = await user.save();

  // Create user object for response without password
  const userResponse = {
    id: updatedUser._id,
    name: updatedUser.name,
    email: updatedUser.email,
    username: updatedUser.username,
    bio: updatedUser.bio,
    avatarUrl: updatedUser.avatarUrl,
    coverPhotoUrl: updatedUser.coverPhotoUrl,
    timezone: updatedUser.timezone,
    settings: updatedUser.settings,
    isAdmin: updatedUser.isAdmin,
    createdAt: updatedUser.createdAt
  };

  res.status(200).json(userResponse);
});

/**
 * @desc    Refresh authentication token
 * @route   POST /api/auth/refresh-token
 * @access  Public
 */
export const refreshToken = catchAsync(async (req: Request, res: Response) => {
  const { refreshToken, userId } = req.body;

  if (!refreshToken) {
    throw new AppError('Refresh token is required', 400);
  }

  if (!userId) {
    throw new AppError('User ID is required', 400);
  }

  // In a real app, we would validate the refresh token against the database
  // For now, we'll just generate a new access token
  // This is a simplified implementation for demonstration purposes only

  try {
    // Check if user exists
    const user = await User.findById(userId);
    if (!user) {
      throw new AppError('User not found', 404);
    }
    
    // Generate new access token
    const newAccessToken = generateAccessToken(userId);
    
    // In a real app, we might also rotate the refresh token
    // For now, we'll just return the new access token
    
    res.status(200).json({
      token: newAccessToken
    });
  } catch (error) {
    throw new AppError('Invalid refresh token', 401);
  }
});
