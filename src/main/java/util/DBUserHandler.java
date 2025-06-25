package rideshare_userhandling;
// import ____.Ride; based on package requirements

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// This will probably be inheriting from DatabaseConnector 

// Add extends DatabaseConnector to this
public class DBUserHandler extends DBConnector {

	public static class User {

		private int userID;
		private String username;
		private String firstName;
		private String lastName;
		private String password;
		private Integer age;
		private String gender;
		private Double budgetMin;
		private Double budgetMax;
		private String prefPlatform;

		public User(int userID, String username, String firstName, String lastName, String password, Integer age,
				String gender, Double budgetMin, Double budgetMax, String prefPlatform) {
			this.userID = userID;
			this.username = username;
			this.firstName = firstName;
			this.lastName = lastName;
			this.password = password;
			this.age = age;
			this.gender = gender;
			this.budgetMin = budgetMin;
			this.budgetMax = budgetMax;
			this.prefPlatform = prefPlatform;
		}

		public int getUserId() {
			return userID;
		}

		public String getUsername() {
			return username;
		}

		public String getFirstName() {
			return firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public String getPassword() {
			return password;
		}

		public Integer getAge() {
			return age;
		}

		public String getGender() {
			return gender;
		}

		public Double getBudgetMin() {
			return budgetMin;
		}

		public Double getBudgetMax() {
			return budgetMax;
		}

		public String getPrefPlatform() {
			return prefPlatform;
		}
	}

	public DBUserHandler() {
		super();
	}

	// Could be used for ride request, match rider + drivers by username
	// Verifying potentially
	public int getUserID(String username) throws SQLException {
		PreparedStatement pst = null;
		ResultSet rs = null;

		try {
			pst = conn.prepareStatement("SELECT user_id FROM Users WHERE username = ?");
			pst.setString(1, username);
			rs = pst.executeQuery();

			if (rs.next()) {
				return rs.getInt("user_id");
			}
		} finally {
			cleanUp(rs);
			cleanUp(pst);
		}
		return -1;
	}

	// username given, get back the data based on that
	public User getSingleUserByUsername(String username) throws SQLException {
		PreparedStatement pst = null;
		ResultSet rs = null;

		try {
			pst = conn.prepareStatement("SELECT * FROM Users WHERE username = ?");
			pst.setString(1, username);
			rs = pst.executeQuery();

			if (rs.next()) {
				// gets all info that is needed
				int userId = rs.getInt("user_id");
				String uname = rs.getString("username");
				String firstName = rs.getString("first_name");
				String lastName = rs.getString("last_name");
				String password = rs.getString("password");

				// Potentially optional
				Integer age = null;
				if (rs.getObject("age") != null) {
					age = rs.getInt("age");
				}

				String gender = null;
				if (rs.getObject("gender") != null) {
					gender = rs.getString("gender");
				}

				Double budgetMin = null;
				if (rs.getObject("budget_min") != null) {
					budgetMin = rs.getDouble("budget_min");
				}

				Double budgetMax = null;
				if (rs.getObject("budget_max") != null) {
					budgetMax = rs.getDouble("budget_max");
				}

				String prefPlatform = null;
				if (rs.getObject("pref_platform") != null) {
					prefPlatform = rs.getString("pref_platform");
				}

				// Return info back
				return new User(userId, uname, firstName, lastName, password, age, gender, budgetMin, budgetMax,
						prefPlatform);
			}
		} finally {
			cleanUp(rs);
			cleanUp(pst);
		}
		return null;
	}

	// adds user information + determine if added successfully
	public boolean addUser(String username, String firstName, String lastName, String password, Integer age,
			String gender, Double budgetMin, Double budgetMax, String prefPlatform) throws SQLException {
		PreparedStatement pst = null;

		try {
			pst = conn.prepareStatement(
					"INSERT INTO Users (username, first_name, last_name, password, age, gender, budget_min, budget_max, pref_platform) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

			// Assuming this is required
			pst.setString(1, username);
			pst.setString(2, firstName);
			pst.setString(3, lastName);
			pst.setString(4, password);

			// Below can be optional
			pst.setInt(5, age);
			pst.setString(6, gender);
			pst.setDouble(7, budgetMin);
			pst.setDouble(8, budgetMax);
			pst.setString(9, prefPlatform);

			// true or false boolean
			return pst.executeUpdate() > 0;

		} finally {
			cleanUp(pst);
		}
	}
	
	// Updates the user info 
	// username, firstname, lastname, password should all be required
	public boolean updateUser(int userId, String username, String firstName, String lastName, String password,
			Integer age, String gender, Double budgetMin, Double budgetMax, String prefPlatform) throws SQLException {
		PreparedStatement pst = null;

		try {
			pst = conn.prepareStatement("UPDATE Users SET username = ?, first_name = ?, last_name = ?, password = ?, "
					+ "age = ?, gender = ?, budget_min = ?, budget_max = ?, pref_platform = ? WHERE user_id = ?");
			
			// Required fields
	        pst.setString(1, username);
	        pst.setString(2, firstName);
	        pst.setString(3, lastName);
	        pst.setString(4, password);
	        
	        // Will still be null if parameter is null
	        pst.setObject(5, age);
	        pst.setObject(6, gender);
	        pst.setObject(7, budgetMin);
	        pst.setObject(8, budgetMax);
	        pst.setObject(9, prefPlatform);
	        pst.setInt(10, userId);

	        return pst.executeUpdate() > 0;
	    } finally {
	        cleanUp(pst);
	    }
	}


	// not sure if needed here
	public boolean addRating(int ratingUserId, int ratedUserId, int rating, String comment) throws SQLException {
		PreparedStatement pst = null;

		try {
			pst = conn.prepareStatement(
					"INSERT INTO Ratings (rating_user_id, rated_user_id, rating, comment) VALUES (?, ?, ?, ?)");

			pst.setInt(1, ratingUserId);
			pst.setInt(2, ratedUserId);
			pst.setInt(3, rating);
			pst.setString(4, comment);

			return pst.executeUpdate() > 0;
		} finally {
			cleanUp(pst);
		}
	}

	// Based on the ride class
	public List<Ride> getRidesByUsername(String username) throws SQLException {
		PreparedStatement pst = null;
		ResultSet rs = null;
		List<Ride> rides = new ArrayList<>();

		try {
			// Take the ride table and user table and join the info together
			// Direct connection between the users and rides table (many to many)
			// Newest should be at the top
			pst = conn.prepareStatement("SELECT r.ride_id, r.pickup_latitude, r.pickup_longitude, "
					+ "r.terminal, r.start_time FROM Rides AS r JOIN UserRides AS ur ON r.ride_id = ur.ride_id "
					+ "JOIN Users AS u ON ur.user_id = u.user_id WHERE u.username = ? "
					+ "ORDER BY r.start_time DESC");

			pst.setString(1, username);
			rs = pst.executeQuery();

			while (rs.next()) {
				Ride ride = new Ride(rs.getDouble("pickup_latitude"), rs.getDouble("pickup_longitude"),
						rs.getString("terminal"), rs.getString("start_time"));
				rides.add(ride);
			}
		} finally {
			cleanUp(rs);
			cleanUp(pst);
		}
		return rides;
	}
	
	// Testing development 
	public static void main(String[] args) throws SQLException {
		DBUserHandler handler = new DBUserHandler();
		
		String test = "testuser";
		
		boolean userMade = handler.addUser(test, "first", "last", "123", 12, null, null, null, "?");
	}
}
