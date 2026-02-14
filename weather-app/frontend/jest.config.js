/**
 * Jest Configuration for Frontend
 * 
 * - testEnvironment: 'jsdom' - Use browser-like environment for React testing
 * - setupFilesAfterEnv: Run setup file after Jest initializes
 * - moduleNameMapper: Map CSS and other assets to mock implementations
 * - transform: Use Babel for JSX transformation
 * - collectCoverageFrom: Specify which files to include in coverage
 * - coverageThreshold: Set minimum coverage requirements
 */

module.exports = {
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/src/setupTests.js'],
  moduleNameMapper: {
    '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
  },
  transform: {
    '^.+\\.(js|jsx)$': 'babel-jest',
  },
  transformIgnorePatterns: ['node_modules/'],
  testMatch: ['**/__tests__/**/*.test.jsx', '**/__tests__/**/*.test.js'],
  collectCoverageFrom: [
    'src/**/*.{js,jsx}',
    '!src/main.jsx',
    '!src/index.jsx'
  ],
  coverageThreshold: {
    global: {
      branches: 50,
      functions: 50,
      lines: 50,
      statements: 50
    }
  }
};
