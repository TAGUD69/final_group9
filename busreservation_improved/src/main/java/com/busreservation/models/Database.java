package com.busreservation.models;

import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static Database instance;
    private Connection connection;
    private static final DateTimeFormatter DB_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Database() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:bus_reservation.db");
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            createTables();
            insertSampleData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized Database getInstance() {
        if (instance == null) instance = new Database();
        return instance;
    }

    // ── Password hashing ──────────────────────────────────────────────────────
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return password;
        }
    }

    // ── Table creation ────────────────────────────────────────────────────────
    private void createTables() {
        String[] ddl = {
            "CREATE TABLE IF NOT EXISTS users (user_id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, full_name TEXT NOT NULL, role TEXT DEFAULT 'user', created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS buses (bus_id INTEGER PRIMARY KEY AUTOINCREMENT, bus_number TEXT UNIQUE NOT NULL, bus_name TEXT NOT NULL, capacity INTEGER NOT NULL, bus_type TEXT CHECK(bus_type IN ('AC','Non-AC','Sleeper','Seater')))",
            "CREATE TABLE IF NOT EXISTS routes (route_id INTEGER PRIMARY KEY AUTOINCREMENT, origin TEXT NOT NULL, destination TEXT NOT NULL, distance_km REAL, base_fare REAL NOT NULL)",
            "CREATE TABLE IF NOT EXISTS schedules (schedule_id INTEGER PRIMARY KEY AUTOINCREMENT, bus_id INTEGER, route_id INTEGER, departure_time TEXT, arrival_time TEXT, available_seats INTEGER, fare_multiplier REAL DEFAULT 1.0, FOREIGN KEY (bus_id) REFERENCES buses(bus_id), FOREIGN KEY (route_id) REFERENCES routes(route_id))",
            "CREATE TABLE IF NOT EXISTS bookings (booking_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, schedule_id INTEGER, seat_numbers TEXT, passenger_name TEXT NOT NULL, passenger_age INTEGER DEFAULT 0, passenger_gender TEXT DEFAULT 'Not Specified', baggage_kg REAL DEFAULT 0, total_fare REAL, booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, status TEXT DEFAULT 'confirmed', FOREIGN KEY (user_id) REFERENCES users(user_id), FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id))"
        };
        try (Statement stmt = connection.createStatement()) {
            for (String sql : ddl) stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Sample data ───────────────────────────────────────────────────────────
    private void insertSampleData() {
        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM users");
            if (rs.getInt(1) > 0) return;

            // Users — passwords hashed
            String[][] users = {
                {"admin","admin123","System Administrator","admin"},
                {"john_doe","pass123","John Doe","user"},
                {"jane_smith","pass456","Jane Smith","user"}
            };
            try (PreparedStatement p = connection.prepareStatement(
                    "INSERT INTO users (username,password,full_name,role) VALUES (?,?,?,?)")) {
                for (String[] u : users) {
                    p.setString(1, u[0]); p.setString(2, hashPassword(u[1]));
                    p.setString(3, u[2]); p.setString(4, u[3]);
                    p.executeUpdate();
                }
            }

            // Buses
            Object[][] buses = {
                {"DDS001","Mount Apo Express",45,"AC"},
                {"DDS002","Digos City Liner",50,"Seater"},
                {"DDS003","Kapatagan Voyager",35,"Sleeper"},
                {"DDS004","Sta. Cruz Transport",42,"AC"},
                {"DDS005","Bansalan Rider",38,"Non-AC"}
            };
            try (PreparedStatement p = connection.prepareStatement(
                    "INSERT INTO buses (bus_number,bus_name,capacity,bus_type) VALUES (?,?,?,?)")) {
                for (Object[] b : buses) {
                    p.setString(1,(String)b[0]); p.setString(2,(String)b[1]);
                    p.setInt(3,(Integer)b[2]);    p.setString(4,(String)b[3]);
                    p.executeUpdate();
                }
            }

            // Routes
            Object[][] routes = {
                {"Digos City","Sta. Cruz",25.0,50.0},
                {"Sta. Cruz","Digos City",25.0,50.0},
                {"Digos City","Bansalan",35.0,70.0},
                {"Bansalan","Digos City",35.0,70.0},
                {"Digos City","Magsaysay",28.0,55.0},
                {"Magsaysay","Digos City",28.0,55.0},
                {"Digos City","Kapatagan",45.0,120.0},
                {"Kapatagan","Digos City",45.0,120.0},
                {"Digos City","Matanao",20.0,40.0},
                {"Matanao","Digos City",20.0,40.0}
            };
            try (PreparedStatement p = connection.prepareStatement(
                    "INSERT INTO routes (origin,destination,distance_km,base_fare) VALUES (?,?,?,?)")) {
                for (Object[] r : routes) {
                    p.setString(1,(String)r[0]); p.setString(2,(String)r[1]);
                    p.setDouble(3,(Double)r[2]);  p.setDouble(4,(Double)r[3]);
                    p.executeUpdate();
                }
            }

            // Schedules — use space-separated format for SQLite DATE() compatibility
            String dep1 = LocalDateTime.now().plusDays(1).withHour(6).withMinute(0).withSecond(0).format(DB_FORMAT);
            String arr1 = LocalDateTime.now().plusDays(1).withHour(7).withMinute(30).withSecond(0).format(DB_FORMAT);
            String dep2 = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0).format(DB_FORMAT);
            String arr2 = LocalDateTime.now().plusDays(1).withHour(15).withMinute(30).withSecond(0).format(DB_FORMAT);
            try (PreparedStatement p = connection.prepareStatement(
                    "INSERT INTO schedules (bus_id,route_id,departure_time,arrival_time,available_seats,fare_multiplier) VALUES (1,1,?,?,45,1.0)")) {
                p.setString(1,dep1); p.setString(2,arr1); p.executeUpdate();
                p.setString(1,dep2); p.setString(2,arr2); p.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── User methods ──────────────────────────────────────────────────────────
    public User authenticate(String username, String password) {
        String sql = "SELECT user_id,username,full_name,role FROM users WHERE username=? AND password=?";
        try (PreparedStatement p = connection.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, hashPassword(password));
            ResultSet rs = p.executeQuery();
            if (rs.next()) return new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("full_name"), rs.getString("role"));
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean userExists(String username) {
        try (PreparedStatement p = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE username=?")) {
            p.setString(1, username);
            return p.executeQuery().getInt(1) > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean addUser(String username, String password, String fullName, String role) {
        try (PreparedStatement p = connection.prepareStatement(
                "INSERT INTO users (username,password,full_name,role) VALUES (?,?,?,?)")) {
            p.setString(1, username); p.setString(2, hashPassword(password));
            p.setString(3, fullName); p.setString(4, role);
            return p.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean updateUser(int userId, String fullName, String role) {
        try (PreparedStatement p = connection.prepareStatement(
                "UPDATE users SET full_name=?,role=? WHERE user_id=?")) {
            p.setString(1, fullName); p.setString(2, role); p.setInt(3, userId);
            return p.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean deleteUser(int userId) {
        try (PreparedStatement p = connection.prepareStatement(
                "DELETE FROM users WHERE user_id=? AND role != 'admin'")) {
            p.setInt(1, userId);
            return p.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean resetPassword(int userId, String newPassword) {
        try (PreparedStatement p = connection.prepareStatement(
                "UPDATE users SET password=? WHERE user_id=?")) {
            p.setString(1, hashPassword(newPassword)); p.setInt(2, userId);
            return p.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT user_id,username,full_name,role,created_at FROM users")) {
            while (rs.next())
                list.add(new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("full_name"), rs.getString("role"), rs.getString("created_at")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Bus methods ───────────────────────────────────────────────────────────
    public List<Bus> getAllBuses() {
        List<Bus> list = new ArrayList<>();
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM buses")) {
            while (rs.next())
                list.add(new Bus(rs.getInt("bus_id"), rs.getString("bus_number"), rs.getString("bus_name"), rs.getInt("capacity"), rs.getString("bus_type")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean addBus(Bus bus) {
        try (PreparedStatement p = connection.prepareStatement(
                "INSERT INTO buses (bus_number,bus_name,capacity,bus_type) VALUES (?,?,?,?)")) {
            p.setString(1, bus.getBusNumber()); p.setString(2, bus.getBusName());
            p.setInt(3, bus.getCapacity());     p.setString(4, bus.getBusType());
            return p.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean updateBus(Bus bus) {
        try (PreparedStatement p = connection.prepareStatement(
                "UPDATE buses SET bus_number=?,bus_name=?,capacity=?,bus_type=? WHERE bus_id=?")) {
            p.setString(1, bus.getBusNumber()); p.setString(2, bus.getBusName());
            p.setInt(3, bus.getCapacity());     p.setString(4, bus.getBusType()); p.setInt(5, bus.getBusId());
            return p.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean deleteBus(int busId) {
        try (PreparedStatement p = connection.prepareStatement("DELETE FROM buses WHERE bus_id=?")) {
            p.setInt(1, busId); return p.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // ── Route methods ─────────────────────────────────────────────────────────
    public List<Route> getAllRoutes() {
        List<Route> list = new ArrayList<>();
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM routes")) {
            while (rs.next())
                list.add(new Route(rs.getInt("route_id"), rs.getString("origin"), rs.getString("destination"), rs.getDouble("distance_km"), rs.getDouble("base_fare")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean addRoute(Route route) {
        try (PreparedStatement p = connection.prepareStatement(
                "INSERT INTO routes (origin,destination,distance_km,base_fare) VALUES (?,?,?,?)")) {
            p.setString(1, route.getOrigin()); p.setString(2, route.getDestination());
            p.setDouble(3, route.getDistanceKm()); p.setDouble(4, route.getBaseFare());
            return p.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean updateRoute(Route route) {
        try (PreparedStatement p = connection.prepareStatement(
                "UPDATE routes SET origin=?,destination=?,distance_km=?,base_fare=? WHERE route_id=?")) {
            p.setString(1, route.getOrigin()); p.setString(2, route.getDestination());
            p.setDouble(3, route.getDistanceKm()); p.setDouble(4, route.getBaseFare()); p.setInt(5, route.getRouteId());
            return p.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean deleteRoute(int routeId) {
        try (PreparedStatement p = connection.prepareStatement("DELETE FROM routes WHERE route_id=?")) {
            p.setInt(1, routeId); return p.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // ── Schedule methods ──────────────────────────────────────────────────────
    public List<Schedule> getAllSchedules() {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT s.schedule_id, b.bus_number, b.bus_name, b.bus_type, r.origin, r.destination, " +
                     "s.departure_time, s.arrival_time, s.available_seats, (r.base_fare * s.fare_multiplier) as fare " +
                     "FROM schedules s JOIN buses b ON s.bus_id=b.bus_id JOIN routes r ON s.route_id=r.route_id " +
                     "WHERE s.available_seats > 0 ORDER BY s.departure_time";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new Schedule(rs.getInt("schedule_id"), rs.getString("bus_number"), rs.getString("bus_name"),
                        rs.getString("bus_type"), rs.getString("origin"), rs.getString("destination"),
                        rs.getString("departure_time"), rs.getString("arrival_time"),
                        rs.getInt("available_seats"), rs.getDouble("fare")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Schedule> getAllSchedulesAdmin() {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT s.schedule_id, b.bus_number, b.bus_name, b.bus_type, r.origin, r.destination, " +
                     "s.departure_time, s.arrival_time, s.available_seats, (r.base_fare * s.fare_multiplier) as fare " +
                     "FROM schedules s JOIN buses b ON s.bus_id=b.bus_id JOIN routes r ON s.route_id=r.route_id " +
                     "ORDER BY s.departure_time";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new Schedule(rs.getInt("schedule_id"), rs.getString("bus_number"), rs.getString("bus_name"),
                        rs.getString("bus_type"), rs.getString("origin"), rs.getString("destination"),
                        rs.getString("departure_time"), rs.getString("arrival_time"),
                        rs.getInt("available_seats"), rs.getDouble("fare")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Schedule> searchSchedules(String origin, String destination, String date) {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT s.schedule_id, b.bus_number, b.bus_name, b.bus_type, b.capacity, r.origin, r.destination, " +
                     "r.distance_km, r.base_fare, s.departure_time, s.arrival_time, s.available_seats, " +
                     "(r.base_fare * s.fare_multiplier) as fare FROM schedules s " +
                     "JOIN buses b ON s.bus_id=b.bus_id JOIN routes r ON s.route_id=r.route_id " +
                     "WHERE r.origin=? AND r.destination=? AND DATE(s.departure_time)=? AND s.available_seats > 0";
        try (PreparedStatement p = connection.prepareStatement(sql)) {
            p.setString(1, origin); p.setString(2, destination); p.setString(3, date);
            ResultSet rs = p.executeQuery();
            while (rs.next())
                list.add(new Schedule(rs.getInt("schedule_id"), rs.getString("bus_number"), rs.getString("bus_name"),
                        rs.getString("bus_type"), rs.getString("origin"), rs.getString("destination"),
                        rs.getString("departure_time"), rs.getString("arrival_time"),
                        rs.getInt("available_seats"), rs.getDouble("fare")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean addSchedule(int busId, int routeId, String departureTime, String arrivalTime, int availableSeats, double fareMultiplier) {
        try (PreparedStatement p = connection.prepareStatement(
                "INSERT INTO schedules (bus_id,route_id,departure_time,arrival_time,available_seats,fare_multiplier) VALUES (?,?,?,?,?,?)")) {
            p.setInt(1, busId); p.setInt(2, routeId);
            p.setString(3, departureTime); p.setString(4, arrivalTime);
            p.setInt(5, availableSeats); p.setDouble(6, fareMultiplier);
            return p.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean deleteSchedule(int scheduleId) {
        try (PreparedStatement p = connection.prepareStatement("DELETE FROM schedules WHERE schedule_id=?")) {
            p.setInt(1, scheduleId); return p.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // ── Booking methods ───────────────────────────────────────────────────────
    public int bookTicket(int userId, int scheduleId, String seatNumbers, String passengerName,
                          int passengerAge, String passengerGender, double baggageKg, double totalFare) {
        try {
            connection.setAutoCommit(false);
            int numSeats = seatNumbers.split(",").length;

            PreparedStatement check = connection.prepareStatement(
                "SELECT available_seats FROM schedules WHERE schedule_id=?");
            check.setInt(1, scheduleId);
            ResultSet rs = check.executeQuery();
            if (!rs.next() || rs.getInt(1) < numSeats) {
                connection.rollback();
                connection.setAutoCommit(true);
                return -2;
            }

            PreparedStatement ins = connection.prepareStatement(
                "INSERT INTO bookings (user_id,schedule_id,seat_numbers,passenger_name,passenger_age,passenger_gender,baggage_kg,total_fare) VALUES (?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ins.setInt(1, userId); ins.setInt(2, scheduleId); ins.setString(3, seatNumbers);
            ins.setString(4, passengerName); ins.setInt(5, passengerAge);
            ins.setString(6, passengerGender); ins.setDouble(7, baggageKg); ins.setDouble(8, totalFare);
            ins.executeUpdate();

            int bookingId = ins.getGeneratedKeys().getInt(1);

            PreparedStatement upd = connection.prepareStatement(
                "UPDATE schedules SET available_seats = available_seats - ? WHERE schedule_id=?");
            upd.setInt(1, numSeats); upd.setInt(2, scheduleId);
            upd.executeUpdate();

            connection.commit();
            connection.setAutoCommit(true);
            return bookingId;
        } catch (SQLException e) {
            try { connection.rollback(); connection.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return -1;
        }
    }

    public List<Booking> getUserBookings(int userId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.booking_date, b.seat_numbers, b.passenger_name, b.total_fare, b.status, " +
                     "s.departure_time, r.origin, r.destination, bu.bus_name " +
                     "FROM bookings b JOIN schedules s ON b.schedule_id=s.schedule_id " +
                     "JOIN routes r ON s.route_id=r.route_id JOIN buses bu ON s.bus_id=bu.bus_id " +
                     "WHERE b.user_id=? ORDER BY b.booking_date DESC";
        try (PreparedStatement p = connection.prepareStatement(sql)) {
            p.setInt(1, userId);
            ResultSet rs = p.executeQuery();
            while (rs.next())
                list.add(new Booking(rs.getInt("booking_id"), rs.getString("booking_date"),
                        rs.getString("seat_numbers"), rs.getString("passenger_name"),
                        rs.getDouble("total_fare"), rs.getString("status"),
                        rs.getString("departure_time"), rs.getString("origin"),
                        rs.getString("destination"), rs.getString("bus_name")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean cancelBooking(int bookingId, int userId) {
        try {
            connection.setAutoCommit(false);
            PreparedStatement get = connection.prepareStatement(
                "SELECT schedule_id, seat_numbers FROM bookings WHERE booking_id=? AND user_id=? AND status='confirmed'");
            get.setInt(1, bookingId); get.setInt(2, userId);
            ResultSet rs = get.executeQuery();
            if (!rs.next()) { connection.rollback(); connection.setAutoCommit(true); return false; }

            int scheduleId = rs.getInt("schedule_id");
            int numSeats = rs.getString("seat_numbers").split(",").length;

            PreparedStatement upd = connection.prepareStatement(
                "UPDATE schedules SET available_seats = available_seats + ? WHERE schedule_id=?");
            upd.setInt(1, numSeats); upd.setInt(2, scheduleId); upd.executeUpdate();

            PreparedStatement cancel = connection.prepareStatement(
                "UPDATE bookings SET status='cancelled' WHERE booking_id=?");
            cancel.setInt(1, bookingId); cancel.executeUpdate();

            connection.commit();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try { connection.rollback(); connection.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        }
    }

    public boolean restoreBooking(int bookingId) {
        try {
            connection.setAutoCommit(false);
            PreparedStatement get = connection.prepareStatement(
                "SELECT schedule_id, seat_numbers FROM bookings WHERE booking_id=?");
            get.setInt(1, bookingId);
            ResultSet rs = get.executeQuery();
            if (!rs.next()) { connection.rollback(); connection.setAutoCommit(true); return false; }

            int scheduleId = rs.getInt("schedule_id");
            int numSeats = rs.getString("seat_numbers").split(",").length;

            PreparedStatement upd = connection.prepareStatement(
                "UPDATE schedules SET available_seats = available_seats - ? WHERE schedule_id=?");
            upd.setInt(1, numSeats); upd.setInt(2, scheduleId); upd.executeUpdate();

            PreparedStatement restore = connection.prepareStatement(
                "UPDATE bookings SET status='confirmed' WHERE booking_id=?");
            restore.setInt(1, bookingId); restore.executeUpdate();

            connection.commit();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try { connection.rollback(); connection.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        }
    }

    // ── Report methods ────────────────────────────────────────────────────────
    public List<BookingReport> getBookingSummaryReport() {
        List<BookingReport> list = new ArrayList<>();
        String sql = "SELECT DATE(booking_date) as date, COUNT(*) as bookings, SUM(total_fare) as revenue " +
                     "FROM bookings WHERE status='confirmed' GROUP BY DATE(booking_date) ORDER BY date DESC LIMIT 30";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new BookingReport(rs.getString("date"), rs.getInt("bookings"), rs.getDouble("revenue"), null, null));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<BookingReport> getPopularRoutesReport() {
        List<BookingReport> list = new ArrayList<>();
        String sql = "SELECT r.origin, r.destination, COUNT(*) as bookings, SUM(b.total_fare) as revenue " +
                     "FROM bookings b JOIN schedules s ON b.schedule_id=s.schedule_id " +
                     "JOIN routes r ON s.route_id=r.route_id WHERE b.status='confirmed' " +
                     "GROUP BY r.route_id ORDER BY bookings DESC LIMIT 5";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new BookingReport(null, rs.getInt("bookings"), rs.getDouble("revenue"), rs.getString("origin"), rs.getString("destination")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Stats for dashboard ───────────────────────────────────────────────────
    public int getTotalBookingsToday() {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM bookings WHERE DATE(booking_date)=DATE('now') AND status='confirmed'")) {
            return rs.getInt(1);
        } catch (SQLException e) { return 0; }
    }

    public double getTotalRevenueToday() {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(total_fare),0) FROM bookings WHERE DATE(booking_date)=DATE('now') AND status='confirmed'")) {
            return rs.getDouble(1);
        } catch (SQLException e) { return 0; }
    }

    public int getTotalActiveSchedules() {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM schedules WHERE available_seats > 0")) {
            return rs.getInt(1);
        } catch (SQLException e) { return 0; }
    }

    public int getTotalUsers() {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE role='user'")) {
            return rs.getInt(1);
        } catch (SQLException e) { return 0; }
    }

    // ========== ADDED METHOD ==========
    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.booking_date, b.seat_numbers, b.passenger_name, b.total_fare, b.status, " +
                     "s.departure_time, r.origin, r.destination, bu.bus_name " +
                     "FROM bookings b JOIN schedules s ON b.schedule_id = s.schedule_id " +
                     "JOIN routes r ON s.route_id = r.route_id JOIN buses bu ON s.bus_id = bu.bus_id " +
                     "ORDER BY b.booking_date DESC";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Booking(
                    rs.getInt("booking_id"),
                    rs.getString("booking_date"),
                    rs.getString("seat_numbers"),
                    rs.getString("passenger_name"),
                    rs.getDouble("total_fare"),
                    rs.getString("status"),
                    rs.getString("departure_time"),
                    rs.getString("origin"),
                    rs.getString("destination"),
                    rs.getString("bus_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    // ==================================

    public void close() {
        try { if (connection != null && !connection.isClosed()) connection.close(); }
        catch (SQLException e) { e.printStackTrace(); }
    }
}