package ourhourback.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class MoreDataInserter {

    private static final String URL = "jdbc:mysql://localhost:3306/secret";
    private static final String USER = "secret";
    private static final String PASSWORD = "secret";

    public static void main(String[] args) {
        Random random = new Random();

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            connection.setAutoCommit(false); // 자동 커밋 비활성화

            String sql = "INSERT INTO friendship (from_user_id, to_user_id, status) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                for (int i = 1; i <= 100000; i++) {
                    long fromUserId = random.nextInt(1000) + 1;
                    long toUserId = random.nextInt(1000) + 1;
                    String status = (i % 2 == 0) ? "ACCEPTED" : "PENDING";

                    preparedStatement.setLong(1, fromUserId);
                    preparedStatement.setLong(2, toUserId);
                    preparedStatement.setString(3, status);

                    preparedStatement.addBatch();

                    if (i % 1000 == 0) {
                        preparedStatement.executeBatch();
                        connection.commit(); // 1000개 배치 단위마다 커밋
                    }
                }

                preparedStatement.executeBatch(); // 나머지 배치 실행
                connection.commit(); // 마지막 커밋
                System.out.println("100,000개의 친구 관계 데이터를 성공적으로 삽입했습니다.");
            } catch (SQLException e) {
                connection.rollback(); // 예외 발생 시 롤백
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
