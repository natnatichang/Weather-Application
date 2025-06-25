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

// Helps with the table portion of find all the cities
// Basically the same thing as the weatherresults except specifically for the table...
@WebServlet("/matching-cities")
public class MatchingCities extends HttpServlet {
	// Unique identifier for servlet serialization
    private static final long serialVersionUID = 1L;
    
    // Handle HTTP GET requests
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
    	// Set response type to JSON and character encoding
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Create the JSON object 
        JsonObject responseJson = new JsonObject();
        
        try {
        	// Get search parameter from request
            String search = request.getParameter("search");
            
            // Validate the search to make sure theres is something or nothing
            if (search == null || search.trim().isEmpty()) {
                throw new ServletException("Search parameter is required");
            }
            
            // Create the OpenWeatherMapApi where there is a limit of 5 since we need more than 1 result
            String apiUrl = "https://api.openweathermap.org/data/2.5/find" +
                          "?q=" + URLEncoder.encode(search, "UTF-8") +
                          "&appid=abf9b861deb6169689cfa0631434d554" +
                          "&units=metric" +
                          "&limit=5";
            
            @SuppressWarnings("deprecation")
            // Create URL object for HTTP
            ///   // https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/net/HttpURLConnection.html
			URL url = new URL(apiUrl);
            
            // Open HTTP connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          
            // Set request method to GET
            conn.setRequestMethod("GET");
            
            
            // Initialize to store JSON response
            String jsonResponse;

            // Get the input stream from the connection
            InputStream inputStream = conn.getInputStream();

            // Create InputStreamReader with UTF-8 encoding
            InputStreamReader streamReader = new InputStreamReader(inputStream, "UTF-8");

            // Create BufferedReader for efficient reading
            BufferedReader reader = new BufferedReader(streamReader);

            // Using try-with-resources to automatically close the reader
            try (reader) {
            	// Create the stirng
                StringBuilder weatherResponseBuilding = new StringBuilder();
                
                // Variable to store each line as it is being read
                String line;
                
                // Read the response line by line until reach end
                while ((line = reader.readLine()) != null) {
                	weatherResponseBuilding.append(line);
                }
                
                // Convert final result to string
                jsonResponse = weatherResponseBuilding.toString();
            }
            
            // Close the connection 
            conn.disconnect();

            // Parse JSON response
            Gson gson = new Gson();
            JsonObject weatherData = gson.fromJson(jsonResponse, JsonObject.class);
            JsonArray citiesArray = new JsonArray();
            
            // Process city list if there is one
            if (weatherData.has("list")) {
            	 JsonArray list = weatherData.getAsJsonArray("list");
            	 
            	 // Get each city object into weather info to add to resulting araray
                 list.forEach(city -> citiesArray.add(cityInfo(city.getAsJsonObject())));
            }
            
            // Create the response that was successful 
            responseJson.addProperty("success", true);
            responseJson.add("cities", citiesArray);

        } catch (Exception e) {
        	// Handle any errors
            responseJson.addProperty("success", false);
            responseJson.addProperty("error", e.getMessage());
        }
        
        // Write the response back
        PrintWriter out = response.getWriter();
        out.print(responseJson.toString());
        out.flush();
    }
    
    // Extract the data needed
    private JsonObject cityInfo(JsonObject cityData) {
        JsonObject main = cityData.getAsJsonObject("main");
        JsonObject cityInfo = new JsonObject();
        
        // Only need name, templow, and temphigh
        cityInfo.add("name", cityData.get("name"));
        cityInfo.add("tempLow", main.get("temp_min"));
        cityInfo.add("tempHigh", main.get("temp_max"));
        return cityInfo;
    }
}
