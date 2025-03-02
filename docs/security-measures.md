# Security Measures & Data Protection

## 1. Authentication System

### 1.1 JWT Implementation

```typescript
interface JWTConfig {
    algorithm: 'RS256';
    privateKey: string;
    publicKey: string;
    accessTokenExpiry: number;  // 15 minutes
    refreshTokenExpiry: number; // 30 days
}

class JWTManager {
    private config: JWTConfig;
    
    async generateTokens(user: User): Promise<TokenPair> {
        const accessToken = await this.signToken(
            {
                userId: user.id,
                type: 'access',
                permissions: user.permissions
            },
            this.config.accessTokenExpiry
        );
        
        const refreshToken = await this.signToken(
            {
                userId: user.id,
                type: 'refresh',
                tokenFamily: crypto.randomUUID()
            },
            this.config.refreshTokenExpiry
        );
        
        return { accessToken, refreshToken };
    }
    
    async verifyToken(token: string): Promise<TokenPayload> {
        try {
            const payload = jwt.verify(
                token,
                this.config.publicKey,
                { algorithms: ['RS256'] }
            );
            
            return payload as TokenPayload;
        } catch (error) {
            if (error instanceof jwt.TokenExpiredError) {
                throw new AuthError('Token expired');
            }
            throw new AuthError('Invalid token');
        }
    }
}
```

### 1.2 Password Security

```typescript
interface PasswordConfig {
    minLength: number;
    requireUppercase: boolean;
    requireNumbers: boolean;
    requireSpecial: boolean;
    saltRounds: number;
}

class PasswordManager {
    private config: PasswordConfig = {
        minLength: 12,
        requireUppercase: true,
        requireNumbers: true,
        requireSpecial: true,
        saltRounds: 12
    };
    
    async hashPassword(password: string): Promise<string> {
        this.validatePassword(password);
        const salt = await bcrypt.genSalt(this.config.saltRounds);
        return bcrypt.hash(password, salt);
    }
    
    async verifyPassword(
        password: string,
        hash: string
    ): Promise<boolean> {
        return bcrypt.compare(password, hash);
    }
    
    private validatePassword(password: string): void {
        if (password.length < this.config.minLength) {
            throw new ValidationError(
                `Password must be at least ${this.config.minLength} characters`
            );
        }
        
        if (this.config.requireUppercase && !/[A-Z]/.test(password)) {
            throw new ValidationError(
                'Password must contain at least one uppercase letter'
            );
        }
        
        if (this.config.requireNumbers && !/\d/.test(password)) {
            throw new ValidationError(
                'Password must contain at least one number'
            );
        }
        
        if (this.config.requireSpecial && !/[^A-Za-z0-9]/.test(password)) {
            throw new ValidationError(
                'Password must contain at least one special character'
            );
        }
    }
}
```

### 1.3 Rate Limiting

```typescript
interface RateLimitConfig {
    window: number;  // time window in seconds
    max: number;     // maximum requests in window
    blockDuration: number;  // blocking duration in seconds
}

class RateLimiter {
    private config: Record<string, RateLimitConfig> = {
        login: {
            window: 300,     // 5 minutes
            max: 5,          // 5 attempts
            blockDuration: 900  // 15 minutes
        },
        api: {
            window: 60,      // 1 minute
            max: 100,        // 100 requests
            blockDuration: 300  // 5 minutes
        }
    };
    
    async checkLimit(
        key: string,
        type: keyof typeof this.config
    ): Promise<void> {
        const config = this.config[type];
        const attempts = await this.getAttempts(key, type);
        
        if (attempts >= config.max) {
            throw new RateLimitError(
                `Too many attempts. Try again in ${config.blockDuration} seconds`
            );
        }
        
        await this.recordAttempt(key, type);
    }
    
    private async getAttempts(
        key: string,
        type: string
    ): Promise<number> {
        const attempts = await redis.get(`ratelimit:${type}:${key}`);
        return attempts ? parseInt(attempts, 10) : 0;
    }
    
    private async recordAttempt(
        key: string,
        type: string
    ): Promise<void> {
        const config = this.config[type];
        const redisKey = `ratelimit:${type}:${key}`;
        
        await redis.multi()
            .incr(redisKey)
            .expire(redisKey, config.window)
            .exec();
    }
}
```

## 2. Data Encryption

### 2.1 Encryption at Rest

```typescript
interface EncryptionConfig {
    algorithm: 'aes-256-gcm';
    keySize: number;
    tagLength: number;
    saltLength: number;
}

class DataEncryption {
    private config: EncryptionConfig = {
        algorithm: 'aes-256-gcm',
        keySize: 32,
        tagLength: 16,
        saltLength: 16
    };
    
    async encrypt(data: string): Promise<EncryptedData> {
        const salt = crypto.randomBytes(this.config.saltLength);
        const key = await this.deriveKey(salt);
        const iv = crypto.randomBytes(12);
        
        const cipher = crypto.createCipheriv(
            this.config.algorithm,
            key,
            iv,
            { authTagLength: this.config.tagLength }
        );
        
        const encrypted = Buffer.concat([
            cipher.update(data, 'utf8'),
            cipher.final()
        ]);
        
        const tag = cipher.getAuthTag();
        
        return {
            encrypted: encrypted.toString('base64'),
            iv: iv.toString('base64'),
            tag: tag.toString('base64'),
            salt: salt.toString('base64')
        };
    }
    
    async decrypt(
        encryptedData: EncryptedData
    ): Promise<string> {
        const salt = Buffer.from(encryptedData.salt, 'base64');
        const key = await this.deriveKey(salt);
        const iv = Buffer.from(encryptedData.iv, 'base64');
        const tag = Buffer.from(encryptedData.tag, 'base64');
        const encrypted = Buffer.from(encryptedData.encrypted, 'base64');
        
        const decipher = crypto.createDecipheriv(
            this.config.algorithm,
            key,
            iv,
            { authTagLength: this.config.tagLength }
        );
        
        decipher.setAuthTag(tag);
        
        return Buffer.concat([
            decipher.update(encrypted),
            decipher.final()
        ]).toString('utf8');
    }
    
    private async deriveKey(salt: Buffer): Promise<Buffer> {
        return crypto.pbkdf2Sync(
            process.env.ENCRYPTION_KEY!,
            salt,
            100000,  // iterations
            this.config.keySize,
            'sha256'
        );
    }
}
```

### 2.2 Data Masking

```typescript
interface MaskingRule {
    pattern: RegExp;
    replacement: string;
}

class DataMasking {
    private rules: Record<string, MaskingRule> = {
        email: {
            pattern: /([^@\s]+)@([\s\S]+)/,
            replacement: '$1***@$2'
        },
        phone: {
            pattern: /(\d{3})\d{6}(\d{3})/,
            replacement: '$1******$2'
        },
        creditCard: {
            pattern: /(\d{4})\d{8}(\d{4})/,
            replacement: '$1********$2'
        }
    };
    
    maskData(
        data: string,
        type: keyof typeof this.rules
    ): string {
        const rule = this.rules[type];
        return data.replace(rule.pattern, rule.replacement);
    }
    
    maskObject(
        obj: Record<string, any>,
        fields: Record<string, keyof typeof this.rules>
    ): Record<string, any> {
        const masked = { ...obj };
        
        for (const [field, type] of Object.entries(fields)) {
            if (masked[field]) {
                masked[field] = this.maskData(masked[field], type);
            }
        }
        
        return masked;
    }
}
```

## 3. API Security

### 3.1 Request Validation

```typescript
interface ValidationRule {
    type: 'string' | 'number' | 'boolean' | 'array' | 'object';
    required?: boolean;
    min?: number;
    max?: number;
    pattern?: RegExp;
    enum?: any[];
    validate?: (value: any) => boolean;
}

class RequestValidator {
    private schemas: Record<string, Record<string, ValidationRule>> = {
        createTask: {
            title: {
                type: 'string',
                required: true,
                min: 1,
                max: 255
            },
            dueDate: {
                type: 'string',
                validate: (value) => !isNaN(Date.parse(value))
            },
            priority: {
                type: 'string',
                enum: ['low', 'medium', 'high']
            }
        }
    };
    
    validate(
        data: Record<string, any>,
        schema: keyof typeof this.schemas
    ): void {
        const rules = this.schemas[schema];
        
        for (const [field, rule] of Object.entries(rules)) {
            this.validateField(data[field], field, rule);
        }
    }
    
    private validateField(
        value: any,
        field: string,
        rule: ValidationRule
    ): void {
        if (rule.required && value === undefined) {
            throw new ValidationError(
                `Field '${field}' is required`
            );
        }
        
        if (value === undefined) return;
        
        if (typeof value !== rule.type) {
            throw new ValidationError(
                `Field '${field}' must be of type ${rule.type}`
            );
        }
        
        if (rule.min !== undefined) {
            if (rule.type === 'string' && value.length < rule.min) {
                throw new ValidationError(
                    `Field '${field}' must be at least ${rule.min} characters`
                );
            }
            if (rule.type === 'number' && value < rule.min) {
                throw new ValidationError(
                    `Field '${field}' must be at least ${rule.min}`
                );
            }
        }
        
        if (rule.pattern && !rule.pattern.test(value)) {
            throw new ValidationError(
                `Field '${field}' has invalid format`
            );
        }
        
        if (rule.enum && !rule.enum.includes(value)) {
            throw new ValidationError(
                `Field '${field}' must be one of: ${rule.enum.join(', ')}`
            );
        }
        
        if (rule.validate && !rule.validate(value)) {
            throw new ValidationError(
                `Field '${field}' failed validation`
            );
        }
    }
}
```

### 3.2 CORS Configuration

```typescript
interface CorsConfig {
    origins: string[];
    methods: string[];
    headers: string[];
    maxAge: number;
    credentials: boolean;
}

const corsConfig: CorsConfig = {
    origins: [
        'https://productivityapp.com',
        'https://api.productivityapp.com',
        /^https:\/\/.*\.productivityapp\.com$/
    ],
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    headers: [
        'Content-Type',
        'Authorization',
        'X-Request-ID'
    ],
    maxAge: 86400,  // 24 hours
    credentials: true
};

app.use(cors((req, callback) => {
    const origin = req.header('Origin');
    let corsOptions: CorsOptions;
    
    if (origin && corsConfig.origins.some(
        allowed => typeof allowed === 'string' 
            ? allowed === origin 
            : allowed.test(origin)
    )) {
        corsOptions = {
            origin: true,
            methods: corsConfig.methods,
            allowedHeaders: corsConfig.headers,
            maxAge: corsConfig.maxAge,
            credentials: corsConfig.credentials
        };
    } else {
        corsOptions = { origin: false };
    }
    
    callback(null, corsOptions);
}));
```

## 4. Audit & Monitoring

### 4.1 Security Logging

```typescript
interface SecurityEvent {
    timestamp: number;
    type: 'auth' | 'access' | 'modification' | 'error';
    severity: 'info' | 'warning' | 'error' | 'critical';
    userId?: string;
    action: string;
    resource: string;
    ip: string;
    userAgent: string;
    status: 'success' | 'failure';
    details?: Record<string, any>;
}

class SecurityLogger {
    async logEvent(event: SecurityEvent): Promise<void> {
        // Store in database
        await db.securityEvents.create(event);
        
        // Send to monitoring system
        await this.monitor(event);
        
        // Alert if critical
        if (event.severity === 'critical') {
            await this.alert(event);
        }
    }
    
    private async monitor(event: SecurityEvent): Promise<void> {
        // Aggregate events for monitoring
        await Promise.all([
            this.updateMetrics(event),
            this.checkThresholds(event)
        ]);
    }
    
    private async alert(event: SecurityEvent): Promise<void> {
        // Send alerts through configured channels
        await Promise.all([
            this.sendEmail(event),
            this.sendSlack(event),
            this.triggerPagerDuty(event)
        ]);
    }
}
```

### 4.2 Intrusion Detection

```typescript
interface SecurityRule {
    type: 'pattern' | 'threshold' | 'anomaly';
    condition: (event: SecurityEvent) => boolean;
    action: 'log' | 'block' | 'alert';
    severity: 'low' | 'medium' | 'high';
}

class IntrusionDetection {
    private rules: SecurityRule[] = [
        {
            type: 'pattern',
            condition: (event) => {
                return event.type === 'auth' &&
                       event.status === 'failure' &&
                       event.action === 'login';
            },
            action: 'block',
            severity: 'high'
        },
        {
            type: 'threshold',
            condition: (event) => {
                return event.type === 'access' &&
                       this.getRequestRate(event.ip) > 1000;
            },
            action: 'block',
            severity: 'medium'
        },
        {
            type: 'anomaly',
            condition: (event) => {
                return this.isAnomalousAccess(event);
            },
            action: 'alert',
            severity: 'medium'
        }
    ];
    
    async analyze(event: SecurityEvent): Promise<void> {
        for (const rule of this.rules) {
            if (rule.condition(event)) {
                await this.handleViolation(rule, event);
            }
        }
    }
    
    private async handleViolation(
        rule: SecurityRule,
        event: SecurityEvent
    ): Promise<void> {
        switch (rule.action) {
            case 'block':
                await this.blockIP(event.ip);
                break;
            case 'alert':
                await this.sendAlert(rule, event);
                break;
            case 'log':
                await this.logViolation(rule, event);
                break;
        }
    }
    
    private async isAnomalousAccess(
        event: SecurityEvent
    ): Promise<boolean> {
        // Check for unusual patterns
        const patterns = await this.getUserPatterns(event.userId);
        return this.detectAnomalies(event, patterns);
    }
}
```

This comprehensive security measures document provides detailed implementations for authentication, encryption, API security, and monitoring systems. The measures are designed to protect user data and system integrity while maintaining usability and performance.
