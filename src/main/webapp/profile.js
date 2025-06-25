// Initailize page DOM content
document.addEventListener('DOMContentLoaded', function() {
	// Get the username from the localStorage
    const username = localStorage.getItem('username');
    
	// Redirect to home page if failed
    if (!username) {
        window.location.href = 'login.html';
        return;
    }

	 // Display the username in the UI
    document.getElementById('username').textContent = username;
    
	// Signout
    document.getElementById('signOutBtn').addEventListener('click', function() {
        localStorage.removeItem('username');
        localStorage.removeItem('isLoggedIn');
        window.location.href = 'index.html';  
    });

	// Load user's search history
    fetchSearchHistory();
});

// get user history for current user 
function fetchSearchHistory() {
	// Get username from localStorage for API request
    const username = localStorage.getItem('username');
	
	// Make into API endpoint URL with encoded username
    const url = `getSearchHistory?username=${encodeURIComponent(username)}`;
    
	// Create API request to fetch search history
    fetch(url)
        .then(response => {
			// Check if HTTP response is successful
            if (!response.ok) {
                throw new Error(`HTTP error: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
			// Check if API response indicates success
            if (data.status === 'success') {
                displaySearchHistory(data.history);
            } else {
                throw new Error('No history');
            }
        })
        .catch(error => {	
			// Display error message in the history table
            document.getElementById('historyTableBody').innerHTML = `
                <tr>
                    <td>Error loading search history: ${error.message}</td>
                </tr>
            `;
        });
}

// Displays search history in table format
function displaySearchHistory(history) {
	// References the table body
    const tableBody = document.getElementById('historyTableBody');
    
	// Check if history exists and has items
    if (!history || history.length === 0) {
        tableBody.innerHTML = '<tr><td>No search history available</td></tr>';
        return;
    }
	
	// Create the table rows from the history items
    tableBody.innerHTML = history.map(item => {
        let formattedQuery = item.query;
        
        // Split the query by comma
        const parts = item.query.split(',');
        
        // Check if we have exactly 2 parts and both are valid numbers
        if (parts.length === 2 && !isNaN(parts[0]) && !isNaN(parts[1])) {
            formattedQuery = `lat=${parts[0].trim()}, lon=${parts[1].trim()}`;
        } else {
            // If it's a city query, only show the city name
            formattedQuery = parts[0].trim();
        }
        	
		// Generate table row HTML with the query 
        return `
            <tr data-query="${item.query}" style="cursor: pointer; padding: 10px;">
                <td style="padding: 12px; border-bottom: 1px solid rgba(255,255,255);">
                    ${formattedQuery}
                </td>
            </tr>
        `;
    }).join(''); // Join into a single HTML

    // Add click handlers to all rows
    const rows = tableBody.getElementsByTagName('tr');
    Array.from(rows).forEach(row => {
        row.addEventListener('click', function() {
			// When clicked, fetch weather data for the stored query
            const query = this.getAttribute('data-query');
            fetchWeatherForHistory(query);
        });
    });
}

// Gets the history of the user 
function fetchWeatherForHistory(query) {
	// Extract context path  from URL for buildling API endpoint
    const contextPath = window.location.pathname.split('/')[1];
	
	// Check the path
	let baseUrl;
	if (contextPath) {
	    baseUrl = '/' + contextPath;
	} else {
	    baseUrl = '';
	}
	
	// Initialize the API endpoint URL
    let url = `${baseUrl}/weather-results`;

    // Split and check if both parts are valid numbers
    const parts = query.split(',');
    const isCoordinates = parts.length === 2 && 
                         !isNaN(parts[0]) && 
                         !isNaN(parts[1]);

    if (isCoordinates) {
        // It's coordinates
        const [lat, lon] = parts.map(coord => coord.trim());
        url += `?lat=${encodeURIComponent(lat)}&lon=${encodeURIComponent(lon)}`;
    } else {
        // It's a city name
        url += `?city=${encodeURIComponent(query)}`;
    }
	
	// Get references to DOM elements for weather display
    const weatherContainer = document.getElementById('weatherIconsContainer');
    const weatherDetailsContainer = document.getElementById('weatherDetailsContainer');
    
    // Show loading state
    weatherDetailsContainer.style.display = 'block';
    weatherContainer.style.display = 'grid';
    document.getElementById('cityName').textContent = '...';
	
	// Make API request to fetch weather data
    fetch(url)
        .then(response => {
			// Check if the HTTP response is successful
            if (!response.ok) {
                throw new Error(`HTTP error: ${response.status}`);
            }
			// Parse the JSON response
            return response.json();
        })
        .then(data => {
			// Verify the API response indicates success
            if (!data.success) {
                throw new Error('Failed to fetch');
            }
			// Update the UI with the weather data
            updateWeatherDisplay(data);
        })
		// Handle any errors that occurred 
        .catch(error => {
            alert('Failed to fetch weather data: ' + error.message);
            weatherDetailsContainer.style.display = 'none';
            weatherContainer.style.display = 'none';
        });
}

// Updates the containers with the new stuff
function updateWeatherDisplay(data) {
    // Handle city name display
    let cityName;
    if (data.city) {
        cityName = data.city.split(',')[0];
    } else {
        cityName = '--';
    }
    document.getElementById('cityName').textContent = cityName;

    // Handle temperature low display
    let tempLowText;
    if (data.tempMin) {
        tempLowText = `${Math.round(celsiusToFahrenheit(data.tempMin))}°F`;
    } else {
        tempLowText = '--';
    }
    document.getElementById('tempLow').textContent = tempLowText;

    // Handle temperature high display
    let tempHighText;
    if (data.tempMax) {
        tempHighText = `${Math.round(celsiusToFahrenheit(data.tempMax))}°F`;
    } else {
        tempHighText = '--';
    }
    document.getElementById('tempHigh').textContent = tempHighText;

    // Handle wind speed display
    let windText;
    if (data.windSpeed) {
		// 1 meter per second = 2.2369 miles per hour
        windText = `${Math.round(data.windSpeed * 2.2369)}mi/h`;
    } else {
        windText = '--';
    }
    document.getElementById('windSpeed').textContent = windText;

    // Handle humidity display
    let humidityText;
    if (data.humidity) {
        humidityText = `${data.humidity}%`;
    } else {
        humidityText = '--';
    }
    document.getElementById('humidity').textContent = humidityText;

    // Handle coordinates display
    const coordText = `${Math.round(data.longitude)}/${Math.round(data.latitude)}`;
    document.getElementById('coordinates').textContent = coordText;

    // Handle current temperature display
    let currentTempText;
    if (data.temperature) {
        currentTempText = `${Math.round(celsiusToFahrenheit(data.temperature))}°F`;
    } else {
        currentTempText = '--';
    }
    document.getElementById('currentTemperature').textContent = currentTempText;

    // Handle sunrise and sunset display
    const sunriseFormatted = formatTimeWithAMPM(data.sunrise);
    const sunsetFormatted = formatTimeWithAMPM(data.sunset);
    const sunText = `${sunriseFormatted}/${sunsetFormatted}`;
    document.getElementById('sunriseSunset').textContent = sunText;

    // Show weather container
    document.getElementById('weatherIconsContainer').style.display = 'grid';
}

// Convert from celsuis to fahrentheit
function celsiusToFahrenheit(celsius) {
    return (celsius * 9/5) + 32;
}


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
