import { Request } from 'express';

export interface UserDocument {
  _id: string;
  name: string;
  email: string;
  password: string;
  isAdmin?: boolean;
  createdAt: Date;
  updatedAt: Date;
  comparePassword(candidatePassword: string): Promise<boolean>;
}

export interface TaskDocument {
  _id: string;
  user: string;
  title: string;
  description?: string;
  completed: boolean;
  priority: 'low' | 'medium' | 'high';
  dueDate?: Date;
  category?: string;
  tags?: string[];
  createdAt: Date;
  updatedAt: Date;
}

export interface HabitDocument {
  _id: string;
  user: string;
  title: string;
  description?: string;
  frequency: 'daily' | 'weekly' | 'monthly';
  timeOfDay?: string;
  daysOfWeek?: number[];
  streak: number;
  completionHistory: {
    date: Date;
    completed: boolean;
  }[];
  createdAt: Date;
  updatedAt: Date;
}

export interface GoalDocument {
  _id: string;
  user: string;
  title: string;
  description?: string;
  targetDate?: Date;
  progress: number;
  status: 'not_started' | 'in_progress' | 'completed';
  relatedTasks?: string[];
  relatedHabits?: string[];
  createdAt: Date;
  updatedAt: Date;
}

export interface AuthRequest extends Request {
  user?: UserDocument;
}
