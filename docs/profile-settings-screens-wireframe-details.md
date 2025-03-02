# Profile & Settings Screens Wireframe Details

## Common Elements Across Profile Screens

### Top App Bar
- Height: 56px
- Title: Screen-specific
- Font: Heading 3 (20px)
- Right Actions: Edit, More
- Elevation: Subtle shadow
- Background: Primary color or white

### Bottom Navigation
- Height: 56px
- Items: Dashboard, Tasks, Habits, Goals, Profile
- Active State: Icon + Label
- Inactive State: Icon only
- Selected Item: Primary color
- Unselected Items: Gray (#666666)

## Screen 1: Profile Overview

### Layout Specifications
- Status Bar: System default
- Cover Photo: 200px height
- Content: Scrollable
- Bottom Navigation: 56px
- Side Padding: 16px

### Component Details

1. Profile Header
   - Cover Photo:
     * Height: 200px
     * Edit button: Top right
     * Gradient overlay: Bottom
   - Profile Picture:
     * Size: 120x120px
     * Border: 4px white
     * Position: -60px from bottom of cover
     * Edit button: Bottom right
   - User Info:
     * Name: 24px font
     * Username: 16px font
     * Bio: 14px font (3 lines max)
     * Location: 14px font with icon
   - Quick Stats:
     * Height: 80px
     * 3 columns:
       - Tasks completed
       - Active streaks
       - Achievement points
     * Each stat:
       - Number: 20px font
       - Label: 14px font

2. Achievement Summary
   - Height: 160px
   - Layout:
     * Title: "Achievements"
     * Level indicator:
       - Current level: Large number
       - Progress to next: Progress bar
       - Points needed: Text
     * Recent badges:
       - Horizontal scroll
       - Badge size: 64x64px
       - Badge name
       - Date earned
     * "View All" button

3. Activity Feed
   - Section Header:
     * Title: "Recent Activity"
     * Filter options
   - Activity Cards:
     * Height: 72px each
     * Icon: Left (24x24px)
     * Content:
       - Action text
       - Timestamp
       - Related item
     * Interactive elements

4. Statistics Overview
   - Height: 200px
   - Grid layout (2x2):
     * Productivity score
     * Task completion rate
     * Streak statistics
     * Goal progress
   - Each card:
     * Icon: Top right
     * Main value: 24px font
     * Label: 14px font
     * Trend indicator

## Screen 2: Achievement Center

### Layout Specifications
- Full screen
- Top bar: 56px
- Tab bar: 48px
- Content: Scrollable
- Padding: 16px

### Component Details

1. Level Progress Header
   - Height: 120px
   - Current level display:
     * Large number: 48px font
     * Level title
     * Progress ring: 80x80px
   - Next level preview:
     * Points needed
     * Unlocks preview

2. Badge Collection
   - Grid layout: 3 columns
   - Each badge card:
     * Size: Square (width/3 - 24px)
     * Icon: 48x48px
     * Name: 14px font
     * Status: Locked/Unlocked
     * Progress indicator
   - Locked badge style:
     * Grayscale
     * Lock icon
     * Requirements shown

3. Achievement Categories
   - Section tabs:
     * All
     * Tasks
     * Habits
     * Goals
     * Special
   - Achievement cards:
     * Height: 100px
     * Icon: Left
     * Title + description
     * Progress bar
     * Points value
     * Status indicator

4. Leaderboard Section
   - Header with time filter
   - User rankings:
     * Height: 72px per row
     * Rank number
     * Avatar
     * Name
     * Points
     * Trend indicator
   - Current user highlight
   - Top 3 special design

5. Rewards Shop
   - Grid/List toggle
   - Reward cards:
     * Height: 160px
     * Image: Top half
     * Title
     * Cost in points
     * Description
     * Claim button
   - Claimed rewards section
   - Points balance sticky header

## Screen 3: Statistics Dashboard

### Layout Specifications
- Full screen
- Top bar: 56px
- Content: Scrollable
- Padding: 16px

### Component Details

1. Time Period Selector
   - Height: 48px
   - Options:
     * Week
     * Month
     * Quarter
     * Year
     * Custom
   - Style: Segmented control
   - Custom date picker

2. Overview Cards
   - Height: 100px each
   - Grid layout (2x2):
     * Total tasks completed
     * Average daily tasks
     * Active streaks
     * Success rate
   - Each card:
     * Main metric
     * Comparison to previous
     * Trend graph
     * Icon

3. Productivity Chart
   - Height: 240px
   - Chart types:
     * Line chart
     * Bar chart
     * Heat map
   - Legend
   - Interactive tooltips
   - Zoom controls

4. Category Distribution
   - Height: 200px
   - Pie/Donut chart
   - Legend with percentages
   - Interactive segments
   - Category filters

5. Time Analysis
   - Height: 180px
   - Hour of day heatmap
   - Day of week analysis
   - Peak productivity times
   - Custom date ranges

6. Streak Records
   - Height: Auto
   - List view:
     * Habit name
     * Streak length
     * Date range
     * Status
   - Sort options
   - Filter controls

7. Export Options
   - Format selection:
     * PDF
     * CSV
     * JSON
   - Date range picker
   - Data selection
   - Export button

## Screen 4: Settings Menu

### Layout Specifications
- Full screen
- Top bar: 56px
- Content: Grouped list
- Padding: 16px

### Component Details

1. Account Settings
   - Section header
   - List items:
     * Profile information
     * Email & password
     * Connected accounts
     * Privacy settings
   - Height: 56px per item
   - Right chevron
   - Icons: Left

2. Notification Preferences
   - Section header
   - Categories:
     * Tasks
     * Habits
     * Goals
     * System
   - Toggle switches
   - Time preferences
   - Custom sounds

3. Appearance Settings
   - Theme selector:
     * Light
     * Dark
     * System
     * Custom
   - Color picker
   - Font size
   - Display density

4. Data Management
   - Backup options
   - Sync settings
   - Export data
   - Delete account
   - Storage usage

5. Integration Settings
   - Calendar sync
   - Cloud storage
   - Third-party apps
   - API access

6. Help & Support
   - FAQ
   - Contact support
   - Bug report
   - Feature request
   - Tutorial reset

## Screen 5: App Settings

### Layout Specifications
- Full screen
- Top bar: 56px
- Content: Scrollable list
- Padding: 16px

### Component Details

1. General Preferences
   - Start screen
   - Language
   - Time zone
   - Date format
   - First day of week
   - Height: 56px per item

2. Sync Settings
   - Auto-sync toggle
   - Sync frequency
   - Wifi only option
   - Last sync status
   - Force sync button

3. Storage Management
   - Usage overview:
     * Chart
     * Categories
     * Available space
   - Clear cache
   - Offline data
   - Auto-cleanup rules

4. About Section
   - App version
   - Build number
   - Terms of service
   - Privacy policy
   - Licenses
   - Credits

### Implementation Notes

#### Color System
- Primary: Brand color
- Secondary: Accent color
- Background: System background
- Surface: Card backgrounds
- Text: Primary and secondary
- Icons: Primary and secondary

#### Typography Hierarchy
- Profile name: 24px
- Section headers: 20px
- List items: 16px
- Secondary text: 14px
- Captions: 12px

#### Interactive Elements
1. List Items
   - Tap feedback
   - Toggle animations
   - Swipe actions
   - Long press menus

2. Charts and Graphs
   - Touch interaction
   - Zoom gestures
   - Data point selection
   - Export options

3. Settings Controls
   - Toggle switches
   - Radio buttons
   - Checkboxes
   - Sliders

#### Animations
1. Screen Transitions
   - Smooth navigation
   - Modal presentations
   - List item updates

2. Interactive Feedback
   - Button states
   - Loading indicators
   - Success confirmations
   - Error states

#### Accessibility
- Large touch targets
- Clear contrast
- Screen reader support
- Keyboard navigation
- Voice control

This detailed specification provides exact measurements and requirements for implementing the profile and settings screens. Each component is broken down with specific dimensions, spacing, and interaction states to ensure consistent implementation.
