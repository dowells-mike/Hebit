import { AppError } from '../middleware/errorHandler';
import { Reminder } from '../types'; // We'll define/update this type definition next

/**
 * Processes and validates an array of reminder inputs.
 * @param remindersInput The raw reminder objects from the request.
 * @param dueDate The due date of the task (required for relative reminders).
 * @returns A validated array of Reminder objects.
 * @throws AppError if validation fails.
 */
export const processReminders = (remindersInput: any[], dueDate?: Date | string | null): Reminder[] => {
  if (!remindersInput || !Array.isArray(remindersInput)) {
    return []; // No reminders to process or invalid format
  }

  const processedReminders: Reminder[] = [];

  for (const r of remindersInput) {
    if (!r.type || (r.type !== 'absolute' && r.type !== 'relative')) {
      throw new AppError('Invalid reminder type. Each reminder must have a type: "absolute" or "relative".', 400);
    }

    const reminder: Partial<Reminder> = { type: r.type };

    if (r.type === 'absolute') {
      if (!r.absoluteTime || isNaN(new Date(r.absoluteTime).getTime())) {
        throw new AppError('Invalid or missing absoluteTime for absolute reminder. Please provide a valid date and time.', 400);
      }
      reminder.absoluteTime = new Date(r.absoluteTime);
    } else { // type === 'relative'
      const taskDueDate = dueDate ? new Date(dueDate) : null;
      if (!taskDueDate || isNaN(taskDueDate.getTime())) {
        throw new AppError('A valid due date must be set on the task to use relative reminders.', 400);
      }
      // offsetMinutes represents minutes *before* the due date.
      // It should be a non-negative number. E.g., 0 for "at time of due date", 5 for "5 minutes before".
      if (typeof r.offsetMinutes !== 'number' || r.offsetMinutes < 0) {
        throw new AppError(
          'Invalid or missing offsetMinutes for relative reminder. Must be a non-negative number (e.g., 0 for on-time, 5 for 5 minutes before).',
          400
        );
      }
      reminder.offsetMinutes = r.offsetMinutes;
    }
    processedReminders.push(reminder as Reminder);
  }
  return processedReminders;
}; 