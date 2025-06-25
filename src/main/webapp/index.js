// Creates the map and overlay
let map;
let mapOverlay = document.getElementById('mapOverlay');

// https://developers.google.com/maps/documentation/javascript/add-google-map 
async function initMap() {
	// Import required libraries
	const { Map } = await google.maps.importLibrary("maps");

	// Create map instance (centered at los angeles in this case)
	map = new Map(document.getElementById('map'), {
		center: { lat: 34.0522, lng: -118.2437 },
		zoom: 8
	});

	// Event click listener to the map
	map.addListener('click', function(mapsMouseEvent) {

		// Get the lat and long from that clicked position
		const lat = mapsMouseEvent.latLng.lat();
		const lng = mapsMouseEvent.latLng.lng();

		// Update the input field with the coordinates that we picked, should only be 6
		// based on pictures
		document.getElementById('latInput').value = lat.toFixed(6);
		document.getElementById('longInput').value = lng.toFixed(6);

		// Hide map
		mapOverlay.style.display = 'none';
	});
}

// Display map
async function showMap() {
	const mapOverlay = document.getElementById('mapOverlay');

	// Lay map to show 
	mapOverlay.style.display = 'block';

	// Based on whether div changes size
	// https://developers.google.com/maps/documentation/javascript/reference/street-view#StreetViewPanorama.resize
	google.maps.event.trigger(map, 'resize');
}

// Initialize the map when the script loads
initMap();


// Listen for changes in localstorage
window.addEventListener('storage', function(e) {
	// Updates the navgation when login or if change to the username
	if (e.key === 'isLoggedIn' || e.key === 'username') {
		updateHeaderNavigation();
	}
});


// Initialize page functionality with the DOM loaded
document.addEventListener('DOMContentLoaded', function() {
	// Update navigation based on status 
	updateHeaderNavigation();

	// Toggle which search for the radio buttons
	toggleSearchType();

	// Initalize the google maps
	initMap();
});


// Fetches the API reuqests 
function fetchWeather() {
	// Get the selected search type (city or coordinates)
	const searchType = document.querySelector('input[name="searchType"]:checked').value;
	const weatherContainer = document.getElementById('weatherIconsContainer');

	// Construct the base URL 
	const contextPath = window.location.pathname.split('/')[1];
	const baseUrl = contextPath ? `/${contextPath}` : '';
	let url = `${baseUrl}/weather-results?`;

	// Handle city-based search
	if (searchType === 'city') {
		const city = document.getElementById('cityInput').value;
		// Trim it if needed
		if (!city.trim()) {
			alert('Please enter a city name');
			return;
		}

		// Create the URL for the city type
		url += 'city=' + encodeURIComponent(city);

		// Loading state
		weatherContainer.style.display = 'grid';
		document.getElementById('cityName').textContent = '--';
	} else {
		// Handle coordinate-based search
		const lat = document.getElementById('latInput').value;
		const lon = document.getElementById('longInput').value;

		// Make sure these are lat and longs both
		if (!lat || !lon) {
			alert('Please enter both latitude and longitude');
			return;
		}

		// Create the URL for the lat/long type
		url += `lat=${encodeURIComponent(lat)}&lon=${encodeURIComponent(lon)}`;

		// Loading state
		weatherContainer.style.display = 'grid';
		document.getElementById('cityName').textContent = '--';
	}

	// Make API request to fetch weather data
	fetch(url)
		.then(response => {
			// Check if response is successful
			if (!response.ok) {
				throw new Error(`HTTP error: ${response.status}`);
			}
			return response.json();
		})
		.then(data => {
			// Verify API response success
			if (!data.success) {
				throw new Error('Failed to fetch weather data');
			}

			// Update weather display for both city and lat/long searches
			updateWeatherDisplay(data);

		})
		.catch(error => {
			// Handle errors if there were any
			alert('Failed to fetch weather data: ' + error.message);
			weatherContainer.style.display = 'none';
		});
}



// Displayall button based on type 
function displayAll() {
	// Get current search type and construct base URL
	const searchType = document.querySelector('input[name="searchType"]:checked').value;

	// Extract context path  from URL for buildling API endpoint
	const contextPath = window.location.pathname.split('/')[1];

	// Check the path
	let baseUrl;
	if (contextPath) {
		baseUrl = '/' + contextPath;
	} else {
		baseUrl = '';
	}

	if (searchType === 'city') {
		// Handle city search
		const cityInput = document.getElementById('cityInput');
		if (!cityInput.value.trim()) {
			alert('Please enter a city name first');
			return;
		}

		// Show the cities table container with loading state
		const citiesTableContainer = document.getElementById('citiesTableContainer');
		const tableBody = document.getElementById('citiesTableBody');
		citiesTableContainer.style.display = 'block';
		tableBody.innerHTML = '<tr><td colspan="2">...</td></tr>';

		// Fetch matching cities from API
		fetch(`${baseUrl}/matching-cities?search=${encodeURIComponent(cityInput.value)}`)
			.then(response => {
				if (!response.ok) {
					throw new Error(`HTTP error: ${response.status}`);
				}
				return response.json();
			})
			.then(data => {
				if (!data.cities || data.cities.length === 0) {
					tableBody.innerHTML = '<tr><td colspan="3">No cities found</td></tr>';
					return;
				}

				// Update table with matching cities
				updateCitiesTable(data.cities);

				// Log search for the cities after displayed
				// Based on video since even the first input after displayall was logged
				logSearch(cityInput.value);
			})
			.catch(error => {
				tableBody.innerHTML = `<tr><td colspan="3">Error: ${error.message}</td></tr>`;
			});

	} else {
		// Handle lat/long search
		const lat = document.getElementById('latInput').value;
		const lon = document.getElementById('longInput').value;

		if (!lat || !lon) {
			alert('Please select a location on the map first');
			return;
		}

		logSearch(`${lat},${lon}`);
		// Show loading state for weather display
		const weatherContainer = document.getElementById('weatherIconsContainer');
		weatherContainer.style.display = 'grid';
		document.getElementById('cityName').textContent = '...';

		// Fetch weather data for lat/long
		const url = `${baseUrl}/weather-results?lat=${encodeURIComponent(lat)}&lon=${encodeURIComponent(lon)}`;

		// Fetch matching lat/long from API
		fetch(url)
			.then(response => {
				if (!response.ok) {
					throw new Error(`HTTP error: ${response.status}`);
				}
				return response.json();
			})
			.then(data => {
				if (!data.success) {
					throw new Error('Failed to fetch');
				}
				updateWeatherDisplay(data);
			})
			.catch(error => {
				alert('Failed to fetch weather data: ' + error.message);
				weatherContainer.style.display = 'none';
			});
	}
}

// Udpates the cities table with the search results, handles the sorting as well 
function updateCitiesTable(cities) {
	// Store cities data for sorting functionality
	window.lastCitiesData = cities;
	const tableBody = document.getElementById('citiesTableBody');
	const sortByContainer = document.querySelector('.sort-by');
	const sortSelect = document.getElementById('sortSelect');

	tableBody.innerHTML = '';

	// Show/hide sort options based on number of results
	if (cities.length <= 1) {
		sortByContainer.style.display = 'none';
	} else {
		sortByContainer.style.display = 'block';
	}

	// Sort cities based on what is needed
	let sortCities = cities;
	if (cities.length > 1) {
		// Handle sort options
		sortCities = Array.from(cities).sort((a, b) => {
			if (sortSelect.value === 'temp-low-desc')
				return b.tempLow - a.tempLow;
			if (sortSelect.value === 'temp-low-asc')
				return a.tempLow - b.tempLow;
			if (sortSelect.value === 'temp-high-desc')
				return b.tempHigh - a.tempHigh;
			if (sortSelect.value === 'temp-high-asc')
				return a.tempHigh - b.tempHigh;
			if (sortSelect.value === 'city-name-a-z')
				return a.name.localeCompare(b.name);
			if (sortSelect.value === 'city-name-z-a')
				return b.name.localeCompare(a.name);
			return 0;
		});
	}

	// Create table rows for each city
	sortCities.forEach(function(city) {
		const row = document.createElement('tr');

		// Populate row with city data
		row.innerHTML = `
            <td>${city.name}</td>
            <td>${Math.round(celsiusToFahrenheit(city.tempLow))}°F</td>
            <td>${Math.round(celsiusToFahrenheit(city.tempHigh))}°F</td>
        `;

		// Add click handler
		row.addEventListener('click', function() {
			// Log the search (images show difference hm)
			logSearch(city.name + ', ' + city.country);

			// Update UI elements
			document.getElementById('cityInput').value = city.name;
			document.getElementById('citiesTableContainer').style.display = 'none';
			document.getElementById('weatherIconsContainer').style.display = 'grid';

			// Create weather data object to display the needed parts
			const weatherData = {
				city: city.name,
				tempMin: city.tempLow,
				tempMax: city.tempHigh,
				temperature: city.tempCurrent,
				windSpeed: city.windSpeed,
				humidity: city.humidity,
				longitude: city.lon,
				latitude: city.lat,
				sunrise: city.sunrise,
				sunset: city.sunset,
				weatherIcon: city.weatherIcon,
				weatherDescription: city.weatherDescription
			};

			// Update displays
			updateWeatherDisplay(weatherData);
			fetchWeather();
		});

		tableBody.appendChild(row);
	});
}

// Add sort functionality
document.getElementById('sortSelect').addEventListener('change', function() {
	if (window.lastCitiesData) {
		updateCitiesTable(window.lastCitiesData);
	}
});


// Converts time to local time 
function formatTimeWithAMPM(timeStr) {
	// Chec to make sure no empty
	if (!timeStr) {
		return '--';
	}

	try {
		// Parse string
		const [hours, minutes] = timeStr.split(':').map(Number);

		// Create date object with the current date
		const date = new Date();

		// Set the hours and minutes based on timezone
		date.setHours(hours);
		date.setMinutes(minutes);

		// Get hours for 12-hour format
		let showHours = date.getHours();
		let ampm;
		let formattedTime;

		// Determine if AM or PM
		if (showHours >= 12) {
			ampm = 'PM';
		} else {
			ampm = 'AM';
		}

		// Convert to 12-hour format
		showHours = showHours % 12;
		if (showHours === 0) {
			showHours = 12;
		}

		formattedTime = showHours + ampm;
		return formattedTime;

	} catch (error) {
		return '--';
	}
}


// Convert from celsuis to fahrentheit
function celsiusToFahrenheit(celsius) {
	return (celsius * 9 / 5) + 32;
}

// Updates the display for the needed parts 
function updateWeatherDisplay(data) {
	// City name
	let cityName = data.city || '--';
	document.getElementById('cityName').textContent = cityName;

	// Temperature Low
	let tempLowText = '--';
	if (data.tempMin) {
		tempLowText = Math.round(celsiusToFahrenheit(data.tempMin));
	}
	document.getElementById('tempLow').textContent = tempLowText;

	// Temperature High
	let tempHighText = '--';
	if (data.tempMax) {
		tempHighText = Math.round(celsiusToFahrenheit(data.tempMax));
	}
	document.getElementById('tempHigh').textContent = tempHighText;

	// Wind Speed
	let windText = '--';
	if (data.windSpeed) {
		// 1 meter per second = 2.2369 miles per hour
		windText = `${Math.round(data.windSpeed * 2.2369)}mi/h`;
	}
	document.getElementById('windSpeed').textContent = windText;

	// Humidity
	let humidityText = '--';
	if (data.humidity) {
		humidityText = `${data.humidity}%`;
	}
	document.getElementById('humidity').textContent = humidityText;

	// Coordinates
	document.getElementById('coordinates').textContent =
		`${Math.round(data.longitude)}/${Math.round(data.latitude)}`;

	// Current Temperature
	let currentTempText = '--';
	if (data.temperature) {
		currentTempText = Math.round(celsiusToFahrenheit(data.temperature));
	}
	document.getElementById('currentTemperature').textContent = currentTempText;

	// Sunrise/Sunset
	const sunriseFormatted = formatTimeWithAMPM(data.sunrise);
	const sunsetFormatted = formatTimeWithAMPM(data.sunset);
	document.getElementById('sunriseSunset').textContent = `${sunriseFormatted}/${sunsetFormatted}`;

	// Weather Icon
	const weatherIcon = document.getElementById('weatherIcon');
	if (weatherIcon && data.weatherIcon) {
		weatherIcon.src = `http://openweathermap.org/img/w/${data.weatherIcon}.png`;
	}

	// Show weather container
	document.getElementById('weatherIconsContainer').style.display = 'grid';
}

// Log user searches to the server
function logSearch(searchQuery) {
	// Check if user is logged in first
	const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
	const userId = localStorage.getItem('username');

	if (!isLoggedIn || !userId) {
		return;
	}

	// Validate search query
	if (!searchQuery || searchQuery.trim() === '') {
		return;
	}

	// Prepare and send log data
	const searchData = {
		user_id: userId,
		search_query: searchQuery
	};

	// Send POST request to log search data 
	fetch('logSearch', {
		// Specify if HTTP, request header, and convert to JSON
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
		},
		body: JSON.stringify(searchData)
	})
		// Handle the initial response
		.then(response => {
			if (!response.ok) {
				throw new Error(`HTTP error: ${response.status}`);
			}
			// Parse response body as JSON
			return response.json();
		})
		// Handle the parsed JSON data
		.then(data => {
			// Success
			console.log('Search logged:', data);
		})
		// No parsed?
		.catch(error => {
			//Error
			console.error('Error logging:', error);
		});
}

// Update header based on if user logged in or not
function updateHeaderNavigation() {
	// Using local
	const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
	const username = localStorage.getItem('username');

	// Get all the navigation elements
	const profileLink = document.getElementById('profileLink');
	const logoutLink = document.getElementById('logoutLink');
	const loginLink = document.getElementById('loginLink');
	const registerLink = document.getElementById('registerLink');

	if (isLoggedIn && username) {
		// Show logged-in links
		profileLink.style.display = '';
		logoutLink.style.display = '';
		// Hide non-logged-in links
		loginLink.style.display = 'none';
		registerLink.style.display = 'none';
	} else {
		// Hide logged-in links
		profileLink.style.display = 'none';
		logoutLink.style.display = 'none';
		// Show non-logged-in links
		loginLink.style.display = '';
		registerLink.style.display = '';
	}
}


// Toggle between city search and coordinate search
function toggleSearchType() {
	// See what type is the selected from the radio button 
	const searchType = document.querySelector('input[name="searchType"]:checked').value;

	// Get references to the needed container elements
	const searchBarContainer = document.getElementById('searchBarContainer');
	const displayAllContainer = document.getElementById('displayAllContainer');
	const weatherContainer = document.getElementById('weatherIconsContainer');
	const citiesTableContainer = document.getElementById('citiesTableContainer');

	// Clear any existing weather display
	if (weatherContainer) {
		weatherContainer.style.display = 'none';
	}

	// Update search interface based on search type
	if (searchType === 'city') {
		searchBarContainer.innerHTML = `
            <input type="text" placeholder="Los Angeles, CA" id="cityInput">
            <button id="searchButton">
                <img src="Assignment 3 Images/magnifying_glass.jpeg" style="width: 20px; height: 20px;">
            </button>
        `;
	} else {
		searchBarContainer.innerHTML = `
            <input type="number" placeholder="Latitude" id="latInput" step="any">
            <input type="number" placeholder="Longitude" id="longInput" step="any">
            <button id="searchButton">
                <img src="Assignment 3 Images/magnifying_glass.jpeg" style="width: 20px; height: 20px;">
            </button>
            <button onclick="showMap()" id="mapButton">
                <img src="Assignment 3 Images/MapIcon.png" style="width: 20px; height: 20px;">
            </button>
        `;
	}

	// Shows the displayall button regardless
	if (displayAllContainer) {
		displayAllContainer.style.display = 'block';
	}

	// Hide cities table
	if (citiesTableContainer) {
		citiesTableContainer.style.display = 'none';
	}

	// Reset weather display values
	const elements = ['cityName', 'tempLow', 'tempHigh', 'windSpeed',
		'humidity', 'coordinates', 'currentTemperature', 'sunriseSunset'];
	elements.forEach(id => {
		const element = document.getElementById(id);
		if (element) {
			element.textContent = '--';
		}
	});
}

// Clears the local storage and goes back to the main page
function logout() {
	// Remove user authethnetication data from local storage
	// https://www.w3schools.com/jsref/prop_win_localstorage.asp
	localStorage.removeItem('isLoggedIn');
	localStorage.removeItem('username');
	updateHeaderNavigation();

	// Back to main page 
	window.location.href = 'index.html';
}

