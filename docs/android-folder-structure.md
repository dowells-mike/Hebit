# Android Project Folder Structure Setup

## Create the following directory structure in your Android project:

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/hebit/app/
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── dao/
│   │   │   │   │   └── database/
│   │   │   │   ├── remote/
│   │   │   │   │   ├── api/
│   │   │   │   │   └── dto/
│   │   │   │   ├── repository/
│   │   │   │   └── model/
│   │   │   ├── di/
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   └── usecase/
│   │   │   ├── ui/
│   │   │   │   ├── theme/
│   │   │   │   ├── components/
│   │   │   │   └── screens/
│   │   │   │       ├── auth/
│   │   │   │       ├── dashboard/
│   │   │   │       ├── tasks/
│   │   │   │       ├── habits/
│   │   │   │       ├── goals/
│   │   │   │       └── settings/
│   │   │   └── util/
│   │   └── res/
│   │       ├── drawable/
│   │       ├── values/
│   │       │   ├── colors.xml
│   │       │   ├── strings.xml
│   │       │   └── themes.xml
│   │       └── font/
│   └── test/
└── build.gradle.kts
```

## Create Initial Files

### 1. Theme Setup

Create `app/src/main/java/com/hebit/app/ui/theme/Color.kt`:

```kotlin
package com.hebit.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary colors
val PrimaryLight = Color(0xFF6200EE)
val PrimaryDark = Color(0xFFBB86FC)
val PrimaryVariant = Color(0xFF3700B3)

// Secondary colors
val SecondaryLight = Color(0xFF03DAC6)
val SecondaryDark = Color(0xFF03DAC6)
val SecondaryVariant = Color(0xFF018786)

// Background colors
val BackgroundLight = Color(0xFFF5F5F5)
val BackgroundDark = Color(0xFF121212)

// Surface colors
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E1E)

// Error colors
val ErrorLight = Color(0xFFB00020)
val ErrorDark = Color(0xFFCF6679)

// Text colors
val OnPrimaryLight = Color(0xFFFFFFFF)
val OnPrimaryDark = Color(0xFF000000)
val OnSecondaryLight = Color(0xFF000000)
val OnSecondaryDark = Color(0xFF000000)
val OnBackgroundLight = Color(0xFF000000)
val OnBackgroundDark = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF000000)
val OnSurfaceDark = Color(0xFFFFFFFF)
val OnErrorLight = Color(0xFFFFFFFF)
val OnErrorDark = Color(0xFF000000)
```

Create `app/src/main/java/com/hebit/app/ui/theme/Theme.kt`:

```kotlin
package com.hebit.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryVariant,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryVariant,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    error = ErrorLight,
    onError = OnErrorLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryVariant,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryVariant,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = ErrorDark,
    onError = OnErrorDark
)

@Composable
fun HebitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

Create `app/src/main/java/com/hebit/app/ui/theme/Type.kt`:

```kotlin
package com.hebit.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    )
)
```

### 2. Application Class

Create `app/src/main/java/com/hebit/app/HebitApplication.kt`:

```kotlin
package com.hebit.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HebitApplication : Application()
```

### 3. Main Activity

Update `app/src/main/java/com/hebit/app/MainActivity.kt`:

```kotlin
package com.hebit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hebit.app.ui.theme.HebitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HebitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // This will be replaced with our app navigation
                    Greeting("Hebit")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Welcome to $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HebitTheme {
        Greeting("Hebit")
    }
}
```

### 4. Update AndroidManifest.xml

Make sure your `app/src/main/AndroidManifest.xml` includes:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".HebitApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Hebit"
        tools:targetApi="31">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Hebit">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

### 5. Update strings.xml

Update `app/src/main/res/values/strings.xml`:

```xml
<resources>
    <string name="app_name">Hebit</string>
    <string name="welcome_message">Welcome to Hebit</string>
    <string name="login">Login</string>
    <string name="register">Register</string>
    <string name="email">Email</string>
    <string name="password">Password</string>
    <string name="confirm_password">Confirm Password</string>
    <string name="forgot_password">Forgot Password?</string>
    <string name="tasks">Tasks</string>
    <string name="habits">Habits</string>
    <string name="goals">Goals</string>
    <string name="settings">Settings</string>
    <string name="dashboard">Dashboard</string>
</resources>
