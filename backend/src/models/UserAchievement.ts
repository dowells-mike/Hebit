import mongoose, { Schema } from 'mongoose';
import { UserAchievementDocument } from '../types';

const userAchievementSchema = new Schema<UserAchievementDocument>(
  {
    user: {
      type: String,
      required: true,
      ref: 'User'
    },
    achievement: {
      type: String,
      required: true,
      ref: 'Achievement'
    },
    progress: {
      type: Number,
      required: true,
      default: 0,
      min: 0,
      max: 100
    },
    earned: {
      type: Boolean,
      default: false
    },
    earnedAt: {
      type: Date
    }
  },
  {
    timestamps: true
  }
);

// Ensure a user can have an achievement only once
userAchievementSchema.index(
  { user: 1, achievement: 1 },
  { unique: true }
);

// Index for filtering earned/unearned achievements
userAchievementSchema.index({ user: 1, earned: 1 });

// Index for recent achievements
userAchievementSchema.index({ earnedAt: -1 });

const UserAchievement = mongoose.model<UserAchievementDocument>(
  'UserAchievement',
  userAchievementSchema
);

export default UserAchievement; 