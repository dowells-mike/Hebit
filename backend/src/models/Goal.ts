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
      enum: ['not_started', 'in_progress', 'completed'],
      default: 'not_started'
    },
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
    ]
  },
  {
    timestamps: true
  }
);

// Index for faster queries
goalSchema.index({ user: 1 });
goalSchema.index({ status: 1 });

const Goal = mongoose.model<GoalDocument>('Goal', goalSchema);

export default Goal;
