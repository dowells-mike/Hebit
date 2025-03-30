import mongoose, { Schema } from 'mongoose';
import { AchievementDocument } from '../types';

const achievementSchema = new Schema<AchievementDocument>(
  {
    name: {
      type: String,
      required: [true, 'Please add an achievement name'],
      trim: true,
      unique: true
    },
    description: {
      type: String,
      required: [true, 'Please add a description'],
      trim: true
    },
    category: {
      type: String,
      enum: ['tasks', 'habits', 'goals', 'special'],
      required: true
    },
    points: {
      type: Number,
      required: true,
      min: 0
    },
    icon: {
      type: String,
      required: true
    },
    criteria: {
      type: {
        type: String,
        enum: ['count', 'streak', 'time', 'complex'],
        required: true
      },
      target: {
        type: Number,
        required: true,
        min: 1
      },
      criteria: {
        type: String,
        required: true
      }
    },
    rarity: {
      type: String,
      enum: ['common', 'rare', 'epic', 'legendary'],
      default: 'common'
    }
  },
  {
    timestamps: true
  }
);

// Index for category-based lookups
achievementSchema.index({ category: 1 });

// Index for points-based queries
achievementSchema.index({ points: 1 });

// Index for rarity-based queries
achievementSchema.index({ rarity: 1 });

const Achievement = mongoose.model<AchievementDocument>('Achievement', achievementSchema);

export default Achievement; 