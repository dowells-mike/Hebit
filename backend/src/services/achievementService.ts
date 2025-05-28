import mongoose, { Document } from 'mongoose';
import { Achievement, UserAchievement, User, Habit, Task, Goal } from '../models'; // Adjust if model paths are different
import {
  UserDocument as IUserDocument, // Rename for local extension
  HabitDocument,
  TaskDocument,
  GoalDocument,
  AchievementDocument,
  UserAchievementDocument as IUserAchievementDocument, // Rename to avoid conflict
  HabitFrequencyConfig // Assuming this type might be useful if we pass full habit objects
} from '../types';
import eventEmitter from './eventEmitter';

// Define minimal interfaces for Mongoose instance methods we rely on,
// ensuring _id aligns with what's in ../types

interface MinimalMongooseDocumentForUserAchievement {
  _id: string; // Must align with IUserAchievementDocument._id
  isModified: () => boolean;
  save: () => Promise<this>; 
  // createdAt and updatedAt are expected from IUserAchievementDocument
}
type ExtendedUserAchievementDocument = IUserAchievementDocument & MinimalMongooseDocumentForUserAchievement;

interface MinimalMongooseDocumentForUser {
  _id: string; // Must align with IUserDocument._id
  save: () => Promise<this>;
  // comparePassword, createdAt, updatedAt are expected from IUserDocument
}
type ExtendedUserDocument = IUserDocument & MinimalMongooseDocumentForUser;

interface StreakEventData {
  userId: string;
  habitId: string;
  streakData?: {
    current: number;
    longest: number;
    lastCompleted?: Date;
  };
  habitTitle?: string;
  frequency?: 'daily' | 'weekly' | 'monthly' | 'specific_dates';
  // other habit details if needed by achievements
}

// New interface for HABIT_COMPLETED_AT event data
interface HabitCompletedAtEventData {
  userId: string;
  habitId: string;
  habitTitle?: string;
  completedAt: Date; // Crucial for completion_time achievements
  frequency?: 'daily' | 'weekly' | 'monthly' | 'specific_dates';
  category?: string;
  // other habit details if needed by achievements
}

// Interface for TASK_COMPLETED event data
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
  // any other relevant fields from the task
}

// Interface for USER_SIGNUP event data
interface UserSignupEventData {
  userId: string;
  username?: string;
  email?: string;
}

// Interface for GOAL_COMPLETED event data
interface GoalCompletedEventData {
  userId: string;
  goalId: string;
  goalTitle?: string;
  completedAt: Date; // The actual time of completion
  category?: string;
  targetDate?: Date; // Original target date of the goal
  status?: string; // Should be 'completed'
  measurementType?: 'numeric' | 'boolean' | 'checklist';
  // any other relevant fields from the goal
}

interface GenericEventPayload {
  userId: string;
  eventType: string; 
  entityId?: string; 
  data?: any; 
}

export class AchievementService {
  constructor() {
    eventEmitter.on('HABIT_STREAK_UPDATED', this.handleHabitStreakUpdated.bind(this));
    eventEmitter.on('HABIT_COMPLETED_AT', this.handleHabitCompletedAt.bind(this));
    eventEmitter.on('TASK_COMPLETED', this.handleTaskCompleted.bind(this));
    eventEmitter.on('USER_SIGNUP', this.handleUserSignup.bind(this));
    eventEmitter.on('GOAL_COMPLETED', this.handleGoalCompleted.bind(this)); // Subscribe to GOAL_COMPLETED
    console.log('AchievementService initialized and listening for events (HABIT_STREAK_UPDATED, HABIT_COMPLETED_AT, TASK_COMPLETED, USER_SIGNUP, GOAL_COMPLETED).');
  }

  private async handleHabitStreakUpdated(data: StreakEventData): Promise<void> {
    console.log(`AchievementService: Received HABIT_STREAK_UPDATED for user ${data.userId}, habit ${data.habitId}`);
    const payload: GenericEventPayload = {
      userId: data.userId,
      eventType: 'HABIT_STREAK_UPDATED',
      entityId: data.habitId,
      data: {
        streakData: data.streakData,
        habitTitle: data.habitTitle,
        frequency: data.frequency,
        entityType: 'habit'
      }
    };
    await this.processEvent(payload);
  }

  private async handleHabitCompletedAt(data: HabitCompletedAtEventData): Promise<void> {
    console.log(`AchievementService: Received HABIT_COMPLETED_AT for user ${data.userId}, habit ${data.habitId} at ${data.completedAt}`);
    const payload: GenericEventPayload = {
      userId: data.userId,
      eventType: 'HABIT_COMPLETED_AT',
      entityId: data.habitId,
      data: {
        completedAt: data.completedAt,
        habitTitle: data.habitTitle,
        frequency: data.frequency,
        category: data.category,
        entityType: 'habit' 
      }
    };
    await this.processEvent(payload);
  }

  // New handler for TASK_COMPLETED
  private async handleTaskCompleted(data: TaskCompletedEventData): Promise<void> {
    console.log(`AchievementService: Received TASK_COMPLETED for user ${data.userId}, task ${data.taskId}`);
    
    const payload: GenericEventPayload = {
      userId: data.userId,
      eventType: 'TASK_COMPLETED',
      entityId: data.taskId,
      data: { // Pass along relevant data from the specific event
        taskTitle: data.taskTitle,
        completedAt: data.completedAt,
        category: data.category,
        priority: data.priority,
        tags: data.tags,
        effort: data.effort,
        complexity: data.complexity,
        description: data.description,
        dueDate: data.dueDate,
        entityType: 'task' // Explicitly add entityType for clarity in generic processing
      }
    };
    await this.processEvent(payload);
  }

  // New handler for USER_SIGNUP
  private async handleUserSignup(data: UserSignupEventData): Promise<void> {
    console.log(`AchievementService: Received USER_SIGNUP for user ${data.userId}`);
    const payload: GenericEventPayload = {
      userId: data.userId,
      eventType: 'USER_SIGNUP',
      data: {
        username: data.username,
        email: data.email, 
        entityType: 'user' 
      }
    };
    await this.processEvent(payload);
  }

  // New handler for GOAL_COMPLETED
  private async handleGoalCompleted(data: GoalCompletedEventData): Promise<void> {
    console.log(`AchievementService: Received GOAL_COMPLETED for user ${data.userId}, goal ${data.goalId}`);
    const payload: GenericEventPayload = {
      userId: data.userId,
      eventType: 'GOAL_COMPLETED',
      entityId: data.goalId,
      data: { // Pass along relevant data from the specific event
        goalTitle: data.goalTitle,
        completedAt: data.completedAt,
        category: data.category,
        targetDate: data.targetDate,
        status: data.status, // Should be 'completed'
        measurementType: data.measurementType,
        entityType: 'goal' // Explicitly add entityType
      }
    };
    await this.processEvent(payload);
  }

  // General event processor (as designed before)
  public async processEvent(payload: GenericEventPayload): Promise<void> {
    console.log(`AchievementService: Processing generic event ${payload.eventType} for user ${payload.userId}, entity ${payload.entityId}`);
    const { userId, eventType, entityId, data } = payload;

    const relevantAchievements = await this.findRelevantAchievements(eventType, entityId, data);

    for (const achievement of relevantAchievements) {
      await this.evaluateAchievementForUser(userId, achievement, payload);
    }
  }

  private async findRelevantAchievements(eventType: string, entityId?: string, eventData?: any): Promise<AchievementDocument[]> {
    const query: any = {};

    if (eventType === 'HABIT_STREAK_UPDATED') {
      query['criteria.type'] = 'streak';
      query['criteria.source'] = 'habits';
      if (entityId) {
        query['$or'] = [
          { 'criteria.conditionDetails.relatedEntityId': entityId },
          { 'criteria.conditionDetails.relatedEntityId': { $exists: false } },
          { 'criteria.conditionDetails.relatedEntityId': null }
        ];
      } else {
        // Generic streak achievements (not tied to a specific habit)
        query['criteria.conditionDetails.relatedEntityId'] = { $exists: false };
      }
    } else if (eventType === 'HABIT_COMPLETED_AT') {
      query['criteria.source'] = 'habits';
      query['$or'] = [
        { 'criteria.type': 'count' }, 
        { 'criteria.type': 'completion_time' } 
      ];
      // Further filtering based on eventData (e.g. habit category) could be added here
      // or handled primarily in evaluateAchievementForUser + matchConditionDetails
    } else if (eventType === 'TASK_COMPLETED') {
      query['criteria.source'] = 'tasks';
      query['$or'] = [
        { 'criteria.type': 'count' },
        { 'criteria.type': 'completion_time' } 
      ];
      // Example of how to potentially use eventData for pre-filtering:
      // if (eventData?.category) {
      //   query['$or'] = [
      //      { 'criteria.conditionDetails.category': eventData.category },
      //      { 'criteria.conditionDetails.category': { $exists: false } } // for generic task achievements
      //   ];
      // }
      // For now, keeping it broader and relying on evaluateAchievementForUser for detailed checks.
    } else if (eventType === 'USER_SIGNUP') {
      query['criteria.type'] = 'event_based';
      query['criteria.conditionDetails.eventName'] = 'USER_SIGNUP';
    } else if (eventType === 'GOAL_COMPLETED') {
      query['criteria.source'] = 'goals';
      query['$or'] = [
        { 'criteria.type': 'count' }, // e.g. "Complete 5 goals"
        { 'criteria.type': 'event_based' }, // e.g. "Complete your first goal"
        { 'criteria.type': 'completion_time' } // e.g. "Complete a goal before its target date"
      ];
    }
    // TODO: Add more conditions based on other event types and eventData specifics

    // console.log('AchievementService: Querying for relevant achievements with:', JSON.stringify(query));
    return Achievement.find(query).lean(); 
  }

  private matchConditionDetails(achievementCondDetails?: { [key: string]: any }, eventData?: { [key: string]: any }): boolean {
    if (!achievementCondDetails || Object.keys(achievementCondDetails).length === 0) {
      return true; 
    }
    if (!eventData) {
      return false; 
    }
  
    for (const key of Object.keys(achievementCondDetails)) {
      // Skip keys that are structurally handled elsewhere or are not direct value comparisons
      if (['eventName', 'relatedEntityId', 'entityType', 'timeOperator', 'timeString'].includes(key)) {
          continue;
      }
  
      if (achievementCondDetails[key] !== eventData[key]) {
        // console.log(`Condition mismatch: achievement.${key} (${achievementCondDetails[key]}) !== event.${key} (${eventData[key]})`);
        return false; 
      }
    }
    return true; 
  }

  private async evaluateAchievementForUser(
    userId: string,
    achievement: AchievementDocument,
    eventPayload: GenericEventPayload
  ): Promise<void> {
    let userAchievement: ExtendedUserAchievementDocument | null = await UserAchievement.findOne({ user: userId, achievement: achievement._id }).exec() as ExtendedUserAchievementDocument | null;
    let isNewAchievement = false;

    if (!userAchievement) {
      userAchievement = await UserAchievement.create({
        user: userId,
        achievement: achievement._id,
        progress: 0,
        earned: false,
        earnedAt: undefined,
        seenByUser: false,
      }) as ExtendedUserAchievementDocument;
      isNewAchievement = true;
    }

    if (!userAchievement) {
      // This case should ideally not be reached if create always returns a document or throws
      console.error('AchievementService: UserAchievement is null after findOne and create attempts.');
      return;
    }

    // If already earned, do nothing further for this specific achievement-event pair.
    // This check might seem redundant if findOne already filters, but create doesn't.
    if (userAchievement.earned && !isNewAchievement) { // Only skip if it was pre-existing and earned
      return; 
    }

    const oldProgress = userAchievement.progress || 0;
    let meetsCriteria = false;
    let newProgress = userAchievement.progress || 0;
    const { eventType, entityId, data: eventData } = eventPayload;

    switch (achievement.criteria.type) {
      case 'streak':
        if (eventType === 'HABIT_STREAK_UPDATED' && 
            achievement.criteria.source === 'habits' && 
            eventData?.streakData) {
          const currentStreak = eventData.streakData.current;
          const targetStreak = achievement.criteria.targetValue as number;
          const specificHabitIdForAchievement = achievement.criteria.conditionDetails?.relatedEntityId;
          
          let applicableStreak = false;
          if (specificHabitIdForAchievement) {
            if (entityId === specificHabitIdForAchievement) {
              applicableStreak = true;
            }
          } else { 
            applicableStreak = true;
          }

          if (applicableStreak) {
            if (currentStreak >= targetStreak) {
              meetsCriteria = true;
              newProgress = 100; // Set to 100% on meeting criteria
            } else {
              // Update progress proportionally if desired, or keep as current streak value if progress means current_streak_value
              // For now, if not met, progress could reflect the current streak value if targetValue represents the goal for 100%
              // This part is tricky: if progress is 0-100, then it should be (currentStreak / targetStreak) * 100
              // However, UserAchievement.progress is a single number. Let's assume it's 0-100.
              // If not meeting criteria, it shouldn't unlock, so progress update is for 'PROGRESSED' event.
              // Let's set newProgress to (currentStreak / targetStreak) * 100, capped at 100 if it's not yet earned.
              if (!userAchievement.earned) { // Only update progress if not already earned
                 newProgress = Math.min(Math.floor((currentStreak / targetStreak) * 100), 100);
              }
            }
          }
        }
        break;

      case 'count':
        let sourceAndEntityTypeMatchCount = false;
        if (achievement.criteria.source === 'tasks' && eventType === 'TASK_COMPLETED' && eventData?.entityType === 'task') {
            sourceAndEntityTypeMatchCount = true;
        } else if (achievement.criteria.source === 'habits' && eventType === 'HABIT_COMPLETED_AT' && eventData?.entityType === 'habit') {
            sourceAndEntityTypeMatchCount = true;
        } else if (achievement.criteria.source === 'goals' && eventType === 'GOAL_COMPLETED' && eventData?.entityType === 'goal') {
            sourceAndEntityTypeMatchCount = true;
        }

        if (sourceAndEntityTypeMatchCount) {
          if (this.matchConditionDetails(achievement.criteria.conditionDetails, eventData)) {
            const specificEntityForCount = achievement.criteria.conditionDetails?.relatedEntityId;
            if (!specificEntityForCount || specificEntityForCount === entityId) { 
                const currentProgressValue = userAchievement.progress || 0;
                // Assuming progress for count types is the actual count, not percentage until earned
                const incrementedCount = (isNewAchievement && currentProgressValue === 0) ? 1 : currentProgressValue + 1;

                if (incrementedCount >= (achievement.criteria.targetValue as number)) {
                    meetsCriteria = true;
                    newProgress = 100; // Set to 100% on meeting criteria
                } else {
                    newProgress = incrementedCount; // Store the actual count as progress
                }
            }
          }
        }
        break;

      case 'completion_time':
        const { entityType: achEntityTypeCT, timeOperator, timeString, relatedEntityId: achRelatedEntityIdCT, check: achCheckCT } = achievement.criteria.conditionDetails || {};
        let eventSatisfiesCompletionTimeCT = false;
        let timeConditionMetCT = false;

        if (eventData?.completedAt && achEntityTypeCT) {
            const completedAtTimeCT = new Date(eventData.completedAt);
            // Determine if the event source matches the achievement's expected entity type
            if ((achEntityTypeCT === 'habit' && eventType === 'HABIT_COMPLETED_AT' && eventData.entityType === 'habit') ||
                (achEntityTypeCT === 'task' && eventType === 'TASK_COMPLETED' && eventData.entityType === 'task') ||
                (achEntityTypeCT === 'goal' && eventType === 'GOAL_COMPLETED' && eventData.entityType === 'goal')) {
                eventSatisfiesCompletionTimeCT = true;
            }

            if (eventSatisfiesCompletionTimeCT) {
                if (achRelatedEntityIdCT && achRelatedEntityIdCT !== entityId) {
                    eventSatisfiesCompletionTimeCT = false; 
                } else {
                    // Time logic for habits/tasks (specific time of day)
                    if ((achEntityTypeCT === 'habit' || achEntityTypeCT === 'task') && timeOperator && timeString) {
                        const [hours, minutes, seconds] = timeString.split(':').map(Number);
                        const targetTimeTodayCT = new Date(completedAtTimeCT); 
                        targetTimeTodayCT.setHours(hours, minutes, seconds || 0, 0);
                        if (timeOperator === 'before' && completedAtTimeCT.getTime() < targetTimeTodayCT.getTime()) {
                            timeConditionMetCT = true;
                        } else if (timeOperator === 'after' && completedAtTimeCT.getTime() > targetTimeTodayCT.getTime()) {
                            timeConditionMetCT = true; 
                        }
                    } else if (achEntityTypeCT === 'goal' && achCheckCT === 'beforeTargetDate' && eventData.targetDate) {
                        const targetDateCT = new Date(eventData.targetDate);
                        if (completedAtTimeCT.getTime() < targetDateCT.getTime()) {
                            timeConditionMetCT = true;
                        }
                    } else if (achEntityTypeCT === 'goal' && !achCheckCT && timeOperator && timeString ){
                        const [hours, minutes, seconds] = timeString.split(':').map(Number);
                        const targetTimeTodayCT = new Date(completedAtTimeCT);
                        targetTimeTodayCT.setHours(hours, minutes, seconds || 0, 0);
                        if (timeOperator === 'before' && completedAtTimeCT.getTime() < targetTimeTodayCT.getTime()) {
                            timeConditionMetCT = true;
                        } else if (timeOperator === 'after' && completedAtTimeCT.getTime() > targetTimeTodayCT.getTime()) {
                           timeConditionMetCT = true;
                        }
                    }

                    if (timeConditionMetCT && this.matchConditionDetails(achievement.criteria.conditionDetails, eventData)) {
                        // For completion_time, it's usually a one-shot event meeting the criteria.
                        // Progress can go from 0 to 100 directly if targetValue is 1.
                        const currentProgressValCT = (userAchievement.progress || 0);
                        const incrementedProgressCT = currentProgressValCT + 1; // Assuming each valid completion increments

                        if (incrementedProgressCT >= (achievement.criteria.targetValue as number)) {
                            meetsCriteria = true;
                            newProgress = 100;
                        } else {
                            newProgress = incrementedProgressCT; // This might represent count of successful time-based completions
                        }
                    } else {
                        // eventSatisfiesCompletionTimeCT = false; // Not needed, condition just not met
                    }
                }
            }
        }
        break;

      case 'event_based':
        if (achievement.criteria.conditionDetails?.eventName === eventPayload.eventType) {
          if (this.matchConditionDetails(achievement.criteria.conditionDetails, eventData)) {
                 const specificEntityForEvent = achievement.criteria.conditionDetails?.relatedEntityId;
                 if (!specificEntityForEvent || specificEntityForEvent === entityId) {
                    // For event_based, assume targetValue is 1 for a single event occurrence leading to unlock.
                    // Progress directly goes to 100%.
                    if ((achievement.criteria.targetValue as number) === 1) {
                        meetsCriteria = true;
                        newProgress = 100;
                    } else {
                        // If targetValue > 1 for event_based, it acts like a counter for that specific event.
                        const currentProgressValEB = userAchievement.progress || 0;
                        const incrementedCountEB = (isNewAchievement && currentProgressValEB === 0 && !userAchievement.earned) ? 1 : currentProgressValEB + 1;
                        if (incrementedCountEB >= (achievement.criteria.targetValue as number)) {
                            meetsCriteria = true;
                            newProgress = 100;
                        } else {
                            newProgress = incrementedCountEB;
                        }
                    }
                 }
            }
          }
        break;

      case 'multi_condition':
        // TODO: Implement multi_condition logic.
        // This is complex and will require careful design.
        // 1. UserAchievement.progress might need to be an object storing progress for each sub-condition.
        //    Example: userAchievement.progress = { condition1_progress: 2, condition2_progress: 5 }
        // 2. achievement.criteria.conditionDetails would define an operator ('AND', 'OR') and an array of sub-conditions.
        //    Each sub-condition would have its own type, source, targetValue, and potentially its own conditionDetails.
        // 3. When an event comes in, this handler would iterate through the sub-conditions.
        // 4. For each sub-condition, it would determine if the current event applies to it.
        //    This might involve reusing logic from how other types (count, streak, completion_time) are evaluated against the event, 
        //    but applied to the sub-condition's definition.
        // 5. If the event applies, update the corresponding progress in userAchievement.progress object.
        // 6. After updating, check if all (for AND) or any (for OR) sub-conditions in userAchievement.progress 
        //    have met their targetValues defined in the achievement's sub-conditions.
        // 7. If the overall multi-condition is met, set meetsCriteria = true.
        console.warn(`AchievementService: 'multi_condition' type for achievement ${achievement.name} is not yet fully implemented.`);
        // For now, this will not result in meetsCriteria = true.
        break;
      
      // TODO: Implement 'complex'
      default:
        console.warn(`AchievementService: Unknown or unhandled criteria type ${achievement.criteria.type} for achievement ${achievement.name} (${achievement._id})`);
        return;
    }

    userAchievement.progress = newProgress;

    if (meetsCriteria && !userAchievement.earned) {
      userAchievement.earned = true;
      userAchievement.earnedAt = new Date();
      
      // Add points to user
      const user = await User.findById(userId).exec() as ExtendedUserDocument | null;

      if (user && achievement.points > 0) {
        user.experiencePoints = (user.experiencePoints || 0) + achievement.points;
        try {
          await user.save();
          console.log(`AchievementService: Awarded ${achievement.points} XP to user ${userId}. New total: ${user.experiencePoints}`);
        } catch (err) {
          console.error(`AchievementService: Failed to save user ${userId} after awarding points.`, err);
          // Decide on error handling: should this roll back achievement earning? For now, it doesn't.
        }
      } else {
        console.warn(`AchievementService: User ${userId} not found when trying to award points for achievement ${achievement.name}`);
      }

      console.log(`AchievementService: User ${userId} UNLOCKED achievement '${achievement.name}'`);
      eventEmitter.emit('ACHIEVEMENT_UNLOCKED', { 
        userId,
        achievementId: achievement._id.toString(), 
        achievementName: achievement.name,
        pointsAwarded: achievement.points // Changed from points to pointsAwarded for clarity
      });
    } else if (newProgress > oldProgress && !userAchievement.earned) {
      console.log(`AchievementService: User ${userId} PROGRESSED on achievement '${achievement.name}' to ${newProgress}/${achievement.criteria.targetValue}`);
      eventEmitter.emit('ACHIEVEMENT_PROGRESSED', {
        userId,
        achievementId: achievement._id.toString(),
        achievementName: achievement.name,
        progress: newProgress,
        targetValue: achievement.criteria.targetValue
      });
    }

    if (userAchievement.isModified()) {
      try {
        await userAchievement.save();
      } catch (error) {
        console.error(`AchievementService: Failed to save UserAchievement for user ${userId}, achievement ${achievement._id}`, error);
      }
    }
  }
}

// Export a singleton instance to be used across the application
export const achievementService = new AchievementService(); 