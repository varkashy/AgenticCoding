# Weather App - Setup Verification Checklist

Use this checklist to verify that everything is properly installed and configured.

## ✅ Pre-Requisites
- [ ] Node.js v16+ installed (`node --version`)
- [ ] npm installed (`npm --version`)
- [ ] Two terminal windows available

## ✅ Backend Setup
- [ ] Backend folder exists: `/Users/varunkashyap/AgenticCoding/weather-app/backend`
- [ ] `backend/package.json` exists
- [ ] `backend/server.js` exists
- [ ] `backend/node_modules/` exists (dependencies installed)
- [ ] Can run: `cd weather-app/backend && npm start`
- [ ] Backend starts without errors
- [ ] Backend listens on port 5000

## ✅ Frontend Setup
- [ ] Frontend folder exists: `/Users/varunkashyap/AgenticCoding/weather-app/frontend`
- [ ] `frontend/package.json` exists
- [ ] `frontend/index.html` exists
- [ ] `frontend/vite.config.js` exists
- [ ] `frontend/src/App.jsx` exists
- [ ] `frontend/src/components/` directory exists with:
  - [ ] `WeatherDisplay.jsx`
  - [ ] `WeatherDisplay.css`
  - [ ] `WeatherSearch.jsx`
  - [ ] `WeatherSearch.css`
- [ ] `frontend/node_modules/` exists (dependencies installed)
- [ ] Can run: `cd weather-app/frontend && npm run dev`
- [ ] Frontend starts without errors
- [ ] Frontend listens on port 3000

## ✅ Installed Dependencies

### Backend Dependencies
Run: `cd weather-app/backend && npm list --depth=0`

Should include:
- [ ] express
- [ ] cors
- [ ] axios
- [ ] nodemon (dev dependency)

### Frontend Dependencies
Run: `cd weather-app/frontend && npm list --depth=0`

Should include:
- [ ] react
- [ ] react-dom
- [ ] vite
- [ ] @vitejs/plugin-react
- [ ] axios

## ✅ Project Configuration

### Backend Server (backend/server.js)
- [ ] Express app initialized
- [ ] CORS middleware configured
- [ ] Port 5000 configured
- [ ] Health check endpoint exists
- [ ] Weather by coordinates endpoint exists
- [ ] Weather by city endpoint exists

### Frontend Configuration

#### Vite Config (frontend/vite.config.js)
- [ ] React plugin configured
- [ ] Port 3000 set
- [ ] Proxy to backend configured at /api

#### Environment
- [ ] index.html exists with root div
- [ ] src/main.jsx exists with React entry point
- [ ] src/App.jsx exists with main component

## ✅ Documentation
- [ ] README.md exists in weather-app/
- [ ] QUICKSTART.md exists in weather-app/
- [ ] This checklist exists

## 🧪 Running Tests

### Backend Connectivity
Open Terminal 1:
```bash
cd weather-app/backend
npm start
```
✅ Should see: "Weather API server running on http://localhost:5000"

### Frontend Startup
Open Terminal 2:
```bash
cd weather-app/frontend
npm run dev
```
✅ Should see: "Local: http://localhost:3000/"

### Browser Test
Open `http://localhost:3000`
✅ Should see:
- Weather App title
- Search bar
- Weather display (or loading message)
- Geolocation prompt (first time)

### API Test
From a third terminal:
```bash
# Test backend health
curl http://localhost:5000/api/health

# Test weather by coords (San Francisco)
curl "http://localhost:5000/api/weather?latitude=37.7749&longitude=-122.4194"

# Test weather by city
curl "http://localhost:5000/api/weather/city/London"
```

✅ Should receive JSON responses

## 🔍 Troubleshooting Checks

If something isn't working, verify:

### Backend Won't Start
- [ ] Node.js is installed
- [ ] npm install completed in backend folder
- [ ] Port 5000 is not in use: `lsof -i :5000`
- [ ] server.js file is not corrupted
- [ ] Express is in node_modules

### Frontend Won't Start
- [ ] Node.js is installed
- [ ] npm install completed in frontend folder
- [ ] Port 3000 is not in use: `lsof -i :3000`
- [ ] Vite is in node_modules
- [ ] React is in node_modules

### Weather Data Not Showing
- [ ] Both backend AND frontend are running
- [ ] Browser shows no console errors (F12)
- [ ] Geolocation permission granted or denied gracefully
- [ ] API proxy configured correctly in vite.config.js

### CORS Issues
- [ ] Backend has CORS middleware enabled
- [ ] Frontend is calling /api/* endpoints
- [ ] Backend is running on port 5000

## 📝 Version Information

Capture these for reference:

```bash
# Node version
node --version

# NPM version
npm --version

# Backend dependencies
cd weather-app/backend && npm list --depth=0

# Frontend dependencies
cd weather-app/frontend && npm list --depth=0
```

## ✨ Ready to Go!

Once all checkboxes are checked:
1. ✅ Keep backend running in Terminal 1
2. ✅ Keep frontend running in Terminal 2
3. ✅ Open http://localhost:3000 in browser
4. ✅ Allow geolocation when prompted
5. ✅ Enjoy your weather app!

## 📞 Support

For detailed information, see:
- **QUICKSTART.md** - Running the app
- **README.md** - Full documentation
- **weather-app/backend/server.js** - API implementation
- **weather-app/frontend/src/** - React components

---

**Last Updated**: February 13, 2026
