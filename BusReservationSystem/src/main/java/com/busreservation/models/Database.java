package com.busreservation.models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static Database instance;
    private Connection connection;
    
    private Database() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:bus_reservation.db");
            createTables();
            insertSampleData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }
    
    private void createTables() {
        String usersTable = "CREATE TABLE IF NOT EXISTS users (user_id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, full_name TEXT NOT NULL, role TEXT DEFAULT 'user', created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        String busesTable = "CREATE TABLE IF NOT EXISTS buses (bus_id INTEGER PRIMARY KEY AUTOINCREMENT, bus_number TEXT UNIQUE NOT NULL, bus_name TEXT NOT NULL, capacity INTEGER NOT NULL, bus_type TEXT CHECK(bus_type IN ('AC', 'Non-AC', 'Sleeper', 'Seater')))";
        String routesTable = "CREATE TABLE IF NOT EXISTS routes (route_id INTEGER PRIMARY KEY AUTOINCREMENT, origin TEXT NOT NULL, destination TEXT NOT NULL, distance_km REAL, base_fare REAL NOT NULL)";
        String schedulesTable = "CREATE TABLE IF NOT EXISTS schedules (schedule_id INTEGER PRIMARY KEY AUTOINCREMENT, bus_id INTEGER, route_id INTEGER, departure_time TIMESTAMP, arrival_time TIMESTAMP, available_seats INTEGER, fare_multiplier REAL DEFAULT 1.0, FOREIGN KEY (bus_id) REFERENCES buses(bus_id), FOREIGN KEY (route_id) REFERENCES routes(route_id))";
        String bookingsTable = "CREATE TABLE IF NOT EXISTS bookings (booking_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, schedule_id INTEGER, seat_numbers TEXT, passenger_name TEXT NOT NULL, passenger_age INTEGER, passenger_gender TEXT, baggage_kg REAL DEFAULT 0, total_fare REAL, booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, status TEXT DEFAULT 'confirmed', FOREIGN KEY (user_id) REFERENCES users(user_id), FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id))";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(usersTable);
            stmt.execute(busesTable);
            stmt.execute(routesTable);
            stmt.execute(schedulesTable);
            stmt.execute(bookingsTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void insertSampleData() {
        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM users");
            if (rs.getInt(1) == 0) {
                String insertUser = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, "admin123");
                    pstmt.setString(3, "System Administrator");
                    pstmt.setString(4, "admin");
                    pstmt.executeUpdate();
                    pstmt.setString(1, "john_doe");
                    pstmt.setString(2, "pass123");
                    pstmt.setString(3, "John Doe");
                    pstmt.setString(4, "user");
                    pstmt.executeUpdate();
                    pstmt.setString(1, "jane_smith");
                    pstmt.setString(2, "pass456");
                    pstmt.setString(3, "Jane Smith");
                    pstmt.setString(4, "user");
                    pstmt.executeUpdate();
                }
                
                String insertBus = "INSERT INTO buses (bus_number, bus_name, capacity, bus_type) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(insertBus)) {
                    pstmt.setString(1, "DDS001");
                    pstmt.setString(2, "Mount Apo Express");
                    pstmt.setInt(3, 45);
                    pstmt.setString(4, "AC");
                    pstmt.executeUpdate();
                    pstmt.setString(1, "DDS002");
                    pstmt.setString(2, "Digos City Liner");
                    pstmt.setInt(3, 50);
                    pstmt.setString(4, "Seater");
                    pstmt.executeUpdate();
                    pstmt.setString(1, "DDS003");
                    pstmt.setString(2, "Kapatagan Voyager");
                    pstmt.setInt(3, 35);
                    pstmt.setString(4, "Sleeper");
                    pstmt.executeUpdate();
                    pstmt.setString(1, "DDS004");
                    pstmt.setString(2, "Sta. Cruz Transport");
                    pstmt.setInt(3, 42);
                    pstmt.setString(4, "AC");
                    pstmt.executeUpdate();
                    pstmt.setString(1, "DDS005");
                    pstmt.setString(2, "Bansalan Rider");
                    pstmt.setInt(3, 38);
                    pstmt.setString(4, "Non-AC");
                    pstmt.executeUpdate();
                }
                
                String insertRoute = "INSERT INTO routes (origin, destination, distance_km, base_fare) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(insertRoute)) {
                    pstmt.setString(1, "Digos City");
                    pstmt.setString(2, "Sta. Cruz");
                    pstmt.setDouble(3, 25);
                    pstmt.setDouble(4, 50.00);
                    pstmt.executeUpdate();
                    pstmt.setString(1, "Sta. Cruz");
                    pstmt.setString(2, "Digos City");
                    pstmt.setDouble(3, 25);
                    pstmt.setDouble(4, 50.00);
                    pstmt.executeUpdate();
                    pstmt.setString(1, "Digos City");
                    pstmt.setString(2, "Bansalan");
                    pstmt.setDouble(3, 35);
                    pstmt.setDouble(4, 70.00);
                    pstmt.executeUpdate();
                    pstmt.setString(1, "Bansalan");
                    pstmt.setString(2, "Digos City");
                    pstmt.setDouble(3, 35);
                    pstmt.setDouble(4, 70.00);
                    pstmt.executeUpdate();
                    pstmt.setString(1, "Digos City");
                    pstmt.setString(2, "Magsaysay");
                    pstmt.setDouble(3, 28);
                    pstmt.setDouble(4, 55.00);
                    pstmt.executeUpdate();
                    pstmt.setString(1, "Magsaysay");
                    pstmt.setString(2, "Digos City");
                    pstmt.setDouble(3, 28);
                    pstmt.setDouble(4, 55.00);
                    pstmt.executeUpdate();
                    pstmt.setString(1, "Digos City");
                    pstmt.setString(2, "Kapatagan");
                    pstmt.setDouble(3, 45);
                    pstmt.setDouble(4, 120.00);
                    pstmt.executeUpdate();
                    pstmt.setString(1, "Kapatagan");
                    pstmt.setString(2, "Digos City");
                    pstmt.setDouble(3, 45);
                    pstmt.setDouble(4, 120.00);
                    pstmt.executeUpdate();
                    pstmt.setString(1, "Digos City");
                    pstmt.setString(2, "Matanao");
                    pstmt.setDouble(3, 20);
                    pstmt.setDouble(4, 40.00);
                    pstmt.executeUpdate();
                    pstmt.setString(1, "Matanao");
                    pstmt.setString(2, "Digos City");
                    pstmt.setDouble(3, 20);
                    pstmt.setDouble(4, 40.00);
                    pstmt.executeUpdate();
                }
                
                String insertSchedule = "INSERT INTO schedules (bus_id, route_id, departure_time, arrival_time, available_seats, fare_multiplier) VALUES (1, 1, ?, ?, 45, 1.0)";
                try (PreparedStatement pstmt = connection.prepareStatement(insertSchedule)) {
                    pstmt.setString(1, LocalDateTime.now().plusDays(1).withHour(6).withMinute(0).withSecond(0).toString());
                    pstmt.setString(2, LocalDateTime.now().plusDays(1).withHour(7).withMinute(30).withSecond(0).toString());
                    pstmt.executeUpdate();
                    pstmt.setString(1, LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0).toString());
                    pstmt.setString(2, LocalDateTime.now().plusDays(1).withHour(15).withMinute(30).withSecond(0).toString());
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public User authenticate(String username, String password) {
        String query = "SELECT user_id, username, full_name, role FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("full_name"), rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean userExists(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean addUser(String username, String password, String fullName, String role) {
        String query = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, fullName);
            pstmt.setString(4, role);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateUser(int userId, String fullName, String role) {
        String query = "UPDATE users SET full_name = ?, role = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, role);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteUser(int userId) {
        String query = "DELETE FROM users WHERE user_id = ? AND role != 'admin'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean resetPassword(int userId, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT user_id, username, full_name, role, created_at FROM users";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                users.add(new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("full_name"), rs.getString("role"), rs.getString("created_at")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    public List<Bus> getAllBuses() {
        List<Bus> buses = new ArrayList<>();
        String query = "SELECT * FROM buses";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                buses.add(new Bus(rs.getInt("bus_id"), rs.getString("bus_number"), rs.getString("bus_name"), rs.getInt("capacity"), rs.getString("bus_type")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return buses;
    }
    
    public boolean addBus(Bus bus) {
        String query = "INSERT INTO buses (bus_number, bus_name, capacity, bus_type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, bus.getBusNumber());
            pstmt.setString(2, bus.getBusName());
            pstmt.setInt(3, bus.getCapacity());
            pstmt.setString(4, bus.getBusType());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public boolean updateBus(Bus bus) {
        String query = "UPDATE buses SET bus_number = ?, bus_name = ?, capacity = ?, bus_type = ? WHERE bus_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, bus.getBusNumber());
            pstmt.setString(2, bus.getBusName());
            pstmt.setInt(3, bus.getCapacity());
            pstmt.setString(4, bus.getBusType());
            pstmt.setInt(5, bus.getBusId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public boolean deleteBus(int busId) {
        String query = "DELETE FROM buses WHERE bus_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, busId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public List<Route> getAllRoutes() {
        List<Route> routes = new ArrayList<>();
        String query = "SELECT * FROM routes";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                routes.add(new Route(rs.getInt("route_id"), rs.getString("origin"), rs.getString("destination"), rs.getDouble("distance_km"), rs.getDouble("base_fare")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return routes;
    }
    
    public boolean addRoute(Route route) {
        String query = "INSERT INTO routes (origin, destination, distance_km, base_fare) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, route.getOrigin());
            pstmt.setString(2, route.getDestination());
            pstmt.setDouble(3, route.getDistanceKm());
            pstmt.setDouble(4, route.getBaseFare());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public boolean updateRoute(Route route) {
        String query = "UPDATE routes SET origin = ?, destination = ?, distance_km = ?, base_fare = ? WHERE route_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, route.getOrigin());
            pstmt.setString(2, route.getDestination());
            pstmt.setDouble(3, route.getDistanceKm());
            pstmt.setDouble(4, route.getBaseFare());
            pstmt.setInt(5, route.getRouteId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public boolean deleteRoute(int routeId) {
        String query = "DELETE FROM routes WHERE route_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, routeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public List<Schedule> searchSchedules(String origin, String destination, String date) {
        List<Schedule> schedules = new ArrayList<>();
        String query = "SELECT s.schedule_id, b.bus_number, b.bus_name, b.bus_type, b.capacity, r.origin, r.destination, r.distance_km, r.base_fare, s.departure_time, s.arrival_time, s.available_seats, (r.base_fare * s.fare_multiplier) as fare FROM schedules s JOIN buses b ON s.bus_id = b.bus_id JOIN routes r ON s.route_id = r.route_id WHERE r.origin = ? AND r.destination = ? AND DATE(s.departure_time) = ? AND s.available_seats > 0";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, origin);
            pstmt.setString(2, destination);
            pstmt.setString(3, date);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                schedules.add(new Schedule(rs.getInt("schedule_id"), rs.getString("bus_number"), rs.getString("bus_name"), rs.getString("bus_type"), rs.getString("origin"), rs.getString("destination"), rs.getString("departure_time"), rs.getString("arrival_time"), rs.getInt("available_seats"), rs.getDouble("fare")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }
    
    public int bookTicket(int userId, int scheduleId, String seatNumbers, String passengerName, int passengerAge, String passengerGender, double baggageKg, double totalFare) {
        String insertBooking = "INSERT INTO bookings (user_id, schedule_id, seat_numbers, passenger_name, passenger_age, passenger_gender, baggage_kg, total_fare) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertBooking, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, scheduleId);
            pstmt.setString(3, seatNumbers);
            pstmt.setString(4, passengerName);
            pstmt.setInt(5, passengerAge);
            pstmt.setString(6, passengerGender);
            pstmt.setDouble(7, baggageKg);
            pstmt.setDouble(8, totalFare);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int bookingId = rs.getInt(1);
                int numSeats = seatNumbers.split(",").length;
                String updateSeats = "UPDATE schedules SET available_seats = available_seats - ? WHERE schedule_id = ?";
                try (PreparedStatement pstmt2 = connection.prepareStatement(updateSeats)) {
                    pstmt2.setInt(1, numSeats);
                    pstmt2.setInt(2, scheduleId);
                    pstmt2.executeUpdate();
                }
                return bookingId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public List<Booking> getUserBookings(int userId) {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.booking_id, b.booking_date, b.seat_numbers, b.passenger_name, b.total_fare, b.status, s.departure_time, r.origin, r.destination, bu.bus_name, bu.bus_number FROM bookings b JOIN schedules s ON b.schedule_id = s.schedule_id JOIN routes r ON s.route_id = r.route_id JOIN buses bu ON s.bus_id = bu.bus_id WHERE b.user_id = ? ORDER BY b.booking_date DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bookings.add(new Booking(rs.getInt("booking_id"), rs.getString("booking_date"), rs.getString("seat_numbers"), rs.getString("passenger_name"), rs.getDouble("total_fare"), rs.getString("status"), rs.getString("departure_time"), rs.getString("origin"), rs.getString("destination"), rs.getString("bus_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }
    
    public boolean cancelBooking(int bookingId, int userId) {
        try {
            String getBooking = "SELECT schedule_id, seat_numbers FROM bookings WHERE booking_id = ? AND user_id = ? AND status = 'confirmed'";
            try (PreparedStatement pstmt = connection.prepareStatement(getBooking)) {
                pstmt.setInt(1, bookingId);
                pstmt.setInt(2, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    int scheduleId = rs.getInt("schedule_id");
                    String seatNumbers = rs.getString("seat_numbers");
                    int numSeats = seatNumbers.split(",").length;
                    String updateSeats = "UPDATE schedules SET available_seats = available_seats + ? WHERE schedule_id = ?";
                    try (PreparedStatement pstmt2 = connection.prepareStatement(updateSeats)) {
                        pstmt2.setInt(1, numSeats);
                        pstmt2.setInt(2, scheduleId);
                        pstmt2.executeUpdate();
                    }
                    String cancelBooking = "UPDATE bookings SET status = 'cancelled' WHERE booking_id = ?";
                    try (PreparedStatement pstmt3 = connection.prepareStatement(cancelBooking)) {
                        pstmt3.setInt(1, bookingId);
                        pstmt3.executeUpdate();
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean restoreBooking(int bookingId) {
        try {
            String getBooking = "SELECT schedule_id, seat_numbers FROM bookings WHERE booking_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(getBooking)) {
                pstmt.setInt(1, bookingId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    int scheduleId = rs.getInt("schedule_id");
                    String seatNumbers = rs.getString("seat_numbers");
                    int numSeats = seatNumbers.split(",").length;
                    
                    String updateSeats = "UPDATE schedules SET available_seats = available_seats - ? WHERE schedule_id = ?";
                    try (PreparedStatement pstmt2 = connection.prepareStatement(updateSeats)) {
                        pstmt2.setInt(1, numSeats);
                        pstmt2.setInt(2, scheduleId);
                        pstmt2.executeUpdate();
                    }
                    
                    String restoreBooking = "UPDATE bookings SET status = 'confirmed' WHERE booking_id = ?";
                    try (PreparedStatement pstmt3 = connection.prepareStatement(restoreBooking)) {
                        pstmt3.setInt(1, bookingId);
                        pstmt3.executeUpdate();
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<Object[]> getBookingSummaryReport() {
        List<Object[]> report = new ArrayList<>();
        String query = "SELECT DATE(b.booking_date) as date, COUNT(*) as bookings, SUM(total_fare) as revenue FROM bookings b WHERE b.status = 'confirmed' GROUP BY DATE(b.booking_date) ORDER BY date DESC LIMIT 30";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                report.add(new Object[]{rs.getString("date"), rs.getInt("bookings"), rs.getDouble("revenue")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }
    
    public List<Object[]> getPopularRoutesReport() {
        List<Object[]> report = new ArrayList<>();
        String query = "SELECT r.origin, r.destination, COUNT(*) as bookings, SUM(b.total_fare) as revenue FROM bookings b JOIN schedules s ON b.schedule_id = s.schedule_id JOIN routes r ON s.route_id = r.route_id WHERE b.status = 'confirmed' GROUP BY r.route_id ORDER BY bookings DESC LIMIT 5";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                report.add(new Object[]{rs.getString("origin"), rs.getString("destination"), rs.getInt("bookings"), rs.getDouble("revenue")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }
    
    public List<Schedule> getAllSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        String query = "SELECT s.schedule_id, b.bus_number, b.bus_name, b.bus_type, r.origin, r.destination, s.departure_time, s.arrival_time, s.available_seats, (r.base_fare * s.fare_multiplier) as fare, b.capacity FROM schedules s JOIN buses b ON s.bus_id = b.bus_id JOIN routes r ON s.route_id = r.route_id WHERE s.available_seats > 0 ORDER BY s.departure_time";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                schedules.add(new Schedule(
                    rs.getInt("schedule_id"),
                    rs.getString("bus_number"),
                    rs.getString("bus_name"),
                    rs.getString("bus_type"),
                    rs.getString("origin"),
                    rs.getString("destination"),
                    rs.getString("departure_time"),
                    rs.getString("arrival_time"),
                    rs.getInt("available_seats"),
                    rs.getDouble("fare")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }
    
    public boolean addSchedule(int busId, int routeId, String departureTime, String arrivalTime, int availableSeats, double fareMultiplier) {
        String query = "INSERT INTO schedules (bus_id, route_id, departure_time, arrival_time, available_seats, fare_multiplier) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, busId);
            pstmt.setInt(2, routeId);
            pstmt.setString(3, departureTime);
            pstmt.setString(4, arrivalTime);
            pstmt.setInt(5, availableSeats);
            pstmt.setDouble(6, fareMultiplier);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}