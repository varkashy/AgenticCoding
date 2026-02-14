# Test Execution Guide

This document explains how to run unit tests and integration tests for the Weather App.

## Overview

The weather app includes two types of tests:

1. **Backend Integration Tests** - Test API endpoints with Jest + Supertest
2. **Frontend Unit Tests** - Test React components with Jest + React Testing Library

## Prerequisites

Ensure you have Node.js v16+ installed:

```bash
node --version
npm --version
```

## Installation

### Backend Test Dependencies

Navigate to backend directory and install test packages:

```bash
cd weather-app/backend
npm install --save-dev jest supertest @babel/preset-env
```

### Frontend Test Dependencies

Navigate to frontend directory and install test packages:

```bash
cd weather-app/frontend
npm install --save-dev jest @testing-library/react @testing-library/jest-dom @testing-library/user-event babel-jest identity-obj-proxy
```

## Running Tests

### Backend Integration Tests

Run all backend tests:

```bash
cd weather-app/backend
npm test
```

Run with coverage report:

```bash
npm test -- --coverage
```

Run a specific test file:

```bash
npm test -- __tests__/api.test.js
```

Run tests in watch mode (re-runs on file changes):

```bash
npm test -- --watch
```

**Backend Test Suite Breakdown:**

- **Health Check Tests** - Verifies `/api/health` endpoint responds correctly
- **Weather by Coordinates** - Tests `/api/weather` endpoint with valid/invalid parameters
- **Response Format Tests** - Validates response structure and data types
- **Error Handling Tests** - Tests error responses for malformed requests

### Frontend Unit Tests

Run all frontend tests:

```bash
cd weather-app/frontend
npm test
```

Run with coverage report:

```bash
npm test -- --coverage
```

Run a specific test file:

```bash
npm test -- __tests__/App.test.jsx
```

Run tests in watch mode:

```bash
npm test -- --watch
```

Exit watch mode by pressing `q` in the terminal.

**Frontend Test Suite Breakdown:**

- **App Component Tests** - Tests main component rendering, state management, temperature toggle
- **WeatherSearch Component Tests** - Tests search input, form submission, input clearing
- **WeatherDisplay Component Tests** - Tests weather data display, formatting, location info
- **Temperature Conversion Tests** - Tests F→C and C→F conversion accuracy

## Test Coverage

### Backend Tests Cover:

✅ Health check endpoint response
✅ Valid coordinate requests
✅ Missing parameter validation
✅ Error handling and responses
✅ Response format and data types
✅ Timezone information inclusion

### Frontend Tests Cover:

✅ Component rendering
✅ User interactions (typing, clicking)
✅ State management
✅ Temperature unit toggling
✅ Temperature conversion logic
✅ Error messages display
✅ Geolocation permission denial
✅ Search functionality
✅ Weather data display

## Expected Test Output

### Backend Tests:

```
PASS  __tests__/api.test.js

  Weather API - Health Check
    ✓ GET /api/health should return OK status (10ms)

  Weather API - Get Weather by Coordinates
    ✓ Should return weather data for valid coordinates (15ms)
    ✓ Should return 400 error when latitude is missing (8ms)
    ✓ Should return 400 error when longitude is missing (7ms)

  ...

Test Suites: 1 passed, 1 total
Tests:       24 passed, 24 total
Snapshots:   0 total
Time:        2.345 s
```

### Frontend Tests:

```
PASS  __tests__/App.test.jsx

  App Component
    ✓ Should render the app title (25ms)
    ✓ Should display temperature toggle button (20ms)
    ✓ Should toggle temperature unit when button is clicked (35ms)

  WeatherSearch Component
    ✓ Should render search input field (15ms)
    ✓ Should call onSearch when form is submitted (30ms)

  ...

Test Suites: 1 passed, 1 total
Tests:       32 passed, 32 total
Snapshots:   0 total
Time:        3.456 s
```

## Test Configuration Files

### backend package.json

Add to `"scripts"` section:

```json
"test": "jest",
"test:watch": "jest --watch",
"test:coverage": "jest --coverage"
```

### backend jest.config.js

Create a file with:

```javascript
module.exports = {
  testEnvironment: 'node',
  collectCoverageFrom: ['server.js'],
  coverageThreshold: {
    global: {
      branches: 50,
      functions: 50,
      lines: 50,
      statements: 50
    }
  }
};
```

### frontend package.json

Add to `"scripts"` section:

```json
"test": "jest",
"test:watch": "jest --watch",
"test:coverage": "jest --coverage"
```

### frontend jest.config.js

Create a file with:

```javascript
module.exports = {
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/src/setupTests.js'],
  moduleNameMapper: {
    '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
  },
  transform: {
    '^.+\\.(js|jsx)$': 'babel-jest',
  },
  collectCoverageFrom: ['src/**/*.{js,jsx}', '!src/main.jsx'],
  coverageThreshold: {
    global: {
      branches: 50,
      functions: 50,
      lines: 50,
      statements: 50
    }
  }
};
```

### frontend src/setupTests.js

Create with:

```javascript
import '@testing-library/jest-dom';
```

## Continuous Integration (CI)

To run all tests as part of CI/CD pipeline:

```bash
#!/bin/bash
# Run backend tests
cd weather-app/backend && npm test || exit 1

# Run frontend tests
cd ../frontend && npm test -- --passWithNoTests || exit 1

echo "✓ All tests passed!"
```

## Debugging Tests

### Run single test with verbose output:

```bash
npm test -- --verbose __tests__/api.test.js
```

### Debug in Node inspector:

```bash
node --inspect-brk node_modules/.bin/jest --runInBand
```

Then open `chrome://inspect` in Chrome DevTools.

### View coverage gaps:

```bash
npm test -- --coverage
cat coverage/lcov-report/index.html
```

## Adding New Tests

### Backend Test Template:

```javascript
describe('New Feature', () => {
  test('Should do something', async () => {
    const response = await request(app).get('/api/new-endpoint');
    expect(response.status).toBe(200);
  });
});
```

### Frontend Test Template:

```javascript
describe('New Component', () => {
  test('Should render correctly', () => {
    render(<NewComponent />);
    expect(screen.getByText('Expected Text')).toBeInTheDocument();
  });
});
```

## Troubleshooting

### Tests won't run

- **Issue**: `jest: command not found`
  - **Solution**: Run `npm install` in the respective directory

### Module not found errors

- **Issue**: Cannot find module (for CSS, etc.)
  - **Solution**: Update `jest.config.js` with proper `moduleNameMapper`

### Timeout errors

- **Issue**: Tests timeout (default 5000ms)
  - **Solution**: Increase timeout: `test('name', () => {...}, 10000)`

### Port already in use

- **Issue**: Backend tests fail with port binding error
  - **Solution**: Use ephemeral ports in tests or close other instances

## Best Practices

1. ✅ Run tests before committing changes
2. ✅ Write tests for new features
3. ✅ Keep tests independent (no shared state)
4. ✅ Use descriptive test names
5. ✅ Mock external APIs (like Open-Meteo)
6. ✅ Test edge cases and error conditions
7. ✅ Maintain >80% code coverage for critical code
8. ✅ Review coverage reports regularly

## Next Steps

1. Run all tests to verify setup
2. Fix any failing tests
3. Integrate tests into your git hooks (pre-commit)
4. Set up CI/CD pipeline to run tests automatically
5. Increase test coverage incrementally
