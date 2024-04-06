package ru.pavlova.java.basic;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcUsersAuthenticationService {

    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USERS_QUERY = "" +
            "SELECT u.id, u.email, u.password, r.name as role_name " +
            "FROM users u " +
            "JOIN user_to_role utr ON u.id = utr.user_id " +
            "JOIN roles r ON utr.role_id = r.id";

    public JdbcUsersAuthenticationService() {
        List<User> users = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, "postgres", "postgres")) {
            Statement statement = connection.createStatement();
            try {
                ResultSet usersResultSet = getUsersResultSet(statement);
                processUsersResultSet(usersResultSet, users);
            } finally {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(users);
    }

    private ResultSet getUsersResultSet(Statement statement) throws SQLException {
        return statement.executeQuery(USERS_QUERY);
    }

    private void processUsersResultSet(ResultSet usersResultSet, List<User> users) throws SQLException {
        while (usersResultSet.next()) {
            int id = usersResultSet.getInt("id");
            String email = usersResultSet.getString("email");
            String password = usersResultSet.getString("password");
            String roleName = usersResultSet.getString("role_name");

            Role role = new Role(1, roleName);
            User user = new User(id, password, email, role);
            users.add(user);
        }
    }
}