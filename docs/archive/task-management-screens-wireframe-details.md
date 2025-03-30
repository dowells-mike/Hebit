# Task Management Screens Wireframe Details

## Common Elements Across Task Screens

### Top App Bar
- Height: 56px
- Title: Screen-specific
- Font: Heading 3 (20px)
- Right Actions: Search, Filter, More
- Elevation: Subtle shadow
- Background: Primary color or white

### Bottom Navigation
- Height: 56px
- Items: Dashboard, Tasks, Habits, Goals, Profile
- Active State: Icon + Label
- Inactive State: Icon only
- Selected Item: Primary color
- Unselected Items: Gray (#666666)

## Screen 1: Task List

### Layout Specifications
- Status Bar: System default
- Top App Bar: 56px
- Content: Scrollable
- Bottom Navigation: 56px
- Side Padding: 16px

### Component Details

1. Search & Filter Bar
   - Height: 48px
   - Search Input:
     * Width: 70% of screen
     * Left icon: Search (24x24px)
     * Border radius: 24px
     * Background: Light gray
   - Filter Button:
     * Width: 25% of screen
     * Icon: Filter (24x24px)
     * Text: "Filter"

2. View Toggle
   - Height: 48px
   - Options: List/Board
   - Style: Segmented control
   - Width: 120px
   - Position: Below search bar
   - Alignment: Right

3. Task List Items
   - Height: 72px per item
   - Layout:
     * Checkbox: Left (24x24px)
     * Content: Middle
       - Title: 16px font
       - Due date: 14px font
       - Category: Label pill
     * Priority: Right indicator
   - Swipe Actions:
     * Right: Complete/Delete
     * Left: Schedule/Edit

4. Quick Add Bar
   - Height: 48px
   - Position: Bottom fixed
   - Above navigation
   - Input field:
     * Left icon: Plus
     * Placeholder text
     * Right: Quick actions

5. FAB (New Task)
   - Size: 56x56px
   - Position: Bottom right
   - Margin: 16px from edges
   - Icon: Plus
   - Elevation: Higher shadow

## Screen 2: Task Creation/Edit

### Layout Specifications
- Full screen modal
- Top bar: 56px
- Content: Scrollable
- Bottom: Action buttons
- Padding: 24px

### Component Details

1. Top Bar
   - Close button: Left
   - Title: "New Task"/"Edit Task"
   - Save button: Right
   - Divider below

2. Title Input
   - Height: 56px
   - Font: 20px
   - No label
   - Placeholder: "Task title"
   - Full width
   - Border bottom only

3. Description Input
   - Min height: 80px
   - Multiline
   - Rich text controls:
     * Bold, Italic, Lists
     * Height: 48px
     * Horizontal scroll

4. Due Date & Time
   - Height: 72px
   - Date picker trigger
   - Time picker trigger
   - Icons: Calendar, Clock
   - Selected values display

5. Priority Selector
   - Height: 56px
   - Options: Low, Medium, High
   - Style: Segmented buttons
   - Icons: Flag (different colors)

6. Category Dropdown
   - Height: 56px
   - Icon: Folder
   - Selected category chip
   - Create new option

7. Labels Input
   - Height: 56px
   - Chip input style
   - Add new label option
   - Horizontal scroll

8. Additional Options
   - Reminder toggle
   - Recurrence selector
   - Attachment button
   - Sub-tasks toggle
   - Height: 56px each

9. Action Buttons
   - Save: Primary button
   - Cancel: Text button
   - Width: Match parent
   - Height: 48px
   - Fixed at bottom

## Screen 3: Task Detail

### Layout Specifications
- Full screen
- Top bar: 56px
- Content: Scrollable
- Bottom: Action bar
- Padding: 16px

### Component Details

1. Header Section
   - Title: 24px font
   - Status pill
   - Due date
   - Priority indicator
   - Height: Auto
   - Padding: 16px

2. Description Section
   - Rich text content
   - Expandable/Collapsible
   - Padding: 16px
   - Background: Subtle

3. Meta Information
   - Category
   - Labels
   - Created date
   - Last modified
   - Height: Auto
   - Style: List items

4. Sub-tasks Section
   - Header with count
   - Add sub-task input
   - Checklist style
   - Progress indicator
   - Individual items:
     * Height: 48px
     * Checkbox
     * Title
     * Due date (optional)

5. Comments Section
   - Input field:
     * Height: 48px
     * Avatar
     * Attachment option
   - Comment list:
     * Avatar
     * Name
     * Timestamp
     * Content
     * Actions

6. Activity Log
   - Timeline style
   - Icons for actions
   - Timestamps
   - User avatars
   - Collapsible sections

7. Action Bar
   - Height: 56px
   - Complete button
   - Edit button
   - Share button
   - More options menu

## Screen 4: Task Categories

### Layout Specifications
- Full screen
- Top bar: 56px
- Content: Scrollable list
- FAB: Add category
- Padding: 16px

### Component Details

1. Category List Items
   - Height: 72px
   - Color indicator: Left
   - Name: 16px font
   - Task count: 14px font
   - Chevron: Right
   - Swipe actions:
     * Edit
     * Delete
     * Archive

2. Category Creation Modal
   - Title input
   - Color picker
   - Icon selector (optional)
   - Parent category (optional)
   - Save/Cancel buttons

3. Empty State
   - Illustration
   - Message
   - Create button
   - Center aligned

## Screen 5: Board View

### Layout Specifications
- Horizontal scroll container
- Column width: 280px
- Padding: 16px
- Gap: 16px

### Component Details

1. Column Headers
   - Height: 48px
   - Title
   - Task count
   - Add task button
   - More options menu

2. Task Cards
   - Width: 264px
   - Margin: 8px
   - Shadow: Subtle
   - Contents:
     * Title
     * Due date
     * Priority
     * Labels
     * Assignee
     * Progress

3. Column Actions
   - Add task
   - Edit column
   - Move column
   - Archive column

### Implementation Notes

#### Interaction States
1. Task Items
   - Tap: Open detail
   - Long press: Drag
   - Swipe: Quick actions
   - Checkbox: Complete animation

2. Input Fields
   - Focus states
   - Validation feedback
   - Error messages
   - Character counts

3. Buttons
   - Pressed state
   - Loading state
   - Disabled state
   - Ripple effect

#### Animations
1. List/Board Transitions
   - Smooth layout change
   - Card scale effect
   - Content fade

2. Modal Transitions
   - Slide up
   - Fade background
   - Spring effect

3. Completion Actions
   - Checkbox animation
   - Strike-through effect
   - Success feedback

#### Accessibility
- Touch targets: 48x48px minimum
- Clear error messages
- High contrast text
- Screen reader support
- Keyboard navigation
- Focus indicators

This detailed specification provides exact measurements and requirements for implementing the task management screens. Each component is broken down with specific dimensions, spacing, and interaction states to ensure consistent implementation.
