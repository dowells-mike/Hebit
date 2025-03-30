import mongoose, { Schema } from 'mongoose';
import { CategoryDocument } from '../types';

const categorySchema = new Schema<CategoryDocument>(
  {
    user: {
      type: String,
      required: true,
      ref: 'User'
    },
    name: {
      type: String,
      required: [true, 'Please add a category name'],
      trim: true
    },
    color: {
      type: String,
      required: [true, 'Please select a color'],
      default: '#3498db', // Default blue color
      match: [/^#([0-9a-f]{3}|[0-9a-f]{6})$/i, 'Please enter a valid hex color']
    },
    icon: {
      type: String
    },
    type: {
      type: String,
      enum: ['task', 'habit', 'goal', 'all'],
      default: 'all'
    },
    isDefault: {
      type: Boolean,
      default: false
    },
    order: {
      type: Number,
      default: 0
    }
  },
  {
    timestamps: true
  }
);

// Compound index for unique categories per user and type
categorySchema.index(
  { user: 1, name: 1, type: 1 },
  { unique: true }
);

// Index for ordering
categorySchema.index({ user: 1, order: 1 });

// Index for type filtering
categorySchema.index({ type: 1 });

const Category = mongoose.model<CategoryDocument>('Category', categorySchema);

export default Category; 