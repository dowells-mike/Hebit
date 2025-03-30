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
 * Format date for display
 */
export const formatDate = (date: Date): string => {
  return new Date(date).toLocaleString();
};

/**
 * Calculate date difference in days
 */
export const dateDiffInDays = (date1: Date, date2: Date): number => {
  const diffTime = Math.abs(date2.getTime() - date1.getTime());
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
};