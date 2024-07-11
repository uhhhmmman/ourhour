package ourhourback.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class IndexManager {

    private static final String URL = "jdbc:mysql://localhost:3306/secret";
    private static final String USER = "secret";
    private static final String PASSWORD = "secret";

    public static void createIndexes() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE INDEX idx_from_user_id ON friendship (from_user_id)");
            statement.executeUpdate("CREATE INDEX idx_to_user_id ON friendship (to_user_id)");
            statement.executeUpdate("CREATE INDEX idx_member_email ON member (email)");
            System.out.println("Indexes created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
