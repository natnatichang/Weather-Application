-- Create the weather application database if it doesn't exist
CREATE DATABASE IF NOT EXISTS weather_app;

-- Use the database
USE weather_app;

-- Create a table called users that stores the user authentication
CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL
);

-- Create search table that helps to store the history of weather searches 
-- Where it has a FK from the users table to help connect the two
CREATE TABLE IF NOT EXISTS searches (
    search_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    search_query VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(username)
);
