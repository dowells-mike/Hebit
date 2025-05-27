module.exports = {
  root: true,
  env: {
    node: true,
    es2021: true,
    jest: true, // Added for Jest support
  },
  parser: '@typescript-eslint/parser',
  parserOptions: {
    ecmaVersion: 12,
    sourceType: 'module',
    project: './tsconfig.eslint.json', // Updated to point to tsconfig.eslint.json
  },
  plugins: [
    '@typescript-eslint',
  ],
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    // Consider adding prettier integration if you use it: 
    // 'plugin:prettier/recommended', 
  ],
  rules: {
    // Add any specific rule overrides here
    // For example, to allow unused variables (not recommended for production):
    // '@typescript-eslint/no-unused-vars': ['warn', { 'argsIgnorePattern': '^_|' }],
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    // Add other rules as needed
  },
  ignorePatterns: ['node_modules/', 'dist/', 'coverage/', 'migrations/', 'hebit_backup/'],
}; 