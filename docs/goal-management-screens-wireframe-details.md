# Goal Management Screens Wireframe Details

## Common Elements Across Goal Screens

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

## Screen 1: Goals Overview

### Layout Specifications
- Status Bar: System default
- Top App Bar: 56px
- Content: Scrollable
- Bottom Navigation: 56px
- Side Padding: 16px

### Component Details

1. View Toggle
   - Height: 48px
   - Options:
     * List View
     * Timeline View
   - Style: Segmented control
   - Position: Below app bar
   - Width: 160px
   - Alignment: Center

2. Category Filter Chips
   - Height: 32px
   - Horizontal scroll
   - Chip specifications:
     * Padding: 8px 16px
     * Icon: Left (16x16px)
     * Text: 14px font
     * Border radius: 16px
   - Categories:
     * All Goals
     * Personal
     * Career
     * Health
     * Custom categories
   - Add category button: Last chip

3. Goal Cards (List View)
   - Height: 160px each
   - Full width
   - Margin bottom: 16px
   - Layout:
     * Header section (48px):
       - Category pill
       - Due date
       - More options menu
     * Title section:
       - Goal title: 18px font
       - Description: 14px font (2 lines max)
     * Progress section:
       - Progress bar: 8px height
       - Percentage: 16px font
       - Status indicator
     * Footer section:
       - Milestone count
       - Related items count
       - Quick action buttons
   - States:
     * Not started
     * In progress
     * At risk
     * Completed
   - Corner radius: 12px
   - Elevation: Medium shadow

4. Timeline View
   - Full width
   - Layout:
     * Time axis: Left (72px width)
     * Goal blocks: Right
     * Connection lines
   - Time periods:
     * Months
     * Quarters
     * Years
   - Goal blocks:
     * Height: Based on duration
     * Width: Screen width - 88px
     * Progress indicator
     * Category color
   - Today indicator:
     * Red line
     * Full width
     * Label: "Today"

5. Quick Add FAB
   - Size: 56x56px
   - Position: Bottom right
   - Margin: 16px from edges
   - Icon: Plus
   - Elevation: Higher shadow

## Screen 2: Goal Creation

### Layout Specifications
- Full screen modal
- Top bar: 56px
- Content: Multi-step form
- Bottom: Action buttons
- Padding: 24px

### Component Details

1. Progress Stepper
   - Height: 48px
   - Steps:
     * Basic Info
     * Success Criteria
     * Milestones
     * Related Items
   - Active step: Primary color
   - Completed steps: Success color
   - Connecting lines
   - Step numbers

2. Step 1: Basic Info
   - Title Input:
     * Height: 56px
     * Font: 20px
     * Placeholder: "What do you want to achieve?"
   - Description Input:
     * Height: 120px
     * Multiline
     * Rich text options
   - Category Selection:
     * Height: 56px
     * Dropdown/Grid
     * Create new option
   - Due Date:
     * Height: 56px
     * Date picker
     * Optional time

3. Step 2: Success Criteria
   - Measurement Type:
     * Height: 56px
     * Options:
       - Numeric (with unit)
       - Percentage
       - Boolean
       - Custom
   - Target Value:
     * Height: 56px
     * Unit selector
     * Validation
   - Tracking Frequency:
     * Height: 56px
     * Daily/Weekly/Monthly
   - Progress Calculation:
     * Height: 80px
     * Formula preview
     * Custom formula option

4. Step 3: Milestones
   - Milestone List:
     * Each item: 72px height
     * Drag handle
     * Title input
     * Due date
     * Delete button
   - Add Milestone Button:
     * Height: 48px
     * Icon: Plus
     * Text: "Add Milestone"
   - Milestone Dependencies:
     * Connection lines
     * Order indicators
     * Dependency type

5. Step 4: Related Items
   - Related Tasks:
     * Search/Select interface
     * Selected items list
     * Create new option
   - Related Habits:
     * Similar to tasks
     * Habit frequency preview
   - Impact Weight:
     * Slider: 0-100%
     * Importance indicator

6. Action Buttons
   - Height: 48px
   - Primary Action:
     * "Next" or "Create"
     * Full width
   - Secondary Action:
     * "Back" or "Cancel"
     * Text button
   - Fixed at bottom

## Screen 3: Goal Detail

### Layout Specifications
- Full screen
- Top bar: 56px
- Content: Scrollable
- Bottom: Action bar
- Padding: 16px

### Component Details

1. Header Section
   - Height: 200px
   - Layout:
     * Title: 24px font
     * Description
     * Category pill
     * Due date
     * Status indicator
   - Background: Custom gradient
   - Progress ring: 80x80px
   - Quick actions:
     * Update progress
     * Add milestone
     * Share

2. Progress Timeline
   - Height: 160px
   - Layout:
     * Horizontal scroll
     * Time markers
     * Progress points
     * Trend line
   - Interactive points:
     * Tap for details
     * Add new entry
   - Time scale options:
     * Week
     * Month
     * Quarter
     * Year

3. Milestone Section
   - Header:
     * Title: "Milestones"
     * Progress: "X of Y completed"
     * Add button
   - Milestone Cards:
     * Height: 100px each
     * Status indicator
     * Due date
     * Dependencies
     * Progress bar
   - Timeline view option
   - Drag to reorder

4. Related Items Section
   - Height: Auto
   - Tabs:
     * Tasks
     * Habits
     * Notes
   - Item cards:
     * Height: 72px
     * Status
     * Progress
     * Quick actions

5. Analytics Section
   - Height: 280px
   - Charts:
     * Progress over time
     * Effort distribution
     * Success prediction
   - Key metrics:
     * Completion rate
     * Time remaining
     * Risk factors

6. Action Bar
   - Height: 56px
   - Update button
   - Edit button
   - Share button
   - More options menu

## Screen 4: Milestone Tracking

### Layout Specifications
- Full screen
- Top bar: 56px
- Content: Scrollable
- Padding: 16px

### Component Details

1. Goal Summary Card
   - Height: 100px
   - Compact view of parent goal
   - Progress indicator
   - Quick actions

2. Timeline View
   - Height: Auto
   - Vertical layout
   - Time axis: Left
   - Milestone blocks: Right
   - Connection lines
   - Today indicator

3. Milestone Cards
   - Height: 120px each
   - Layout:
     * Title
     * Due date
     * Status
     * Dependencies
     * Progress
   - States:
     * Not started
     * In progress
     * Completed
     * Blocked
   - Actions:
     * Update
     * Edit
     * Delete

4. Dependency Map
   - Height: 200px
   - Network graph
   - Node connections
   - Critical path
   - Interactive:
     * Zoom
     * Pan
     * Select

5. Progress Updates
   - Input form:
     * Update type
     * Value
     * Notes
     * Date
   - History list:
     * Timeline style
     * Update cards
     * Edit option

### Implementation Notes

#### Color System
- Primary: Brand color
- Success: #34C759
- Warning: #FFCC00
- Danger: #FF3B30
- Neutral: #8E8E93
- Background: #FFFFFF
- Surface: #F2F2F7

#### Typography Hierarchy
- Goal titles: 24px
- Section headers: 20px
- Card titles: 18px
- Body text: 16px
- Secondary text: 14px
- Labels: 12px

#### Interactive Elements
1. Goal Cards
   - Tap: Open detail
   - Long press: Quick actions
   - Swipe: Update progress
   - Progress animations

2. Timeline
   - Pinch to zoom
   - Scroll navigation
   - Tap points for details
   - Drag to adjust dates

3. Milestones
   - Drag to reorder
   - Dependency arrows
   - Progress updates
   - Status transitions

#### Animations
1. Progress Updates
   - Progress bar fill
   - Milestone completion
   - Status changes
   - Celebration effects

2. Transitions
   - Screen transitions
   - Step transitions
   - Card expand/collapse
   - Loading states

#### Accessibility
- Clear touch targets
- Color blind friendly
- Screen reader support
- Keyboard navigation
- Voice control support

This detailed specification provides exact measurements and requirements for implementing the goal management screens. Each component is broken down with specific dimensions, spacing, and interaction states to ensure consistent implementation.
