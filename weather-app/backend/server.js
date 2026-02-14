/**
 * Weather App Backend Server
 * 
 * This Express server provides API endpoints to fetch weather data from the Open-Meteo API.
 * It supports two main features:
 * 1. Get weather by geographic coordinates (latitude/longitude)
 * 2. Get weather by city name (with automatic geocoding)
 */

import express from 'express';
import cors from 'cors';
import axios from 'axios';

const app = express();
const PORT = process.env.PORT || 8000;

// ============ MIDDLEWARE ============
// Enable CORS - allows frontend (different port) to communicate with this backend
app.use(cors());
// Enable JSON request body parsing
app.use(express.json());

// ============ HEALTH CHECK ENDPOINT ============
// Simple endpoint to verify the server is running
// Used by frontend to confirm backend connectivity
app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', message: 'Weather API is running' });
});

// ============ WEATHER BY COORDINATES ENDPOINT ============
// Endpoint: GET /api/weather?latitude=X&longitude=Y
// Query Parameters:
//   - latitude: A number representing latitude coordinates
//   - longitude: A number representing longitude coordinates
// 
// This endpoint fetches weather data from the Open-Meteo API (free, no API key required)
// and returns formatted weather information including temperature, humidity, wind speed, etc.
app.get('/api/weather', async (req, res) => {
  try {
    const { latitude, longitude } = req.query;

    // Validate required parameters
    if (!latitude || !longitude) {
      return res.status(400).json({
        error: 'Missing latitude or longitude parameters'
      });
    }

    // Call Open-Meteo API with the requested coordinates
    // Parameters requested: temperature, humidity, weather conditions, wind speed, precipitation
    const response = await axios.get('https://api.open-meteo.com/v1/forecast', {
      params: {
        latitude,
        longitude,
        current: 'temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m',
        temperature_unit: 'fahrenheit',
        wind_speed_unit: 'mph',
        timezone: 'auto'
      }
    });

    const weatherData = response.data;

    // Format and return the weather data to the frontend
    res.json({
      success: true,
      location: {
        latitude: parseFloat(latitude),
        longitude: parseFloat(longitude),
        timezone: weatherData.timezone
      },
      current_weather: {
        temperature: weatherData.current.temperature_2m,
        apparent_temperature: weatherData.current.apparent_temperature,
        humidity: weatherData.current.relative_humidity_2m,
        precipitation: weatherData.current.precipitation,
        weather_code: weatherData.current.weather_code,
        wind_speed: weatherData.current.wind_speed_10m,
        description: getWeatherDescription(weatherData.current.weather_code)
      }
    });
  } catch (error) {
    console.error('Error fetching weather:', error.message);
    res.status(500).json({
      error: 'Failed to fetch weather data',
      details: error.message
    });
  }
});

// ============ WEATHER BY CITY NAME ENDPOINT ============
// Endpoint: GET /api/weather/city/:city
// URL Parameter:
//   - city: City name as a string (e.g., "London", "New York")
// 
// This endpoint converts a city name to coordinates using Geocoding API,
// then fetches weather data for that location
app.get('/api/weather/city/:city', async (req, res) => {
  try {
    const { city } = req.params;

    // Step 1: Use Open-Meteo Geocoding API to convert city name to coordinates
    const geoResponse = await axios.get('https://geocoding-api.open-meteo.com/v1/search', {
      params: {
        name: city,
        count: 1,
        language: 'en',
        format: 'json'
      }
    });

    // Check if city was found
    if (!geoResponse.data.results || geoResponse.data.results.length === 0) {
      return res.status(404).json({ error: 'City not found' });
    }

    // Extract the first matching city result
    const cityData = geoResponse.data.results[0];
    const { latitude, longitude } = cityData;

    // Step 2: Fetch weather data for the city coordinates
    const weatherResponse = await axios.get('https://api.open-meteo.com/v1/forecast', {
      params: {
        latitude,
        longitude,
        current: 'temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m',
        temperature_unit: 'fahrenheit',
        wind_speed_unit: 'mph',
        timezone: 'auto'
      }
    });

    const weatherData = weatherResponse.data;

    // Format city name with state and country information if available
    res.json({
      success: true,
      location: {
        name: `${cityData.name}${cityData.admin1 ? ', ' + cityData.admin1 : ''}${cityData.country ? ', ' + cityData.country : ''}`,
        latitude: cityData.latitude,
        longitude: cityData.longitude,
        timezone: weatherData.timezone
      },
      current_weather: {
        temperature: weatherData.current.temperature_2m,
        apparent_temperature: weatherData.current.apparent_temperature,
        humidity: weatherData.current.relative_humidity_2m,
        precipitation: weatherData.current.precipitation,
        weather_code: weatherData.current.weather_code,
        wind_speed: weatherData.current.wind_speed_10m,
        description: getWeatherDescription(weatherData.current.weather_code)
      }
    });
  } catch (error) {
    console.error('Error fetching weather:', error.message);
    res.status(500).json({
      error: 'Failed to fetch weather data',
      details: error.message
    });
  }
});

// ============ HELPER FUNCTION ============
// Converts WMO (World Meteorological Organization) weather codes to human-readable descriptions
// WMO codes describe weather conditions like clear, cloudy, rainy, snowy, etc.
function getWeatherDescription(code) {
  const weatherCodes = {
    0: 'Clear sky',
    1: 'Mainly clear',
    2: 'Partly cloudy',
    3: 'Overcast',
    45: 'Foggy',
    48: 'Foggy with rime',
    51: 'Light drizzle',
    53: 'Moderate drizzle',
    55: 'Dense drizzle',
    61: 'Slight rain',
    63: 'Moderate rain',
    65: 'Heavy rain',
    71: 'Slight snow',
    73: 'Moderate snow',
    75: 'Heavy snow',
    80: 'Slight rain showers',
    81: 'Moderate rain showers',
    82: 'Violent rain showers',
    85: 'Slight snow showers',
    86: 'Heavy snow showers',
    95: 'Thunderstorm',
    96: 'Thunderstorm with slight hail',
    99: 'Thunderstorm with heavy hail'
  };
  return weatherCodes[code] || 'Unknown';
}

// ============ START SERVER ============
// Listen on the specified port (8000 by default, or override with PORT environment variable)
app.listen(PORT, () => {
  console.log(`Weather API server running on http://localhost:${PORT}`);
});
