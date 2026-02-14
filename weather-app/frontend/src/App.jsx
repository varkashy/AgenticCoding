/**
 * Weather App - Main Component
 * 
 * This is the root component that:
 * 1. Manages the overall state (weather data, location, loading, errors)
 * 2. Fetches weather data from the backend API
 * 3. Handles geolocation for the user's current position
 * 4. Renders the search component and weather display
 */

import { useState, useEffect } from 'react';
import axios from 'axios';
import WeatherDisplay from './components/WeatherDisplay';
import WeatherSearch from './components/WeatherSearch';
import './App.css';

function App() {
  // ============ STATE VARIABLES ============
  const [weather, setWeather] = useState(null);           // Stores current weather data
  const [location, setLocation] = useState(null);         // Stores location info (coordinates, timezone)
  const [loading, setLoading] = useState(false);          // Shows loading spinner while fetching
  const [error, setError] = useState(null);               // Stores error messages to display
  const [temperatureUnit, setTemperatureUnit] = useState('F');  // Toggle between 'F' (Fahrenheit) and 'C' (Celsius)

  // ============ TEMPERATURE CONVERSION HELPER ============
  // Converts Fahrenheit to Celsius or vice versa
  const convertTemp = (fahrenheit, toUnit) => {
    if (toUnit === 'C') {
      return Math.round((fahrenheit - 32) * (5 / 9));
    }
    return Math.round(fahrenheit);  // Already in Fahrenheit
  };

  // ============ FETCH WEATHER BY COORDINATES ============
  // Called when user grants geolocation permission or when app loads
  // Sends latitude/longitude to backend to get weather data
  const fetchWeatherByCoords = async (latitude, longitude) => {
    setLoading(true);
    setError(null);
    try {
      // Call backend API with coordinates
      const response = await axios.get('/api/weather', {
        params: { latitude, longitude }
      });
      setWeather(response.data);            // Store the full weather response
      setLocation(response.data.location);  // Store location info separately
    } catch (err) {
      setError('Failed to fetch weather data. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);  // Always stop loading, error or success
    }
  };

  // ============ FETCH WEATHER BY CITY NAME ============
  // Called when user submits the search form with a city name
  // Backend converts city name to coordinates, then fetches weather
  const fetchWeatherByCity = async (city) => {
    setLoading(true);
    setError(null);
    try {
      // Call backend API with city name
      const response = await axios.get(`/api/weather/city/${city}`);
      setWeather(response.data);
      setLocation(response.data.location);
    } catch (err) {
      setError('City not found. Please try another search.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // ============ GEOLOCATION EFFECT ============
  // Runs once when component mounts ([] dependency array)
  // Requests user's current location and fetches weather for it
  // Falls back to San Francisco if geolocation fails
  //
  // NAVIGATOR OBJECT EXPLAINED:
  // ===========================
  // navigator is a built-in JavaScript object that contains information about the user's browser
  // and device. It has many properties and methods:
  //   - navigator.geolocation: API to access user's GPS location
  //   - navigator.language: Browser language setting
  //   - navigator.userAgent: Information about the browser
  //   - navigator.onLine: Whether device has internet connection
  //
  // navigator.geolocation API:
  // - getCurrentPosition(): Gets the user's CURRENT location (one-time request)
  // - watchPosition(): Continuously monitors location changes
  // - First asks for user permission; user can accept or deny
  // - Returns coordinates object with latitude, longitude, accuracy, altitude, etc.
  // - HTTPS required in production for security reasons
  //
  // In this app:
  // - We use navigator.geolocation.getCurrentPosition() on app load
  // - If user denies permission, we default to San Francisco coordinates
  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          // Success: User granted permission and location was found
          const { latitude, longitude } = position.coords;
          fetchWeatherByCoords(latitude, longitude);
        },
        (error) => {
          // Error: User denied permission or location unavailable
          console.log('Geolocation error:', error);
          setError('Could not get your location. Using default location.');
          // Use default location (San Francisco coordinates)
          fetchWeatherByCoords(37.7749, -122.4194);
        }
      );
    } else {
      setError('Geolocation is not supported by your browser.');
    }
  }, []);

  return (
    <div className="app">
      <div className="container">
        <div className="header">
          <h1>🌤️ Weather App</h1>
          
          {/* Temperature unit toggle button */}
          <button 
            className="temp-toggle" 
            onClick={() => setTemperatureUnit(temperatureUnit === 'F' ? 'C' : 'F')}
            title="Toggle between Fahrenheit and Celsius"
          >
            °{temperatureUnit}
          </button>
        </div>
        
        {/* Search component - lets user enter a city name */}
        <WeatherSearch onSearch={fetchWeatherByCity} />
        
        {/* Show error message if any error occurred */}
        {error && <div className="error-message">{error}</div>}
        
        {/* Show loading spinner while fetching data */}
        {loading && <div className="loading">Loading weather data...</div>}
        
        {/* Show weather details with converted temperature if needed */}
        {weather && <WeatherDisplay weather={weather} temperatureUnit={temperatureUnit} convertTemp={convertTemp} />}
      </div>
    </div>
  );
}

export default App;
