# Git and GitHub Setup Guide for Productivity App

## 0. Setting Up Git for an Existing Workspace

If you already have an existing workspace (like your SDP_Workspace/productivity-app folder), follow these steps to initialize Git:

```bash
# Navigate to your existing workspace directory
cd /path/to/SDP_Workspace/productivity-app

# Initialize Git repository
git init

# Create a .gitignore file
cat > .gitignore << EOL
# Android
*.iml
.gradle
/local.properties
/.idea
.DS_Store
/build
/captures
.externalNativeBuild
.cxx

# Node.js
node_modules/
npm-debug.log
yarn-error.log
.env
dist/
coverage/

# General
*.log
.DS_Store
EOL

# Create initial README if it doesn't exist
if [ ! -f README.md ]; then
  echo "# Hebit App\n\nAn intelligent productivity app that adapts to user behavior using machine learning." > README.md
fi

# Add all files to staging
git add .

# Make initial commit
git commit -m "chore: initial project setup from existing workspace"

# Create GitHub repository (do this on GitHub.com)
# Then connect your local repository
git remote add origin https://github.com/[your-username]/hebit-app.git

# Create and switch to develop branch
git checkout -b develop

# Push both branches to GitHub
git push -u origin main
git push -u origin develop
```

### 0.1 Handling Existing Files

When initializing Git in an existing workspace:

1. **Review files before committing**: Check which files should be tracked and which should be ignored
   ```bash
   # See what would be committed
   git status
   ```

2. **Add specific files/folders if needed**:
   ```bash
   # Add only specific directories
   git add mobile/ backend/ docs/
   ```

3. **Organize your initial commits logically**:
   ```bash
   # Commit project structure first
   git add .gitignore README.md
   git commit -m "chore: initial project setup"
   
   # Commit backend code
   git add backend/
   git commit -m "feat(backend): add initial backend implementation"
   
   # Commit mobile code
   git add mobile/
   git commit -m "feat(mobile): add initial mobile app structure"
   ```

## 1. Repository Structure

We'll use a monorepo structure to keep all related projects in one repository, making it easier to manage dependencies and coordinate changes across different parts of the application.

```
productivity-app/
├── mobile/              # Kotlin Android app
├── backend/             # Node.js backend
├── ml-services/         # Machine learning services
├── docs/                # Documentation
└── shared/              # Shared types, utilities, etc.
```

## 2. Initial Setup Steps

### 2.1 Repository Creation

1. Create a new repository on GitHub:
   - Name: hebit-app
   - Description: "Hebit: An intelligent productivity app that adapts to user behavior"
   - Initialize with:
     - README.md
     - .gitignore (Android + Node)
     - MIT License

2. Local setup commands:
```bash
# Clone the repository
git clone https://github.com/[your-username]/hebit-app.git
cd hebit-app

# Create basic directory structure
mkdir mobile backend ml-services docs shared

# Create initial README content
echo "# Hebit App\n\nAn intelligent productivity app that adapts to user behavior using machine learning." > README.md
```

### 2.2 Branch Strategy

We'll use a modified Git Flow strategy with these branches:

1. **main**
   - Production-ready code
   - Protected branch
   - Requires pull request reviews
   - Only merged from develop after QA

2. **develop**
   - Main development branch
   - Features and fixes are merged here first
   - Must pass CI/CD checks before merging

3. **feature/***
   - For new features
   - Named as: feature/task-management, feature/habit-tracking, etc.
   - Created from and merged back to develop

4. **bugfix/***
   - For bug fixes
   - Named as: bugfix/notification-timing, bugfix/data-sync, etc.

5. **release/***
   - For release preparation
   - Named as: release/v1.0.0, release/v1.1.0, etc.
   - Created from develop, merged to main and develop

## 3. Branch Protection Rules

Set up these rules in GitHub repository settings:

### 3.1 main Branch
- Require pull request reviews before merging
- Require status checks to pass before merging
- Require branches to be up to date before merging
- Include administrators in these restrictions

### 3.2 develop Branch
- Require pull request reviews before merging
- Require status checks to pass before merging
- Allow force pushes for administrators

## 4. Commit Message Convention

We'll use conventional commits format:

```
type(scope): description

[optional body]

[optional footer]
```

Types:
- feat: New feature
- fix: Bug fix
- docs: Documentation changes
- style: Code style changes (formatting, etc.)
- refactor: Code refactoring
- test: Adding or modifying tests
- chore: Maintenance tasks

Example:
```
feat(task-management): add task prioritization algorithm

- Implements Q-Learning for task scheduling
- Adds unit tests for priority calculation
- Updates documentation

Closes #123
```

## 5. GitHub Actions Setup

Create these workflow files:

### 5.1 Android CI (.github/workflows/android-ci.yml)
```yaml
name: Android CI

on:
  push:
    branches: [ develop, main ]
    paths:
      - 'mobile/**'
  pull_request:
    branches: [ develop, main ]
    paths:
      - 'mobile/**'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK
        uses: actions/setup-java@v2
        with:
          java-version: '11'
          distribution: 'adopt'
      - name: Build with Gradle
        run: cd mobile && ./gradlew build
```

### 5.2 Backend CI (.github/workflows/backend-ci.yml)
```yaml
name: Backend CI

on:
  push:
    branches: [ develop, main ]
    paths:
      - 'backend/**'
  pull_request:
    branches: [ develop, main ]
    paths:
      - 'backend/**'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Use Node.js
        uses: actions/setup-node@v2
        with:
          node-version: '16.x'
      - run: cd backend && npm ci
      - run: cd backend && npm test
```

## 6. Initial Setup Commands

### 6.1 For New Projects

If you're starting from scratch:

```bash
# Initialize the repository
git init

# Add remote (after creating GitHub repository)
git remote add origin https://github.com/[your-username]/hebit-app.git

# Create develop branch
git checkout -b develop

# Create initial structure (if not already created)
mkdir -p mobile/app/src/main
mkdir -p backend/src
mkdir -p docs/architecture
mkdir -p shared/types

# Create initial files
touch mobile/app/build.gradle.kts
touch backend/package.json
touch docs/architecture/overview.md
touch shared/types/index.ts

# Initial commit
git add .
git commit -m "chore: initial project setup"

# Push to remote
git push -u origin develop
```

### 6.2 For Existing Workspace (SDP_Workspace)

If you're using your existing SDP_Workspace:

```bash
# Navigate to your workspace
cd /path/to/SDP_Workspace/productivity-app

# If Git is not initialized yet
git init

# Create a good .gitignore file
# (Use the one provided in section 0)

# Stage and commit existing files
git add .
git commit -m "chore: initial commit of existing project"

# Create GitHub repository through the web interface
# Then add the remote
git remote add origin https://github.com/[your-username]/hebit-app.git

# Create develop branch
git checkout -b develop

# Push both branches
git push -u origin main
git push -u origin develop
```

## 7. Development Workflow

1. **Starting a new feature:**
```bash
git checkout develop
git pull origin develop
git checkout -b feature/new-feature-name
```

2. **Making changes:**
```bash
git add .
git commit -m "feat(scope): description"
```

3. **Updating feature branch:**
```bash
git checkout develop
git pull origin develop
git checkout feature/new-feature-name
git rebase develop
```

4. **Creating a pull request:**
- Push your feature branch to GitHub
- Create PR through GitHub interface
- Assign reviewers
- Link related issues

5. **After PR approval:**
```bash
git checkout develop
git pull origin develop
git merge --no-ff feature/new-feature-name
git push origin develop
```

## 8. Best Practices

1. **Commits**
   - Make atomic commits (one logical change per commit)
   - Write clear commit messages following the convention
   - Reference issues in commit messages when applicable

2. **Branches**
   - Keep branches focused on single features/fixes
   - Delete branches after merging
   - Regularly update from develop to avoid conflicts

3. **Pull Requests**
   - Include clear descriptions
   - Add screenshots for UI changes
   - Update documentation when needed
   - Respond to review comments promptly

4. **Code Review**
   - Review PRs within 24 hours
   - Be constructive in feedback
   - Test changes locally when necessary
   - Ensure CI checks pass

## 9. Documentation

Maintain these documentation files:

1. **README.md**
   - Project overview
   - Setup instructions
   - Development workflow
   - Link to detailed docs

2. **CONTRIBUTING.md**
   - Contribution guidelines
   - Code style guide
   - PR process
   - Contact information

3. **CHANGELOG.md**
   - Version history
   - Notable changes
   - Breaking changes
   - Migration guides

## 10. Next Steps

After setting up the repository:

1. Create project boards for:
   - Feature tracking
   - Bug tracking
   - Release planning

2. Set up branch protection rules

3. Configure CI/CD pipelines

4. Add team members and assign roles

5. Create initial project documentation

Remember to regularly update this guide as the project evolves and new patterns emerge.
