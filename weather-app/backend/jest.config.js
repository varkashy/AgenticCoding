/**
 * Jest Configuration for Backend
 * 
 * - testEnvironment: 'node' - Use Node.js environment (not browser)
 * - collectCoverageFrom: Specify which files to include in coverage
 * - coverageThreshold: Set minimum coverage requirements
 */

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
  },
  testMatch: ['**/__tests__/**/*.test.js'],
  transformIgnorePatterns: ['node_modules/']
};
