import mongoose, { Schema } from 'mongoose';
import { HabitDocument } from '../types';

const habitSchema = new Schema<HabitDocument>(
  {
    user: {
      type: String,
      required: true,
      ref: 'User'
    },
    title: {
      type: String,
      required: [true, 'Please add a title'],
      trim: true
    },
    description: {
      type: String,
      trim: true
    },
    frequency: {
      type: String,
      enum: ['daily', 'weekly', 'monthly'],
      default: 'daily'
    },
    frequencyConfig: {
      daysOfWeek: [
        {
          type: Number,
          min: 0,
          max: 6
        }
      ],
      datesOfMonth: [
        {
          type: Number,
          min: 1,
          max: 31
        }
      ],
      timesPerPeriod: {
        type: Number,
        default: 1,
        min: 1
      }
    },
    timeOfDay: {
      type: String
    },
    daysOfWeek: [
      {
        type: Number,
        min: 0,
        max: 6
      }
    ],
    timePreference: {
      preferredTime: {
        type: String
      },
      flexibility: {
        type: Number,
        default: 30, // 30 minutes flexibility
        min: 0
      }
    },
    streak: {
      type: Number,
      default: 0
    },
    streakData: {
      current: {
        type: Number,
        default: 0
      },
      longest: {
        type: Number,
        default: 0
      },
      lastCompleted: {
        type: Date
      }
    },
    category: {
      type: String,
      ref: 'Category'
    },
    completionHistory: [
      {
        date: {
          type: Date,
          required: true
        },
        completed: {
          type: Boolean,
          required: true
        },
        value: {
          type: Number
        },
        notes: {
          type: String
        },
        mood: {
          type: Number,
          min: 1,
          max: 5
        },
        skipReason: {
          type: String
        }
      }
    ],
    difficulty: {
      type: String,
      enum: ['easy', 'medium', 'hard'],
      default: 'medium'
    },
    startDate: {
      type: Date,
      default: Date.now
    },
    endDate: {
      type: Date
    },
    reminderSettings: {
      time: {
        type: String
      },
      customMessage: {
        type: String
      },
      notificationStyle: {
        type: String,
        enum: ['basic', 'motivational'],
        default: 'basic'
      }
    },
    successCriteria: {
      type: {
        type: String,
        enum: ['boolean', 'numeric', 'timer'],
        default: 'boolean'
      },
      target: {
        type: Number
      },
      unit: {
        type: String
      },
      minimumThreshold: {
        type: Number
      }
    },
    metadata: {
      successRate: {
        type: Number,
        min: 0,
        max: 100,
        default: 0
      },
      averageCompletionTime: {
        type: String
      },
      contextPatterns: {
        location: [String],
        precedingActivities: [String],
        followingActivities: [String]
      }
    }
  },
  {
    timestamps: true
  }
);

// Index for faster queries
habitSchema.index({ user: 1 });
habitSchema.index({ frequency: 1 });
habitSchema.index({ 'streakData.current': -1 });
habitSchema.index({ difficulty: 1 });
habitSchema.index({ 'completionHistory.date': 1 });

const Habit = mongoose.model<HabitDocument>('Habit', habitSchema);

export default Habit;
