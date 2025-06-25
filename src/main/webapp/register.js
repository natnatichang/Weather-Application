// Listens for form submission on the register form
document.getElementById('registerForm').addEventListener('submit', function(e) {
    // Prevent the default form submission
    e.preventDefault();
    
    // Get form input values and error message field
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const errorMessage = document.getElementById('errorMessage');
    
    // Check if passwords match before sending to server
    if (password !== confirmPassword) {
        errorMessage.style.display = 'block';
        errorMessage.textContent = 'The passwords do not match.';
        return;
    }
    
    // Send registration request to server
    fetch('RegisterServlet', {
        // Use POST method for registration
        method: 'POST',
        // Set content type to JSON
        headers: {
            'Content-Type': 'application/json',
        },
        // Convert registration data to JSON
        body: JSON.stringify({
            username: username,
            password: password,
            confirmPassword: confirmPassword
        })
    })
    // Parse JSON response
    .then(response => response.json())
    .then(data => {
 
        // Check if registration was successful
        if (data.status === 'success') {
			// https://www.w3schools.com/jsref/prop_win_localstorage.asp
            // Store user state in localStorage and the username
            localStorage.setItem('username', data.username);
            localStorage.setItem('isLoggedIn', 'true');
            
            // Redirect to home page if was able to register
            window.location.href = 'index.html';
        } else {
            // Display error if username is already taken
            errorMessage.style.display = 'block';
            errorMessage.textContent = 'Username is already taken';
        }
    })
    // Handle network or other errors
    .catch(error => {
        errorMessage.style.display = 'block';
        errorMessage.textContent = 'Error.';
    });
});
