# Authentication Flow Wireframe Details

## Screen 1: Splash Screen

### Layout Specifications
- Screen Size: Full screen (match_parent)
- Status Bar: System default
- Background: Solid color or subtle gradient

### Component Details
1. App Logo
   - Size: 200x200px
   - Position: Center of screen
   - Vertical offset: -50px from center
   - Animation: Fade in (1s)

2. App Name
   - Font: Heading 1 (32px)
   - Position: Below logo, 24px spacing
   - Alignment: Center
   - Animation: Fade in after logo (0.5s delay)

3. Loading Indicator
   - Size: 48x48px
   - Position: 48px below app name
   - Type: Circular progress
   - Animation: Infinite rotation

4. Version Number
   - Font: Small (12px)
   - Position: Bottom of screen, 32px from bottom
   - Alignment: Center
   - Format: "Version X.Y.Z"

## Screen 2: Login Screen

### Layout Specifications
- Screen Size: Full screen
- Top Padding: 48px (xxl)
- Side Padding: 24px (lg)
- Vertical Spacing: 16px (md) between elements

### Component Details
1. App Logo
   - Size: 100x100px
   - Position: Top center
   - Margin Bottom: 32px (xl)

2. Email Input Field
   - Height: 48px
   - Width: Match parent - 48px
   - Padding: 16px
   - Label: "Email"
   - Keyboard: Email type
   - Validation: Email format
   - Error State: Red border + message

3. Password Input Field
   - Height: 48px
   - Width: Match parent - 48px
   - Padding: 16px
   - Label: "Password"
   - Type: Password (dots)
   - Right Icon: Show/hide password
   - Error State: Red border + message

4. Remember Me Checkbox
   - Size: 24x24px
   - Label Right: "Remember me"
   - Position: Below password field
   - Spacing: 16px from password field

5. Login Button
   - Height: 48px
   - Width: Match parent - 48px
   - Style: Filled button
   - Text: "Log In"
   - Font: Body (16px)
   - States: Normal, Pressed, Loading

6. Google Sign-In Button
   - Height: 48px
   - Width: Match parent - 48px
   - Style: Outlined button
   - Icon: Google logo (24x24px)
   - Text: "Continue with Google"
   - Spacing: 24px from login button

7. Forgot Password Link
   - Font: Caption (14px)
   - Style: Underlined
   - Position: Below Google button
   - Spacing: 16px from Google button

8. Create Account Link
   - Font: Body (16px)
   - Text: "Don't have an account? Sign up"
   - Position: Bottom of screen
   - Margin Bottom: 32px

## Screen 3: Registration Screen

### Layout Specifications
- Screen Size: Full screen
- Top Bar Height: 56px
- Content Padding: 24px
- Form Spacing: 16px between fields

### Component Details
1. Top Bar
   - Height: 56px
   - Back Button: Left aligned
   - Title: "Create Account"
   - Font: Heading 3 (20px)

2. Email Input Field
   - Height: 48px
   - Width: Match parent - 48px
   - Label: "Email"
   - Validation: Email format
   - Error Messages: Below field

3. Password Field
   - Height: 48px
   - Width: Match parent - 48px
   - Label: "Password"
   - Show/Hide Toggle
   - Strength Indicator:
     - Height: 4px
     - Colors: Red, Yellow, Green
     - Width: Full field width
     - Position: Below field

4. Confirm Password Field
   - Height: 48px
   - Width: Match parent - 48px
   - Label: "Confirm Password"
   - Show/Hide Toggle
   - Validation: Match password

5. Terms & Conditions
   - Checkbox: 24x24px
   - Text: "I agree to Terms & Privacy Policy"
   - Links: Underlined terms, privacy
   - Spacing: 24px from fields

6. Register Button
   - Height: 48px
   - Width: Match parent - 48px
   - Style: Filled button
   - States: Enabled/Disabled
   - Position: Bottom fixed

7. Google Sign-Up
   - Height: 48px
   - Width: Match parent - 48px
   - Style: Outlined
   - Position: Above register button
   - Spacing: 16px

## Screen 4: Password Reset Screen

### Layout Specifications
- Screen Size: Full screen
- Top Bar: 56px
- Content Padding: 24px
- Vertical Spacing: 16px

### Component Details
1. Top Bar
   - Height: 56px
   - Back Button: Left aligned
   - Title: "Reset Password"
   - Font: Heading 3 (20px)

2. Instructions Text
   - Font: Body (16px)
   - Width: Match parent - 48px
   - Margin Top: 32px
   - Text: "Enter your email address to receive password reset instructions"

3. Email Input Field
   - Height: 48px
   - Width: Match parent - 48px
   - Label: "Email"
   - Validation: Email format
   - Error State: Red border + message

4. Submit Button
   - Height: 48px
   - Width: Match parent - 48px
   - Style: Filled button
   - Text: "Send Reset Link"
   - Position: 32px below email
   - States: Normal, Loading, Disabled

5. Return to Login
   - Font: Body (16px)
   - Style: Text button
   - Text: "Back to Login"
   - Position: 24px below submit
   - Alignment: Center

### Success State
1. Success Icon
   - Size: 64x64px
   - Color: Green
   - Position: Center
   - Animation: Scale + Fade

2. Success Message
   - Font: Body (16px)
   - Text: "Reset link sent! Check your email"
   - Position: Below icon
   - Spacing: 16px

3. Return Button
   - Style: Text button
   - Text: "Return to Login"
   - Position: Below message
   - Spacing: 24px

## Implementation Notes

### Color Scheme
- Primary: [Your brand color]
- Secondary: [Accent color]
- Error: #FF3B30
- Success: #34C759
- Text Primary: #000000
- Text Secondary: #666666
- Background: #FFFFFF
- Input Background: #F5F5F5

### Typography
- Use system font for better performance
- Maintain consistent line heights:
  - Headings: 1.2
  - Body: 1.5
  - Buttons: 1

### Interactions
1. Splash Screen
   - Auto-proceed after 2 seconds
   - Skip if authenticated

2. Login Screen
   - Validate on blur
   - Show errors on submit
   - Disable submit when invalid

3. Registration
   - Real-time password strength
   - Validate confirm password on change
   - Enable submit only when valid

4. Password Reset
   - Show loading on submit
   - Clear form on success
   - Maintain back stack

### Error States
- Input Fields:
  - Red border (#FF3B30)
  - Error message below
  - Error icon right
- Buttons:
  - Disable on invalid
  - Show loading spinner
- Forms:
  - Scroll to first error
  - Shake animation on error

### Accessibility
- Minimum touch targets: 48x48px
- Clear error messages
- High contrast text
- Support screen readers
- Keyboard navigation

This detailed specification provides exact measurements and requirements for implementing the authentication flow wireframes. Each screen is broken down into precise components with specific dimensions, spacing, and interaction states.
