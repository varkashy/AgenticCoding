/**
 * Vite Configuration
 * 
 * Configures the frontend development environment:
 * - React plugin for JSX support
 * - Development server on port 3000
 * - API proxy to forward /api requests to backend on port 8000
 */

import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  // Enable React plugin for JSX support
  plugins: [react()],
  
  // Development server configuration
  server: {
    // Frontend runs on this port
    port: 3000,
    
    // ============ API PROXY ============
    // All requests to /api/* are forwarded to the backend
    // This solves CORS issues during development
    // Example: http://localhost:3000/api/weather → http://localhost:8000/api/weather
    proxy: {
      '/api': {
        target: 'http://localhost:8000',  // Backend server address
        changeOrigin: true                 // Modify origin header for compatibility
      }
    }
  }
});
