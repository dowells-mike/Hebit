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
    c. Due Date & Time (e.g., “Tomorrow, 2:00 PM”)  
    d. Priority Indicator  
    e. Category Tag (Optional)  
    f. Subtask Progress (e.g., “2/5 subtasks”)  
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

1. **Recurrence Rules:**
    a. Daily (e.g., every X days)  
    b. Weekly (specific weekdays, every X weeks)  
    c. Monthly (e.g., 2nd Tuesday)  
    d. Yearly  
2. **Recurrence End Conditions:**
    a. Never  
    b. After X times  
    c. End on specific date  
3. **Instance Management:**
    a. Generated dynamically or stored in backend  
4. **Edit/Delete Recurring Tasks:**
    a. Modify/delete:
        - i. This instance  
        - ii. This + future  
        - iii. All  

---

### C. Reminders & Notifications

1. Multiple reminders per task  
2. System-level notifications  
3. **Snooze** functionality  
4. Notification actions:
    a. Mark as done  
    b. Snooze  
    c. View task  

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
    b. “See All” links to Task screen (filtered)  
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
    a. If goal = “Fitness”, suggest "Workout today"  
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