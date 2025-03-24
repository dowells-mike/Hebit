import mongoose, { Schema } from 'mongoose';
import { TaskDocument } from '../types';

const taskSchema = new Schema<TaskDocument>(
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
    completed: {
      type: Boolean,
      default: false
    },
    priority: {
      type: String,
      enum: ['low', 'medium', 'high'],
      default: 'medium'
    },
    dueDate: {
      type: Date
    },
    category: {
      type: String
    },
    tags: [
      {
        type: String
      }
    ]
  },
  {
    timestamps: true
  }
);

// Index for faster queries
taskSchema.index({ user: 1, completed: 1 });
taskSchema.index({ dueDate: 1 });

const Task = mongoose.model<TaskDocument>('Task', taskSchema);

export default Task;
