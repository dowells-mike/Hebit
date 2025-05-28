import { describe, it, expect, beforeEach, afterEach, jest } from '@jest/globals';
import { AchievementService } from '../../../src/services/achievementService';
import eventEmitter from '../../../src/services/eventEmitter';
import { Achievement, UserAchievement, User } from '../../../src/models';
import { AchievementDocument, UserAchievementDocument, UserDocument } from '../../../src/types';

// Local interface definitions for test data clarity, mirroring those in AchievementService
interface StreakEventData {
  userId: string;
  habitId: string;
  streakData?: { current: number; longest: number; lastCompleted?: Date };
  habitTitle?: string;
  frequency?: 'daily' | 'weekly' | 'monthly' | 'specific_dates';
}

// Added local definition for HabitCompletedAtEventData
interface HabitCompletedAtEventData {
  userId: string;
  habitId: string;
  habitTitle?: string;
  completedAt: Date;
  frequency?: 'daily' | 'weekly' | 'monthly' | 'specific_dates';
  category?: string;
}

// Added local definition for TaskCompletedEventData
interface TaskCompletedEventData {
  userId: string;
  taskId: string;
  taskTitle?: string;
  completedAt: Date;
  category?: string;
  priority?: 'low' | 'medium' | 'high';
  tags?: string[];
  effort?: number;
  complexity?: number;
  description?: string;
  dueDate?: Date;
}

// Added local definition for UserSignupEventData
interface UserSignupEventData {
  userId: string;
  username?: string;
  email?: string;
}

// Added local definition for GoalCompletedEventData
interface GoalCompletedEventData {
  userId: string;
  goalId: string;
  goalTitle?: string;
  completedAt: Date;
  category?: string;
  targetDate?: Date;
  status?: string;
  measurementType?: 'numeric' | 'boolean' | 'checklist';
}

interface GenericEventPayload {
  userId: string;
  eventType: string;
  entityId?: string;
  data?: any;
}

// Mock the models
jest.mock('../../../src/models', () => ({
  Achievement: {
    find: jest.fn(),
  },
  UserAchievement: {
    findOne: jest.fn(),
    create: jest.fn(),
    findOneAndUpdate: jest.fn(),
  },
  User: {
    findById: jest.fn(),
    findByIdAndUpdate: jest.fn(),
  },
}));

// Mock the eventEmitter
jest.mock('../../../src/services/eventEmitter', () => ({
  on: jest.fn(),      // To verify constructor subscriptions
  emit: jest.fn(),    // To verify achievement events are emitted
  // We don't need to mock off, removeListener, etc. unless service uses them
}));

// Define a type for our mocked models to help with Jest mock function typing
type MockedAchievement = jest.Mocked<typeof Achievement>;
type MockedUserAchievement = jest.Mocked<typeof UserAchievement>;
type MockedUser = jest.Mocked<typeof User>;

const mockAchievementModel = Achievement as MockedAchievement;
const mockUserAchievementModel = UserAchievement as MockedUserAchievement;
const mockUserModel = User as MockedUser;

describe('AchievementService Unit Tests', () => {
  let achievementService: AchievementService;
  let leanMock: jest.Mock;

  beforeEach(() => {
    // Reset mocks before each test
    jest.clearAllMocks();
    
    // Global mock for Achievement.find().lean()
    leanMock = jest.fn().mockImplementation(() => Promise.resolve([] as AchievementDocument[]));
    (mockAchievementModel.find as jest.Mock).mockImplementation(() => ({
      lean: leanMock
    }));
    
    // Instantiate the service. Event listeners are set up in the constructor.
    achievementService = new AchievementService();
  });

  describe('Constructor and Event Subscription', () => {
    it('should subscribe to relevant events on construction', () => {
      // AchievementService is instantiated in beforeEach, so listeners are already set.
      expect(eventEmitter.on).toHaveBeenCalledWith('HABIT_STREAK_UPDATED', expect.any(Function));
      expect(eventEmitter.on).toHaveBeenCalledWith('HABIT_COMPLETED_AT', expect.any(Function));
      expect(eventEmitter.on).toHaveBeenCalledWith('TASK_COMPLETED', expect.any(Function));
      expect(eventEmitter.on).toHaveBeenCalledWith('USER_SIGNUP', expect.any(Function));
      expect(eventEmitter.on).toHaveBeenCalledWith('GOAL_COMPLETED', expect.any(Function));
      expect(eventEmitter.on).toHaveBeenCalledTimes(5);
    });
  });

  describe('handleHabitStreakUpdated', () => {
    it('should transform StreakEventData and call processEvent', async () => {
      const processEventSpy = jest.spyOn(achievementService as any, 'processEvent');
      
      const streakEventData: StreakEventData = {
        userId: 'user123',
        habitId: 'habit456',
        streakData: { current: 5, longest: 10 },
        habitTitle: 'Morning Run',
        frequency: 'daily',
      };

      // Directly call the private method for testing (common in unit tests for private methods called by public ones)
      // Or, if we want to test it via eventEmitter.emit, we need to get the bound function.
      // For simplicity, let's call it directly. It's a private method, but its role is to transform and delegate.
      await (achievementService as any).handleHabitStreakUpdated(streakEventData);

      const expectedPayload: GenericEventPayload = {
        userId: 'user123',
        eventType: 'HABIT_STREAK_UPDATED',
        entityId: 'habit456',
        data: {
          streakData: { current: 5, longest: 10 },
          habitTitle: 'Morning Run',
          frequency: 'daily',
          entityType: 'habit'
        }
      };

      expect(processEventSpy).toHaveBeenCalledTimes(1);
      expect(processEventSpy).toHaveBeenCalledWith(expectedPayload);
      
      processEventSpy.mockRestore(); // Clean up spy
    });
  });

  describe('handleHabitCompletedAt', () => {
    it('should transform HabitCompletedAtEventData and call processEvent', async () => {
      const processEventSpy = jest.spyOn(achievementService as any, 'processEvent');
      
      const completedAtDate = new Date();
      const eventData: HabitCompletedAtEventData = {
        userId: 'user789',
        habitId: 'habit012',
        habitTitle: 'Evening Meditation',
        completedAt: completedAtDate,
        frequency: 'daily',
        category: 'Mindfulness'
      };

      await (achievementService as any).handleHabitCompletedAt(eventData);

      const expectedPayload: GenericEventPayload = {
        userId: 'user789',
        eventType: 'HABIT_COMPLETED_AT',
        entityId: 'habit012',
        data: {
          completedAt: completedAtDate,
          habitTitle: 'Evening Meditation',
          frequency: 'daily',
          category: 'Mindfulness',
          entityType: 'habit'
        }
      };

      expect(processEventSpy).toHaveBeenCalledTimes(1);
      expect(processEventSpy).toHaveBeenCalledWith(expectedPayload);
      processEventSpy.mockRestore();
    });
  });

  describe('handleTaskCompleted', () => {
    it('should transform TaskCompletedEventData and call processEvent', async () => {
      const processEventSpy = jest.spyOn(achievementService as any, 'processEvent');
      const completedAtDate = new Date();
      const dueDate = new Date(Date.now() + 24 * 60 * 60 * 1000); // Tomorrow

      const eventData: TaskCompletedEventData = {
        userId: 'userTask1',
        taskId: 'taskAbc',
        taskTitle: 'Finish report',
        completedAt: completedAtDate,
        category: 'Work',
        priority: 'high',
        tags: ['urgent', 'client'],
        effort: 3,
        complexity: 5,
        description: 'Finalize Q3 client report',
        dueDate: dueDate,
      };

      await (achievementService as any).handleTaskCompleted(eventData);

      const expectedPayload: GenericEventPayload = {
        userId: 'userTask1',
        eventType: 'TASK_COMPLETED',
        entityId: 'taskAbc',
        data: {
          taskTitle: 'Finish report',
          completedAt: completedAtDate,
          category: 'Work',
          priority: 'high',
          tags: ['urgent', 'client'],
          effort: 3,
          complexity: 5,
          description: 'Finalize Q3 client report',
          dueDate: dueDate,
          entityType: 'task'
        }
      };

      expect(processEventSpy).toHaveBeenCalledTimes(1);
      expect(processEventSpy).toHaveBeenCalledWith(expectedPayload);
      processEventSpy.mockRestore();
    });
  });

  describe('handleUserSignup', () => {
    it('should transform UserSignupEventData and call processEvent', async () => {
      const processEventSpy = jest.spyOn(achievementService as any, 'processEvent');
      
      const eventData: UserSignupEventData = {
        userId: 'newUser777',
        username: 'TestUser',
        email: 'testuser@example.com'
      };

      await (achievementService as any).handleUserSignup(eventData);

      const expectedPayload: GenericEventPayload = {
        userId: 'newUser777',
        eventType: 'USER_SIGNUP',
        // No entityId for USER_SIGNUP as per service logic
        data: {
          username: 'TestUser',
          email: 'testuser@example.com',
          entityType: 'user' 
        }
      };

      expect(processEventSpy).toHaveBeenCalledTimes(1);
      expect(processEventSpy).toHaveBeenCalledWith(expectedPayload);
      processEventSpy.mockRestore();
    });
  });

  describe('handleGoalCompleted', () => {
    it('should transform GoalCompletedEventData and call processEvent', async () => {
      const processEventSpy = jest.spyOn(achievementService as any, 'processEvent');
      const completedAtDate = new Date();
      const targetDate = new Date(Date.now() + 5 * 24 * 60 * 60 * 1000);

      const eventData: GoalCompletedEventData = {
        userId: 'userGoal1',
        goalId: 'goalXyz',
        goalTitle: 'Learn new skill',
        completedAt: completedAtDate,
        category: 'Personal Development',
        targetDate: targetDate,
        status: 'completed',
        measurementType: 'checklist'
      };

      await (achievementService as any).handleGoalCompleted(eventData);

      const expectedPayload: GenericEventPayload = {
        userId: 'userGoal1',
        eventType: 'GOAL_COMPLETED',
        entityId: 'goalXyz',
        data: {
          goalTitle: 'Learn new skill',
          completedAt: completedAtDate,
          category: 'Personal Development',
          targetDate: targetDate,
          status: 'completed',
          measurementType: 'checklist',
          entityType: 'goal'
        }
      };

      expect(processEventSpy).toHaveBeenCalledTimes(1);
      expect(processEventSpy).toHaveBeenCalledWith(expectedPayload);
      processEventSpy.mockRestore();
    });
  });

  describe('processEvent', () => {
    // Tests for processEvent will go here. 
    // It will call findRelevantAchievements and evaluateAchievementForUser
    it('should call findRelevantAchievements and then evaluateAchievementForUser for each relevant achievement', async () => {
      const payload: GenericEventPayload = {
        userId: 'user1',
        eventType: 'TEST_EVENT',
        entityId: 'entity1',
        data: { some: 'data' }
      };

      const ach1 = { _id: 'ach1', name: 'Achievement 1' } as AchievementDocument;
      const ach2 = { _id: 'ach2', name: 'Achievement 2' } as AchievementDocument;
      const relevantAchievements = [ach1, ach2];

      const findRelevantAchievementsSpy = jest.spyOn(achievementService as any, 'findRelevantAchievements').mockResolvedValue(relevantAchievements);
      const evaluateAchievementForUserSpy = jest.spyOn(achievementService as any, 'evaluateAchievementForUser').mockResolvedValue(undefined);

      await achievementService.processEvent(payload);

      expect(findRelevantAchievementsSpy).toHaveBeenCalledTimes(1);
      expect(findRelevantAchievementsSpy).toHaveBeenCalledWith(payload.eventType, payload.entityId, payload.data);

      expect(evaluateAchievementForUserSpy).toHaveBeenCalledTimes(2);
      expect(evaluateAchievementForUserSpy).toHaveBeenCalledWith(payload.userId, ach1, payload);
      expect(evaluateAchievementForUserSpy).toHaveBeenCalledWith(payload.userId, ach2, payload);

      findRelevantAchievementsSpy.mockRestore();
      evaluateAchievementForUserSpy.mockRestore();
    });

    it('should not call evaluateAchievementForUser if no relevant achievements are found', async () => {
      const payload: GenericEventPayload = {
        userId: 'user1',
        eventType: 'TEST_EVENT_NO_ACHIEVEMENTS',
      };

      const findRelevantAchievementsSpy = jest.spyOn(achievementService as any, 'findRelevantAchievements').mockResolvedValue([]);
      const evaluateAchievementForUserSpy = jest.spyOn(achievementService as any, 'evaluateAchievementForUser');

      await achievementService.processEvent(payload);

      expect(findRelevantAchievementsSpy).toHaveBeenCalledTimes(1);
      expect(evaluateAchievementForUserSpy).not.toHaveBeenCalled();

      findRelevantAchievementsSpy.mockRestore();
    });
  });

  describe('findRelevantAchievements', () => {
    let mockExistingUserAchievement: UserAchievementDocument | null;

    beforeEach(() => {
      // Reset specific model mocks and eventEmitter.emit before each test in this suite
      mockUserAchievementModel.findOne.mockReset();
      mockUserAchievementModel.create.mockReset();
      mockUserAchievementModel.findOneAndUpdate.mockReset();
      mockUserModel.findByIdAndUpdate.mockReset();
      mockUserModel.findById.mockReset();
      (eventEmitter.emit as jest.Mock).mockReset();

      mockExistingUserAchievement = null;
    });

    it('should build correct query for HABIT_STREAK_UPDATED with entityId', async () => {
      await (achievementService as any).findRelevantAchievements('HABIT_STREAK_UPDATED', 'habit123');
      expect(mockAchievementModel.find).toHaveBeenCalledWith({
        'criteria.type': 'streak',
        'criteria.source': 'habits',
        '$or': [
          { 'criteria.conditionDetails.relatedEntityId': 'habit123' },
          { 'criteria.conditionDetails.relatedEntityId': { $exists: false } },
          { 'criteria.conditionDetails.relatedEntityId': null }
        ]
      });
      expect(leanMock).toHaveBeenCalledTimes(1);
    });

    it('should build correct query for HABIT_STREAK_UPDATED without entityId (generic streak)', async () => {
      await (achievementService as any).findRelevantAchievements('HABIT_STREAK_UPDATED');
      expect(mockAchievementModel.find).toHaveBeenCalledWith({
        'criteria.type': 'streak',
        'criteria.source': 'habits',
        'criteria.conditionDetails.relatedEntityId': { $exists: false }
      });
      expect(leanMock).toHaveBeenCalledTimes(1);
    });

    it('should build correct query for HABIT_COMPLETED_AT', async () => {
      await (achievementService as any).findRelevantAchievements('HABIT_COMPLETED_AT', 'habit123');
      expect(mockAchievementModel.find).toHaveBeenCalledWith({
        'criteria.source': 'habits',
        '$or': [
          { 'criteria.type': 'count' }, 
          { 'criteria.type': 'completion_time' } 
        ]
      });
      expect(leanMock).toHaveBeenCalledTimes(1);
    });

    it('should build correct query for TASK_COMPLETED', async () => {
      await (achievementService as any).findRelevantAchievements('TASK_COMPLETED', 'task123');
      expect(mockAchievementModel.find).toHaveBeenCalledWith({
        'criteria.source': 'tasks',
        '$or': [
          { 'criteria.type': 'count' },
          { 'criteria.type': 'completion_time' }
        ]
      });
      expect(leanMock).toHaveBeenCalledTimes(1);
    });

    it('should build correct query for USER_SIGNUP', async () => {
      await (achievementService as any).findRelevantAchievements('USER_SIGNUP');
      expect(mockAchievementModel.find).toHaveBeenCalledWith({
        'criteria.type': 'event_based',
        'criteria.conditionDetails.eventName': 'USER_SIGNUP'
      });
      expect(leanMock).toHaveBeenCalledTimes(1);
    });

    it('should build correct query for GOAL_COMPLETED', async () => {
      await (achievementService as any).findRelevantAchievements('GOAL_COMPLETED', 'goal123');
      expect(mockAchievementModel.find).toHaveBeenCalledWith({
        'criteria.source': 'goals',
        '$or': [
          { 'criteria.type': 'count' },
          { 'criteria.type': 'event_based' },
          { 'criteria.type': 'completion_time' }
        ]
      });
      expect(leanMock).toHaveBeenCalledTimes(1);
    });
  });

  describe('matchConditionDetails', () => {
    const callMatchConditionDetails = (achDetails?: any, eventDetails?: any) => {
      return (achievementService as any).matchConditionDetails(achDetails, eventDetails);
    };

    it('should return true if achievementCondDetails is undefined or null', () => {
      expect(callMatchConditionDetails(undefined, { some: 'data' })).toBe(true);
      expect(callMatchConditionDetails(null, { some: 'data' })).toBe(true);
    });

    it('should return true if achievementCondDetails is an empty object', () => {
      expect(callMatchConditionDetails({}, { some: 'data' })).toBe(true);
    });

    it('should return true if all properties in achievementCondDetails match eventData', () => {
      const achDetails = { entityType: 'task', category: 'Work' };
      const eventData = { entityType: 'task', category: 'Work', priority: 'high' };
      expect(callMatchConditionDetails(achDetails, eventData)).toBe(true);
    });

    it('should return false if a property in achievementCondDetails does not match eventData', () => {
      const achDetails = { entityType: 'task', category: 'Personal' };
      const eventData = { entityType: 'task', category: 'Work', priority: 'high' };
      expect(callMatchConditionDetails(achDetails, eventData)).toBe(false);
    });

    it('should return false if a property in achievementCondDetails is not present in eventData', () => {
      const achDetails = { entityType: 'task', specificTag: 'urgent' };
      const eventData = { entityType: 'task', category: 'Work' };
      expect(callMatchConditionDetails(achDetails, eventData)).toBe(false);
    });
    
    it('should return true for matching nested properties (if service logic supported it - current is shallow)', () => {
      // Current matchConditionDetails is shallow. This test assumes future or current shallow behavior.
      const achDetails = { details: { subKey: 'value'}};
      const eventData = { details: { subKey: 'value'}, other: 'data'};
      // For shallow comparison, this would be false as objects are different references.
      // If deep comparison was intended for sub-objects, this test would change.
      // Based on current simple loop, it will compare `achDetails.details` with `eventData.details` by reference.
      // Let's test the current behavior which is a shallow check.
      // If achDetails.details was a primitive, it would work. With objects, it's by reference.
      // The implementation `if (eventData[key] !== achievementCondDetails[key])` performs shallow compare.
      expect(callMatchConditionDetails({prop:1}, {prop:1})).toBe(true); // This should pass
      // For objects, direct comparison is by reference
      const objVal = { subKey: 'val' };
      expect(callMatchConditionDetails({ details: objVal }, { details: objVal })).toBe(true); // Same reference
      expect(callMatchConditionDetails({ details: { subKey: 'val' } }, { details: { subKey: 'val'} })).toBe(false); // Different references
    });

    it('should handle cases where eventData is undefined or null', () => {
        const achDetails = { entityType: 'task' };
        expect(callMatchConditionDetails(achDetails, undefined)).toBe(false);
        expect(callMatchConditionDetails(achDetails, null)).toBe(false);
    });

    it('should return true when eventData has more properties than achievementCondDetails, but matching ones align', () => {
        const achDetails = { entityType: 'habit' };
        const eventData = { entityType: 'habit', frequency: 'daily', habitTitle: 'Exercise' };
        expect(callMatchConditionDetails(achDetails, eventData)).toBe(true);
    });
  });

  describe('evaluateAchievementForUser', () => {
    let mockUserId: string;
    let mockAchievement: AchievementDocument;
    let mockEventPayload: GenericEventPayload;
    let mockExistingUserAchievement: UserAchievementDocument | null;

    beforeEach(() => {
      // Reset specific model mocks and eventEmitter.emit before each test in this suite
      mockUserAchievementModel.findOne.mockReset();
      mockUserAchievementModel.create.mockReset();
      mockUserAchievementModel.findOneAndUpdate.mockReset();
      mockUserModel.findByIdAndUpdate.mockReset();
      mockUserModel.findById.mockReset();
      (eventEmitter.emit as jest.Mock).mockReset();

      mockUserId = 'userEval123';
      // Basic achievement structure, customize per test
      mockAchievement = {
        _id: 'achTestId1',
        name: 'Test Achievement',
        description: '...',
        category: 'special',
        points: 10,
        icon: 'icon.png',
        criteria: { type: 'event_based', targetValue: 1 }, // Default, override in tests
        rarity: 'common',
        secret: false,
        // other fields from AchievementDocument if needed by the function
      } as AchievementDocument;

      mockEventPayload = {
        userId: mockUserId,
        eventType: 'USER_SIGNUP', // Default, override in tests
        data: {}
      };
      mockExistingUserAchievement = null; // Default to no existing user achievement

      // Setup default mock implementations
      (mockUserAchievementModel.findOne as jest.Mock).mockImplementation(() => ({
        exec: jest.fn().mockImplementation(() => Promise.resolve(mockExistingUserAchievement))
      }));

      (mockUserAchievementModel.create as jest.MockedFunction<typeof UserAchievement.create>).mockImplementation(
        async (docs: ReadonlyArray<Partial<UserAchievementDocument>> | Partial<UserAchievementDocument>) => {
        const firstDocData = Array.isArray(docs) ? docs[0] : docs;
        let newDoc: UserAchievementDocument;
        newDoc = {
          _id: 'newUserAchId-' + Date.now(),
          earned: firstDocData.earned !== undefined ? firstDocData.earned : false,
          progress: firstDocData.progress || 0,
          seenByUser: firstDocData.seenByUser !== undefined ? firstDocData.seenByUser : false,
          createdAt: new Date(),
          updatedAt: new Date(),
          user: firstDocData.user || mockUserId,
          achievement: firstDocData.achievement || (mockAchievement ? mockAchievement._id : 'defaultAchId'),
          ...firstDocData,
          isModified: jest.fn().mockReturnValue(false),
          save: jest.fn().mockImplementation(async function() { return newDoc; }),
        } as unknown as UserAchievementDocument;
        return Promise.resolve(Array.isArray(docs) ? [newDoc] : newDoc) as any;
      });
      
      (mockUserAchievementModel.findOneAndUpdate as jest.Mock).mockImplementation(() => ({
        exec: jest.fn().mockImplementation(() => {
            const calls = (mockUserAchievementModel.findOneAndUpdate as jest.Mock).mock.calls;
            if (calls.length > 0) {
                const lastCallArgs = calls[calls.length - 1];
                const queryArg = lastCallArgs[0] as { user: string; achievement: string; };
                const updateArg = lastCallArgs[1] as { $set?: Partial<UserAchievementDocument>; $inc?: { progress?: number } } | Partial<UserAchievementDocument>;
                
                const baseDocData = mockExistingUserAchievement || { 
                    _id: 'defaultId-' + queryArg.achievement, 
                    user: queryArg.user, 
                    achievement: queryArg.achievement, 
                    progress: 0, 
                    earned: false, 
                    seenByUser: false, 
                    createdAt: new Date(), 
                    updatedAt: new Date() 
                };
                const baseDoc = {
                    ...baseDocData,
                    isModified: jest.fn().mockReturnValue(false),
                    save: jest.fn().mockImplementation(async function() { return this; })
                } as UserAchievementDocument;

                let updatedProgress = baseDoc.progress || 0;
                let earnedStatus = baseDoc.earned || false;
                let earnedAtTime = baseDoc.earnedAt;

                if ('$inc' in updateArg && typeof updateArg.$inc?.progress === 'number') {
                    updatedProgress += updateArg.$inc.progress;
                }
                if ('$set' in updateArg && updateArg.$set) {
                    if (updateArg.$set.progress !== undefined) updatedProgress = updateArg.$set.progress;
                    if (updateArg.$set.earned !== undefined) earnedStatus = updateArg.$set.earned;
                    if (updateArg.$set.earnedAt !== undefined) earnedAtTime = updateArg.$set.earnedAt;
                    if (updateArg.$set.seenByUser !== undefined) baseDoc.seenByUser = updateArg.$set.seenByUser;
                }
                let resultDoc: UserAchievementDocument;
                resultDoc = { 
                    ...(baseDoc as UserAchievementDocument),
                    _id: baseDoc._id,
                    user: baseDoc.user,
                    achievement: baseDoc.achievement,
                    ...('$set' in updateArg && updateArg.$set ? updateArg.$set : {}),
                    progress: updatedProgress,
                    earned: earnedStatus,
                    earnedAt: earnedAtTime,
                    isModified: jest.fn().mockReturnValue(true),
                    save: jest.fn().mockImplementation(async function() { return resultDoc; }),
                 } as unknown as UserAchievementDocument;
                 return Promise.resolve(resultDoc);
            }
            return Promise.resolve(null);
        })
    }));

      (mockUserModel.findByIdAndUpdate as jest.MockedFunction<typeof User.findByIdAndUpdate>).mockImplementation((filter: any, update: any) => (
        {
          exec: jest.fn().mockImplementation(async () => {
            let currentPoints = 0;
            if (typeof filter === 'string') {
            } else if (filter && filter._id) {
            }

            if (update && update.$inc && typeof update.$inc.experiencePoints === 'number') {
                currentPoints += update.$inc.experiencePoints;
            }
            
            let userDocInstance: UserDocument; // Declare for capturing
            userDocInstance = {
                _id: typeof filter === 'string' ? filter : filter?._id || mockUserId, 
                name: 'Mock User',
                email: 'mockuser@example.com',
                password: 'mockPassword',
                experiencePoints: currentPoints,
                username: 'mockUser',
                bio: 'Mock bio',
                avatarUrl: 'mockAvatar.png',
                coverPhotoUrl: 'mockCover.png',
                timezone: 'UTC', 
                authProviders: { email: true, google: false, biometric: false },
                settings: { 
                  theme: 'dark',
                  startScreen: 'dashboard',
                  notificationPreferences: { tasks: true, habits: true, goals: true, system: true },
                  privacySettings: { shareActivity: false, allowSuggestions: true },
                },
                productivity: { 
                    peakHours: { '10': 1, '14': 1}, 
                    peakDays: { '1': 1, '3': 1}, 
                    preferredWorkDays: [1,2,3,4,5],
                    focusDuration: 25,
                    completionRate: 0,
                    taskCompletionCount: 0,
                    mlPreferences: { enableRecommendations: true, enablePredictions: true, dataCollectionLevel: 'standard' }
                },
                isAdmin: false, 
                lastLogin: new Date(),
                comparePassword: jest.fn().mockImplementation(() => Promise.resolve(true)), 
                createdAt: new Date(),
                updatedAt: new Date(),
                save: jest.fn().mockImplementation(async function() { return userDocInstance; }), // Use the instance
                isModified: jest.fn().mockReturnValue(false),
              } as UserDocument;
            return Promise.resolve(userDocInstance);
          })
        } as any
      ));
      
      // Mock for User.findById
      (mockUserModel.findById as jest.MockedFunction<typeof User.findById>).mockImplementation((id: string) => (
        {
          exec: jest.fn().mockImplementation(async () => {
             let userDocInstance: UserDocument; // Declare for capturing
             userDocInstance = {
                _id: id,
                name: 'Mock User',
                email: 'mockuser@example.com',
                password: 'mockPassword',
                experiencePoints: 0, 
                username: 'mockUser',
                save: jest.fn().mockImplementation(async function() { return this; }), // Use the instance
                isModified: jest.fn().mockReturnValue(false),
                comparePassword: jest.fn().mockImplementation(() => Promise.resolve(true)),
                createdAt: new Date(),
                updatedAt: new Date(),
            } as unknown as UserDocument;
            return Promise.resolve(userDocInstance);
        })
        } as any
      ));
    });

    describe("criteria.type = 'event_based'", () => {
      it('should unlock achievement if eventType matches criteria.conditionDetails.eventName and not already earned', async () => {
        mockAchievement.criteria = {
          type: 'event_based',
          targetValue: 1, // Typically 1 for event_based unless it's a count of events
          conditionDetails: { eventName: 'USER_SIGNUP' }
        };
        mockEventPayload.eventType = 'USER_SIGNUP';
        mockExistingUserAchievement = null; // No prior record

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);

        expect(mockUserAchievementModel.create).toHaveBeenCalledTimes(1);
        const createdData = (mockUserAchievementModel.create as jest.Mock).mock.calls[0][0] as Partial<UserAchievementDocument>;
        expect(createdData.user).toBe(mockUserId);
        expect(createdData.achievement).toBe(mockAchievement._id);
        expect(createdData.progress).toBe(100); // Event based are 0 to 100
        expect(createdData.earned).toBe(true);
        expect(createdData.earnedAt).toBeInstanceOf(Date);
        
        expect(eventEmitter.emit).toHaveBeenCalledWith('ACHIEVEMENT_UNLOCKED', expect.objectContaining({
          userId: mockUserId,
          achievementId: mockAchievement._id,
          achievementName: mockAchievement.name
        }));
        expect(mockUserModel.findByIdAndUpdate).toHaveBeenCalledWith(mockUserId, { $inc: { experiencePoints: mockAchievement.points } });
      });

      it('should do nothing if eventType matches but achievement already earned', async () => {
        mockAchievement.criteria = {
          type: 'event_based',
          targetValue: 1,
          conditionDetails: { eventName: 'USER_SIGNUP' }
        };
        mockEventPayload.eventType = 'USER_SIGNUP';
        mockExistingUserAchievement = {
          _id: 'ua1',
          user: mockUserId,
          achievement: mockAchievement._id,
          progress: 100,
          earned: true,
          earnedAt: new Date(),
          seenByUser: false,
          createdAt: new Date(),
          updatedAt: new Date(),
          isModified: jest.fn().mockReturnValue(false),
          save: jest.fn().mockImplementation(function(this: UserAchievementDocument) { return Promise.resolve(this); }),
        } as UserAchievementDocument;

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);

        expect(mockUserAchievementModel.create).not.toHaveBeenCalled();
        expect(mockUserAchievementModel.findOneAndUpdate).not.toHaveBeenCalled();
        expect(eventEmitter.emit).not.toHaveBeenCalled();
        expect(mockUserModel.findByIdAndUpdate).not.toHaveBeenCalled();
      });

      it('should do nothing if eventType does not match criteria.conditionDetails.eventName', async () => {
        mockAchievement.criteria = {
          type: 'event_based',
          targetValue: 1,
          conditionDetails: { eventName: 'USER_SIGNUP' }
        };
        mockEventPayload.eventType = 'TASK_COMPLETED'; // Mismatch
        mockExistingUserAchievement = null;

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);

        expect(mockUserAchievementModel.create).not.toHaveBeenCalled();
        expect(eventEmitter.emit).not.toHaveBeenCalled();
      });
    });

    describe("criteria.type = 'count'", () => {
      beforeEach(() => {
        mockAchievement.criteria = { type: 'count', targetValue: 3, conditionDetails: { entityType: 'task' } };
      });

      it('should create UserAchievement and increment progress if none exists', async () => {
        mockExistingUserAchievement = null;
        (achievementService as any).matchConditionDetails = jest.fn().mockReturnValue(true); // Assume conditions match

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);

        expect(mockUserAchievementModel.create).toHaveBeenCalledTimes(1);
        const createdData = (mockUserAchievementModel.create as jest.Mock).mock.calls[0][0] as Partial<UserAchievementDocument>;
        expect(createdData.progress).toBe(1);
        expect(createdData.earned).toBe(false);
        expect(eventEmitter.emit).toHaveBeenCalledWith('ACHIEVEMENT_PROGRESSED', expect.anything());
      });

      it('should update progress for existing UserAchievement and unlock if target met', async () => {
        mockEventPayload.data = { entityType: 'task' };
        mockExistingUserAchievement = {
          _id: 'uaCount1', user: mockUserId, achievement: mockAchievement._id,
          progress: 2, earned: false, seenByUser: false,
          createdAt: new Date(),
          updatedAt: new Date(),
          isModified: jest.fn().mockReturnValue(true),
          save: jest.fn().mockImplementation(function(this: UserAchievementDocument) { return Promise.resolve(this); }),
        } as UserAchievementDocument;
        (achievementService as any).matchConditionDetails = jest.fn().mockReturnValue(true);

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);

        expect(mockUserAchievementModel.findOneAndUpdate).toHaveBeenCalledTimes(1);
        const updateCallArgs = (mockUserAchievementModel.findOneAndUpdate as jest.Mock).mock.calls[0];
        const updatePayload = updateCallArgs[1] as { $set: Partial<UserAchievementDocument> };
        expect(updatePayload.$set.progress).toBe(3);
        expect(updatePayload.$set.earned).toBe(true);
        expect(updatePayload.$set.earnedAt).toBeInstanceOf(Date);

        expect(eventEmitter.emit).toHaveBeenCalledWith('ACHIEVEMENT_UNLOCKED', expect.anything());
        expect(mockUserModel.findByIdAndUpdate).toHaveBeenCalledWith(mockUserId, { $inc: { experiencePoints: mockAchievement.points } });
      });
      
      it('should update progress and emit PROGRESSED if target not met', async () => {
        mockEventPayload.data = { entityType: 'task' };
        mockExistingUserAchievement = { 
          _id: 'uaCount2', user: mockUserId, achievement: mockAchievement._id, 
          progress: 1, earned: false, seenByUser: false,
          createdAt: new Date(),
          updatedAt: new Date(),
          isModified: jest.fn().mockReturnValue(true),
          save: jest.fn().mockImplementation(function(this: UserAchievementDocument) { return Promise.resolve(this); }),
        } as UserAchievementDocument;
        (achievementService as any).matchConditionDetails = jest.fn().mockReturnValue(true);

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);

        expect(mockUserAchievementModel.findOneAndUpdate).toHaveBeenCalledTimes(1);
        const updateCallArgs = (mockUserAchievementModel.findOneAndUpdate as jest.Mock).mock.calls[0];
        const updatePayload = updateCallArgs[1] as { $set: Partial<UserAchievementDocument> };
        expect(updatePayload.$set.progress).toBe(2);
        expect(updatePayload.$set.earned).toBe(false);
        expect(updatePayload.$set.earnedAt).toBeUndefined();
        expect(eventEmitter.emit).toHaveBeenCalledWith('ACHIEVEMENT_PROGRESSED', expect.anything());
        expect(mockUserModel.findByIdAndUpdate).not.toHaveBeenCalled();
      });

      it('should not increment progress if matchConditionDetails returns false', async () => {
        mockExistingUserAchievement = null;
        (achievementService as any).matchConditionDetails = jest.fn().mockReturnValue(false); // Conditions DO NOT match

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);

        expect(mockUserAchievementModel.create).not.toHaveBeenCalled();
        expect(mockUserAchievementModel.findOneAndUpdate).not.toHaveBeenCalled();
        expect(eventEmitter.emit).not.toHaveBeenCalled();
      });

      it('should do nothing if achievement already earned', async () => {
        mockExistingUserAchievement = {
            _id: 'uaCount3', user: mockUserId, achievement: mockAchievement._id,
            progress: mockAchievement.criteria.targetValue as number, earned: true, earnedAt: new Date(), seenByUser: false,
            createdAt: new Date(),
            updatedAt: new Date(),
            isModified: jest.fn().mockReturnValue(false),
            save: jest.fn().mockImplementation(function(this: UserAchievementDocument) { return Promise.resolve(this); }),
        } as UserAchievementDocument;
        (achievementService as any).matchConditionDetails = jest.fn().mockReturnValue(true);

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);
        
        expect(mockUserAchievementModel.findOneAndUpdate).not.toHaveBeenCalled();
        expect(eventEmitter.emit).not.toHaveBeenCalled();
      });

      it('should correctly use matchConditionDetails with event data for filtering', async () => {
        mockAchievement.criteria.conditionDetails = { entityType: 'task', category: 'Work' };
        mockEventPayload.data = { entityType: 'task', category: 'Work' }; // Matches
        mockExistingUserAchievement = null;
        
        const matchDetailsSpy = jest.spyOn(achievementService as any, 'matchConditionDetails').mockReturnValue(true);

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);
        
        expect(matchDetailsSpy).toHaveBeenCalledWith(mockAchievement.criteria.conditionDetails, mockEventPayload.data);
        expect(mockUserAchievementModel.create).toHaveBeenCalled(); // Progress should be made
        matchDetailsSpy.mockRestore();
      });
    });

    describe("criteria.type = 'streak'", () => {
      beforeEach(() => {
        mockAchievement.criteria = {
          type: 'streak',
          source: 'habits',
          targetValue: 5, // e.g., 5-day streak
          // conditionDetails might specify a relatedEntityId for a specific habit
        };
        mockEventPayload.eventType = 'HABIT_STREAK_UPDATED';
        mockEventPayload.entityId = 'habitGeneralStreak'; // Can be a specific habit ID
        mockEventPayload.data = {
          streakData: { current: 0, longest: 0 }, // Default, override in tests
          entityType: 'habit',
        };
        mockExistingUserAchievement = null;
        (achievementService as any).matchConditionDetails = jest.fn().mockReturnValue(true); // Assume other details match by default
      });

      it('should unlock achievement if current streak meets targetValue (generic habit streak)', async () => {
        mockEventPayload.data.streakData.current = 5;
        mockAchievement.criteria.conditionDetails = {}; // Generic, no specific relatedEntityId

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);

        expect(mockUserAchievementModel.create).toHaveBeenCalledTimes(1);
        const createdCall = (mockUserAchievementModel.create as jest.Mock).mock.calls[0][0] as Partial<UserAchievementDocument>;
        expect(createdCall.progress).toBe(100); 
        expect(createdCall.earned).toBe(true);
        expect(eventEmitter.emit).toHaveBeenCalledWith('ACHIEVEMENT_UNLOCKED', expect.anything());
        expect(mockUserModel.findByIdAndUpdate).toHaveBeenCalled();
      });

      it('should unlock achievement for specific habit streak if relatedEntityId matches', async () => {
        mockAchievement.criteria.conditionDetails = { relatedEntityId: 'habitSpecific123' };
        mockEventPayload.entityId = 'habitSpecific123'; // Event is for the specific habit
        mockEventPayload.data.streakData.current = 5;

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);
        expect(mockUserAchievementModel.create).toHaveBeenCalled();
        expect(eventEmitter.emit).toHaveBeenCalledWith('ACHIEVEMENT_UNLOCKED', expect.anything());
      });

      it('should NOT unlock for specific habit streak if relatedEntityId does NOT match event entityId', async () => {
        mockAchievement.criteria.conditionDetails = { relatedEntityId: 'habitSpecificXYZ' }; 
        mockEventPayload.entityId = 'habitDifferentABC'; // Event for a different habit
        mockEventPayload.data.streakData.current = 5;

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);
        expect(mockUserAchievementModel.create).not.toHaveBeenCalled();
        expect(eventEmitter.emit).not.toHaveBeenCalledWith('ACHIEVEMENT_UNLOCKED', expect.anything());
      });

      it('should do nothing if current streak is less than targetValue', async () => {
        mockEventPayload.data.streakData.current = 3; // Less than targetValue 5
        mockAchievement.criteria.conditionDetails = {};

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);
        expect(mockUserAchievementModel.create).not.toHaveBeenCalled();
        expect(eventEmitter.emit).not.toHaveBeenCalled();
      });

      it('should do nothing if achievement already earned', async () => {
        mockEventPayload.data.streakData.current = 5;
        mockExistingUserAchievement = { 
            _id: 'uaStreak', user: mockUserId, achievement: mockAchievement._id, 
            progress: 100, earned: true, earnedAt: new Date(),
            seenByUser: false,
            createdAt: new Date(),
            updatedAt: new Date(),
            isModified: jest.fn().mockReturnValue(false),
            save: jest.fn().mockImplementation(function(this: UserAchievementDocument) { return Promise.resolve(this); }),
        } as UserAchievementDocument;

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);
        expect(mockUserAchievementModel.create).not.toHaveBeenCalled();
        expect(mockUserAchievementModel.findOneAndUpdate).not.toHaveBeenCalled();
        expect(eventEmitter.emit).not.toHaveBeenCalled();
      });

      // Test for updating progress on an existing, unearned streak achievement (if applicable, though streaks are often 0 or 100)
      // Current logic for 'streak' type goes directly to 100% or nothing.
      // If streak achievements could have partial progress, those tests would go here.
      // For now, the logic seems to be: if streak condition met -> earned=true, progress=100.
      it('should update existing unearned UserAchievement to earned if streak met', async () => {
        mockEventPayload.data.streakData.current = 5;
        mockAchievement.criteria.conditionDetails = {};
        mockExistingUserAchievement = {
          _id: 'uaStreakUpdate', user: mockUserId, achievement: mockAchievement._id,
          progress: 0, earned: false, seenByUser: false,
          createdAt: new Date(),
          updatedAt: new Date(),
          isModified: jest.fn().mockReturnValue(true),
          save: jest.fn().mockImplementation(function(this: UserAchievementDocument) { return Promise.resolve(this); }),
        } as UserAchievementDocument;

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);
        
        expect(mockUserAchievementModel.findOneAndUpdate).toHaveBeenCalledTimes(1);
        const updateCallArgs = (mockUserAchievementModel.findOneAndUpdate as jest.Mock).mock.calls[0];
        const updatePayload = updateCallArgs[1] as { $set: Partial<UserAchievementDocument> };
        expect(updatePayload.$set.progress).toBe(100);
        expect(updatePayload.$set.earned).toBe(true);
        expect(updatePayload.$set.earnedAt).toBeInstanceOf(Date);
        expect(eventEmitter.emit).toHaveBeenCalledWith('ACHIEVEMENT_UNLOCKED', expect.anything());
      });
    });

    describe("criteria.type = 'completion_time'", () => {
      beforeEach(() => {
        mockAchievement.criteria = {
          type: 'completion_time',
          source: 'tasks', // Default to tasks
          targetValue: 1, // Usually 1 for a single event check
          conditionDetails: {
            // Default, will be overridden in specific tests
            timeOperator: 'before',
            timeString: '10:00' // Example: complete task before 10 AM
          }
        };
        mockEventPayload.eventType = 'TASK_COMPLETED';
        mockEventPayload.entityId = 'taskTime1';
        mockEventPayload.data = {
          entityType: 'task',
          completedAt: new Date(), // Will be set in tests
        };
        mockExistingUserAchievement = null;
        (achievementService as any).matchConditionDetails = jest.fn().mockReturnValue(true);
      });

      it('should unlock task achievement if completed before specified timeString', async () => {
        mockEventPayload.data.completedAt = new Date('2023-01-01T09:00:00Z'); // 9 AM
        mockAchievement.criteria.conditionDetails = { timeOperator: 'before', timeString: '10:00' };

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);
        expect(mockUserAchievementModel.create).toHaveBeenCalled();
        expect(eventEmitter.emit).toHaveBeenCalledWith('ACHIEVEMENT_UNLOCKED', expect.anything());
      });

      it('should NOT unlock task achievement if completed after specified timeString for "before" operator', async () => {
        mockEventPayload.data.completedAt = new Date('2023-01-01T11:00:00Z'); // 11 AM
        mockAchievement.criteria.conditionDetails = { timeOperator: 'before', timeString: '10:00' };

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);
        expect(mockUserAchievementModel.create).not.toHaveBeenCalled();
      });
      
      it('should unlock task achievement if completed after specified timeString for "after" operator', async () => {
        mockEventPayload.data.completedAt = new Date('2023-01-01T17:00:00Z'); // 5 PM
        mockAchievement.criteria.conditionDetails = { timeOperator: 'after', timeString: '16:00' };

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);
        expect(mockUserAchievementModel.create).toHaveBeenCalled();
        expect(eventEmitter.emit).toHaveBeenCalledWith('ACHIEVEMENT_UNLOCKED', expect.anything());
      });

      it('should unlock goal achievement if completed before targetDate', async () => {
        mockAchievement.criteria.source = 'goals';
        mockAchievement.criteria.conditionDetails = { check: 'beforeTargetDate' }; 
        mockEventPayload.eventType = 'GOAL_COMPLETED';
        mockEventPayload.data = {
          entityType: 'goal',
          completedAt: new Date('2023-01-10T00:00:00Z'),
          targetDate: new Date('2023-01-15T00:00:00Z') // Goal target date
        };

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);
        expect(mockUserAchievementModel.create).toHaveBeenCalled();
      });

      it('should NOT unlock goal achievement if completed after targetDate for "beforeTargetDate" check', async () => {
        mockAchievement.criteria.source = 'goals';
        mockAchievement.criteria.conditionDetails = { check: 'beforeTargetDate' };
        mockEventPayload.eventType = 'GOAL_COMPLETED';
        mockEventPayload.data = {
          entityType: 'goal',
          completedAt: new Date('2023-01-20T00:00:00Z'),
          targetDate: new Date('2023-01-15T00:00:00Z')
        };

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);
        expect(mockUserAchievementModel.create).not.toHaveBeenCalled();
      });

      it('should do nothing if already earned', async () => {
        mockEventPayload.data.completedAt = new Date('2023-01-01T09:00:00Z');
        mockAchievement.criteria.conditionDetails = { timeOperator: 'before', timeString: '10:00' }; 
        mockExistingUserAchievement = { 
            _id: 'uaTime', user: mockUserId, achievement: mockAchievement._id, 
            progress: 100, earned: true, earnedAt: new Date(),
            seenByUser: false,
            createdAt: new Date(),
            updatedAt: new Date(),
            isModified: jest.fn().mockReturnValue(false),
            save: jest.fn().mockImplementation(function(this: UserAchievementDocument) { return Promise.resolve(this); }),
        } as UserAchievementDocument;

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);
        expect(mockUserAchievementModel.create).not.toHaveBeenCalled();
        expect(mockUserAchievementModel.findOneAndUpdate).not.toHaveBeenCalled();
      });

      it('should use matchConditionDetails for further filtering if provided', async () => {
        mockEventPayload.data.completedAt = new Date('2023-01-01T08:00:00Z');
        mockAchievement.criteria.conditionDetails = { 
            timeOperator: 'before', 
            timeString: '09:00',
            category: 'Work' // Additional detail to match
        };
        mockEventPayload.data.category = 'Work'; // Event data has the category

        // Reset the spy as it's also used in the beforeEach of the parent describe block
        const matchDetailsSpy = jest.spyOn(achievementService as any, 'matchConditionDetails').mockReturnValue(true);

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);
        
        expect(matchDetailsSpy).toHaveBeenCalledWith(mockAchievement.criteria.conditionDetails, mockEventPayload.data);
        expect(mockUserAchievementModel.create).toHaveBeenCalled();
        matchDetailsSpy.mockRestore(); 
      });

      it('should NOT unlock if matchConditionDetails returns false, even if time condition met', async () => {
        mockEventPayload.data.completedAt = new Date('2023-01-01T08:00:00Z'); // Time condition met
        mockAchievement.criteria.conditionDetails = { 
            timeOperator: 'before', 
            timeString: '09:00',
            category: 'Work' 
        };
        mockEventPayload.data.category = 'Personal'; // Category mismatch

        const matchDetailsSpy = jest.spyOn(achievementService as any, 'matchConditionDetails').mockReturnValue(false); // Simulate mismatch

        await (achievementService as any).evaluateAchievementForUser(mockUserId, mockAchievement, mockEventPayload);
        
        expect(matchDetailsSpy).toHaveBeenCalledWith(mockAchievement.criteria.conditionDetails, mockEventPayload.data);
        expect(mockUserAchievementModel.create).not.toHaveBeenCalled();
        matchDetailsSpy.mockRestore();
      });
    });

    // TODO: Add tests for 'multi_condition' and 'complex' when implemented
  });

}); 