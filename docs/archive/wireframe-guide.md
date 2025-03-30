# Comprehensive Wireframe Guide

## Design System Guidelines

### 1. Typography Scale
- Heading 1: 32px (App name, main screen titles)
- Heading 2: 24px (Section headers)
- Heading 3: 20px (Card titles, modal headers)
- Body: 16px (Main content)
- Caption: 14px (Secondary information)
- Small: 12px (Timestamps, labels)

### 2. Spacing System
- xs: 4px (Minimal spacing between related elements)
- sm: 8px (Standard spacing between elements)
- md: 16px (Spacing between components)
- lg: 24px (Section spacing)
- xl: 32px (Major section spacing)
- xxl: 48px (Screen padding top/bottom)

### 3. Component Dimensions
- Bottom Navigation Height: 56px
- Top App Bar Height: 56px
- FAB Size: 56x56px
- Card Padding: 16px
- Input Field Height: 48px
- Button Height: 48px
- Icon Size: 24x24px
- Avatar Size: 40x40px

## Required Wireframes (Total: 25)

### 1. Authentication Flow (4 Wireframes)

#### 1.1 Splash Screen
Elements:
- Centered app logo (200x200px)
- App name below logo
- Loading indicator
- Version number at bottom

#### 1.2 Login Screen
Elements:
- App logo (smaller than splash, 100x100px)
- Email input field
- Password input field
- "Remember me" checkbox
- "Forgot password?" link
- Login button (full width)
- Google sign-in button
- "Create account" link at bottom

#### 1.3 Registration Screen
Elements:
- Back button in header
- Email input field
- Password input field with strength indicator
- Confirm password field
- Terms & conditions checkbox
- Registration button (full width)
- Google sign-up button

#### 1.4 Password Reset Screen
Elements:
- Back button in header
- Email input field
- Reset instructions text
- Submit button
- Return to login link

### 2. Dashboard Screens (3 Wireframes)

#### 2.1 Today's Overview
Elements:
- Date header with weather
- Priority tasks section (scrollable horizontally)
- Today's habits section (scrollable horizontally)
- Goal progress section
- Quick add FAB
Layout:
- Vertical scrolling main content
- Each section as a card
- Progress indicators for each goal

#### 2.2 Progress Stats
Elements:
- Weekly calendar view
- Task completion chart
- Active streaks list
- Productivity score card
- Time distribution pie chart
Layout:
- Scrollable vertical layout
- Charts should be 75% screen width
- Stats in card format

#### 2.3 Quick Actions
Elements:
- Recent tasks grid (2x2)
- Suggested actions list
- Shortcuts bar
- Quick input field
Layout:
- Grid layout for recent items
- List view for suggestions
- Horizontal scroll for shortcuts

### 3. Task Management (5 Wireframes)

#### 3.1 Task List
Elements:
- Search bar
- Filter/sort buttons
- View toggle (list/board)
- Task items with:
  - Checkbox
  - Title
  - Due date
  - Priority indicator
  - Category label
- Quick add bar at bottom
- FAB for new task
Layout:
- Vertical list
- Swipe actions for complete/delete
- Pull to refresh

#### 3.2 Task Creation
Elements:
- Close button
- Title input
- Description input
- Due date picker
- Priority selector (3 levels)
- Category dropdown
- Labels input
- Reminder toggle
- Recurrence selector
- Attachment button
- Save button
Layout:
- Full screen modal
- Scrollable form
- Fixed save button at bottom

#### 3.3 Task Detail
Elements:
- Back button
- Task title
- Description
- Due date
- Priority indicator
- Category
- Labels
- Sub-tasks list
- Comments section
- Activity log
Layout:
- Scrollable content
- Sticky header
- Action buttons in header

#### 3.4 Task Categories
Elements:
- Category list
- Count badge
- Color indicator
- Add category button
- Edit/delete actions
Layout:
- List view
- Swipe actions
- FAB for new category

#### 3.5 Board View
Elements:
- Column headers (Todo, In Progress, Done)
- Task cards
- Quick add button per column
- Column menu
Layout:
- Horizontal scrolling columns
- Vertical scrolling cards
- Drag handle on cards

### 4. Habit Tracking (4 Wireframes)

#### 4.1 Habit List
Elements:
- Category filter tabs
- Habit cards with:
  - Title
  - Streak counter
  - Today's status
  - Progress bar
- Quick complete buttons
Layout:
- Scrollable tabs at top
- Vertical list of cards
- FAB for new habit

#### 4.2 Habit Creation
Elements:
- Title input
- Description input
- Frequency selector
- Time preference
- Category selector
- Reminder settings
- Success criteria
- Difficulty level
Layout:
- Full screen modal
- Scrollable form
- Save button at bottom

#### 4.3 Habit Detail
Elements:
- Habit info header
- Streak statistics
- Calendar heatmap
- Progress graphs
- History log
Layout:
- Sticky header
- Scrollable content
- Edit button in header

#### 4.4 Streak Analytics
Elements:
- Current streak card
- Best streak card
- Completion rate chart
- Success patterns
- Suggestions
Layout:
- Card grid layout
- Full-width charts
- Scrollable content

### 5. Goal Management (4 Wireframes)

#### 5.1 Goal List
Elements:
- Timeline toggle
- Category filters
- Goal cards with:
  - Title
  - Progress bar
  - Due date
  - Priority indicator
Layout:
- Toggle view buttons
- Filterable list
- FAB for new goal

#### 5.2 Goal Creation
Elements:
- Title input
- Description input
- Category selector
- Deadline picker
- Milestone creator
- Success metrics
- Related items linker
Layout:
- Multi-step form
- Progress indicator
- Preview section

#### 5.3 Goal Detail
Elements:
- Goal info header
- Progress tracker
- Milestone timeline
- Related items
- Activity feed
Layout:
- Sticky header
- Tabbed content
- Action buttons

#### 5.4 Milestone Tracking
Elements:
- Timeline view
- Milestone cards
- Dependencies
- Progress updates
Layout:
- Vertical timeline
- Connected cards
- Update buttons

### 6. Profile & Settings (5 Wireframes)

#### 6.1 User Profile
Elements:
- Profile header with avatar
- Stats summary
- Achievement highlights
- Recent activity
Layout:
- Cover photo
- Profile section
- Scrollable content

#### 6.2 Achievement Center
Elements:
- Badges grid
- Progress tracks
- Leaderboard
- Rewards shop
Layout:
- Tab navigation
- Grid/list views
- Progress indicators

#### 6.3 Statistics Dashboard
Elements:
- Time period selector
- Productivity chart
- Category breakdown
- Streak records
Layout:
- Filter controls
- Interactive charts
- Export button

#### 6.4 Settings Menu
Elements:
- Profile settings
- Notification settings
- Privacy settings
- Theme selector
- Data management
Layout:
- Grouped list
- Toggle switches
- Sub-menu indicators

#### 6.5 App Settings
Elements:
- General preferences
- Sync settings
- Storage info
- About section
- Help & support
Layout:
- Grouped list
- Info cards
- Action buttons

## Wireframing Guidelines

### 1. General Principles
- Use a grid system (8pt grid)
- Maintain consistent spacing
- Include status bar
- Show keyboard when relevant
- Include system gestures area

### 2. Interactive Elements
- Show pressed states
- Include loading states
- Error states for forms
- Empty states for lists
- Success confirmations

### 3. Navigation Patterns
- Back buttons on sub-screens
- Gesture hints
- Modal close buttons
- Tab bar highlighting
- Navigation breadcrumbs

### 4. Content Hierarchy
- Clear headings
- Section dividers
- Group related items
- Progressive disclosure
- Important actions prominent

### 5. Accessibility Considerations
- Touch target sizes (minimum 48x48px)
- Sufficient contrast
- Clear error messages
- Alternative text placeholders
- Keyboard navigation support

## Wireframing Process

1. Start with authentication flow
2. Move to main dashboard
3. Detail each major feature section
4. Include all CRUD operations
5. Add analysis/reporting screens
6. Finally, settings and profile

Remember to:
- Label all components
- Include dimensions
- Note interactions
- Show state changes
- Document gestures

This guide provides a complete roadmap for creating all necessary wireframes. Each screen has been broken down into its essential components with specific layout instructions. Follow this guide sequentially to ensure all user flows are properly represented in your wireframes.
