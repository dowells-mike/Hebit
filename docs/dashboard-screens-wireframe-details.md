# Dashboard Screens Wireframe Details

## Common Elements Across Dashboard Screens

### Top App Bar
- Height: 56px
- Title: Screen-specific
- Font: Heading 3 (20px)
- Right Actions: Notifications, Profile
- Elevation: Subtle shadow
- Background: Primary color or white

### Bottom Navigation
- Height: 56px
- Items: Dashboard, Tasks, Habits, Goals, Profile
- Active State: Icon + Label
- Inactive State: Icon only
- Selected Item: Primary color
- Unselected Items: Gray (#666666)

## Screen 1: Today's Overview

### Layout Specifications
- Status Bar: System default
- Top App Bar: 56px
- Content: Scrollable
- Bottom Navigation: 56px
- Side Padding: 16px

### Component Details

1. Date & Weather Header
   - Height: 80px
   - Date Format: "Monday, February 17"
   - Font: Heading 2 (24px)
   - Weather:
     * Icon: 24x24px
     * Temperature: 16px font
     * Position: Right aligned
   - Divider below: 1px line

2. Priority Tasks Section
   - Header:
     * Title: "Priority Tasks"
     * "See All" link: Right aligned
     * Height: 48px
   - Task Cards Container:
     * Height: 160px
     * Horizontal scroll
     * Snap to card
   - Individual Task Cards:
     * Width: 280px
     * Height: 140px
     * Margin Right: 16px
     * Corner Radius: 8px
     * Shadow: Subtle elevation
     * Contents:
       - Title: 16px font
       - Due time: 14px font
       - Priority indicator
       - Category label
       - Progress/status

3. Today's Habits Section
   - Header:
     * Title: "Today's Habits"
     * Progress: "X/Y Completed"
     * Height: 48px
   - Habit Cards Container:
     * Height: 120px
     * Horizontal scroll
     * Snap to card
   - Individual Habit Cards:
     * Width: 200px
     * Height: 100px
     * Margin Right: 16px
     * Contents:
       - Icon: 24x24px
       - Title: 16px font
       - Progress indicator
       - Quick complete button

4. Goal Progress Section
   - Header:
     * Title: "Active Goals"
     * Height: 48px
   - Goal Cards:
     * Height: 100px each
     * Full width
     * Margin Bottom: 16px
     * Contents:
       - Title: 16px font
       - Progress bar
       - Percentage
       - Due date
       - Category label

5. Quick Add FAB
   - Size: 56x56px
   - Position: Bottom right
   - Margin: 16px from edges
   - Icon: Plus (+)
   - Elevation: Higher shadow
   - Z-index: Above all content

## Screen 2: Progress Stats

### Layout Specifications
- Full screen with scrollable content
- Top and bottom bars consistent
- Content padding: 16px

### Component Details

1. Time Period Selector
   - Height: 48px
   - Options: Day, Week, Month
   - Style: Segmented control
   - Position: Below app bar
   - Full width minus 32px

2. Weekly Calendar View
   - Height: 100px
   - Day cells: 7 equal width
   - Contents per cell:
     * Day name
     * Date number
     * Completion indicator
   - Current day: Highlighted
   - Past days: With data
   - Future days: Subtle style

3. Task Completion Chart
   - Height: 200px
   - Chart type: Line or bar
   - X-axis: Time periods
   - Y-axis: Completion rate
   - Legend: Task categories
   - Interactive: Tap for details

4. Active Streaks List
   - Header height: 48px
   - Each streak item:
     * Height: 72px
     * Icon: Left
     * Title + streak count
     * Progress indicator
     * Chevron right

5. Productivity Score Card
   - Height: 120px
   - Score: Large number (32px)
   - Trend indicator
   - Comparison to average
   - Background: Gradient
   - Corner radius: 12px

6. Time Distribution Chart
   - Height: 240px
   - Type: Pie chart
   - Legend: Below chart
   - Categories:
     * Tasks
     * Habits
     * Goals
     * Other
   - Interactive segments

## Screen 3: Quick Actions

### Layout Specifications
- Full screen minus system bars
- Grid-based layout
- 16px grid spacing

### Component Details

1. Recent Tasks Grid
   - Layout: 2x2 grid
   - Each cell:
     * Size: (Screen width - 48px) / 2
     * Height: Equal to width
     * Corner radius: 8px
     * Contents:
       - Icon: 24x24px
       - Title: 16px font
       - Subtitle: 14px font
       - Action button

2. Suggested Actions List
   - Header height: 48px
   - Each suggestion:
     * Height: 72px
     * Icon: 24x24px
     * Title: 16px font
     * Description: 14px font
     * Action button/chevron
   - Maximum 5 items

3. Shortcuts Bar
   - Height: 80px
   - Horizontal scroll
   - Each shortcut:
     * Width: 80px
     * Icon: 32x32px
     * Label: 12px font
     * Spacing: 16px

4. Quick Input Field
   - Height: 48px
   - Padding: 16px
   - Border radius: 24px
   - Icon: Left aligned
   - Placeholder text
   - Voice input option

### Implementation Notes

#### Color Usage
- Primary: Brand color
- Secondary: Accent color
- Background: System background
- Surface: White or light gray
- Text: Primary and secondary text colors
- Charts: Color palette for data visualization

#### Typography Hierarchy
- Screen titles: 24px
- Section headers: 20px
- Card titles: 16px
- Body text: 16px
- Secondary text: 14px
- Labels: 12px

#### Interactive Elements
1. Cards
   - Tap feedback
   - Long press options
   - Swipe actions where applicable

2. Charts
   - Tap for details
   - Pinch to zoom (where applicable)
   - Scroll/swipe through time periods

3. Quick Actions
   - Immediate feedback
   - Loading states
   - Success/error states

#### Accessibility Considerations
- Touch targets: Minimum 48x48px
- Color contrast: WCAG AA standard
- Screen reader support
- Scalable text
- Clear navigation patterns

This detailed specification provides exact measurements and requirements for implementing the dashboard screens. Each component is broken down with specific dimensions, spacing, and interaction states to ensure consistent implementation.
