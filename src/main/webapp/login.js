// Listens for form info from login
document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();
	
	// Get form input values and error message field forwhere it should be
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const errorMessage = document.getElementById('errorMessage');
    
    fetch('login', {  
		// Use POST method for login
        method: 'POST',
		// Set content type to JSON
        headers: {
            'Content-Type': 'application/json',
        },
		// Convert data to JSON
        body: JSON.stringify({
            username: username,
            password: password
        })
    })
	// Parse JSON
    .then(response => response.json())
    .then(data => {
		// Check if successful 
        if (data.status === 'success') {
			// https://www.w3schools.com/jsref/prop_win_localstorage.asp
			// Store state to localstorage
            localStorage.setItem('isLoggedIn', 'true');
			
			// Store username to localstorage
            localStorage.setItem('username', data.username);
			
			// Redirect to home page
            window.location.href = 'index.html';
        } else {
			// If login failed, show error message that matches with response
            const errorMessages = {
                'user_not_exist': 'This user does not exist.',
                'incorrect_password': 'Incorrect password.',
                'invalid_input': 'Please enter both username and password.'
            };
			// Display error message container
            errorMessage.style.display = 'block';
            errorMessage.textContent = errorMessages[data.message];
        }
    })
	// Handle other errors
    .catch(error => {
        errorMessage.style.display = 'block';
        errorMessage.textContent = 'Error, try again.';
    });
});
