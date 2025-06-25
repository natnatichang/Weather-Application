package servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

//Helps with the overall of finding the weather
@WebServlet("/weather-results")
public class WeatherResults extends HttpServlet {
	// Unique identifier for servlet serialization
	private static final long serialVersionUID = 1L;

	// Handle HTTP GET requests
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Set response type to JSON and character encoding
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		JsonObject weatherJson = new JsonObject();

		try {
			// Get request parameters
			String city = request.getParameter("city");
			String latitude = request.getParameter("lat");
			String longitude = request.getParameter("lon");

			String apiUrl;

			// If city name is provided, use the first for the query
			if (city != null && !city.trim().isEmpty()) {
				apiUrl = "https://api.openweathermap.org/data/2.5/weather" + "?q="
						+ URLEncoder.encode(city, "UTF-8") + "&appid=9825627b80e61786ca64cd58fea15a64"
						+ "&units=metric";
			} else if (latitude != null && longitude != null && !latitude.trim().isEmpty() && !longitude.trim().isEmpty()) {
				// If latitude and longitude are provided, use this one instead
				apiUrl = "https://api.openweathermap.org/data/2.5/weather" + "?lat=" + latitude + "&lon=" + longitude
						+ "&appid=9825627b80e61786ca64cd58fea15a64" + "&units=metric";
			} else {
				// Set error in JSON instead
				weatherJson.addProperty("success", false);
				weatherJson.addProperty("error", "Not enough");
				PrintWriter out = response.getWriter();
				out.print(weatherJson.toString());
				out.flush();
				return;
			}

			@SuppressWarnings("deprecation")
			// Create URL object for HTTP
			// https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/net/HttpURLConnection.html
			URL url = new URL(apiUrl);

			// Open HTTP connection <- make that connection
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			// Set request method to GET
			conn.setRequestMethod("GET");

			// Initialize to store JSON response
			String jsonResponse;

			// Get input stream from the connection
			InputStream inputStream = conn.getInputStream();

			// Create InputStreamReader with UTF-8 encoding
			InputStreamReader streamReader = new InputStreamReader(inputStream, "UTF-8");

			// Create buffered reader for efficient reading
			BufferedReader reader = new BufferedReader(streamReader);

			// Using try-with-resources to automatically close the reader
			try (reader) {
				// Create string
				StringBuilder response1 = new StringBuilder();

				// Variable to store each line as it is being read
				String line;

				// Read the response line by line until reach end
				while ((line = reader.readLine()) != null) {
					response1.append(line);
				}

				// Convert final result to string
				jsonResponse = response1.toString();
			}

			// Close the connection
			conn.disconnect();

			// Parse JSON response
	        Gson gson = new Gson();
	        JsonObject weatherData = gson.fromJson(jsonResponse, JsonObject.class);

	        //Add the weather data
	        weatherData(weatherJson, weatherData);
			
		} catch (Exception e) {
			// Handle any errors
			e.printStackTrace();
			weatherJson.addProperty("success", false);
			weatherJson.addProperty("error", e.getMessage());
		}

		// Send the response
		PrintWriter out = response.getWriter();
		out.print(weatherJson.toString());
		out.flush();
	}
	
    // Extract the data needed
	private void weatherData(JsonObject weatherJson, JsonObject weatherData) {
        JsonObject mainData = weatherData.getAsJsonObject("main");
        JsonObject windData = weatherData.getAsJsonObject("wind");
        JsonObject sysData = weatherData.getAsJsonObject("sys");
        JsonObject coordData = weatherData.getAsJsonObject("coord");
        JsonArray weatherArray = weatherData.getAsJsonArray("weather");
        JsonObject weatherInfo = weatherArray.get(0).getAsJsonObject();

        // Need to convert to localtime
        long sunrise = sysData.get("sunrise").getAsLong() * 1000;
        long sunset = sysData.get("sunset").getAsLong() * 1000;

        // Format to time displayed
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        // Build response JSON with all weather information
        weatherJson.addProperty("success", true);
        weatherJson.add("city", weatherData.get("name"));
        weatherJson.add("temperature", mainData.get("temp"));
        weatherJson.add("tempMin", mainData.get("temp_min"));
        weatherJson.add("tempMax", mainData.get("temp_max"));
        weatherJson.add("humidity", mainData.get("humidity"));
        weatherJson.add("windSpeed", windData.get("speed"));
        weatherJson.add("latitude", coordData.get("lat"));
        weatherJson.add("longitude", coordData.get("lon"));
        weatherJson.addProperty("sunrise", timeFormat.format(new Date(sunrise)));
        weatherJson.addProperty("sunset", timeFormat.format(new Date(sunset)));
        weatherJson.add("weatherMain", weatherInfo.get("main"));
        weatherJson.add("weatherDescription", weatherInfo.get("description"));
        weatherJson.add("weatherIcon", weatherInfo.get("icon"));
	}
}
