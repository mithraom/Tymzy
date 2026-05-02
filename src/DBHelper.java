import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBHelper {

    // --- EDIT THESE to match your DB ---
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USER = "system";   // <-- change if needed
    private static final String PASSWORD = "orcl"; // <-- change to your password

    // load driver early so missing-jar errors are obvious
    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle JDBC Driver not found. Add ojdbc jar to project classpath.");
            e.printStackTrace();
        }
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Insert new activity
    public static void insertActivity(String activity) {
        String sql = "INSERT INTO activities(activity_name) VALUES(?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, activity);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Insert activity error:");
            e.printStackTrace();
        }
    }

    // Fetch all activities (returns strings like "activity (timestamp)")
    public static List<String> getAllActivities() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT id, activity_name, created_at FROM activities ORDER BY id ASC";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String activity = rs.getString("activity_name");
                Timestamp ts = rs.getTimestamp("created_at");
                list.add(activity + " (" + ts + ")");
            }
        } catch (SQLException e) {
            System.err.println("Fetch activities error:");
            e.printStackTrace();
        }
        return list;
    }

    // Delete an activity by exact name (as your UI does)
    public static void deleteActivity(String activity) {
        String sql = "DELETE FROM activities WHERE activity_name = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, activity);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Delete activity error:");
            e.printStackTrace();
        }
    }

    // Add credits (transactional: update, if no row then insert)
    public static void addCredits(int minutes) {
        String sqlUpdate = "UPDATE user_credits SET total_credits = total_credits + ? WHERE id = 1";
        String sqlInsert = "INSERT INTO user_credits(id, total_credits) VALUES(1, ?)";
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                pstmt.setInt(1, minutes);
                int rows = pstmt.executeUpdate();
                if (rows == 0) {
                    try (PreparedStatement insert = conn.prepareStatement(sqlInsert)) {
                        insert.setInt(1, minutes);
                        insert.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Add credits error:");
            e.printStackTrace();
        }
    }

    // Use credits: returns true when update succeeded (enough credits), false otherwise
    public static boolean useCredits(int minutes) {
        String sql = "UPDATE user_credits SET total_credits = total_credits - ? WHERE id = 1 AND total_credits >= ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, minutes);
            pstmt.setInt(2, minutes);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Use credits error:");
            e.printStackTrace();
            return false;
        }
    }

    // Get current credits
    public static int getCredits() {
        String sql = "SELECT total_credits FROM user_credits WHERE id = 1";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total_credits");
            }
        } catch (SQLException e) {
            System.err.println("Get credits error:");
            e.printStackTrace();
        }
        return 0;
    }
}
