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
    streak: {
      type: Number,
      default: 0
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
        }
      }
    ]
  },
  {
    timestamps: true
  }
);

// Index for faster queries
habitSchema.index({ user: 1 });
habitSchema.index({ frequency: 1 });

const Habit = mongoose.model<HabitDocument>('Habit', habitSchema);

export default Habit;
