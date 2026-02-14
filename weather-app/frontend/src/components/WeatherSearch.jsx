/**
 * Weather Search Component
 * 
 * Renders a search form that allows users to:
 * 1. Type a city name in the input field
 * 2. Click the Search button or press Enter to search
 * 3. Triggers the onSearch callback with the city name
 */

import { useState } from 'react';
import './WeatherSearch.css';

function WeatherSearch({ onSearch }) {
  // Track the text user is typing
  const [searchTerm, setSearchTerm] = useState('');

  // ============ HANDLE FORM SUBMISSION ============
  // Called when user clicks Search button or presses Enter
  const handleSubmit = (e) => {
    e.preventDefault();  // Prevent page reload on form submission
    if (searchTerm.trim()) {
      onSearch(searchTerm);  // Call parent component's search function
      setSearchTerm('');     // Clear input field after search
    }
  };

  return (
    <form className="weather-search" onSubmit={handleSubmit}>
      {/* Text input for city search */}
      <input
        type="text"
        placeholder="Search for a city..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}  // Update state as user types
        className="search-input"
      />
      {/* Search button to trigger the search */}
      <button type="submit" className="search-button">
        Search
      </button>
    </form>
  );
}

export default WeatherSearch;
