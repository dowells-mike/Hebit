import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import { AuthRequest } from '../types';
import { Goal, Category, Task } from '../models'; // Import necessary models
import suggestionTemplates from '../config/suggestionTemplates.json';

interface SuggestionTemplate {
    id: string;
    title: string;
    description?: string;
    defaultCategoryName?: string;
    triggerConditions: {
        timeOfDay?: string[];       // e.g., ["morning", "afternoon", "evening"]
        dayOfWeek?: number[];       // 1 for Monday, 7 for Sunday
        requiresGoalKeywords?: string[];
        requiresCategories?: string[]; // Names of categories user has recently used or has tasks in
        minTasksCompleted?: number;
    };
}

interface ActiveSuggestion {
    id: string;
    title: string;
    description?: string;
    defaultCategoryName?: string;
    // We might add a calculated relevance score later
}

const getCurrentTimeOfDay = (): string => {
    const hour = new Date().getHours();
    if (hour >= 5 && hour < 12) return 'morning';
    if (hour >= 12 && hour < 18) return 'afternoon';
    return 'evening'; // Covers 6 PM to 4:59 AM
};

const getCurrentDayOfWeek = (): number => {
    const day = new Date().getDay(); // 0 for Sunday, 1 for Monday, ..., 6 for Saturday
    return day === 0 ? 7 : day; // Adjust to 1 (Mon) - 7 (Sun)
};

export const getTaskSuggestions = catchAsync(async (req: AuthRequest, res: Response) => {
    const userId = req.user?._id;
    if (!userId) {
        throw new AppError('User not found', 401);
    }

    const currentTimeOfDay = getCurrentTimeOfDay();
    const currentDayOfWeek = getCurrentDayOfWeek();

    // Fetch user-specific data
    const userGoals = await Goal.find({ user: userId, status: 'active' }).select('title description');
    const userCategories = await Category.find({ user: userId }).select('name');
    const userCategoryNames = userCategories.map(cat => cat.name);
    const totalTasksCompleted = await Task.countDocuments({ user: userId, completed: true });

    const eligibleSuggestions: ActiveSuggestion[] = [];

    for (const template of suggestionTemplates as SuggestionTemplate[]) {
        let isEligible = true;
        const conditions = template.triggerConditions;

        if (conditions.timeOfDay && !conditions.timeOfDay.includes(currentTimeOfDay)) {
            isEligible = false;
        }
        if (conditions.dayOfWeek && !conditions.dayOfWeek.includes(currentDayOfWeek)) {
            isEligible = false;
        }
        if (conditions.minTasksCompleted && totalTasksCompleted < conditions.minTasksCompleted) {
            isEligible = false;
        }

        let goalMatchSuccessful = !conditions.requiresGoalKeywords; // True if no goal keywords are required
        let matchedGoalTitle: string | null = null;

        if (conditions.requiresGoalKeywords && userGoals.length > 0) {
            goalMatchSuccessful = false; // Assume false until a match is found
            for (const goal of userGoals) {
                if (goal.title && typeof goal.title === 'string' && conditions.requiresGoalKeywords.some(keyword => goal.title.toLowerCase().includes(keyword.toLowerCase()))) {
                    goalMatchSuccessful = true;
                    matchedGoalTitle = goal.title;
                    break; 
                }
            }
        }
        if (!goalMatchSuccessful && conditions.requiresGoalKeywords) { // Explicitly check if required but not found
            isEligible = false;
        }
        
        let categoryMatchSuccessful = !conditions.requiresCategories;
        if (conditions.requiresCategories && userCategoryNames.length > 0) {
            categoryMatchSuccessful = false;
             if (conditions.requiresCategories.some(reqCat => userCategoryNames.includes(reqCat))) {
                categoryMatchSuccessful = true;
            }
        }
        if(!categoryMatchSuccessful && conditions.requiresCategories){
            isEligible = false;
        }


        if (isEligible) {
            let suggestionTitle = template.title;
            if (matchedGoalTitle && template.title.includes('[GoalName]')) {
                suggestionTitle = template.title.replace('[GoalName]', matchedGoalTitle);
            }
            eligibleSuggestions.push({
                id: template.id,
                title: suggestionTitle,
                description: template.description,
                defaultCategoryName: template.defaultCategoryName,
            });
        }
    }

    // Simple selection logic: shuffle and take top N (e.g., 3)
    const shuffledSuggestions = eligibleSuggestions.sort(() => 0.5 - Math.random());
    const selectedSuggestions = shuffledSuggestions.slice(0, 3);

    res.status(200).json(selectedSuggestions);
}); 