package servlets;

import util.DatabaseConnector;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;


//Handle register
@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
	// default?
	private static final long serialVersionUID = 1L;
	// Initialize Gson for JSON Parsing
    private final Gson gson = new Gson();
    
    // JSON structure for the request to get the username, password, and confirmed if same
    private static class User {
        String username;
        String password;
        String confirmPassword;
    }
    
    // Handle POST requests to register users
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
     	// Response type as JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Database resources
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JsonObject jsonResponse = new JsonObject();

        try {
            // Parse user input
            User user = gson.fromJson(request.getReader(), User.class);
            
            // Validate input data, otherwise send back a response to day there is an error
            if (user == null || user.username == null || user.password == null || 
                user.confirmPassword == null || !user.password.equals(user.confirmPassword)) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Input wasn't correct");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }
            
            // Get database connection
            conn = DatabaseConnector.getConnection();
            
            // Check if connection was successful
            if (conn == null) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Error");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Check if username exists
            ps = conn.prepareStatement("SELECT username FROM users WHERE username = ?");
            ps.setString(1, user.username);
            rs = ps.executeQuery();
            
            // Make sure the username hasnt been taken or if it has return an error
            if (rs.next()) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "username_taken");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Prepare the statement for new user
            ps = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
            ps.setString(1, user.username);
            ps.setString(2, user.password);
            
            // Check if insertion was successful
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
            	// If it was correct, then can create a new session for the new user
                HttpSession session = request.getSession();
                session.setAttribute("user", user.username);
                
                // Send a success response back
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("username", user.username);
            } else {
            	 // Return error if insertion failed
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Could not register...");
            }
            
        } catch (Exception e) {
        	// Handle all errors
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Errorr");
        } finally {
        	// Close resources
            DatabaseConnector.closeResources(conn, ps, rs);
        }
        
        // Final JSON response
        response.getWriter().write(gson.toJson(jsonResponse));
    }
}


