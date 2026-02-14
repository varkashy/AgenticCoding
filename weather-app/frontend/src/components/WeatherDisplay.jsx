/**
 * Weather Display Component
 * 
 * Shows the current weather details for a location:
 * - Location name, coordinates, and timezone
 * - Current temperature and weather description
 * - Weather icon based on weather conditions
 * - Additional metrics: feels-like temp, humidity, wind speed, precipitation
 */

import './WeatherDisplay.css';

function WeatherDisplay({ weather }) {
  // Don't render if no weather data is available
  if (!weather) return null;

  // Extract location and weather data from props
  const {
    location,
    current_weather
  } = weather;

  // ============ GET WEATHER ICON ============
  // Converts WMO weather code to an emoji icon for visual representation
  // Uses weather code ranges to determine appropriate emoji
  const getWeatherIcon = (code) => {
    if (code === 0 || code === 1) return '☀️';            // Clear sky
    if (code === 2) return '⛅';                           // Partly cloudy
    if (code === 3) return '☁️';                           // Overcast
    if (code === 45 || code === 48) return '🌫️';          // Foggy
    if (code >= 51 && code <= 67) return '🌧️';            // Drizzle or rain
    if (code >= 71 && code <= 86) return '❄️';            // Snow
    if (code >= 95 && code <= 99) return '⛈️';            // Thunderstorm
    return '🌤️';                                          // Default
  };

  return (
    <div className="weather-display">
      {/* ============ LOCATION SECTION ============ */}
      <div className="location">
        {/* Show city name, state, country if available; otherwise show coordinates */}
        <h2>📍 {location.name || `${location.latitude.toFixed(2)}°, ${location.longitude.toFixed(2)}°`}</h2>
        {/* Display the timezone for this location */}
        <p className="timezone">Timezone: {location.timezone}</p>
      </div>

      {/* ============ WEATHER CARD ============ */}
      <div className="weather-card">
        {/* Large emoji icon representing current weather */}
        <div className="weather-icon">
          {getWeatherIcon(current_weather.weather_code)}
        </div>
        
        <div className="weather-info">
          {/* ============ MAIN TEMPERATURE DISPLAY ============ */}
          <div className="temp-section">
            {/* Large font temperature in Fahrenheit */}
            <div className="temperature">
              {Math.round(current_weather.temperature)}°F
            </div>
            {/* Human-readable weather description */}
            <div className="description">
              {current_weather.description}
            </div>
          </div>

          {/* ============ DETAILS GRID ============ */}
          {/* 2x2 grid showing additional weather metrics */}
          <div className="details-grid">
            {/* Apparent temperature: what it feels like due to wind chill/humidity */}
            <div className="detail-item">
              <span className="label">Feels Like:</span>
              <span className="value">{Math.round(current_weather.apparent_temperature)}°F</span>
            </div>
            
            {/* Humidity percentage: amount of moisture in air */}
            <div className="detail-item">
              <span className="label">Humidity:</span>
              <span className="value">{current_weather.humidity}%</span>
            </div>
            
            {/* Wind speed in miles per hour */}
            <div className="detail-item">
              <span className="label">Wind Speed:</span>
              <span className="value">{Math.round(current_weather.wind_speed)} mph</span>
            </div>
            
            {/* Precipitation: amount of rain/snow in millimeters */}
            <div className="detail-item">
              <span className="label">Precipitation:</span>
              <span className="value">{current_weather.precipitation} mm</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default WeatherDisplay;
