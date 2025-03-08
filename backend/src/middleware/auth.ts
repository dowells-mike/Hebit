import { Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { catchAsync } from './errorHandler';
import { AuthRequest } from '../types';
import { AppError } from './errorHandler';

interface JwtPayload {
  id: string;
}

export const protect = catchAsync(async (req: AuthRequest, res: Response, next: NextFunction) => {
  let token: string | undefined;

  // Check for token in Authorization header
  if (req.headers.authorization && req.headers.authorization.startsWith('Bearer')) {
    token = req.headers.authorization.split(' ')[1];
  }

  // Check if token exists
  if (!token) {
    throw new AppError('Not authorized, no token provided', 401);
  }

  try {
    // Verify token
    const decoded = jwt.verify(token, process.env.JWT_SECRET as string) as JwtPayload;

    // Attach user to request object
    // Note: This will be updated once we have the User model implemented
    req.user = { id: decoded.id } as any;
    
    next();
  } catch (error) {
    throw new AppError('Not authorized, token invalid or expired', 401);
  }
});

// Additional middleware to check if user has admin role
export const adminOnly = catchAsync(async (req: AuthRequest, res: Response, next: NextFunction) => {
  if (!req.user || !req.user.isAdmin) {
    throw new AppError('Not authorized as an admin', 403);
  }

  next();
});
