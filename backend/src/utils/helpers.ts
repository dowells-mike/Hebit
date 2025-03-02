import { Request, Response, NextFunction } from 'express';

/**
 * Helper function to handle async/await errors
 */
export const asyncHandler = 
  (fn: (req: Request, res: Response, next: NextFunction) => Promise<any>) => 
  (req: Request, res: Response, next: NextFunction) => {
    Promise.resolve(fn(req, res, next)).catch(next);
  };

/**
 * Generate JWT token
 */
export const generateToken = (id: string): string => {
  // This will be implemented in the auth middleware
  return '';
};