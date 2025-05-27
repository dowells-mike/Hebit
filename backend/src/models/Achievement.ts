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
        enum: ['count', 'streak', 'completion_time', 'multi_condition', 'event_based', 'complex'],
        required: true
      },
      source: {
        type: String,
        enum: ['tasks', 'habits', 'goals', 'user_activity', 'app_usage']
      },
      targetValue: {
        type: Schema.Types.Mixed,
        required: true
      },
      conditionDetails: {
        entityType: {
          type: String,
          enum: ['task', 'habit', 'goal']
        },
        entityName: String,
        relatedEntityId: String,
        timeOperator: {
          type: String,
          enum: ['before', 'after']
        },
        timeString: String,
        eventName: String
      }
    },
    rarity: {
      type: String,
      enum: ['common', 'rare', 'epic', 'legendary'],
      default: 'common'
    },
    secret: {
      type: Boolean,
      default: false
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