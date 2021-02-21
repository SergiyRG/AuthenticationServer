package org.example.app.repositories;

import org.example.app.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.sql.Types;

@org.springframework.stereotype.Repository("userRepository")
@PropertySource("classpath:database.properties")
public class UserRepository implements Repository<User> {

    @Value("${database.tableName}")
    private String tableName;

    @Value("${database.emailColumn}")
    private String emailColumn;

    @Value("${database.nameColumn}")
    private String nameColumn;

    @Value("${database.passwordColumn}")
    private String passwordColumn;

    public static final String containsQuery = "SELECT * FROM \"%s\" WHERE \"%s\" = ?;";
    public static final String insertQuery = "INSERT INTO \"%s\"(\"%s\", \"%s\", \"%s\") VALUES(?, ?, ?)";
    public static final String authorizedQuery = "SELECT * FROM \"%s\" WHERE \"%s\" = ? AND \"%s\" = ?;";
    public static final String deleteQuery = "DELETE FROM \"%s\" WHERE \"%s\" = ?";

    @Autowired
    private JdbcOperations jdbcOperations;

    @Override
    public boolean contains(User user) {
        String query = String.format(containsQuery, tableName, emailColumn);

        Object[] objects = {user.getEmail()};
        int[] types = {Types.VARCHAR};

        SqlRowSet rowSet = jdbcOperations.queryForRowSet(query, objects, types);
        return rowSet.next();
    }

    @Override
    public boolean insert(User user) {
        String query = String.format(insertQuery, tableName, nameColumn, emailColumn, passwordColumn);

        Object[] objects = {user.getName(), user.getEmail(), user.getPassword()};
        int[] types = {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};

        return jdbcOperations.update(query, objects, types) > 0;
    }

    @Override
    public boolean isAuthorized(User user) {
        String query = String.format(authorizedQuery, tableName, emailColumn, passwordColumn);

        Object[] objects = {user.getEmail(), user.getPassword()};
        int[] types = {Types.VARCHAR, Types.VARCHAR};

        SqlRowSet rowSet = jdbcOperations.queryForRowSet(query, objects, types);
        return rowSet.next();
    }

    @Override
    public boolean remove(User user) {
        String query = String.format(deleteQuery, tableName, emailColumn);

        Object[] objects = {user.getEmail()};
        int[] types = {Types.VARCHAR};

        return jdbcOperations.update(query, objects, types) > 0;
    }
}
