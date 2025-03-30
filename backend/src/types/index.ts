import { Request } from 'express';

export interface UserDocument {
  _id: string;
  name: string;
  email: string;
  password: string;
  username?: string;               // Add username for social features
  bio?: string;                    // User profile description
  avatarUrl?: string;              // Profile picture
  coverPhotoUrl?: string;          // Profile cover image
  timezone?: string;               // For time-sensitive features
  authProviders?: {                // Track authentication methods
    email?: boolean;
    google?: boolean;
    biometric?: boolean;
  };
  settings?: {                     // User preferences
    theme?: 'light' | 'dark' | 'system';
    startScreen?: string;
    notificationPreferences?: {
      tasks?: boolean;
      habits?: boolean;
      goals?: boolean;
      system?: boolean;
    };
    privacySettings?: {
      shareActivity?: boolean;
      allowSuggestions?: boolean;
    };
  };
  productivity?: {                 // ML-related user patterns
    peakHours?: number[];          // 0-23 hours when most productive
    preferredWorkDays?: number[];  // 0-6 days of week
    focusDuration?: number;        // Average focus minutes
    completionRate?: number;       // Task completion rate
  };
  isAdmin?: boolean;
  lastLogin?: Date;
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
  completedAt?: Date;              // When was it completed
  priority: 'low' | 'medium' | 'high';
  dueDate?: Date;
  status?: 'todo' | 'in_progress' | 'completed' | 'archived';  // More granular status
  category?: string;
  parentTaskId?: string;           // For hierarchical tasks
  tags?: string[];
  recurrence?: {                   // Recurring task pattern
    frequency: 'daily' | 'weekly' | 'monthly';
    interval: number;
    endDate?: Date;
  };
  reminderTime?: Date;             // When to remind
  effort?: number;                 // Estimated effort (1-5)
  complexity?: number;             // Task complexity (1-5) for ML
  attachments?: [{                 // File attachments
    name: string;
    url: string;
    type: string;
    size: number;
  }];
  metadata?: {                     // For ML and additional data
    contextualUrgency?: number;    // ML-calculated urgency
    estimatedDuration?: number;    // Minutes to complete
    lastModifiedField?: string;    // Track which field changed last
    completionContext?: {          // Context when completed
      location?: string;
      timeOfDay?: number;
      dayOfWeek?: number;
    }
  };
  createdAt: Date;
  updatedAt: Date;
}

export interface HabitDocument {
  _id: string;
  user: string;
  title: string;
  description?: string;
  frequency: 'daily' | 'weekly' | 'monthly';
  frequencyConfig?: {              // More detailed frequency
    daysOfWeek?: number[];         // 0-6 for weekly
    datesOfMonth?: number[];       // 1-31 for monthly
    timesPerPeriod?: number;       // How many times in period
  };
  timeOfDay?: string;              // Kept for backward compatibility
  daysOfWeek?: number[];           // Kept for backward compatibility
  timePreference?: {               // When to do habit
    preferredTime?: string;        // Preferred time
    flexibility?: number;          // Minutes of flexibility
  };
  streak: number;                  // Kept for backward compatibility
  streakData?: {                   // Enhanced streak tracking
    current: number;
    longest: number;
    lastCompleted?: Date;
  };
  category?: string;
  completionHistory: {             // Expanded history
    date: Date;
    completed: boolean;
    value?: number;                // For measurable habits
    notes?: string;
    mood?: number;                 // 1-5 mood rating
    skipReason?: string;
  }[];
  difficulty?: 'easy' | 'medium' | 'hard';
  startDate?: Date;
  endDate?: Date;
  reminderSettings?: {             // Enhanced reminders
    time?: string;
    customMessage?: string;
    notificationStyle?: 'basic' | 'motivational';
  };
  successCriteria?: {              // How to measure success
    type?: 'boolean' | 'numeric' | 'timer';
    target?: number;
    unit?: string;
    minimumThreshold?: number;
  };
  metadata?: {                     // For ML features
    successRate?: number;          // % success rate
    averageCompletionTime?: string;
    contextPatterns?: {            // When habit is usually completed
      location?: string[];
      precedingActivities?: string[];
      followingActivities?: string[];
    }
  };
  createdAt: Date;
  updatedAt: Date;
}

export interface GoalDocument {
  _id: string;
  user: string;
  title: string;
  description?: string;
  category?: string;
  startDate?: Date;
  targetDate?: Date;
  progress: number;               // 0-100
  status: 'not_started' | 'in_progress' | 'completed' | 'abandoned';
  measurementType?: 'numeric' | 'boolean' | 'checklist';
  metrics?: {                     // How to measure progress
    type: 'numeric' | 'boolean' | 'checklist';
    target: number;
    current: number;
    unit?: string;
  };
  milestones?: [{                 // Milestone sub-entity
    id: string;
    title: string;
    description?: string;
    dueDate?: Date;
    completed: boolean;
    completedAt?: Date;
    dependsOn?: string[];        // IDs of prerequisite milestones
  }];
  relatedTasks?: string[];
  relatedHabits?: string[];
  impact?: {                     // Impact weights for related items
    tasks?: [{
      id: string;
      weight: number;           // 0-100%
    }];
    habits?: [{
      id: string;
      weight: number;           // 0-100%
    }];
  };
  checkIns?: [{                  // Goal check-in history
    date: Date;
    notes: string;
    progressUpdate: number;
    blockers?: string[];
  }];
  metadata?: {                   // For ML
    predictedCompletion?: Date;  // ML prediction
    riskFactors?: string[];      // Factors that might affect completion
    similarGoalsSuccessRate?: number;
  };
  createdAt: Date;
  updatedAt: Date;
}

export interface CategoryDocument {
  _id: string;
  user: string;
  name: string;
  color: string;                // HEX color code
  icon?: string;
  type: 'task' | 'habit' | 'goal' | 'all';
  isDefault: boolean;
  order: number;
  createdAt: Date;
  updatedAt: Date;
}

export interface ProductivityMetricsDocument {
  _id: string;
  user: string;
  date: Date;
  tasksCompleted: number;
  tasksCreated: number;
  habitCompletionRate: number;
  goalProgress: {
    goalId: string;
    progress: number;
  }[];
  focusTime: number;             // Minutes of focus time
  productivityScore: number;     // 0-100 calculated score
  dayRating?: number;            // 1-5 user-provided rating
  createdAt: Date;
  updatedAt: Date;
}

export interface AchievementDocument {
  _id: string;
  name: string;
  description: string;
  category: 'tasks' | 'habits' | 'goals' | 'special';
  points: number;
  icon: string;
  criteria: {
    type: 'count' | 'streak' | 'time' | 'complex';
    target: number;
    criteria: string;           // JSON or string criteria definition
  };
  rarity: 'common' | 'rare' | 'epic' | 'legendary';
  createdAt: Date;
}

export interface UserAchievementDocument {
  _id: string;
  user: string;
  achievement: string;          // Reference to achievement ID
  progress: number;             // Progress towards achievement (0-100)
  earned: boolean;
  earnedAt?: Date;
  createdAt: Date;
  updatedAt: Date;
}

export interface AuthRequest extends Request {
  user?: UserDocument;
}
