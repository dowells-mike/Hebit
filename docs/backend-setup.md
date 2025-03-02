# Node.js Backend Setup for Hebit

## 1. Create Project Structure

Create the following directory structure for your backend:

```
backend/
├── src/
│   ├── config/
│   │   ├── database.ts
│   │   └── config.ts
│   ├── controllers/
│   │   ├── authController.ts
│   │   ├── taskController.ts
│   │   ├── habitController.ts
│   │   └── goalController.ts
│   ├── middleware/
│   │   ├── auth.ts
│   │   ├── errorHandler.ts
│   │   └── validator.ts
│   ├── models/
│   │   ├── User.ts
│   │   ├── Task.ts
│   │   ├── Habit.ts
│   │   └── Goal.ts
│   ├── routes/
│   │   ├── auth.ts
│   │   ├── tasks.ts
│   │   ├── habits.ts
│   │   └── goals.ts
│   ├── services/
│   │   ├── authService.ts
│   │   ├── taskService.ts
│   │   ├── habitService.ts
│   │   ├── goalService.ts
│   │   └── mlService.ts
│   ├── utils/
│   │   ├── helpers.ts
│   │   └── logger.ts
│   ├── types/
│   │   └── index.ts
│   └── server.ts
├── .env
├── .gitignore
├── package.json
└── tsconfig.json
```

## 2. Initialize the Project

Navigate to your backend directory and run:

```bash
# Create backend directory if not exists
mkdir -p backend
cd backend

# Initialize npm project
npm init -y

# Install core dependencies
npm install express typescript ts-node @types/node @types/express
npm install -D nodemon @types/nodemon

# Install additional dependencies
npm install cors dotenv mongoose bcryptjs jsonwebtoken
npm install -D @types/cors @types/bcryptjs @types/jsonwebtoken

# Install validation and utility libraries
npm install joi winston express-async-handler
npm install -D @types/joi

# Install testing libraries
npm install -D jest ts-jest @types/jest supertest @types/supertest
```

## 3. Configure TypeScript

Create `tsconfig.json` in the backend directory:

```json
{
  "compilerOptions": {
    "target": "es2020",
    "module": "commonjs",
    "lib": ["es2020"],
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "moduleResolution": "node",
    "resolveJsonModule": true,
    "outDir": "./dist",
    "rootDir": "./src",
    "typeRoots": ["./node_modules/@types", "./src/types"]
  },
  "include": ["src/**/*"],
  "exclude": ["node_modules", "**/*.spec.ts", "dist"]
}
```

## 4. Update package.json Scripts

Update your `package.json` scripts:

```json
{
  "scripts": {
    "start": "node dist/server.js",
    "dev": "nodemon src/server.ts",
    "build": "tsc",
    "test": "jest",
    "test:watch": "jest --watch",
    "lint": "eslint . --ext .ts"
  }
}
```

## 5. Create Initial Files

### 5.1 Environment Configuration

Create `.env` file:

```env
PORT=5000
MONGODB_URI=mongodb://localhost:27017/hebit
JWT_SECRET=your_jwt_secret_here
NODE_ENV=development
JWT_EXPIRATION=7d
```

Create `.gitignore` file:

```
node_modules/
dist/
.env
*.log
coverage/
```

### 5.2 Configuration Files

Create `src/config/config.ts`:

```typescript
import dotenv from 'dotenv';
import path from 'path';

// Load environment variables from .env file
dotenv.config();

const config = {
  env: process.env.NODE_ENV || 'development',
  port: process.env.PORT || 5000,
  mongoUri: process.env.MONGODB_URI || 'mongodb://localhost:27017/hebit',
  jwtSecret: process.env.JWT_SECRET || 'your_jwt_secret_here',
  jwtExpiration: process.env.JWT_EXPIRATION || '7d',
};

export default config;
```

Create `src/config/database.ts`:

```typescript
import mongoose from 'mongoose';
import config from './config';
import logger from '../utils/logger';

const connectDB = async (): Promise<void> => {
  try {
    const conn = await mongoose.connect(config.mongoUri);
    logger.info(`MongoDB Connected: ${conn.connection.host}`);
  } catch (error) {
    if (error instanceof Error) {
      logger.error(`Error: ${error.message}`);
    } else {
      logger.error('Unknown error occurred while connecting to MongoDB');
    }
    process.exit(1);
  }
};

export default connectDB;
```

### 5.3 Utility Files

Create `src/utils/logger.ts`:

```typescript
import winston from 'winston';
import config from '../config/config';

const levels = {
  error: 0,
  warn: 1,
  info: 2,
  http: 3,
  debug: 4,
};

const level = () => {
  const env = config.env || 'development';
  const isDevelopment = env === 'development';
  return isDevelopment ? 'debug' : 'warn';
};

const colors = {
  error: 'red',
  warn: 'yellow',
  info: 'green',
  http: 'magenta',
  debug: 'white',
};

winston.addColors(colors);

const format = winston.format.combine(
  winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss:ms' }),
  winston.format.colorize({ all: true }),
  winston.format.printf(
    (info) => `${info.timestamp} ${info.level}: ${info.message}`,
  ),
);

const transports = [
  new winston.transports.Console(),
  new winston.transports.File({
    filename: 'logs/error.log',
    level: 'error',
  }),
  new winston.transports.File({ filename: 'logs/all.log' }),
];

const logger = winston.createLogger({
  level: level(),
  levels,
  format,
  transports,
});

export default logger;
```

Create `src/utils/helpers.ts`:

```typescript
import { Request, Response, NextFunction } from 'express';

/**
 * Helper function to handle async/await errors
 */
export const asyncHandler = 
  (fn: (req: Request, res: Response, next: NextFunction) => Promise<any>) => 
  (req: Request, res: Response, next: NextFunction) => {
    Promise.resolve(fn(req, res, next)).catch(next);
  };

/**
 * Generate JWT token
 */
export const generateToken = (id: string): string => {
  // This will be implemented in the auth middleware
  return '';
};
```

### 5.4 Types

Create `src/types/index.ts`:

```typescript
import { Request } from 'express';

export interface UserDocument {
  _id: string;
  name: string;
  email: string;
  password: string;
  createdAt: Date;
  updatedAt: Date;
  comparePassword(candidatePassword: string): Promise<boolean>;
}

export interface TaskDocument {
  _id: string;
  user: string;
  title: string;
  description?: string;
  completed: boolean;
  priority: 'low' | 'medium' | 'high';
  dueDate?: Date;
  category?: string;
  tags?: string[];
  createdAt: Date;
  updatedAt: Date;
}

export interface HabitDocument {
  _id: string;
  user: string;
  title: string;
  description?: string;
  frequency: 'daily' | 'weekly' | 'monthly';
  timeOfDay?: string;
  daysOfWeek?: number[];
  streak: number;
  completionHistory: {
    date: Date;
    completed: boolean;
  }[];
  createdAt: Date;
  updatedAt: Date;
}

export interface GoalDocument {
  _id: string;
  user: string;
  title: string;
  description?: string;
  targetDate?: Date;
  progress: number;
  status: 'not_started' | 'in_progress' | 'completed';
  relatedTasks?: string[];
  relatedHabits?: string[];
  createdAt: Date;
  updatedAt: Date;
}

export interface AuthRequest extends Request {
  user?: UserDocument;
}
```

### 5.5 Middleware

Create `src/middleware/errorHandler.ts`:

```typescript
import { Request, Response, NextFunction } from 'express';
import logger from '../utils/logger';

interface AppError extends Error {
  statusCode?: number;
  kind?: string;
}

const errorHandler = (
  err: AppError,
  req: Request,
  res: Response,
  next: NextFunction
) => {
  const statusCode = err.statusCode || 500;
  
  // Log error
  logger.error(`${statusCode} - ${err.message} - ${req.originalUrl} - ${req.method} - ${req.ip}`);
  
  res.status(statusCode).json({
    message: err.message,
    stack: process.env.NODE_ENV === 'production' ? null : err.stack,
  });
};

export default errorHandler;
```

Create `src/middleware/auth.ts`:

```typescript
import { Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { asyncHandler } from '../utils/helpers';
import User from '../models/User';
import config from '../config/config';
import { AuthRequest } from '../types';

interface JwtPayload {
  id: string;
}

export const protect = asyncHandler(async (req: AuthRequest, res: Response, next: NextFunction) => {
  let token;

  if (
    req.headers.authorization &&
    req.headers.authorization.startsWith('Bearer')
  ) {
    try {
      // Get token from header
      token = req.headers.authorization.split(' ')[1];

      // Verify token
      const decoded = jwt.verify(token, config.jwtSecret) as JwtPayload;

      // Get user from the token
      req.user = await User.findById(decoded.id).select('-password');

      next();
    } catch (error) {
      res.status(401);
      throw new Error('Not authorized, token failed');
    }
  }

  if (!token) {
    res.status(401);
    throw new Error('Not authorized, no token');
  }
});
```

### 5.6 Models

Create `src/models/User.ts`:

```typescript
import mongoose, { Schema } from 'mongoose';
import bcrypt from 'bcryptjs';
import { UserDocument } from '../types';

const userSchema = new Schema<UserDocument>(
  {
    name: {
      type: String,
      required: [true, 'Please add a name'],
    },
    email: {
      type: String,
      required: [true, 'Please add an email'],
      unique: true,
      match: [
        /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/,
        'Please add a valid email',
      ],
    },
    password: {
      type: String,
      required: [true, 'Please add a password'],
      minlength: 6,
      select: false,
    },
  },
  {
    timestamps: true,
  }
);

// Encrypt password using bcrypt
userSchema.pre('save', async function (next) {
  if (!this.isModified('password')) {
    next();
  }

  const salt = await bcrypt.genSalt(10);
  this.password = await bcrypt.hash(this.password, salt);
});

// Match user entered password to hashed password in database
userSchema.methods.comparePassword = async function (enteredPassword: string) {
  return await bcrypt.compare(enteredPassword, this.password);
};

const User = mongoose.model<UserDocument>('User', userSchema);

export default User;
```

Create `src/models/Task.ts`:

```typescript
import mongoose, { Schema } from 'mongoose';
import { TaskDocument } from '../types';

const taskSchema = new Schema<TaskDocument>(
  {
    user: {
      type: Schema.Types.ObjectId,
      required: true,
      ref: 'User',
    },
    title: {
      type: String,
      required: [true, 'Please add a title'],
      trim: true,
    },
    description: {
      type: String,
      trim: true,
    },
    completed: {
      type: Boolean,
      default: false,
    },
    priority: {
      type: String,
      enum: ['low', 'medium', 'high'],
      default: 'medium',
    },
    dueDate: {
      type: Date,
    },
    category: {
      type: String,
    },
    tags: [
      {
        type: String,
      },
    ],
  },
  {
    timestamps: true,
  }
);

const Task = mongoose.model<TaskDocument>('Task', taskSchema);

export default Task;
```

Create `src/models/Habit.ts`:

```typescript
import mongoose, { Schema } from 'mongoose';
import { HabitDocument } from '../types';

const habitSchema = new Schema<HabitDocument>(
  {
    user: {
      type: Schema.Types.ObjectId,
      required: true,
      ref: 'User',
    },
    title: {
      type: String,
      required: [true, 'Please add a title'],
      trim: true,
    },
    description: {
      type: String,
      trim: true,
    },
    frequency: {
      type: String,
      enum: ['daily', 'weekly', 'monthly'],
      default: 'daily',
    },
    timeOfDay: {
      type: String,
    },
    daysOfWeek: [
      {
        type: Number,
        min: 0,
        max: 6,
      },
    ],
    streak: {
      type: Number,
      default: 0,
    },
    completionHistory: [
      {
        date: {
          type: Date,
          required: true,
        },
        completed: {
          type: Boolean,
          required: true,
        },
      },
    ],
  },
  {
    timestamps: true,
  }
);

const Habit = mongoose.model<HabitDocument>('Habit', habitSchema);

export default Habit;
```

Create `src/models/Goal.ts`:

```typescript
import mongoose, { Schema } from 'mongoose';
import { GoalDocument } from '../types';

const goalSchema = new Schema<GoalDocument>(
  {
    user: {
      type: Schema.Types.ObjectId,
      required: true,
      ref: 'User',
    },
    title: {
      type: String,
      required: [true, 'Please add a title'],
      trim: true,
    },
    description: {
      type: String,
      trim: true,
