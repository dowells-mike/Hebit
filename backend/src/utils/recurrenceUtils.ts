import { RRule, RRuleSet, rrulestr, Options as RRuleOptions } from 'rrule';
import { TaskDocument } from '../types';

/**
 * Calculates upcoming occurrences for a recurring task.
 *
 * @param task The task document, which includes recurrenceRule, recurrenceStartDate, and recurrenceExceptions.
 * @param count The maximum number of occurrences to return.
 * @param fromDate The date from which to start calculating occurrences.
 * @param untilDate Optional date to limit occurrences until.
 * @returns An array of Date objects representing the upcoming occurrences.
 */
export const calculateUpcomingOccurrences = (
  task: TaskDocument,
  count: number,
  fromDate: Date, // This is the local 'today' from the user's perspective
  untilDate?: Date // Optional local 'until' date from user's perspective
): Date[] => {
  if (!task.recurrenceRule || !task.recurrenceStartDate) {
    return [];
  }

  try {
    // --- DTSTART Handling ---
    // rrule.js works best with DTSTART in UTC. 
    // task.recurrenceStartDate is assumed to be a JS Date object that was intended for UTC storage, 
    // or represents the local start date/time that needs to be treated as UTC for the rule.
    const dtStartUTC = new Date(task.recurrenceStartDate);
    // Ensure it's treated as UTC by rrule by formatting it explicitly as a UTC string for rrulestr
    // Format: YYYYMMDDTHHMMSSZ
    const dtStartString = dtStartUTC.getUTCFullYear() +
                        ("0" + (dtStartUTC.getUTCMonth() + 1)).slice(-2) +
                        ("0" + dtStartUTC.getUTCDate()).slice(-2) + 'T' +
                        ("0" + dtStartUTC.getUTCHours()).slice(-2) +
                        ("0" + dtStartUTC.getUTCMinutes()).slice(-2) +
                        ("0" + dtStartUTC.getUTCSeconds()).slice(-2) + 'Z';

    const fullRuleString = `DTSTART:${dtStartString}\n${task.recurrenceRule}`;
    
    const ruleOrSet = rrulestr(fullRuleString, { unfold: true });

    let rruleSet: RRuleSet;
    if (ruleOrSet instanceof RRuleSet) {
      rruleSet = ruleOrSet;
    } else if (ruleOrSet instanceof RRule) {
      rruleSet = new RRuleSet();
      rruleSet.rrule(ruleOrSet);
    } else {
      // This case should ideally not happen if rrulestr is working as expected with a DTSTART
      console.error('Failed to parse rrule string into RRule or RRuleSet', task._id);
      return [];
    }

    // --- EXDATE Handling ---
    if (task.recurrenceExceptions && task.recurrenceExceptions.length > 0) {
      task.recurrenceExceptions.forEach(exDateLocal => {
        if (exDateLocal) {
          // Convert local exDate to UTC for rrule.js exdate function
          const exDateUTC = new Date(Date.UTC(
            exDateLocal.getFullYear(), // Assuming exDateLocal is a JS Date object
            exDateLocal.getMonth(),
            exDateLocal.getDate(),
            exDateLocal.getHours(),
            exDateLocal.getMinutes(),
            exDateLocal.getSeconds()
          ));
          rruleSet.exdate(exDateUTC);
        }
      });
    }

    // --- Search Window (fromDate and untilDate) Handling in UTC ---
    // Convert the local `fromDate` (e.g., today in user's timezone) to UTC start of that day
    const searchStartUTC = new Date(Date.UTC(fromDate.getFullYear(), fromDate.getMonth(), fromDate.getDate(), 0, 0, 0, 0));

    let searchEndUTC;
    if (untilDate) {
      // Convert local `untilDate` to UTC end of that day
      searchEndUTC = new Date(Date.UTC(untilDate.getFullYear(), untilDate.getMonth(), untilDate.getDate(), 23, 59, 59, 999));
    } else {
      // Default search window: 2 years from searchStartUTC, end of day
      searchEndUTC = new Date(Date.UTC(searchStartUTC.getUTCFullYear() + 2, searchStartUTC.getUTCMonth(), searchStartUTC.getUTCDate(), 23, 59, 59, 999));
    }
    
    const occurrencesUTC = rruleSet.between(searchStartUTC, searchEndUTC, true);

    // Filter occurrences: only those that are on or after the original `fromDate` (in its local context)
    // and then take the count. The dates returned by rrule.js `between` will be JS Date objects
    // representing UTC moments. These can be used as-is or converted to local time on client.
    const finalOccurrences = occurrencesUTC
      .filter(occUTC => occUTC.getTime() >= fromDate.getTime())
      .slice(0, count);

    return finalOccurrences;

  } catch (error) {
    console.error(`Error in calculateUpcomingOccurrences for task ${task._id}:`, error);
    return [];
  }
}; 