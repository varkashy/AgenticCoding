# Quick Start Guide - Weather App

## 📋 Prerequisites
- Node.js v16+ installed
- Two terminal windows or tabs available

## 🚀 Quick Start

### Step 1: Start the Backend Server

Open Terminal 1 and run:
```bash
cd weather-app/backend
npm start
```

Expected output:
```
Weather API server running on http://localhost:5000
```

### Step 2: Start the Frontend Development Server

Open Terminal 2 and run:
```bash
cd weather-app/frontend
npm run dev
```

Expected output:
```
VITE v5.0.0  ready in 500 ms

➜  Local:   http://localhost:3000/
```

### Step 3: Open in Browser

Navigate to `http://localhost:3000` in your web browser.

## ✨ What You'll See

1. **Weather App title** with a search bar
2. **Current weather** for your location (requires geolocation permission)
3. **Weather details**:
   - Temperature in Fahrenheit
   - "Feels like" temperature
   - Humidity percentage
   - Wind speed in mph
   - Precipitation amount

## 🔍 Features to Try

### 1. Use Your Current Location
- The app will ask for permission to access your location
- Accept to see weather for your current location

### 2. Search for a City
- Type a city name (e.g., "New York", "Tokyo", "London")
- Click "Search" to see weather for that city

### 3. View Weather Details
- Temperature is displayed prominently
- Additional metrics are shown below in a grid layout
- Weather description matches the conditions

## 🛑 Stopping the Servers

### Stop Backend (Terminal 1)
Press `Ctrl+C`

### Stop Frontend (Terminal 2)
Press `Ctrl+C`

## ⚠️ Troubleshooting

### "Cannot GET /" or blank page
- Ensure both backend AND frontend are running
- Frontend needs backend to fetch weather data

### Geolocation not working
- Check browser permissions (usually top-left of address bar)
- Some browsers require HTTPS for geolocation
- App defaults to San Francisco if geolocation fails

### Weather data not showing
- Check that backend is running (http://localhost:5000)
- Open browser console (F12) to see errors
- Try searching for a specific city

### Port already in use
If port 5000 or 3000 is already in use:
- Backend: Edit `weather-app/backend/server.js` line with PORT
- Frontend: Edit `weather-app/frontend/vite.config.js` server.port

## 📝 Development Mode

### Backend Auto-Reload
While in `weather-app/backend`, run:
```bash
npm run dev
```
This uses Nodemon to auto-restart on file changes.

### Frontend Hot Module Replacement
Vite automatically reloads the frontend when you save changes.

## 🌐 API Endpoints

Test the backend directly:

```bash
# Test health check
curl http://localhost:5000/api/health

# Get weather by coordinates (San Francisco)
curl "http://localhost:5000/api/weather?latitude=37.7749&longitude=-122.4194"

# Get weather by city name
curl "http://localhost:5000/api/weather/city/London"
```

## 📁 Project Layout

```
weather-app/
├── backend/          ← Express server (port 5000)
├── frontend/         ← React app (port 3000)
└── README.md        ← Full documentation
```

## 💡 Next Steps

1. **Try searching** for different cities
2. **Check the console** (F12) to see API responses
3. **Modify styles** in frontend CSS files to customize the look
4. **Extend the backend** to add more weather endpoints
5. **Add features** like weather forecast, alerts, or favorites

## 🎨 UI Customization

Edit CSS files in `weather-app/frontend/src/`:
- `App.css` - Main container styles
- `components/WeatherDisplay.css` - Weather card styling
- `components/WeatherSearch.css` - Search bar styling

## 📦 Production Build

To build for production:
```bash
cd weather-app/frontend
npm run build
```

Output will be in `weather-app/frontend/dist/`

## ❓ Need Help?

Refer to:
- Full README: `weather-app/README.md`
- Backend API docs: See backend/server.js comments
- External docs: https://open-meteo.com/ (weather API)
