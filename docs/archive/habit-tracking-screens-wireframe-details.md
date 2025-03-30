# Habit Tracking Screens Wireframe Details

## Common Elements Across Habit Screens

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

## Screen 1: Habit List/Overview

### Layout Specifications
- Status Bar: System default
- Top App Bar: 56px
- Content: Scrollable
- Bottom Navigation: 56px
- Side Padding: 16px

### Component Details

1. Category Filter Tabs
   - Height: 48px
   - Horizontal scroll
   - Tab width: Auto (based on text)
   - Minimum tab width: 80px
   - Active indicator: Bottom line
   - Categories:
     * All Habits
     * Health
     * Productivity
     * Learning
     * Custom categories

2. Today's Progress Card
   - Height: 120px
   - Full width
   - Layout:
     * Date: Top left (16px font)
     * Overall progress: Top right
     * Circular progress indicator: 64x64px
     * Completion rate: Below circle
     * Motivational message: Bottom
   - Background: Gradient or solid
   - Corner radius: 12px
   - Elevation: Medium shadow

3. Habit Cards
   - Height: 100px each
   - Full width
   - Margin bottom: 12px
   - Layout:
     * Left section (30%):
       - Icon: 32x32px
       - Category indicator
       - Current streak
     * Middle section (50%):
       - Title: 16px font
       - Schedule: 14px font
       - Progress bar
     * Right section (20%):
       - Quick complete button
       - More options
   - States:
     * Not started
     * In progress
     * Completed
     * Missed
   - Corner radius: 8px
   - Shadow: Subtle

4. Quick Add FAB
   - Size: 56x56px
   - Position: Bottom right
   - Margin: 16px from edges
   - Icon: Plus
   - Elevation: Higher shadow

## Screen 2: Habit Creation/Edit

### Layout Specifications
- Full screen modal
- Top bar: 56px
- Content: Scrollable
- Bottom: Action buttons
- Padding: 24px

### Component Details

1. Top Bar
   - Close button: Left
   - Title: "New Habit"/"Edit Habit"
   - Save button: Right
   - Divider below

2. Title Section
   - Height: 80px
   - Title input:
     * Height: 56px
     * Font: 20px
     * Placeholder: "Habit name"
   - Icon selector:
     * Grid of icons
     * Custom upload option
     * Size: 32x32px each

3. Description Input
   - Height: 80px
   - Multiline
   - Placeholder: "Why do you want to build this habit?"
   - Character limit: 200

4. Frequency Selector
   - Section height: 160px
   - Options:
     * Daily
     * Weekly
     * Monthly
     * Custom
   - When "Custom":
     * Day picker grid
     * Time picker
     * Interval setter

5. Time Preference
   - Height: 72px
   - Time picker trigger
   - Flexibility range:
     * Slider: 0-120 minutes
     * Presets: Strict, Flexible, Any time

6. Success Criteria
   - Height: 120px
   - Type selector:
     * Boolean (Done/Not done)
     * Numeric (with unit)
     * Timer (duration)
     * Checklist
   - Target input
   - Minimum threshold

7. Reminder Settings
   - Height: 160px
   - Time picker
   - Repeat options
   - Notification style:
     * Basic
     * Motivational
     * Custom message

8. Category Assignment
   - Height: 72px
   - Dropdown or grid
   - Create new option
   - Color picker for new

9. Difficulty Level
   - Height: 56px
   - Options: Easy, Medium, Hard
   - Visual indicators
   - Points value preview

10. Action Buttons
    - Save: Primary button
    - Cancel: Text button
    - Width: Match parent
    - Height: 48px
    - Fixed at bottom

## Screen 3: Habit Detail

### Layout Specifications
- Full screen
- Top bar: 56px
- Content: Scrollable
- Bottom: Action bar
- Padding: 16px

### Component Details

1. Header Card
   - Height: 160px
   - Layout:
     * Icon: 48x48px
     * Title: 24px font
     * Category pill
     * Current streak
     * All-time best streak
   - Background: Custom gradient
   - Padding: 24px

2. Progress Section
   - Height: 200px
   - Today's status
   - Week view:
     * 7-day grid
     * Status indicators
     * Quick complete
   - Monthly calendar:
     * Heat map style
     * Legend
     * Navigation arrows

3. Streak Information
   - Height: 120px
   - Current streak card:
     * Large number
     * "Days" label
     * Flame icon
   - Best streak card:
     * Trophy icon
     * Record number
     * Date achieved

4. Statistics Cards
   - Height: 100px each
   - Grid layout (2x2):
     * Completion rate
     * Average timing
     * Total completions
     * Points earned
   - Each card:
     * Icon
     * Value
     * Label
     * Trend indicator

5. History Graph
   - Height: 240px
   - Line/Bar chart
   - Time period selector
   - Data points:
     * Completion status
     * Timing
     * Streak phases
   - Interactive tooltips

6. Notes Section
   - Height: Auto
   - Add note button
   - Note cards:
     * Date
     * Content
     * Images (if any)
     * Edit/Delete

7. Action Bar
   - Height: 56px
   - Complete/Skip button
   - Edit button
   - Share button
   - Archive button

## Screen 4: Streak Analytics

### Layout Specifications
- Full screen
- Top bar: 56px
- Content: Scrollable
- Padding: 16px

### Component Details

1. Overview Cards
   - Height: 120px each
   - Current Streak Card:
     * Large streak number
     * Flame animation
     * "Days" label
     * Previous best comparison
   - Success Rate Card:
     * Percentage
     * Circular progress
     * 30-day average
   - Points Card:
     * Total points
     * Level progress
     * Rewards available

2. Streak Calendar
   - Height: 300px
   - Month view
   - Heat map style
   - Legend:
     * Perfect day
     * Partial completion
     * Missed day
   - Navigation:
     * Month selector
     * Arrow controls

3. Performance Analysis
   - Height: 200px
   - Charts:
     * Completion by day
     * Time of day pattern
     * Duration trend
   - Insights:
     * Best performing days
     * Challenging periods
     * Improvement suggestions

4. Achievement Timeline
   - Height: Auto
   - Vertical timeline
   - Milestone markers:
     * Streak achievements
     * Level ups
     * Badges earned
   - Date labels
   - Icons for each type

5. Improvement Suggestions
   - Height: Auto
   - Cards with:
     * Insight title
     * Data-backed explanation
     * Actionable tips
     * Apply button

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
- Screen titles: 24px
- Card titles: 20px
- Primary text: 16px
- Secondary text: 14px
- Labels: 12px

#### Interactive Elements
1. Habit Cards
   - Tap: Open detail
   - Long press: Quick actions
   - Swipe: Complete/Skip
   - Progress animations

2. Charts and Graphs
   - Tap points for details
   - Pinch to zoom
   - Scroll through time
   - Export options

3. Streak Features
   - Milestone celebrations
   - Recovery suggestions
   - Freeze streak option
   - Share achievements

#### Animations
1. Status Changes
   - Completion checkmark
   - Progress bar fill
   - Streak counter increment
   - Celebration effects

2. Transitions
   - Screen transitions
   - Card expand/collapse
   - Data loading states
   - Success feedback

#### Accessibility
- Clear touch targets
- Color blind friendly
- Screen reader support
- Reduced motion option
- Voice control support

This detailed specification provides exact measurements and requirements for implementing the habit tracking screens. Each component is broken down with specific dimensions, spacing, and interaction states to ensure consistent implementation.
