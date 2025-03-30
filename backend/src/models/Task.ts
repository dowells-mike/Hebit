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
    completedAt: {
      type: Date
    },
    priority: {
      type: String,
      enum: ['low', 'medium', 'high'],
      default: 'medium'
    },
    dueDate: {
      type: Date
    },
    status: {
      type: String,
      enum: ['todo', 'in_progress', 'completed', 'archived'],
      default: 'todo'
    },
    category: {
      type: String,
      ref: 'Category'
    },
    parentTaskId: {
      type: String,
      ref: 'Task'
    },
    tags: [
      {
        type: String
      }
    ],
    recurrence: {
      frequency: {
        type: String,
        enum: ['daily', 'weekly', 'monthly']
      },
      interval: {
        type: Number,
        min: 1
      },
      endDate: {
        type: Date
      }
    },
    reminderTime: {
      type: Date
    },
    effort: {
      type: Number,
      min: 1,
      max: 5
    },
    complexity: {
      type: Number,
      min: 1,
      max: 5
    },
    attachments: [
      {
        name: {
          type: String,
          required: true
        },
        url: {
          type: String,
          required: true
        },
        type: {
          type: String
        },
        size: {
          type: Number
        }
      }
    ],
    metadata: {
      contextualUrgency: {
        type: Number,
        min: 0,
        max: 100
      },
      estimatedDuration: {
        type: Number,
        min: 0
      },
      lastModifiedField: {
        type: String
      },
      completionContext: {
        location: {
          type: String
        },
        timeOfDay: {
          type: Number,
          min: 0,
          max: 23
        },
        dayOfWeek: {
          type: Number,
          min: 0,
          max: 6
        }
      }
    }
  },
  {
    timestamps: true
  }
);

// Index for faster queries
taskSchema.index({ user: 1, completed: 1 });
taskSchema.index({ dueDate: 1 });
taskSchema.index({ status: 1 });
taskSchema.index({ parentTaskId: 1 });

const Task = mongoose.model<TaskDocument>('Task', taskSchema);

export default Task;
