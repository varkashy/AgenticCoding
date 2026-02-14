/**
 * Frontend Unit Tests
 * 
 * Tests for React components using Jest and React Testing Library
 * Tests the following:
 * 1. Component rendering
 * 2. User interactions (search, button clicks)
 * 3. State management
 * 4. Temperature conversion logic
 * 5. Error handling
 */

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import axios from 'axios';
import App from '../App';
import WeatherSearch from '../components/WeatherSearch';
import WeatherDisplay from '../components/WeatherDisplay';

// Mock axios for API calls
jest.mock('axios');

// Mock navigator.geolocation
global.navigator.geolocation = {
  getCurrentPosition: jest.fn()
};

// ============ APP COMPONENT TESTS ============

describe('App Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('Should render the app title', () => {
    navigator.geolocation.getCurrentPosition.mockImplementation((success) => {
      success({ coords: { latitude: 0, longitude: 0 } });
    });

    render(<App />);
    const title = screen.getByText(/Weather App/i);
    expect(title).toBeInTheDocument();
  });

  test('Should display temperature toggle button', () => {
    navigator.geolocation.getCurrentPosition.mockImplementation((success) => {
      success({ coords: { latitude: 0, longitude: 0 } });
    });

    render(<App />);
    const toggleButton = screen.getByTitle(/Toggle between Fahrenheit and Celsius/i);
    expect(toggleButton).toBeInTheDocument();
  });

  test('Should toggle temperature unit when button is clicked', async () => {
    navigator.geolocation.getCurrentPosition.mockImplementation((success) => {
      success({ coords: { latitude: 0, longitude: 0 } });
    });

    axios.get.mockResolvedValue({
      data: {
        success: true,
        location: { latitude: 0, longitude: 0, timezone: 'UTC' },
        current_weather: {
          temperature: 72,
          apparent_temperature: 70,
          humidity: 65,
          precipitation: 0,
          weather_code: 1,
          wind_speed: 5,
          description: 'Clear'
        }
      }
    });

    render(<App />);
    
    await waitFor(() => {
      expect(screen.getByText('°F')).toBeInTheDocument();
    });

    const toggleButton = screen.getByTitle(/Toggle between Fahrenheit and Celsius/i);
    fireEvent.click(toggleButton);

    await waitFor(() => {
      expect(screen.getByText('°C')).toBeInTheDocument();
    });
  });

  test('Should render WeatherSearch component', () => {
    navigator.geolocation.getCurrentPosition.mockImplementation((success) => {
      success({ coords: { latitude: 0, longitude: 0 } });
    });

    render(<App />);
    const searchInput = screen.getByPlaceholderText(/Search for a city/i);
    expect(searchInput).toBeInTheDocument();
  });

  test('Should handle geolocation permission denial gracefully', async () => {
    navigator.geolocation.getCurrentPosition.mockImplementation((success, error) => {
      error({ code: 1, message: 'User denied permission' });
    });

    axios.get.mockResolvedValue({
      data: {
        success: true,
        location: { latitude: 37.7749, longitude: -122.4194, timezone: 'PST' },
        current_weather: {
          temperature: 72,
          apparent_temperature: 70,
          humidity: 65,
          precipitation: 0,
          weather_code: 1,
          wind_speed: 5,
          description: 'Clear'
        }
      }
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText(/Could not get your location/i)).toBeInTheDocument();
    });
  });
});

// ============ WEATHER SEARCH COMPONENT TESTS ============

describe('WeatherSearch Component', () => {
  test('Should render search input field', () => {
    const mockOnSearch = jest.fn();
    render(<WeatherSearch onSearch={mockOnSearch} />);
    
    const input = screen.getByPlaceholderText(/Search for a city/i);
    expect(input).toBeInTheDocument();
  });

  test('Should render search button', () => {
    const mockOnSearch = jest.fn();
    render(<WeatherSearch onSearch={mockOnSearch} />);
    
    const button = screen.getByRole('button', { name: /Search/i });
    expect(button).toBeInTheDocument();
  });

  test('Should call onSearch when form is submitted', async () => {
    const mockOnSearch = jest.fn();
    render(<WeatherSearch onSearch={mockOnSearch} />);
    
    const input = screen.getByPlaceholderText(/Search for a city/i);
    const button = screen.getByRole('button', { name: /Search/i });

    await userEvent.type(input, 'London');
    fireEvent.click(button);

    expect(mockOnSearch).toHaveBeenCalledWith('London');
  });

  test('Should clear input field after search', async () => {
    const mockOnSearch = jest.fn();
    render(<WeatherSearch onSearch={mockOnSearch} />);
    
    const input = screen.getByPlaceholderText(/Search for a city/i);
    const button = screen.getByRole('button', { name: /Search/i });

    await userEvent.type(input, 'Paris');
    fireEvent.click(button);

    await waitFor(() => {
      expect(input.value).toBe('');
    });
  });

  test('Should not call onSearch for empty input', async () => {
    const mockOnSearch = jest.fn();
    render(<WeatherSearch onSearch={mockOnSearch} />);
    
    const button = screen.getByRole('button', { name: /Search/i });
    fireEvent.click(button);

    expect(mockOnSearch).not.toHaveBeenCalled();
  });
});

// ============ WEATHER DISPLAY COMPONENT TESTS ============

describe('WeatherDisplay Component', () => {
  const mockWeatherData = {
    location: {
      name: 'San Francisco',
      latitude: 37.7749,
      longitude: -122.4194,
      timezone: 'America/Los_Angeles'
    },
    current_weather: {
      temperature: 72,
      apparent_temperature: 70,
      humidity: 65,
      precipitation: 0,
      weather_code: 1,
      wind_speed: 5,
      description: 'Mainly clear'
    }
  };

  test('Should not render when weather data is null', () => {
    const { container } = render(
      <WeatherDisplay weather={null} temperatureUnit="F" convertTemp={() => {}} />
    );
    expect(container.firstChild).toBeNull();
  });

  test('Should display location name', () => {
    render(
      <WeatherDisplay 
        weather={mockWeatherData} 
        temperatureUnit="F" 
        convertTemp={(temp) => Math.round(temp)}
      />
    );
    
    expect(screen.getByText(/San Francisco/i)).toBeInTheDocument();
  });

  test('Should display timezone information', () => {
    render(
      <WeatherDisplay 
        weather={mockWeatherData} 
        temperatureUnit="F" 
        convertTemp={(temp) => Math.round(temp)}
      />
    );
    
    expect(screen.getByText(/America\/Los_Angeles/i)).toBeInTheDocument();
  });

  test('Should display temperature in Fahrenheit', () => {
    const convertTemp = (temp, unit) => {
      if (unit === 'C') return Math.round((temp - 32) * (5 / 9));
      return Math.round(temp);
    };

    render(
      <WeatherDisplay 
        weather={mockWeatherData} 
        temperatureUnit="F" 
        convertTemp={convertTemp}
      />
    );
    
    expect(screen.getByText(/72°F/)).toBeInTheDocument();
  });

  test('Should display temperature in Celsius', () => {
    const convertTemp = (temp, unit) => {
      if (unit === 'C') return Math.round((temp - 32) * (5 / 9));
      return Math.round(temp);
    };

    render(
      <WeatherDisplay 
        weather={mockWeatherData} 
        temperatureUnit="C" 
        convertTemp={convertTemp}
      />
    );
    
    // 72°F = 22°C
    expect(screen.getByText(/22°C/)).toBeInTheDocument();
  });

  test('Should display weather description', () => {
    render(
      <WeatherDisplay 
        weather={mockWeatherData} 
        temperatureUnit="F" 
        convertTemp={(temp) => Math.round(temp)}
      />
    );
    
    expect(screen.getByText(/Mainly clear/i)).toBeInTheDocument();
  });

  test('Should display all weather metrics', () => {
    render(
      <WeatherDisplay 
        weather={mockWeatherData} 
        temperatureUnit="F" 
        convertTemp={(temp) => Math.round(temp)}
      />
    );
    
    expect(screen.getByText(/Feels Like:/)).toBeInTheDocument();
    expect(screen.getByText(/Humidity:/)).toBeInTheDocument();
    expect(screen.getByText(/Wind Speed:/)).toBeInTheDocument();
    expect(screen.getByText(/Precipitation:/)).toBeInTheDocument();
  });

  test('Should display feels-like temperature in Fahrenheit', () => {
    render(
      <WeatherDisplay 
        weather={mockWeatherData} 
        temperatureUnit="F" 
        convertTemp={(temp) => Math.round(temp)}
      />
    );
    
    expect(screen.getByText(/70°F/)).toBeInTheDocument();
  });

  test('Should display humidity percentage', () => {
    render(
      <WeatherDisplay 
        weather={mockWeatherData} 
        temperatureUnit="F" 
        convertTemp={(temp) => Math.round(temp)}
      />
    );
    
    expect(screen.getByText(/65%/)).toBeInTheDocument();
  });

  test('Should display wind speed', () => {
    render(
      <WeatherDisplay 
        weather={mockWeatherData} 
        temperatureUnit="F" 
        convertTemp={(temp) => Math.round(temp)}
      />
    );
    
    expect(screen.getByText(/5 mph/)).toBeInTheDocument();
  });
});

// ============ TEMPERATURE CONVERSION TESTS ============

describe('Temperature Conversion', () => {
  test('Should convert 32°F to 0°C', () => {
    const convertTemp = (temp, unit) => {
      if (unit === 'C') return Math.round((temp - 32) * (5 / 9));
      return Math.round(temp);
    };

    expect(convertTemp(32, 'C')).toBe(0);
  });

  test('Should convert 72°F to 22°C', () => {
    const convertTemp = (temp, unit) => {
      if (unit === 'C') return Math.round((temp - 32) * (5 / 9));
      return Math.round(temp);
    };

    expect(convertTemp(72, 'C')).toBe(22);
  });

  test('Should keep Fahrenheit when unit is F', () => {
    const convertTemp = (temp, unit) => {
      if (unit === 'C') return Math.round((temp - 32) * (5 / 9));
      return Math.round(temp);
    };

    expect(convertTemp(72, 'F')).toBe(72);
  });

  test('Should handle negative temperatures', () => {
    const convertTemp = (temp, unit) => {
      if (unit === 'C') return Math.round((temp - 32) * (5 / 9));
      return Math.round(temp);
    };

    // -4°F = -20°C
    expect(convertTemp(-4, 'C')).toBe(-20);
  });
});
