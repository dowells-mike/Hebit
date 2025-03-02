# Testing Strategy & Quality Assurance

## 1. Unit Testing

### 1.1 Test Structure

```typescript
import { describe, it, expect, beforeEach } from 'jest';
import { TaskService } from '../services/TaskService';
import { TaskRepository } from '../repositories/TaskRepository';
import { Task, TaskStatus, TaskPriority } from '../models/Task';

describe('TaskService', () => {
    let taskService: TaskService;
    let taskRepository: jest.Mocked<TaskRepository>;
    
    beforeEach(() => {
        taskRepository = {
            findById: jest.fn(),
            create: jest.fn(),
            update: jest.fn(),
            delete: jest.fn(),
            findAll: jest.fn()
        };
        
        taskService = new TaskService(taskRepository);
    });
    
    describe('createTask', () => {
        it('should create a task with valid data', async () => {
            // Arrange
            const taskData = {
                title: 'Test Task',
                description: 'Test Description',
                dueDate: new Date(),
                priority: TaskPriority.HIGH
            };
            
            const expectedTask = {
                id: '123',
                ...taskData,
                status: TaskStatus.PENDING,
                createdAt: expect.any(Date),
                updatedAt: expect.any(Date)
            };
            
            taskRepository.create.mockResolvedValue(expectedTask);
            
            // Act
            const result = await taskService.createTask(taskData);
            
            // Assert
            expect(result).toEqual(expectedTask);
            expect(taskRepository.create).toHaveBeenCalledWith({
                ...taskData,
                status: TaskStatus.PENDING
            });
        });
        
        it('should throw error for invalid task data', async () => {
            // Arrange
            const invalidData = {
                title: '',  // Empty title
                priority: 'INVALID'
            };
            
            // Act & Assert
            await expect(
                taskService.createTask(invalidData)
            ).rejects.toThrow('Invalid task data');
        });
    });
    
    describe('updateTaskStatus', () => {
        it('should update task status correctly', async () => {
            // Arrange
            const taskId = '123';
            const existingTask = {
                id: taskId,
                title: 'Test Task',
                status: TaskStatus.PENDING
            };
            
            taskRepository.findById.mockResolvedValue(existingTask);
            taskRepository.update.mockImplementation(
                async (id, data) => ({
                    ...existingTask,
                    ...data
                })
            );
            
            // Act
            const result = await taskService.updateTaskStatus(
                taskId,
                TaskStatus.COMPLETED
            );
            
            // Assert
            expect(result.status).toBe(TaskStatus.COMPLETED);
            expect(taskRepository.update).toHaveBeenCalledWith(
                taskId,
                expect.objectContaining({
                    status: TaskStatus.COMPLETED,
                    completedAt: expect.any(Date)
                })
            );
        });
    });
});
```

### 1.2 Mocking Strategies

```typescript
// Mock for external service
jest.mock('../services/NotificationService', () => ({
    NotificationService: jest.fn().mockImplementation(() => ({
        sendNotification: jest.fn().mockResolvedValue(undefined)
    }))
}));

// Mock for database
jest.mock('../database', () => ({
    query: jest.fn(),
    transaction: jest.fn(callback => callback())
}));

// Mock for Redis cache
jest.mock('../cache', () => ({
    get: jest.fn(),
    set: jest.fn(),
    del: jest.fn()
}));

// Example test with mocks
describe('HabitService', () => {
    let habitService: HabitService;
    let notificationService: jest.Mocked<NotificationService>;
    let cache: jest.Mocked<typeof import('../cache')>;
    
    beforeEach(() => {
        jest.clearAllMocks();
        
        notificationService = new NotificationService() as any;
        cache = require('../cache');
        
        habitService = new HabitService(
            notificationService,
            cache
        );
    });
    
    describe('completeHabit', () => {
        it('should update streak and send notification', async () => {
            // Arrange
            const habitId = '123';
            const userId = '456';
            
            cache.get.mockResolvedValue(null);
            cache.set.mockResolvedValue(true);
            
            // Act
            await habitService.completeHabit(habitId, userId);
            
            // Assert
            expect(cache.set).toHaveBeenCalledWith(
                `habit:${habitId}:streak`,
                1,
                'EX',
                86400
            );
            
            expect(notificationService.sendNotification)
                .toHaveBeenCalledWith(
                    userId,
                    expect.stringContaining('streak')
                );
        });
    });
});
```

## 2. Integration Testing

### 2.1 API Tests

```typescript
import request from 'supertest';
import { app } from '../app';
import { db } from '../database';
import { createTestUser, createTestTask } from './helpers';

describe('Task API', () => {
    let authToken: string;
    let userId: string;
    
    beforeAll(async () => {
        // Setup test database
        await db.migrate.latest();
        
        // Create test user
        const user = await createTestUser();
        userId = user.id;
        
        // Get auth token
        const response = await request(app)
            .post('/api/auth/login')
            .send({
                email: user.email,
                password: 'testpassword'
            });
        
        authToken = response.body.token;
    });
    
    afterAll(async () => {
        await db.destroy();
    });
    
    describe('GET /api/tasks', () => {
        beforeEach(async () => {
            // Create test tasks
            await Promise.all([
                createTestTask({ userId, title: 'Task 1' }),
                createTestTask({ userId, title: 'Task 2' }),
                createTestTask({ userId, title: 'Task 3' })
            ]);
        });
        
        it('should return user tasks', async () => {
            const response = await request(app)
                .get('/api/tasks')
                .set('Authorization', `Bearer ${authToken}`);
            
            expect(response.status).toBe(200);
            expect(response.body).toHaveLength(3);
            expect(response.body[0]).toHaveProperty('title');
            expect(response.body[0]).toHaveProperty('status');
        });
        
        it('should filter tasks by status', async () => {
            const response = await request(app)
                .get('/api/tasks')
                .query({ status: 'pending' })
                .set('Authorization', `Bearer ${authToken}`);
            
            expect(response.status).toBe(200);
            expect(response.body).toHaveLength(3);
            expect(response.body).toEqual(
                expect.arrayContaining([
                    expect.objectContaining({
                        status: 'pending'
                    })
                ])
            );
        });
    });
    
    describe('POST /api/tasks', () => {
        it('should create new task', async () => {
            const taskData = {
                title: 'New Task',
                description: 'Test Description',
                dueDate: new Date().toISOString(),
                priority: 'high'
            };
            
            const response = await request(app)
                .post('/api/tasks')
                .set('Authorization', `Bearer ${authToken}`)
                .send(taskData);
            
            expect(response.status).toBe(201);
            expect(response.body).toMatchObject({
                title: taskData.title,
                description: taskData.description,
                priority: taskData.priority
            });
        });
        
        it('should validate request body', async () => {
            const response = await request(app)
                .post('/api/tasks')
                .set('Authorization', `Bearer ${authToken}`)
                .send({});  // Empty body
            
            expect(response.status).toBe(400);
            expect(response.body).toHaveProperty('error');
        });
    });
});
```

### 2.2 Database Tests

```typescript
import { db } from '../database';
import { TaskRepository } from '../repositories/TaskRepository';
import { createTestUser } from './helpers';

describe('TaskRepository', () => {
    let taskRepository: TaskRepository;
    let userId: string;
    
    beforeAll(async () => {
        await db.migrate.latest();
        const user = await createTestUser();
        userId = user.id;
    });
    
    afterAll(async () => {
        await db.destroy();
    });
    
    beforeEach(async () => {
        await db('tasks').delete();
        taskRepository = new TaskRepository(db);
    });
    
    describe('create', () => {
        it('should create task with correct data', async () => {
            const taskData = {
                userId,
                title: 'Test Task',
                description: 'Description',
                dueDate: new Date(),
                priority: 'high'
            };
            
            const task = await taskRepository.create(taskData);
            
            expect(task).toMatchObject(taskData);
            expect(task).toHaveProperty('id');
            expect(task).toHaveProperty('createdAt');
            
            // Verify in database
            const dbTask = await db('tasks')
                .where('id', task.id)
                .first();
            
            expect(dbTask).toMatchObject(taskData);
        });
    });
    
    describe('findAll', () => {
        it('should return tasks with pagination', async () => {
            // Create 15 test tasks
            await Promise.all(
                Array(15).fill(0).map((_, i) =>
                    taskRepository.create({
                        userId,
                        title: `Task ${i + 1}`
                    })
                )
            );
            
            const result = await taskRepository.findAll({
                userId,
                page: 1,
                limit: 10
            });
            
            expect(result.items).toHaveLength(10);
            expect(result.total).toBe(15);
            expect(result.hasMore).toBe(true);
        });
    });
});
```

## 3. End-to-End Testing

### 3.1 UI Tests

```typescript
import { test, expect } from '@playwright/test';

test.describe('Task Management', () => {
    test.beforeEach(async ({ page }) => {
        // Login before each test
        await page.goto('/login');
        await page.fill('[data-testid="email"]', 'test@example.com');
        await page.fill('[data-testid="password"]', 'password');
        await page.click('[data-testid="login-button"]');
        
        // Wait for dashboard to load
        await page.waitForSelector('[data-testid="dashboard"]');
    });
    
    test('should create new task', async ({ page }) => {
        // Click create task button
        await page.click('[data-testid="create-task-button"]');
        
        // Fill task form
        await page.fill('[data-testid="task-title"]', 'New Test Task');
        await page.fill(
            '[data-testid="task-description"]',
            'Test Description'
        );
        await page.selectOption(
            '[data-testid="task-priority"]',
            'high'
        );
        
        // Submit form
        await page.click('[data-testid="submit-task"]');
        
        // Verify task appears in list
        await expect(
            page.locator('text=New Test Task')
        ).toBeVisible();
        
        // Verify task details
        await page.click('text=New Test Task');
        await expect(
            page.locator('[data-testid="task-description"]')
        ).toHaveText('Test Description');
        await expect(
            page.locator('[data-testid="task-priority"]')
        ).toHaveText('High');
    });
    
    test('should complete task', async ({ page }) => {
        // Find task in list
        const taskRow = page.locator(
            '[data-testid="task-row"]:has-text("New Test Task")'
        );
        
        // Click complete button
        await taskRow
            .locator('[data-testid="complete-task"]')
            .click();
        
        // Verify task is marked as completed
        await expect(taskRow).toHaveClass(/completed/);
        
        // Verify task appears in completed list
        await page.click('[data-testid="completed-tasks-tab"]');
        await expect(
            page.locator('text=New Test Task')
        ).toBeVisible();
    });
});
```

### 3.2 Mobile Tests

```typescript
import { test, expect } from '@playwright/test';

test.describe('Mobile Experience', () => {
    test.use({ viewport: { width: 375, height: 667 } });
    
    test.beforeEach(async ({ page }) => {
        // Set mobile user agent
        await page.setExtraHTTPHeaders({
            'User-Agent': 'Mobile Device'
        });
        
        await page.goto('/');
    });
    
    test('should show mobile navigation', async ({ page }) => {
        // Verify hamburger menu
        await expect(
            page.locator('[data-testid="mobile-menu"]')
        ).toBeVisible();
        
        // Open menu
        await page.click('[data-testid="mobile-menu"]');
        
        // Verify navigation items
        await expect(
            page.locator('[data-testid="nav-tasks"]')
        ).toBeVisible();
        await expect(
            page.locator('[data-testid="nav-habits"]')
        ).toBeVisible();
        await expect(
            page.locator('[data-testid="nav-goals"]')
        ).toBeVisible();
    });
    
    test('should handle touch interactions', async ({ page }) => {
        // Find task item
        const taskItem = page.locator(
            '[data-testid="task-item"]:first-child'
        );
        
        // Swipe left to reveal actions
        await taskItem.evaluate((element: HTMLElement) => {
            element.dispatchEvent(new TouchEvent('touchstart', {
                touches: [{ clientX: 300, clientY: 200 }]
            }));
            element.dispatchEvent(new TouchEvent('touchmove', {
                touches: [{ clientX: 100, clientY: 200 }]
            }));
            element.dispatchEvent(new TouchEvent('touchend'));
        });
        
        // Verify action buttons are visible
        await expect(
            taskItem.locator('[data-testid="complete-action"]')
        ).toBeVisible();
        await expect(
            taskItem.locator('[data-testid="delete-action"]')
        ).toBeVisible();
    });
});
```

## 4. Performance Testing

### 4.1 Load Testing

```typescript
import { check, sleep } from 'k6';
import http from 'k6/http';

export const options = {
    stages: [
        { duration: '1m', target: 50 },   // Ramp up
        { duration: '3m', target: 50 },   // Stay at 50 users
        { duration: '1m', target: 100 },  // Ramp up to 100
        { duration: '3m', target: 100 },  // Stay at 100
        { duration: '1m', target: 0 }     // Ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],  // 95% under 500ms
        http_req_failed: ['rate<0.01']     // Less than 1% errors
    }
};

const BASE_URL = 'https://api.productivityapp.com';

export default function() {
    const authToken = authenticate();
    
    // Get tasks
    const tasksResponse = http.get(
        `${BASE_URL}/api/tasks`,
        {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        }
    );
    
    check(tasksResponse, {
        'tasks status 200': (r) => r.status === 200,
        'tasks load time OK': (r) => r.timings.duration < 500
    });
    
    sleep(1);
    
    // Create task
    const createResponse = http.post(
        `${BASE_URL}/api/tasks`,
        JSON.stringify({
            title: 'Load Test Task',
            priority: 'medium'
        }),
        {
            headers: {
                'Authorization': `Bearer ${authToken}`,
                'Content-Type': 'application/json'
            }
        }
    );
    
    check(createResponse, {
        'create status 201': (r) => r.status === 201,
        'create time OK': (r) => r.timings.duration < 1000
    });
    
    sleep(1);
}

function authenticate() {
    const loginResponse = http.post(
        `${BASE_URL}/api/auth/login`,
        JSON.stringify({
            email: 'loadtest@example.com',
            password: 'testpassword'
        }),
        {
            headers: {
                'Content-Type': 'application/json'
            }
        }
    );
    
    return loginResponse.json('token');
}
```

### 4.2 Stress Testing

```typescript
import { check, sleep } from 'k6';
import http from 'k6/http';

export const options = {
    scenarios: {
        stress: {
            executor: 'ramping-arrival-rate',
            preAllocatedVUs: 500,
            timeUnit: '1s',
            stages: [
                { duration: '2m', target: 10 },   // Below normal load
                { duration: '5m', target: 10 },
                { duration: '2m', target: 20 },   // Normal load
                { duration: '5m', target: 20 },
                { duration: '2m', target: 30 },   // Around breaking point
                { duration: '5m', target: 30 },
                { duration: '2m', target: 40 },   // Beyond breaking point
                { duration: '5m', target: 40 },
                { duration: '10m', target: 0 }    // Scale down
            ]
        }
    }
};

const API_BASE_URL = 'https://api.productivityapp.com';

export default function() {
    const requests = {
        'get_tasks': {
            method: 'GET',
            url: `${API_BASE_URL}/api/tasks`,
            params: {
                headers: {
                    'Authorization': `Bearer ${getToken()}`
                }
            }
        },
        'create_task': {
            method: 'POST',
            url: `${API_BASE_URL}/api/tasks`,
            params: {
                headers: {
                    'Authorization': `Bearer ${getToken()}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    title: 'Stress Test Task',
                    priority: 'high'
                })
            }
        },
        'get_habits': {
            method: 'GET',
            url: `${API_BASE_URL}/api/habits`,
            params: {
                headers: {
                    'Authorization': `Bearer ${getToken()}`
                }
            }
        }
    };
    
    const responses = http.batch(requests);
    
    // Check responses
    for (const [name, response] of Object.entries(responses)) {
        check(response, {
            [`${name} status OK`]: (r) => r.status < 400,
            [`${name} duration OK`]: (r) => r.timings.duration < 2000
        });
    }
    
    sleep(1);
}
```

## 5. Security Testing

### 5.1 Penetration Testing

```typescript
import { test, expect } from '@playwright/test';

test.describe('Security Tests', () => {
    test('should prevent XSS attacks', async ({ page }) => {
        // Attempt XSS in task title
        const xssPayload = '<script>alert("XSS")</script>';
        
        await page.goto('/tasks/new');
        await page.fill('[data-testid="task-title"]', xssPayload);
        await page.click('[data-testid="submit"]');
        
        // Verify XSS payload is escaped
        const taskTitle = await page.textContent(
            '[data-testid="task-title"]'
        );
        expect(taskTitle).not.toContain('<script>');
    });
    
    test('should prevent SQL injection', async ({ request }) => {
        // Attempt SQL injection in query parameter
        const response = await request.get(
            '/api/tasks?title=test\' OR \'1\'=\'1'
        );
        
        expect(response.status()).toBe(400);
    });
    
    test('should enforce authentication', async ({ request }) => {
        // Attempt to access protected endpoint without token
        const response = await request.get('/api/tasks');
        
        expect(response.status()).toBe(401);
    });
    
    test('should prevent CSRF attacks', async ({ page }) => {
        // Verify CSRF token is required for mutations
        const response = await page.request.post('/api/tasks', {
            data: {
                title: 'Test Task'
            }
        });
        
        expect(response.status()).toBe(403);
        expect(response.statusText()).toContain('CSRF');
    });
});
```

### 5.2 Security Scanning

```typescript
import { test, expect } from '@playwright/test';
import { runSecurityScan } from './security-scanner';

test.describe('Security Scanning', () => {
    test('should not have known vulnerabilities', async ({ page }) => {
        const results = await runSecurityScan({
            url: 'https://productivityapp.com',
            options: {
                includeXss: true,
                includeSqli: true,
                includeWeakCrypto: true
            }
        });
        
        expect(results.critical).toHaveLength(0);
        expect(results.high).toHaveLength(0);
        
        // Document any medium/low risks
        if (results.medium.length > 0) {
            console.warn('Medium risks:', results.medium);
        }
    });
    
    test('should use secure headers', async ({ request }) => {
        const response = await request.get('/');
        const headers = response.headers();
        
        expect(headers['strict-transport-security'])
            .toBeDefined();
        expect(headers['x-content-type-options'])
            .toBe('nosniff');
        expect(headers['x-frame-options'])
            .toBe('DENY');
        expect(headers['content-security-policy'])
            .toBeDefined();
    });
    
    test('should handle rate limiting', async ({ request }) => {
        const attempts = Array(10).fill(0).map(() =>
            request.post('/api/auth/login', {
                data: {
                    email: 'test@example.com',
                    password: 'wrong'
                }
            })
        );
        
        const responses = await Promise.all(attempts);
        const lastResponse = responses[responses.length - 1];
        
        expect(lastResponse.status()).toBe(429);
    });
});
```

This comprehensive testing strategy document provides detailed implementations for unit testing, integration testing, end-to-end testing, performance testing, and security testing. The testing approach is designed to ensure high quality and reliability of the application while maintaining good test coverage and early detection of issues.
