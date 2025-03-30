import mongoose, { Schema, Document } from 'mongoose';
import bcrypt from 'bcryptjs';
import { UserDocument } from '../types';

const userSchema = new Schema<UserDocument>(
  {
    name: {
      type: String,
      required: [true, 'Please add a name'],
      trim: true
    },
    email: {
      type: String,
      required: [true, 'Please add an email'],
      unique: true,
      trim: true,
      lowercase: true,
      match: [
        /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/,
        'Please add a valid email'
      ]
    },
    password: {
      type: String,
      required: [true, 'Please add a password'],
      minlength: [6, 'Password must be at least 6 characters'],
      select: false
    },
    username: {
      type: String,
      trim: true,
      sparse: true,
      unique: true
    },
    bio: {
      type: String,
      maxlength: [300, 'Bio cannot be more than 300 characters']
    },
    avatarUrl: {
      type: String
    },
    coverPhotoUrl: {
      type: String
    },
    timezone: {
      type: String,
      default: 'UTC'
    },
    authProviders: {
      email: {
        type: Boolean,
        default: true
      },
      google: {
        type: Boolean,
        default: false
      },
      biometric: {
        type: Boolean,
        default: false
      }
    },
    settings: {
      theme: {
        type: String,
        enum: ['light', 'dark', 'system'],
        default: 'system'
      },
      startScreen: {
        type: String,
        default: 'dashboard'
      },
      notificationPreferences: {
        tasks: {
          type: Boolean,
          default: true
        },
        habits: {
          type: Boolean,
          default: true
        },
        goals: {
          type: Boolean,
          default: true
        },
        system: {
          type: Boolean,
          default: true
        }
      },
      privacySettings: {
        shareActivity: {
          type: Boolean,
          default: false
        },
        allowSuggestions: {
          type: Boolean,
          default: true
        }
      }
    },
    productivity: {
      peakHours: [Number],
      preferredWorkDays: [Number],
      focusDuration: {
        type: Number,
        default: 0
      },
      completionRate: {
        type: Number,
        default: 0
      }
    },
    isAdmin: {
      type: Boolean,
      default: false
    },
    lastLogin: {
      type: Date,
      default: Date.now
    }
  },
  {
    timestamps: true
  }
);

// Encrypt password using bcrypt
userSchema.pre('save', async function(next) {
  if (!this.isModified('password')) {
    next();
  }

  const salt = await bcrypt.genSalt(10);
  this.password = await bcrypt.hash(this.password, salt);
});

// Match user entered password to hashed password in database
userSchema.methods.comparePassword = async function(enteredPassword: string): Promise<boolean> {
  return await bcrypt.compare(enteredPassword, this.password);
};

const User = mongoose.model<UserDocument>('User', userSchema);

export default User;
