# Deep Table of Functionalities/Tasks for the "Tasks" Screens

---

## I. Core Task Management (CRUD & Display)

### A. Create New Task Screen *(New Task screen - Screenshot 4)*

1. **UI Elements & Basic Functionality:**
    a. **X (Close)** button: Discards changes and navigates back.  
    b. **Save** button: Validates and saves the task.  
    c. **Task title input field:**
        - i.  Required field.  
        - ii. Character limit *(e.g., 255 chars)*.  
    d. **Rich Text Editor for Description** (Bold, Italic, Bullet List, Numbered List):
        - i. *"Add description..."* input field:
            1. Optional.  
            2. Supports basic markdown or HTML for formatting.  
    e. **Date Picker** *(e.g., May 20, 2025)*:
        - i. Select due date.  
        - ii. Defaults to today or no date.  
        - iii. Clearable.  
    f. **Time Picker** *(e.g., 2:00 PM)*:
        - i. Select due time.  
        - ii. Enabled only if a date is selected.  
        - iii. Clearable.  
    g. **Priority Selector** *(Low, Medium, High)*:
        - i. Defaults to Medium or no priority.  
        - ii. Only one can be selected.  
    h. **Category Selector** *(Category: General >)*:
        - i. Opens dialog/screen to pick or create category.  
        - ii. Displays selected category.  
        - iii. Defaults to "General" or "Uncategorized".  
    i. **Recurrence Selector** *(Recurrence: Not repeating >)*:
        - i. Opens dialog to set rules *(daily, weekly, etc.)*.  
        - ii. Displays rule summary.  
        - iii. Defaults to "Not repeating".  
    j. **Subtasks Toggle & Adder**:
        - i. Checkbox to show/hide subtasks.  
        - ii. **+ icon** adds subtasks.  
        - iii. Allows multiple items.  
        - iv. Subtasks include a delete icon.  
    k. **Reminder Selector** *(Reminder: No reminder >)*:
        - i. Dialog to set one/more reminders *(e.g., 5 mins before)*.  
        - ii. Displays summary.  
        - iii. Defaults to "No reminder".  
    l. **Cancel** button: Discards changes.  
    m. **Save Task** button (bottom): Validates & saves.  

2. **Logic & Validation:**
    a. Title is **mandatory**.  
    b. If **time** is set, **date** must be set.  
    c. Save button enabled only after **validation**.  
    d. Support **state persistence** across navigation.  

---

### B. View Tasks Screen *(Screenshot 5)*

1. **UI Elements - Header/Top Bar:**
    a. Header: "Tasks"  
    b. **Search Icon**: Toggles or navigates to search bar.  
    c. **Filter/Sort Icon**: Opens filtering/sorting bottom sheet.  
    d. **More Options (Kebab)**: Delete all completed, settings, etc.  

2. **Search Bar** *(Search tasks...):**
    a. Filters tasks in real-time.  
    b. Searches titles and descriptions.  

3. **Task List Area:**
    a. Empty State:
        > "No tasks yet. Tap + to create your first task"  
    b. Populated list: Scrollable, auto-sorted.  

4. **Floating Action Button (FAB)** (+):  
    a. Navigates to **Create Task** screen.  

5. **Individual Task Item in List:**
    a. Completion Checkbox  
    b. Title  
    c. Due Date & Time (e.g., "Tomorrow, 2:00 PM")  
    d. Priority Indicator  
    e. Category Tag (Optional)  
    f. Subtask Progress (e.g., "2/5 subtasks")  
    g. Recurrence Icon  
    h. Tap: Navigates to task details or edit screen  
    i. Optional Swipe Actions:
        - i. Left: Delete  
        - ii. Right: Complete / Reschedule  

6. **Filtering Options:**
    a. By Status: All, Pending, Completed, Overdue  
    b. By Priority: Low, Medium, High  
    c. By Due Date: Today, Tomorrow, Next 7 Days, Custom, No Due Date  
    d. By Category  
    e. **Clear All Filters**  

7. **Sorting Options:**
    a. By Due Date: Asc/Desc  
    b. By Priority: High → Low / Low → High  
    c. By Creation Date: Newest/Oldest  
    d. By Title: A-Z / Z-A  

---

### C. Edit Task Screen

1. Same structure and UI as **Create Task**, but pre-filled.  
2. **Save** button updates task.  
3. **Delete** button (if needed).  

---

### D. Task Details Screen *(Optional)*

1. Read-only view of task data.  
2. **Edit** button navigates to edit mode.  
3. **Delete** button.  
4. Toggle **complete/incomplete**.  
5. View and manage **subtasks**.  

---

### E. Delete Task

1. Confirm deletion dialog.  
2. **Soft delete** vs **Hard delete** options.  
3. If task is recurring, user can delete:
    a. This instance only  
    b. This + all future  
    c. All instances  

---

## II. Advanced Task Features

### A. Subtasks Management

1. **In Task Editor:**
    a. Add Subtask  
    b. Delete Subtask  
    c. Reorder (drag & drop)  
2. **In Task List / Details:**
    a. View subtasks  
    b. Mark subtask as complete/incomplete  
3. **Optional**: Parent task auto-completes when all subtasks are done.  

---

### B. Recurrence Engine

1. **Recurrence Rules Definition & Storage:**
    a. Support standard recurrence patterns:
        - i. Daily (every X days, specific days of the week if X > 1)
        - ii. Weekly (every X weeks, on specific days of the week - e.g., Mon, Wed, Fri)
        - iii. Monthly (every X months, on a specific day of the month - e.g., the 15th, or a relative day like "the second Tuesday")
        - iv. Yearly (every X years, on a specific month and day - e.g., July 26th, or "the last Friday of October")
    b. **Data Storage**:
        - i. Store recurrence rules using a standard format (e.g., iCalendar `RRULE` string - RFC 5545). This is robust and widely supported.
        - ii. Include `DTSTART` (the first occurrence's date/time) as part of the rule definition.
    c. **Rule Parameters**:
        - i. `FREQ`: (DAILY, WEEKLY, MONTHLY, YEARLY)
        - ii. `INTERVAL`: (every X days/weeks/months/years)
        - iii. `BYDAY`: (e.g., MO, TU, WE, TH, FR, SA, SU)
        - iv. `BYMONTHDAY`: (e.g., 1, 15, -1 for last day)
        - v. `BYSETPOS`: (e.g., 1 for first, -1 for last, used with `BYDAY` for "nth weekday of month")
        - vi. `BYMONTH`: (1-12)
2. **Recurrence End Conditions:**
    a. `COUNT`: End after X occurrences.
    b. `UNTIL`: End on a specific date (inclusive).
    c. Never (no end date/count specified).
3. **Instance Management & Generation:**
    a. **Dynamic Generation**: Future instances are typically generated on-the-fly for display rather than pre-storing a large number of them.
        - i. Define a reasonable horizon for generating instances (e.g., next 1-2 years).
    b. **Original Task as Template**: The original task serves as the template for all instances.
    c. **Exception Handling**:
        - i. Store exceptions separately. If an instance is modified (e.g., time changed, description updated) or deleted, it becomes an exception.
        - ii. An exception record should link to the original recurring task ID and store the original date of the instance it's replacing/modifying, along with the new data.
        - iii. Deleting an instance can create an `EXDATE` (exception date) in iCalendar terms.
    d. **Time Zone Handling**:
        - i. All dates and times should be stored in UTC in the backend.
        - ii. Recurrence rules should be applied based on the user's current timezone or a specified timezone for the task.
        - iii. Floating tasks (no specific time, just date) should adapt to the user's current timezone for their "due day".
4. **Editing & Deleting Recurring Tasks:**
    a. When editing properties (title, description, etc.):
        - i. Apply to "This instance only" (creates an exception).
        - ii. Apply to "This and all future instances" (ends the current series at the previous instance and starts a new recurring series with the changes).
        - iii. Apply to "All instances" (modifies the original template task and all its occurrences, potentially invalidating past exceptions if the core rule changes).
    b. When deleting:
        - i. "This instance only" (creates an `EXDATE`).
        - ii. "This and all future instances" (modifies the `UNTIL` or `COUNT` of the original rule to end before this instance).
        - iii. "All instances" (deletes the main recurring task and all its associated instances/exceptions).
    c. Changing the recurrence rule itself:
        - i. Typically applies to "This and all future instances" or "All instances". Careful consideration for existing exceptions.
5. **Completion of Recurring Tasks:**
    a. Completing an instance marks only that specific instance as done.
    b. The next instance in the series will still appear as due.

---

### C. Reminders & Notifications

1. **Reminder Configuration:**
    a. **Multiple Reminders per Task**: Allow users to set several reminders for a single task.
    b. **Reminder Types**:
        - i. At time of due date/time.
        - ii. Minutes before (e.g., 5, 10, 15, 30, 60 minutes).
        - iii. Hours before (e.g., 1, 2, 3 hours).
        - iv. Days before (e.g., 1, 2 days).
        - v. Custom date and time for a specific reminder.
    c. **Storage**:
        - i. Relative reminders (e.g., "15 minutes before") stored as an offset from the due time.
        - ii. Absolute reminders (custom date/time) stored as a specific timestamp (UTC).
        - iii. Link reminders to the specific task ID. For recurring tasks, reminders apply to each instance unless overridden for a specific instance.
2. **Notification System:**
    a. **Delivery Mechanism**:
        - i. **Local Notifications**: Scheduled by the mobile app using platform-specific APIs (e.g., `AlarmManager` or `WorkManager` on Android, `UserNotifications` framework on iOS). Suitable for offline reminders.
        - ii. **Push Notifications (Optional but Recommended for Robustness)**: Sent from the backend server (e.g., via FCM for Android, APNS for iOS).
            - Useful for ensuring reminder delivery even if the app is not active or for cross-device reminders.
            - Requires backend infrastructure for scheduling and sending.
    b. **Notification Content**:
        - i. Task Title.
        - ii. Due time (if applicable).
        - iii. Short description snippet (optional).
        - iv. App icon and name.
    c. **Notification Channels (Android Specific)**:
        - i. Use appropriate notification channels (e.g., "Task Reminders", "Overdue Tasks").
        - ii. Allow users to customize channel settings (sound, vibration, importance) in system settings.
    d. **Sound & Vibration**:
        - i. Customizable default notification sound.
        - ii. Option for vibration.
        - iii. User can override per-app or per-channel system settings.
3. **Notification Actions & Interactivity:**
    a. **Standard Actions**:
        - i. **Mark as Done**: Completes the task directly from the notification.
        - ii. **Snooze**: Dismisses the notification and schedules a new reminder for a short period later (e.g., 5, 10, 15 minutes).
        - iii. **View Task**: Opens the app to the specific task details screen.
    b. **Quick Reply (Advanced)**: For certain notifications, allow a quick text reply that could be added as a comment to the task.
4. **Snooze Functionality:**
    a. Predefined snooze options (e.g., 5 min, 15 min, 1 hour).
    b. Option for custom snooze time/date.
5. **User Settings for Reminders & Notifications:**
    a. **Default Reminder Times**: Allow users to set default reminder preferences for new tasks (e.g., "always remind 30 minutes before").
    b. **Enable/Disable Notifications**: Global switch for all task notifications.
    c. **Notification Sound & Vibration Preferences** (if not solely relying on system channel settings).
    d. **Quiet Hours/Do Not Disturb**: Respect system DND settings or offer in-app quiet hours.
6. **Persistence & Reliability:**
    a. Reminders must persist even if the app is closed or the device is restarted. This usually involves scheduling with the OS.
    b. If using server-side push, ensure reliable delivery and handling of unreceived notifications (e.g., when device was offline).
7. **Contextual/Smart Reminders (Optional - Links to ML Features):**
    a. **Location-based reminders**: Trigger when the user arrives at or leaves a specific location (requires location permission and geofencing).
    b. **Time-based suggestions for reminders**: e.g., "You usually set a reminder 1 hour before for work tasks. Add one?"

---

### D. Categories/Tags Management

1. Management Screen:
    a. Create/Edit/Delete categories/tags  
    b. Assign category colors  
2. Tasks can have **multiple tags/categories**  

---

### E. Task Dependencies

1. Tasks can be dependent on completion of others  
2. **Optional visualization** – lightweight graph/tree  

---

### F. App Section Integrations

1. **Dashboard ("Today's Overview")**
    a. Display **High Priority / Due Today**  
    b. "See All" links to Task screen (filtered)  
2. **Goals:**
    a. Tasks linked to goals update progress  
3. **Statistics/Profile:**
    a. Track completion rate, streaks  
    b. Analytics by category, priority  

---

## III. ML Feature: Task Suggestion 🎯

### A. Goal

1. Suggest helpful, timely tasks based on behavior and context  

---

### B. Data Inputs for ML (Backend)

1. Historical Task Data:
    a. Titles, descriptions, tags, priority, due/completion  
2. Habit Data:
    a. Frequency, times, success rates  
3. Goal Data:
    a. Linked tasks, descriptions, deadlines  
4. Activity Patterns:
    a. Times/days tasks are created/completed  
    b. Frequent categories/themes  
5. **Optional Context Data** (*Privacy-respecting*):
    a. Time of day / day of week  
    b. Location-based context  
    c. Calendar events (if integrated)  
6. Task Templates:
    a. Common suggestions per category (e.g., Health, Work)  

---

### C. ML Model Approaches

1. **Collaborative Filtering**  
2. **Content-Based Filtering** (using NLP)  
3. **Rule-Based System**:
    a. If goal = "Fitness", suggest "Workout today"  
    b. If Monday, suggest "Plan your week"  
4. **Sequence Modeling** (Advanced):  
    a. Task A → Task B flow  
5. **Reinforcement Learning**:
    a. Learns from accept/dismiss actions  

---

### D. Suggestion Delivery (Front + Back)

1. **In-App Prompts:**
    a. Dashboard suggestions: "Feeling stuck? Try..."  
    b. Empty state suggestions  
    c. Smart auto-complete during task creation  
2. **Push Notifications:**
    a. Time-based nudges  
    b. Contextual prompts (e.g., goal-related)

---

### E. User Interaction with Suggestions

1. **Add Task**: Add pre-filled suggested task  
2. **Dismiss** or **Not Now**: Hide card  
3. **Not Relevant**: Offers feedback to backend model  

---

### F. ML Feature Technicals

1. **Backend:**
    a. Data collection / prep pipeline  
    b. ML model training & hosting  
    c. Suggestion API  
    d. Feedback collection system  
2. **Frontend:**
    a. UI for cards, suggestions, banners  
    b. Integrate API requests  
    c. Handle add/dismiss/feedback flow  
    d. User settings to manage frequency & types of suggestions  

---