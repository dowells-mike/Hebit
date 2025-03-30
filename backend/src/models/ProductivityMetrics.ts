import mongoose, { Schema } from 'mongoose';
import { ProductivityMetricsDocument } from '../types';

const productivityMetricsSchema = new Schema<ProductivityMetricsDocument>(
  {
    user: {
      type: String,
      required: true,
      ref: 'User'
    },
    date: {
      type: Date,
      required: true,
      default: () => {
        const now = new Date();
        return new Date(now.setHours(0, 0, 0, 0));
      }
    },
    tasksCompleted: {
      type: Number,
      default: 0
    },
    tasksCreated: {
      type: Number,
      default: 0
    },
    habitCompletionRate: {
      type: Number,
      min: 0,
      max: 100,
      default: 0
    },
    goalProgress: [
      {
        goalId: {
          type: String,
          ref: 'Goal',
          required: true
        },
        progress: {
          type: Number,
          required: true,
          min: 0,
          max: 100
        }
      }
    ],
    focusTime: {
      type: Number, // In minutes
      default: 0,
      min: 0
    },
    productivityScore: {
      type: Number,
      min: 0,
      max: 100,
      default: 0
    },
    dayRating: {
      type: Number,
      min: 1,
      max: 5
    }
  },
  {
    timestamps: true
  }
);

// Ensure a user can only have one entry per day
productivityMetricsSchema.index(
  { user: 1, date: 1 },
  { unique: true }
);

// Index for date-based queries
productivityMetricsSchema.index({ date: -1 });

// Index for productivity score analysis
productivityMetricsSchema.index({ productivityScore: -1 });

const ProductivityMetrics = mongoose.model<ProductivityMetricsDocument>(
  'ProductivityMetrics',
  productivityMetricsSchema
);

export default ProductivityMetrics; 