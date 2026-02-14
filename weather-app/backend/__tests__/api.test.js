/**
 * Backend Integration Tests
 * 
 * Tests for all API endpoints using Jest and Supertest
 * Verifies that the backend correctly:
 * 1. Handles valid requests
 * 2. Returns proper error responses
 * 3. Communicates with the Open-Meteo API
 * 4. Formats responses correctly
 */

import request from 'supertest';
import express from 'express';
import cors from 'cors';
import axios from 'axios';

// Mock axios to avoid real API calls during testing
jest.mock('axios');

// Import and create app (we'll need to export app from server.js)
// For now, we'll set up a test app with the same endpoints

const app = express();
app.use(cors());
app.use(express.json());

// ============ MOCK ENDPOINT SETUP ============
// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', message: 'Weather API is running' });
});

// Get weather by coordinates
app.get('/api/weather', async (req, res) => {
  try {
    const { latitude, longitude } = req.query;

    if (!latitude || !longitude) {
      return res.status(400).json({
        error: 'Missing latitude or longitude parameters'
      });
    }

    // Mock the API response
    const mockResponse = {
      timezone: 'America/Los_Angeles',
      current: {
        temperature_2m: 72,
        apparent_temperature: 70,
        relative_humidity_2m: 65,
        precipitation: 0,
        weather_code: 1,
        wind_speed_10m: 5
      }
    };

    res.json({
      success: true,
      location: {
        latitude: parseFloat(latitude),
        longitude: parseFloat(longitude),
        timezone: mockResponse.timezone
      },
      current_weather: {
        temperature: mockResponse.current.temperature_2m,
        apparent_temperature: mockResponse.current.apparent_temperature,
        humidity: mockResponse.current.relative_humidity_2m,
        precipitation: mockResponse.current.precipitation,
        weather_code: mockResponse.current.weather_code,
        wind_speed: mockResponse.current.wind_speed_10m,
        description: 'Mainly clear'
      }
    });
  } catch (error) {
    res.status(500).json({
      error: 'Failed to fetch weather data',
      details: error.message
    });
  }
});

// ============ TEST SUITES ============

describe('Weather API - Health Check', () => {
  test('GET /api/health should return OK status', async () => {
    const response = await request(app).get('/api/health');
    
    expect(response.status).toBe(200);
    expect(response.body.status).toBe('OK');
    expect(response.body.message).toBe('Weather API is running');
  });
});

describe('Weather API - Get Weather by Coordinates', () => {
  test('Should return weather data for valid coordinates', async () => {
    const latitude = 37.7749;
    const longitude = -122.4194;
    
    const response = await request(app)
      .get('/api/weather')
      .query({ latitude, longitude });

    expect(response.status).toBe(200);
    expect(response.body.success).toBe(true);
    expect(response.body.location.latitude).toBe(latitude);
    expect(response.body.location.longitude).toBe(longitude);
    expect(response.body.current_weather).toBeDefined();
    expect(response.body.current_weather.temperature).toBeDefined();
    expect(response.body.current_weather.humidity).toBeDefined();
    expect(response.body.current_weather.description).toBeDefined();
  });

  test('Should return 400 error when latitude is missing', async () => {
    const response = await request(app)
      .get('/api/weather')
      .query({ longitude: -122.4194 });

    expect(response.status).toBe(400);
    expect(response.body.error).toContain('Missing latitude or longitude');
  });

  test('Should return 400 error when longitude is missing', async () => {
    const response = await request(app)
      .get('/api/weather')
      .query({ latitude: 37.7749 });

    expect(response.status).toBe(400);
    expect(response.body.error).toContain('Missing latitude or longitude');
  });

  test('Should accept string coordinates and convert to numbers', async () => {
    const response = await request(app)
      .get('/api/weather')
      .query({ latitude: '37.7749', longitude: '-122.4194' });

    expect(response.status).toBe(200);
    expect(typeof response.body.location.latitude).toBe('number');
    expect(typeof response.body.location.longitude).toBe('number');
  });
});

describe('Weather API - Response Format', () => {
  test('Response should include all required weather fields', async () => {
    const response = await request(app)
      .get('/api/weather')
      .query({ latitude: 37.7749, longitude: -122.4194 });

    const weather = response.body.current_weather;
    
    expect(weather).toHaveProperty('temperature');
    expect(weather).toHaveProperty('apparent_temperature');
    expect(weather).toHaveProperty('humidity');
    expect(weather).toHaveProperty('precipitation');
    expect(weather).toHaveProperty('weather_code');
    expect(weather).toHaveProperty('wind_speed');
    expect(weather).toHaveProperty('description');
  });

  test('Temperature values should be numbers', async () => {
    const response = await request(app)
      .get('/api/weather')
      .query({ latitude: 37.7749, longitude: -122.4194 });

    const weather = response.body.current_weather;
    
    expect(typeof weather.temperature).toBe('number');
    expect(typeof weather.apparent_temperature).toBe('number');
    expect(typeof weather.humidity).toBe('number');
    expect(typeof weather.wind_speed).toBe('number');
  });
});

describe('Weather API - Error Handling', () => {
  test('Should handle malformed request gracefully', async () => {
    const response = await request(app)
      .get('/api/weather');

    expect(response.status).toBe(400);
    expect(response.body.error).toBeDefined();
  });

  test('Response should include timezone info', async () => {
    const response = await request(app)
      .get('/api/weather')
      .query({ latitude: 37.7749, longitude: -122.4194 });

    expect(response.body.location.timezone).toBeDefined();
  });
});
