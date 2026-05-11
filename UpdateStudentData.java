import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UpdateStudentData {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/club_management_system?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8";
        String username = "root";
        String password = "123456";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            // 查看当前students表中的数据
            String selectSql = "SELECT student_id, name, phone, email FROM students";
            try (PreparedStatement pstmt = conn.prepareStatement(selectSql);
                 ResultSet rs = pstmt.executeQuery()) {
                System.out.println("更新前的学生数据：");
                System.out.println("学号		姓名		电话		邮箱");
                while (rs.next()) {
                    String studentId = rs.getString("student_id");
                    String name = rs.getString("name");
                    String phone = rs.getString("phone");
                    String email = rs.getString("email");
                    System.out.printf("%s	%s	%s	%s%n", studentId, name, phone, email);
                }
            }

            // 更新学生的phone和email字段
            updateStudent(conn, "2020001001", "13800138001", "wang_president@club-system.com");
            updateStudent(conn, "2020002001", "13800138002", "liu_president@club-system.com");
            updateStudent(conn, "2020003001", "13800138003", "chen_president@club-system.com");
            updateStudent(conn, "2020004001", "13800138004", "yang_president@club-system.com");
            updateStudent(conn, "2021001001", "13800138005", "zhang_san@club-system.com");
            updateStudent(conn, "2021001002", "13800138006", "li_si@club-system.com");
            updateStudent(conn, "T001", "13800138007", "zhang_teacher@club-system.com");

            // 查看更新后的学生数据
            try (PreparedStatement pstmt = conn.prepareStatement(selectSql);
                 ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n更新后的学生数据：");
                System.out.println("学号		姓名		电话		邮箱");
                while (rs.next()) {
                    String studentId = rs.getString("student_id");
                    String name = rs.getString("name");
                    String phone = rs.getString("phone");
                    String email = rs.getString("email");
                    System.out.printf("%s	%s	%s	%s%n", studentId, name, phone, email);
                }
            }

            System.out.println("\n学生数据更新完成！");

        } catch (SQLException e) {
            System.err.println("数据库连接或操作失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updateStudent(Connection conn, String studentId, String phone, String email) throws SQLException {
        String updateSql = "UPDATE students SET phone = ?, email = ? WHERE student_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setString(1, phone);
            pstmt.setString(2, email);
            pstmt.setString(3, studentId);
            pstmt.executeUpdate();
        }
    }
}
