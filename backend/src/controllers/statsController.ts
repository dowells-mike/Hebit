import { Response } from 'express';
import { catchAsync, AppError } from '../middleware/errorHandler';
import { AuthRequest } from '../types';
import { Task, ProductivityMetrics, User } from '../models'; // Assuming ProductivityMetrics model exists
import mongoose from 'mongoose';

const parseDateRange = (queryParams: any) => {
    let { period, startDate: startDateStr, endDate: endDateStr } = queryParams;
    let startDate: Date | null = null;
    let endDate: Date | null = null;

    if (startDateStr) {
        startDate = new Date(startDateStr);
        if (isNaN(startDate.getTime())) throw new AppError('Invalid startDate format. Use ISO date format (YYYY-MM-DD).', 400);
    }
    if (endDateStr) {
        endDate = new Date(endDateStr);
        if (isNaN(endDate.getTime())) throw new AppError('Invalid endDate format. Use ISO date format (YYYY-MM-DD).', 400);
    }

    const now = new Date();
    // Ensure time part is zeroed out for day-based calculations unless specified otherwise
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate()); 

    if (period) {
        period = period.toLowerCase();

        switch (period) {
            case 'today':
                startDate = todayStart;
                endDate = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);
                break;
            case 'week':
                startDate = new Date(todayStart); // Start from today
                // Adjust to Monday of the current week (0=Sunday, 1=Monday, ..., 6=Saturday)
                startDate.setDate(todayStart.getDate() - (todayStart.getDay() === 0 ? 6 : todayStart.getDay() - 1));
                endDate = new Date(startDate); // End date is Sunday of that week
                endDate.setDate(startDate.getDate() + 6);
                endDate.setHours(23, 59, 59, 999);
                break;
            case 'month':
                startDate = new Date(now.getFullYear(), now.getMonth(), 1);
                endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999); // Last day of current month
                break;
            case 'all':
                startDate = new Date(0); // Epoch time
                endDate = endDate ? endDate : new Date(); // Use provided endDate if available, else now
                break;
            default:
                // Invalid period. If full custom startDate and endDate were provided, use them.
                // Otherwise, it's an error because the period is unrecognized.
                if (!startDate || !endDate) { // check if startDate or endDate are still null (i.e. not provided via startDateStr/endDateStr)
                    throw new AppError(
                        `Invalid period: '${period}'. Valid periods are 'today', 'week', 'month', 'all', or provide specific startDate and endDate.`, 
                        400
                    );
                }
                // If startDate and endDate were set from queryParams, and period is invalid, we proceed with those custom dates.
                break;
        }
    } else {
        // No period specified.
        if (startDate && endDate) {
            // Both custom dates are provided. Use them as is.
        } else if (startDate && !endDate) {
            // Only startDate provided. Default endDate to end of the day of startDate.
            endDate = new Date(startDate);
            endDate.setHours(23, 59, 59, 999);
        } else if (!startDate && endDate) {
            // Only endDate provided. Default startDate to beginning of the day of endDate.
            startDate = new Date(endDate);
            startDate.setHours(0, 0, 0, 0);
        } else {
            // No period, no custom dates either. Default to current week.
            startDate = new Date(todayStart);
            startDate.setDate(todayStart.getDate() - (todayStart.getDay() === 0 ? 6 : todayStart.getDay() - 1)); // Monday
            endDate = new Date(startDate);
            endDate.setDate(startDate.getDate() + 6);
            endDate.setHours(23, 59, 59, 999); // Sunday
        }
    }

    // Final validation after all logic paths
    if (!startDate || !endDate) {
        // This case should ideally not be reached if the logic above is complete.
        throw new AppError('Date range could not be determined. Please provide a period or valid startDate and endDate.', 400);
    }

    if (startDate > endDate) {
        throw new AppError('Start date cannot be after end date.', 400);
    }

    return { startDate, endDate };
};

/**
 * @desc    Get aggregated task statistics
 * @route   GET /api/stats/tasks
 * @access  Private
 */
export const getTaskStatistics = catchAsync(async (req: AuthRequest, res: Response) => {
    const userId = req.user?._id;
    const { startDate, endDate } = parseDateRange(req.query);

    // --- 1. Total Tasks Created --- 
    const totalTasksCreated = await Task.countDocuments({
        user: userId,
        createdAt: { $gte: startDate, $lte: endDate },
        status: { $ne: 'archived' } // Exclude archived unless specified
    });

    // --- 2. Total Tasks Completed --- 
    const totalTasksCompleted = await Task.countDocuments({
        user: userId,
        completed: true,
        completedAt: { $gte: startDate, $lte: endDate },
        status: { $ne: 'archived' }
    });

    // --- 3. Completion Rate --- 
    const relevantTasksForCompletionRate = await Task.countDocuments({
        user: userId,
        status: { $ne: 'archived' },
        $or: [
            { createdAt: { $gte: startDate, $lte: endDate } }, // Created in period
            { completedAt: { $gte: startDate, $lte: endDate } } // Or completed in period
        ]
    });
    const completionRate = relevantTasksForCompletionRate > 0 ? Math.round((totalTasksCompleted / relevantTasksForCompletionRate) * 100) : 0;


    // --- 4. Tasks Completed On Time --- 
    const tasksCompletedOnTime = await Task.countDocuments({
        user: userId,
        completed: true,
        completedAt: { $gte: startDate, $lte: endDate },
        dueDate: { $ne: null },
        $expr: { $lte: ["$completedAt", "$dueDate"] },
        status: { $ne: 'archived' }
    });

    // --- 5. Tasks Completed Late --- 
    const tasksCompletedLate = await Task.countDocuments({
        user: userId,
        completed: true,
        completedAt: { $gte: startDate, $lte: endDate },
        dueDate: { $ne: null },
        $expr: { $gt: ["$completedAt", "$dueDate"] },
        status: { $ne: 'archived' }
    });

    // --- 7. Tasks Overdue --- 
    // For a historical period (endDate is in the past), calculate tasks that were overdue by the endDate.
    // For current periods (today, or if endDate is today/future), calculate currently overdue tasks.
    const overdueReferenceDate = (endDate < new Date(new Date().setHours(0,0,0,0))) ? endDate : new Date(); 
    const tasksOverdue = await Task.countDocuments({
        user: userId,
        completed: false,
        dueDate: { $lt: overdueReferenceDate, $ne: null },
        status: { $nin: ['completed', 'archived'] } 
    });

    // --- 8. Breakdown by Priority ---
    const priorityBreakdown = await Task.aggregate([
        { $match: { 
            user: new mongoose.Types.ObjectId(userId as string),
            status: { $ne: 'archived' }, 
            $or: [
                { createdAt: { $gte: startDate, $lte: endDate } }, 
                { completedAt: { $gte: startDate, $lte: endDate } }
            ]
        } },
        { $group: {
            _id: "$priority",
            totalCreated: { $sum: { $cond: [ { $and: [ { $gte: ["$createdAt", startDate] }, { $lte: ["$createdAt", endDate] } ] }, 1, 0] } },
            totalCompleted: { $sum: { $cond: [ { $and: [ "$completed", { $gte: ["$completedAt", startDate] }, { $lte: ["$completedAt", endDate] } ] }, 1, 0] } },
            completedOnTime: { $sum: { $cond: [ { $and: [ "$completed", { $gte: ["$completedAt", startDate] }, { $lte: ["$completedAt", endDate] }, { $ne: ["$dueDate", null] }, { $lte: ["$completedAt", "$dueDate"] } ] }, 1, 0] } },
        } },
        { $project: {
            _id: 0, // Exclude _id from final projection for cleaner output
            priority: "$_id",
            totalCreated: 1,
            totalCompleted: 1,
            completedOnTime: 1,
            completionRate: { 
                $round: [
                    { $cond: [ { $gt: ["$totalCreated", 0] }, { $multiply: [ { $divide: ["$totalCompleted", "$totalCreated"] }, 100 ] }, 0 ] },
                    0 // Round to 0 decimal places
                ]
            }
        } }
    ]);

    // --- 9. Breakdown by Category ---
    const categoryBreakdown = await Task.aggregate([
        { $match: { 
            user: new mongoose.Types.ObjectId(userId as string),
            status: { $ne: 'archived' },
             $or: [
                { createdAt: { $gte: startDate, $lte: endDate } },
                { completedAt: { $gte: startDate, $lte: endDate } }
            ]
        } },
        { $lookup: { from: 'categories', localField: 'category', foreignField: '_id', as: 'categoryDetails' } },
        { $unwind: { path: "$categoryDetails", preserveNullAndEmptyArrays: true } }, // Keep tasks even if category is null/invalid
        { $group: {
            _id: "$categoryDetails.name", 
            categoryId: { $first: "$categoryDetails._id"},
            totalCreated: { $sum: { $cond: [ { $and: [ { $gte: ["$createdAt", startDate] }, { $lte: ["$createdAt", endDate] } ] }, 1, 0] } },
            totalCompleted: { $sum: { $cond: [ { $and: [ "$completed", { $gte: ["$completedAt", startDate] }, { $lte: ["$completedAt", endDate] } ] }, 1, 0] } }
        } },
        { $project: {
            _id: 0,
            category: { $ifNull: [ "$_id", "Uncategorized" ] }, // Handle tasks with no category
            categoryId: 1,
            totalCreated: 1,
            totalCompleted: 1,
            completionRate: { 
                 $round: [
                    { $cond: [ { $gt: ["$totalCreated", 0] }, { $multiply: [ { $divide: ["$totalCompleted", "$totalCreated"] }, 100 ] }, 0 ] },
                    0
                 ]
            }
        } }
    ]);

    res.status(200).json({
        period: { 
            startDate: startDate.toISOString(), 
            endDate: endDate.toISOString(),
            query: req.query // For debugging what was parsed
        },
        totalTasksCreated,
        totalTasksCompleted,
        completionRate: parseFloat(completionRate.toFixed(0)), // Ensure it's a rounded number
        tasksCompletedOnTime,
        tasksCompletedLate,
        tasksOverdue,
        priorityBreakdown,
        categoryBreakdown
    });
});

// Helper function to calculate productivity score for a given period
async function calculateProductivityScoreForPeriod(userId: string, startDate: Date, endDate: Date): Promise<number> {
    // --- Logic from getProductivityScore --- 
    const tasksInPeriod = await Task.find({
        user: userId,
        status: { $ne: 'archived' },
        $or: [
            { createdAt: { $gte: startDate, $lte: endDate } }, 
            { completedAt: { $gte: startDate, $lte: endDate } },
            { dueDate: { $gte: startDate, $lte: endDate } } // Consider tasks due in period as well
        ]
    }).select('+priority +effort +complexity'); // Ensure these are selected if not by default

    let score = 0;
    let completedTasks = 0;
    const tasksConsideredForConsistency = new Set<string>();

    for (const task of tasksInPeriod) {
        if (task.completed && task.completedAt && task.completedAt >= startDate && task.completedAt <= endDate) {
            completedTasks++;
            tasksConsideredForConsistency.add(task._id.toString());

            // Base points for completion
            switch (task.priority) {
                case 'high': score += 15; break;
                case 'medium': score += 10; break;
                case 'low': score += 5; break;
                default: score += 7; // Default for tasks with no/other priority
            }

            // Bonus for on-time completion
            if (task.dueDate && task.completedAt <= task.dueDate) {
                score += 5; // On-time bonus
            }

            // Bonus for effort/complexity (if defined)
            score += (task.effort || 0) * 0.5; // Example: 0.5 point per effort point
            score += (task.complexity || 0) * 0.5; // Example: 0.5 point per complexity point

        } else if (task.dueDate && task.dueDate <= endDate && !task.completed) {
            // Penalty for overdue tasks within the period (if not completed by endDate)
            const today = new Date();
            today.setHours(0,0,0,0);
            // Only penalize if due date is in the past relative to 'today' or period's end if historical
            const referenceDateForOverdue = endDate < today ? endDate : today;
            if (task.dueDate < referenceDateForOverdue) {
                 score -= 5; 
            }
        }

        // Penalty for late completion (if completed in period but after due date)
        if (task.completed && task.completedAt && task.completedAt >= startDate && task.completedAt <= endDate && task.dueDate && task.completedAt > task.dueDate) {
            score -= 3; // Penalty for being late
        }
    }
    
    // Basic consistency: more than 2 tasks completed in the period
    if (tasksConsideredForConsistency.size > 2) {
        score += 5;
    }
     // Proactiveness: (Placeholder - e.g. tasks completed well before due date)

    // Normalization / Scaling (Example: cap score or scale to a 0-100 range if needed)
    // For now, raw score. Max score can be high if many high priority tasks.
    return Math.max(0, Math.round(score)); // Ensure score is not negative and is an integer
}

/**
 * @desc    Get productivity score
 * @route   GET /api/stats/productivity-score
 * @access  Private
 */
export const getProductivityScore = catchAsync(async (req: AuthRequest, res: Response) => {
    const userId = req.user?._id;
    if (!userId) {
        throw new AppError('User not found', 401);
    }
    const { startDate, endDate } = parseDateRange(req.query);

    const score = await calculateProductivityScoreForPeriod(userId.toString(), startDate, endDate);

    res.status(200).json({
        period: {
            startDate: startDate.toISOString(),
            endDate: endDate.toISOString(),
            queryUsed: req.query
        },
        productivityScore: score,
    });
});

/**
 * @desc    Get historical productivity scores
 * @route   GET /api/stats/score-history
 * @access  Private
 */
export const getScoreHistory = catchAsync(async (req: AuthRequest, res: Response) => {
    const userId = req.user?._id;
    if (!userId) {
        throw new AppError('User not found', 401);
    }

    const periodType = (req.query.periodType as string)?.toLowerCase() || 'daily'; // daily, weekly
    const count = parseInt(req.query.count as string) || 7; // Number of periods to fetch

    if (count > 30 && periodType === 'daily') {
        throw new AppError('For daily history, count cannot exceed 30. For longer histories, use weekly.', 400);
    }
    if (count > 52 && periodType === 'weekly') {
        throw new AppError('For weekly history, count cannot exceed 52.', 400);
    }

    const history: { date: string; score: number }[] = [];
    const today = new Date();
    today.setHours(0, 0, 0, 0); // Normalize today to the start of the day

    for (let i = 0; i < count; i++) {
        let periodStartDate: Date;
        let periodEndDate: Date;

        if (periodType === 'daily') {
            periodStartDate = new Date(today);
            periodStartDate.setDate(today.getDate() - i);
            periodEndDate = new Date(periodStartDate);
            periodEndDate.setHours(23, 59, 59, 999);
        } else if (periodType === 'weekly') {
            // Calculate start of the week (Monday) for i weeks ago
            periodStartDate = new Date(today);
            periodStartDate.setDate(today.getDate() - (today.getDay() === 0 ? 6 : today.getDay() -1) - (i * 7) ); // Go to Monday of current week, then i weeks back
            periodEndDate = new Date(periodStartDate);
            periodEndDate.setDate(periodStartDate.getDate() + 6);
            periodEndDate.setHours(23, 59, 59, 999); // Sunday of that week
        } else {
            throw new AppError('Invalid periodType. Use "daily" or "weekly".', 400);
        }

        const score = await calculateProductivityScoreForPeriod(userId.toString(), periodStartDate, periodEndDate);
        history.push({
            date: periodStartDate.toISOString().split('T')[0], // Store date as YYYY-MM-DD
            score: score,
        });
    }

    res.status(200).json({
        periodType,
        count,
        history: history.reverse(), // Return in chronological order (oldest to newest)
    });
});

// Ensure this is at the end of the file or where appropriate
// module.exports = { getTaskStatistics, getProductivityScore, getScoreHistory }; // If using CommonJS (not typical for TS) 