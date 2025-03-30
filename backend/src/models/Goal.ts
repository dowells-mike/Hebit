import mongoose, { Schema } from 'mongoose';
import { GoalDocument } from '../types';

const goalSchema = new Schema<GoalDocument>(
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
    category: {
      type: String,
      ref: 'Category'
    },
    startDate: {
      type: Date,
      default: Date.now
    },
    targetDate: {
      type: Date
    },
    progress: {
      type: Number,
      default: 0,
      min: 0,
      max: 100
    },
    status: {
      type: String,
      enum: ['not_started', 'in_progress', 'completed', 'abandoned'],
      default: 'not_started'
    },
    measurementType: {
      type: String,
      enum: ['numeric', 'boolean', 'checklist'],
      default: 'numeric'
    },
    metrics: {
      type: {
        type: String,
        enum: ['numeric', 'boolean', 'checklist'],
        default: 'numeric'
      },
      target: {
        type: Number,
        required: true,
        default: 100
      },
      current: {
        type: Number,
        default: 0
      },
      unit: {
        type: String
      }
    },
    milestones: [
      {
        id: {
          type: String,
          default: () => new mongoose.Types.ObjectId().toString()
        },
        title: {
          type: String,
          required: true
        },
        description: {
          type: String
        },
        dueDate: {
          type: Date
        },
        completed: {
          type: Boolean,
          default: false
        },
        completedAt: {
          type: Date
        },
        dependsOn: [String]
      }
    ],
    relatedTasks: [
      {
        type: String,
        ref: 'Task'
      }
    ],
    relatedHabits: [
      {
        type: String,
        ref: 'Habit'
      }
    ],
    impact: {
      tasks: [
        {
          id: {
            type: String,
            ref: 'Task'
          },
          weight: {
            type: Number,
            min: 0,
            max: 100,
            default: 50
          }
        }
      ],
      habits: [
        {
          id: {
            type: String,
            ref: 'Habit'
          },
          weight: {
            type: Number,
            min: 0,
            max: 100,
            default: 50
          }
        }
      ]
    },
    checkIns: [
      {
        date: {
          type: Date,
          default: Date.now
        },
        notes: {
          type: String
        },
        progressUpdate: {
          type: Number,
          required: true
        },
        blockers: [String]
      }
    ],
    metadata: {
      predictedCompletion: {
        type: Date
      },
      riskFactors: [String],
      similarGoalsSuccessRate: {
        type: Number,
        min: 0,
        max: 100
      }
    }
  },
  {
    timestamps: true
  }
);

// Index for faster queries
goalSchema.index({ user: 1 });
goalSchema.index({ status: 1 });
goalSchema.index({ targetDate: 1 });
goalSchema.index({ progress: 1 });

const Goal = mongoose.model<GoalDocument>('Goal', goalSchema);

export default Goal;
