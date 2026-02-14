# Weather App

A full-stack weather application with a React-based frontend and Express backend that fetches real-time weather data using the free Open-Meteo API.

## Features

- **🌍 Geolocation**: Automatically detects your current location and displays weather
- **🔍 City Search**: Search weather for any city in the world
- **🎨 Beautiful UI**: Modern, responsive design with gradient backgrounds
- **📱 Mobile Friendly**: Works seamlessly on desktop, tablet, and mobile devices
- **⚡ No API Key Required**: Uses Open-Meteo free API
- **📊 Detailed Information**: Shows temperature, humidity, wind speed, precipitation, and more

## Project Structure

```
weather-app/
├── backend/                    # Express.js server
│   ├── server.js              # Main server file with API endpoints
│   ├── package.json           # Backend dependencies
│   └── .gitignore
├── frontend/                   # React + Vite app
│   ├── src/
│   │   ├── components/        # React components
│   │   │   ├── WeatherDisplay.jsx
│   │   │   ├── WeatherDisplay.css
│   │   │   ├── WeatherSearch.jsx
│   │   │   └── WeatherSearch.css
│   │   ├── App.jsx            # Main App component
│   │   ├── App.css            # App styles
│   │   └── main.jsx           # React entry point
│   ├── index.html             # HTML template
│   ├── vite.config.js         # Vite configuration
│   ├── package.json           # Frontend dependencies
│   └── .gitignore
└── README.md
```

## Tech Stack

### Backend
- **Node.js** - JavaScript runtime
- **Express.js** - Web framework
- **Axios** - HTTP client
- **CORS** - Cross-Origin Resource Sharing

### Frontend
- **React 18** - UI library
- **Vite** - Build tool and dev server
- **Axios** - HTTP client

### External API
- **Open-Meteo** - Free weather API (no authentication required)

## Getting Started

### Prerequisites

- Node.js (v16 or higher)
- npm or yarn

### Installation

1. **Install Backend Dependencies**
   ```bash
   cd backend
   npm install
   ```

2. **Install Frontend Dependencies**
   ```bash
   cd ../frontend
   npm install
   ```

## Running the Application

### Start Backend Server

```bash
cd backend
npm start
```

The backend will run on `http://localhost:5000`

For development with auto-reload:
```bash
npm run dev
```

### Start Frontend Development Server

In a new terminal:

```bash
cd frontend
npm run dev
```

The frontend will run on `http://localhost:3000`

### Build for Production

**Frontend:**
```bash
cd frontend
npm run build
```

This creates an optimized production build in the `dist/` folder.

## API Endpoints

### Backend API Routes

#### 1. Health Check
```
GET /api/health
```
Returns the status of the API server.

#### 2. Get Weather by Coordinates
```
GET /api/weather?latitude=<lat>&longitude=<lon>
```
**Parameters:**
- `latitude` (required): Latitude of the location
- `longitude` (required): Longitude of the location

**Response:**
```json
{
  "success": true,
  "location": {
    "latitude": 37.7749,
    "longitude": -122.4194,
    "timezone": "America/Los_Angeles"
  },
  "current_weather": {
    "temperature": 72,
    "apparent_temperature": 70,
    "humidity": 65,
    "precipitation": 0,
    "weather_code": 1,
    "wind_speed": 8,
    "description": "Mainly clear"
  }
}
```

#### 3. Get Weather by City Name
```
GET /api/weather/city/:city
```
**Parameters:**
- `city` (required): City name to search

**Response:** Same as above

## How It Works

1. **Geolocation**: When the app loads, it requests browser permission to access your location
2. **API Call**: The frontend sends your coordinates to the backend
3. **Backend Processing**: The backend queries the Open-Meteo API with your coordinates
4. **Response**: Weather data is returned and displayed beautifully in the frontend
5. **Search**: Users can search for any city to view its weather

## Environment Variables

Currently, the application doesn't require any environment variables, but you can customize:

**Backend**: Modify `PORT` in `server.js`
```javascript
const PORT = process.env.PORT || 5000;
```

**Frontend**: Modify port and API proxy in `vite.config.js`

## Weather Codes

The app uses WMO weather codes to display appropriate weather descriptions and icons:

- `0-1`: Clear/Mainly clear ☀️
- `2`: Partly cloudy ⛅
- `3`: Overcast ☁️
- `45-48`: Foggy 🌫️
- `51-67`: Drizzle/Rain 🌧️
- `71-86`: Snow ❄️
- `95-99`: Thunderstorm ⛈️

## Troubleshooting

### "Geolocation is not supported"
- Use a modern browser (Chrome, Firefox, Safari, Edge)
- Ensure you're using HTTPS or localhost

### "Could not get your location"
- Check your browser's location permissions
- The app will default to San Francisco if geolocation fails

### Backend not responding
- Ensure backend is running on port 5000
- Check that `npm install` completed successfully in the backend folder
- Look for error messages in the terminal

### Frontend not loading
- Clear browser cache
- Check that frontend is running on port 3000
- Ensure backend is running (frontend depends on it)

## Future Enhancements

- [ ] Add weather forecast for upcoming days
- [ ] Add weather alerts
- [ ] Save favorite locations
- [ ] Dark mode toggle
- [ ] Temperature unit toggle (Celsius/Fahrenheit)
- [ ] Add air quality information
- [ ] Add precipitation radar map

## API Attribution

Weather data provided by **Open-Meteo** - https://open-meteo.com/

## License

This project is open source and available for educational purposes.

## Notes

- No API keys are required for this application
- Open-Meteo API is free and open to use
- The app respects rate limits of the Open-Meteo API
