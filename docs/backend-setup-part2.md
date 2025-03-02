# Backend Setup (Continued)

## 5.6 Models (Continued)

Complete the `src/models/Goal.ts` file:

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
    },
    targetDate: {
      type: Date,
    },
    progress: {
      type: Number,
      default: 0,
      min: 0,
      max: 100,
    },
    status: {
      type: String,
      enum: ['not_started', 'in_progress', 'completed'],
      default: 'not_started',
    },
    relatedTasks: [
      {
        type: Schema.Types.ObjectId,
        ref: 'Task',
      },
    ],
    relatedHabits: [
      {
        type: Schema.Types.ObjectId,
        ref: 'Habit',
      },
    ],
  },
  {
    timestamps: true,
  }
);

const Goal = mongoose.model<GoalDocument>('Goal', goalSchema);

export default Goal;
```

### 5.7 Controllers

Create `src/controllers/authController.ts`:

```typescript
import { Request, Response } from 'express';
import jwt from 'jsonwebtoken';
import { asyncHandler } from '../utils/helpers';
import User from '../models/User';
import config from '../config/config';

// Generate JWT
const generateToken = (id: string) => {
  return jwt.sign({ id }, config.jwtSecret, {
    expiresIn: config.jwtExpiration,
  });
};

// @desc    Register a new user
// @route   POST /api/auth/register
// @access  Public
export const registerUser = asyncHandler(async (req: Request, res: Response) => {
  const { name, email, password } = req.body;

  // Check if user exists
  const userExists = await User.findOne({ email });

  if (userExists) {
    res.status(400);
    throw new Error('User already exists');
  }

  // Create user
  const user = await User.create({
    name,
    email,
    password,
  });

  if (user) {
    res.status(201).json({
      _id: user._id,
      name: user.name,
      email: user.email,
      token: generateToken(user._id),
    });
  } else {
    res.status(400);
    throw new Error('Invalid user data');
  }
});

// @desc    Authenticate a user
// @route   POST /api/auth/login
// @access  Public
export const loginUser = asyncHandler(async (req: Request, res: Response) => {
  const { email, password } = req.body;

  // Check for user email
  const user = await User.findOne({ email }).select('+password');

  if (user && (await user.comparePassword(password))) {
    res.json({
      _id: user._id,
      name: user.name,
      email: user.email,
      token: generateToken(user._id),
    });
  } else {
    res.status(401);
    throw new Error('Invalid email or password');
  }
});

// @desc    Get user profile
// @route   GET /api/auth/profile
// @access  Private
export const getUserProfile = asyncHandler(async (req: Request, res: Response) => {
  const user = await User.findById(req.user?._id);

  if (user) {
    res.json({
      _id: user._id,
      name: user.name,
      email: user.email,
    });
  } else {
    res.status(404);
    throw new Error('User not found');
  }
});
```

### 5.8 Routes

Create `src/routes/auth.ts`:

```typescript
import express from 'express';
import { registerUser, loginUser, getUserProfile } from '../controllers/authController';
import { protect } from '../middleware/auth';

const router = express.Router();

router.post('/register', registerUser);
router.post('/login', loginUser);
router.get('/profile', protect, getUserProfile);

export default router;
```

### 5.9 Server Setup

Create `src/server.ts`:

```typescript
import express from 'express';
import cors from 'cors';
import connectDB from './config/database';
import config from './config/config';
import logger from './utils/logger';
import errorHandler from './middleware/errorHandler';

// Route imports
import authRoutes from './routes/auth';

// Connect to MongoDB
connectDB();

const app = express();

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: false }));

// Routes
app.use('/api/auth', authRoutes);

// Health check route
app.get('/', (req, res) => {
  res.send('Hebit API is running');
});

// Error handler
app.use(errorHandler);

// Start server
const PORT = config.port;
app.listen(PORT, () => {
  logger.info(`Server running in ${config.env} mode on port ${PORT}`);
});

export default app;
```

## 6. Testing Setup

### 6.1 Jest Configuration

Create `jest.config.js` in the backend directory:

```javascript
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  testMatch: ['**/*.test.ts'],
  verbose: true,
  forceExit: true,
  clearMocks: true,
  resetMocks: true,
  restoreMocks: true,
};
```

### 6.2 Sample Test

Create `src/tests/auth.test.ts`:

```typescript
import request from 'supertest';
import mongoose from 'mongoose';
import app from '../server';
import User from '../models/User';

describe('Auth Endpoints', () => {
  beforeAll(async () => {
    // Clear users collection before tests
    await User.deleteMany({});
  });

  afterAll(async () => {
    // Disconnect from MongoDB after tests
    await mongoose.connection.close();
  });

  describe('POST /api/auth/register', () => {
    it('should register a new user', async () => {
      const res = await request(app)
        .post('/api/auth/register')
        .send({
          name: 'Test User',
          email: 'test@example.com',
          password: 'password123',
        });
      
      expect(res.statusCode).toEqual(201);
      expect(res.body).toHaveProperty('token');
      expect(res.body).toHaveProperty('_id');
      expect(res.body.name).toEqual('Test User');
      expect(res.body.email).toEqual('test@example.com');
    });

    it('should not register a user with existing email', async () => {
      const res = await request(app)
        .post('/api/auth/register')
        .send({
          name: 'Another User',
          email: 'test@example.com',
          password: 'password123',
        });
      
      expect(res.statusCode).toEqual(400);
      expect(res.body).toHaveProperty('message');
      expect(res.body.message).toEqual('User already exists');
    });
  });

  describe('POST /api/auth/login', () => {
    it('should login an existing user', async () => {
      const res = await request(app)
        .post('/api/auth/login')
        .send({
          email: 'test@example.com',
          password: 'password123',
        });
      
      expect(res.statusCode).toEqual(200);
      expect(res.body).toHaveProperty('token');
      expect(res.body.name).toEqual('Test User');
      expect(res.body.email).toEqual('test@example.com');
    });

    it('should not login with invalid credentials', async () => {
      const res = await request(app)
        .post('/api/auth/login')
        .send({
          email: 'test@example.com',
          password: 'wrongpassword',
        });
      
      expect(res.statusCode).toEqual(401);
      expect(res.body).toHaveProperty('message');
      expect(res.body.message).toEqual('Invalid email or password');
    });
  });
});
```

## 7. Next Steps

After setting up the basic structure and authentication system, you should:

1. **Implement Task Management**:
   - Create task CRUD operations in `taskController.ts`
   - Set up task routes in `routes/tasks.ts`
   - Add task routes to `server.ts`

2. **Implement Habit Tracking**:
   - Create habit CRUD operations in `habitController.ts`
   - Set up habit routes in `routes/habits.ts`
   - Add habit routes to `server.ts`

3. **Implement Goal Management**:
   - Create goal CRUD operations in `goalController.ts`
   - Set up goal routes in `routes/goals.ts`
   - Add goal routes to `server.ts`

4. **Implement ML Service**:
   - Create basic ML service in `services/mlService.ts`
   - Start with simple recommendation algorithms
   - Gradually implement more advanced ML features

5. **Test the API**:
   - Write tests for all endpoints
   - Test with Postman or similar tool
   - Ensure proper error handling

6. **Connect with Frontend**:
   - Set up CORS properly
   - Test API calls from the Android app
   - Implement authentication flow in the app

Remember to follow the Git workflow outlined in the git-setup-guide.md when implementing these features.
