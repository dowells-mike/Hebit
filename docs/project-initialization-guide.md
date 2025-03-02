# Project Initialization Guide for Hebit

## 1. Android Studio Project Setup

### 1.1 Create New Android Project
1. Open Android Studio
2. Click "New Project"
3. Select "Empty Activity" as your template
4. Configure the project:
   ```
   Name: Hebit
   Package name: com.hebit.app
   Save location: [your-workspace]/productivity-app/mobile
   Language: Kotlin
   Minimum SDK: API 24 (Android 7.0)
   ```
5. Click "Finish"

### 1.2 Configure build.gradle (app level)
Add these dependencies to your app/build.gradle:

```gradle
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'kotlin-kapt'
    id 'dagger.hilt.android.plugin'
}

android {
    compileSdk 34

    defaultConfig {
        applicationId "com.hebit.app"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion "1.5.1"
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }
}

dependencies {
    def compose_version = "1.5.4"
    def lifecycle_version = "2.7.0"

    // Android core
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'

    // Compose
    implementation "androidx.compose.ui:ui:$compose_version"
    implementation "androidx.compose.material:material:$compose_version"
    implementation "androidx.compose.ui:ui-tooling-preview:$compose_version"
    implementation 'androidx.activity:activity-compose:1.8.2'
    debugImplementation "androidx.compose.ui:ui-tooling:$compose_version"

    // Architecture Components
    implementation "androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-runtime-compose:$lifecycle_version"
    implementation "androidx.navigation:navigation-compose:2.7.6"

    // Dependency Injection
    implementation "com.google.dagger:hilt-android:2.48"
    kapt "com.google.dagger:hilt-compiler:2.48"

    // Networking
    implementation "com.squareup.retrofit2:retrofit:2.9.0"
    implementation "com.squareup.retrofit2:converter-gson:2.9.0"
    implementation "com.squareup.okhttp3:logging-interceptor:4.11.0"

    // Local Database
    implementation "androidx.room:room-runtime:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    kapt "androidx.room:room-compiler:2.6.1"

    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation "androidx.compose.ui:ui-test-junit4:$compose_version"
}
```

### 1.3 Configure build.gradle (project level)
Update your project level build.gradle:

```gradle
buildscript {
    ext {
        compose_version = '1.5.4'
        kotlin_version = '1.9.0'
    }
    
    dependencies {
        classpath 'com.android.tools.build:gradle:8.1.0'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath 'com.google.dagger:hilt-android-gradle-plugin:2.48'
    }
}
```

### 1.4 Create Initial Project Structure
Create these directories in your Android project:

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/hebit/app/
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   ├── remote/
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
│   │   │   └── util/
│   │   └── res/
│   └── test/
└── build.gradle
```

## 2. Node.js Backend Setup

### 2.1 Initialize Node.js Project
Navigate to your backend directory and run:

```bash
# Create backend directory if not exists
mkdir -p backend
cd backend

# Initialize npm project
npm init -y

# Install core dependencies
npm install express typescript ts-node @types/node @types/express
npm install -D nodemon @types/nodemon

# Install additional dependencies
npm install cors dotenv mongoose bcryptjs jsonwebtoken
npm install -D @types/cors @types/bcryptjs @types/jsonwebtoken
```

### 2.2 Configure TypeScript
Create tsconfig.json in the backend directory:

```json
{
  "compilerOptions": {
    "target": "es2020",
    "module": "commonjs",
    "lib": ["es2020"],
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "moduleResolution": "node",
    "resolveJsonModule": true,
    "outDir": "./dist",
    "rootDir": "./src"
  },
  "include": ["src/**/*"],
  "exclude": ["node_modules", "**/*.spec.ts"]
}
```

### 2.3 Update package.json Scripts
Add these scripts to package.json:

```json
{
  "scripts": {
    "start": "node dist/server.js",
    "dev": "nodemon src/server.ts",
    "build": "tsc",
    "test": "jest"
  }
}
```

### 2.4 Create Backend Project Structure
Create these directories and files:

```
backend/
├── src/
│   ├── config/
│   │   └── database.ts
│   ├── controllers/
│   │   ├── authController.ts
│   │   ├── taskController.ts
│   │   └── habitController.ts
│   ├── middleware/
│   │   ├── auth.ts
│   │   └── errorHandler.ts
│   ├── models/
│   │   ├── User.ts
│   │   ├── Task.ts
│   │   └── Habit.ts
│   ├── routes/
│   │   ├── auth.ts
│   │   ├── tasks.ts
│   │   └── habits.ts
│   ├── services/
│   │   ├── authService.ts
│   │   └── mlService.ts
│   ├── utils/
│   │   └── helpers.ts
│   └── server.ts
├── .env
├── .gitignore
├── package.json
└── tsconfig.json
```

### 2.5 Create Initial Server File
Create src/server.ts:

```typescript
import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import mongoose from 'mongoose';

dotenv.config();

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Routes (to be added)
app.get('/', (req, res) => {
  res.send('Hebit API is running');
});

// Start server
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
```

### 2.6 Create .env File
Create .env in the backend directory:

```env
PORT=5000
MONGODB_URI=mongodb://localhost:27017/hebit
JWT_SECRET=your_jwt_secret_here
NODE_ENV=development
```

### 2.7 Create .gitignore
Create .gitignore in the backend directory:

```
node_modules/
dist/
.env
*.log
```

## 3. Next Steps

### 3.1 Android Studio
1. Sync project with Gradle files
2. Run the app to verify the setup
3. Begin implementing the UI theme and base components

### 3.2 Backend
1. Install MongoDB locally or set up MongoDB Atlas
2. Test the server by running:
   ```bash
   npm run dev
   ```
3. Begin implementing the authentication system

### 3.3 Version Control
Follow the git-setup-guide.md to:
1. Initialize Git repository
2. Create initial commits for both frontend and backend
3. Push to GitHub

Remember to:
- Keep sensitive information out of version control
- Test both frontend and backend before committing
- Follow the established project structure when adding new files
