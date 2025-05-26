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

/**
 * @desc    Get productivity score
 * @route   GET /api/stats/productivity-score
 * @access  Private
 */
export const getProductivityScore = catchAsync(async (req: AuthRequest, res: Response) => {
    const userId = req.user?._id;
    const { startDate, endDate } = parseDateRange(req.query);
    let score = 0;
    const pointsLog: string[] = []; // For debugging score calculation

    // 1. Volume of Work (Completion)
    const completedTasks = await Task.find({
        user: userId,
        completed: true,
        completedAt: { $gte: startDate, $lte: endDate },
        status: { $ne: 'archived' }
    }).select('priority dueDate completedAt effort complexity'); // Select necessary fields

    completedTasks.forEach(task => {
        let taskScore = 0;
        let reason = "";
        switch (task.priority) {
            case 'high': taskScore += 3; reason += "High Priority (+3)"; break;
            case 'medium': taskScore += 2; reason += "Medium Priority (+2)"; break;
            case 'low': taskScore += 1; reason += "Low Priority (+1)"; break;
            default: taskScore += 1; reason += "Default Priority (+1)";
        }
        
        // Add points for effort/complexity if available
        if (task.effort) { taskScore += (task.effort * 0.2); reason += `, Effort (${task.effort}*0.2 = +${task.effort*0.2})`; }
        if (task.complexity) { taskScore += (task.complexity * 0.2); reason += `, Complexity (${task.complexity}*0.2 = +${task.complexity*0.2})`;}

        // Add points for timeliness
        if (task.dueDate) {
            if (task.completedAt && task.completedAt <= task.dueDate) {
                taskScore += 1; reason += ", On-time (+1)";
            } else {
                taskScore -= 0.5; reason += ", Late (-0.5)"; // Minor penalty for lateness
            }
        } else {
            taskScore += 0.5; reason += ", No Due Date Bonus (+0.5)";
        }
        pointsLog.push(`Task ${task._id}: ${reason} = ${taskScore.toFixed(1)} points`);
        score += taskScore;
    });

    // 3. Consistency (Streak) - Placeholder for MVP
    // This would query a ProductivityMetrics collection for daily completions.
    // For now, we can add a small bonus if tasks were completed on multiple days within the period.
    const completionDays = new Set(completedTasks.map(t => t.completedAt?.toISOString().split('T')[0]));
    if (completionDays.size >= 3 && (endDate.getTime() - startDate.getTime()) >= (2 * 24 * 60 * 60 * 1000)) { // Min 3 days in a period of at least 3 days
        score += 5;
        pointsLog.push("Consistency Bonus: Completed tasks on >= 3 days in period (+5)");
    }

    // 4. Proactiveness (Reducing Overdue)
    // This requires knowing if a task *was* overdue and then completed.
    // Simpler: Penalty for tasks that are currently overdue at the end of the period or became overdue.
    const overdueReferenceDate = (endDate < new Date(new Date().setHours(0,0,0,0))) ? endDate : new Date();
    const currentOverdueTasksCount = await Task.countDocuments({
        user: userId,
        completed: false,
        dueDate: { $lt: overdueReferenceDate, $ne: null },
        status: { $nin: ['completed', 'archived'] }
    });
    if (currentOverdueTasksCount > 0) {
        score -= (currentOverdueTasksCount * 0.5);
        pointsLog.push(`Overdue Penalty: ${currentOverdueTasksCount} tasks overdue (-${currentOverdueTasksCount * 0.5})`);
    }
    
    // Score should not be negative
    score = Math.max(0, score);

    // Normalize/Scale Score (Example: to 0-100 if a max is known or desired)
    // For now, let's cap at 100 for simplicity, though a dynamic scale or no cap might be better.
    // const normalizedScore = Math.min(100, Math.round(score)); 
    const finalScore = Math.round(score);

    res.status(200).json({
        period: { 
            startDate: startDate.toISOString(), 
            endDate: endDate.toISOString(),
            query: req.query 
        },
        productivityScore: finalScore,
        // pointsLog // Uncomment for debugging how score was calculated
    });
});

/**
 * @desc    Get historical scores for trends
 * @route   GET /api/stats/score-history
 * @access  Private
 */
export const getScoreHistory = catchAsync(async (req: AuthRequest, res: Response) => {
    const userId = req.user?._id;
    const { periodType = 'daily', count = 7 } = req.query; // Default to daily for last 7 days
    const limit = parseInt(count as string, 10);

    if (!userId) {
        throw new AppError('User not found', 401);
    }
    if (isNaN(limit) || limit <= 0 || limit > 100) { // Basic validation for count
        throw new AppError('Invalid count parameter for history. Must be between 1 and 100.', 400);
    }

    // This endpoint relies on the `ProductivityMetrics` model being populated regularly (e.g., daily)
    // with a field like `dailyProductivityScore` or `weeklyProductivityScore`.
    // For now, this is a placeholder as that population mechanism isn't built yet.
    
    // Example assuming ProductivityMetrics has 'date' and 'dailyProductivityScore'
    // and we want to fetch them in descending order of date.
    /*
    const metrics = await ProductivityMetrics.find({
        user: userId,
        // Add filter for periodType if storing weekly scores separately, e.g., metricType: periodType
    })
    .sort({ date: -1 })
    .limit(limit)
    .select('date dailyProductivityScore'); // Adjust field based on actual model structure

    const history = metrics.map(m => ({
        date: m.date.toISOString().split('T')[0],
        score: m.dailyProductivityScore
    })).reverse(); // Reverse to have oldest first for charting
    */

    // Placeholder response until ProductivityMetrics is fully integrated for historical scores
    res.status(200).json({ 
        message: "Score history endpoint placeholder. Daily/Weekly score aggregation in ProductivityMetrics model needed.",
        query: req.query,
        history: [] 
        // history: history // once implemented
    });
}); 