package server;

import java.sql.*;

public class UsersDatabase {

    static final String DATABASE_URL = "jdbc:sqlite:users.db";
    static Connection connection;
    static Statement statement;

    static {

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DATABASE_URL);
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    public void createTable() throws SQLException {
        String createTable = "create table if not exists users (" +
                "id integer not null primary key, " +
                "nickname varchar(30) not null," +
                "login varchar(30) not null unique," +
                "password varchar(30) not null)";
        statement.execute(createTable);
    }

    public String getNickname(String login, String password) {

        try(PreparedStatement preparedStatement = connection.prepareStatement("select nickname from users where login = ? and password = ?")){
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    public boolean updateNickname(String login, String newNickname) {

        try(
                PreparedStatement preparedStatementSelect = connection.prepareStatement("select nickname from users where nickname = ?");
                PreparedStatement preparedStatementUpdate = connection.prepareStatement("update users set nickname = ? where login = ?")
        ){
            preparedStatementSelect.setString(1, newNickname);
            ResultSet resultSet = preparedStatementSelect.executeQuery();
            if(resultSet.next()){
                return false;
            }
            preparedStatementUpdate.setString(1, newNickname);
            preparedStatementUpdate.setString(2, login);
            preparedStatementUpdate.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean createUser(String login, String password, String nickname) {

        try(
                PreparedStatement preparedStatementSelect = connection.prepareStatement("select login, nickname from users where login = ? or nickname = ?");
                PreparedStatement preparedStatementInsert = connection.prepareStatement("insert into users (login, password, nickname) values (?, ?, ?)")
        ){
            preparedStatementSelect.setString(1, login);
            preparedStatementSelect.setString(2, nickname);
            ResultSet resultSet = preparedStatementSelect.executeQuery();
            if(resultSet.next()){
                return false;
            }

            preparedStatementInsert.setString(1, login);
            preparedStatementInsert.setString(2, password);
            preparedStatementInsert.setString(3, nickname);
            preparedStatementInsert.execute();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void createDefaultUsers() {
        createUser("user1", "pass1", "user1");
        createUser("user2", "pass2", "user2");
        createUser("user3", "pass3", "user3");
    }
}
