package ourhourback.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataInserter {

    private static final String URL = "jdbc:mysql://localhost:3306/secret";
    private static final String USER = "secret";
    private static final String PASSWORD = "secret";

    public static void main(String[] args) {

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            connection.setAutoCommit(false); // 자동 커밋 비활성화

            String sql = "INSERT INTO member (authority, email, gender, language, login_type, nickname, password, phone_number, profile_image, user_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                for (int i = 1; i <= 100000; i++) {
                    String authority = "ROLE_USER";
                    String email = "user" + i + "@example.com";
                    String gender = (i % 2 == 0) ? "Male" : "Female";
                    String language = (i % 2 == 0) ? "English" : "Korean";
                    String loginType = (i % 3 == 0) ? "GOOGLE" : (i % 3 == 1) ? "KAKAO" : "LOCAL";
                    String nickname = "User" + i;
                    String password = "password" + i;
                    String phoneNumber = "010-" + String.format("%04d", i % 10000) + "-" + String.format("%04d", i % 10000);
                    String profileImage = "profile" + i + ".jpg";
                    String userId = "user" + i;

                    preparedStatement.setString(1, authority);
                    preparedStatement.setString(2, email);
                    preparedStatement.setString(3, gender);
                    preparedStatement.setString(4, language);
                    preparedStatement.setString(5, loginType);
                    preparedStatement.setString(6, nickname);
                    preparedStatement.setString(7, password);
                    preparedStatement.setString(8, phoneNumber);
                    preparedStatement.setString(9, profileImage);
                    preparedStatement.setString(10, userId);

                    preparedStatement.addBatch();

                    if (i % 1000 == 0) {  // 1000개 단위로 배치 실행
                        preparedStatement.executeBatch();
                        connection.commit(); // 커밋
                    }
                }

                preparedStatement.executeBatch(); // 나머지 배치 실행
                connection.commit(); // 마지막 커밋
                System.out.println("100,000개의 사용자 데이터를 성공적으로 삽입했습니다.");
            } catch (SQLException e) {
                connection.rollback(); // 예외 발생 시 롤백
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
