/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Message;
import model.User;

/**
 *
 * @author ADMIN
 */
public class MessageDAO {

    Connection con;
    String hostName = "localhost";
    String dbName = "chatapp";
    String userName = "root";
    String password = "123456";

    private UserDAO userDAO;

    public void getCon() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String connectionURL = "jdbc:mysql://" + hostName + "/" + dbName;
            con = DriverManager.getConnection(connectionURL, userName,
                    password);
        } catch (ClassNotFoundException | SQLException e) {
        }
    }

    public void disconnect() {
        try {
            con.close();
        } catch (SQLException ex) {

        }
    }

    public int saveMessage(Message message) {
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatement2 = null;
        try {
            getCon();
            if (this.con != null) {
                Integer id = null;
                con.setAutoCommit(false);

                String sql = "INSERT INTO `chatapp`.`message` (`msg`) VALUES (?);";
                preparedStatement = this.con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, message.getMsg());
                preparedStatement.executeUpdate();

                resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    id = resultSet.getInt(1);
                }

                String sql2 = "INSERT INTO `chatapp`.`user_message` "
                        + "(`user_from`, `user_to`, `msg_id`) "
                        + "VALUES (?, ? , ?);";
                preparedStatement2 = this.con.prepareStatement(sql2);
                preparedStatement2.setString(1, message.getFrom().getUsername());
                preparedStatement2.setString(2, message.getTo().getUsername());
                preparedStatement2.setInt(3, id);
                int result = preparedStatement2.executeUpdate();

                con.commit();
                return result;
            }

        } catch (SQLException ex) {
            try {
                con.rollback();
            } catch (SQLException ex1) {
                System.out.println(ex1);
            }
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (preparedStatement2 != null) {
                    preparedStatement2.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.out.println(ex);
                }
            }
        }
        return -1;
    }

    public Message findByID(int id) {
        Message message = null;
        try {
            getCon();
            if (this.con != null) {
                String sql = "Select * from message WHERE id = ? ;";
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setInt(1, id);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    message = new Message(id, rs.getString(2));
                }
                disconnect();
                return message;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return message;
    }

    public List<Message> findPrivateChat(String username1, String username2) {
        List<Message> messages = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;

        userDAO = new UserDAO();

        try {
            getCon();
            if (con != null) {
                String sql = "SELECT * FROM chatapp.user_message "
                        + "WHERE (user_from = ? AND user_to = ?) "
                        + "OR (user_from = ? AND user_to = ?)";

                preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1, username1);
                preparedStatement.setString(2, username2);
                preparedStatement.setString(3, username2);
                preparedStatement.setString(4, username1);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String nameFrom = resultSet.getString(2);
                    String nameTo = resultSet.getString(3);
                    int idMsg = resultSet.getInt(4);
                    messages.add(
                            new Message(
                                    idMsg,
                                    userDAO.findByUsername(nameFrom),
                                    userDAO.findByUsername(nameTo),
                                    findByID(idMsg).getMsg()
                            )
                    );
                }
                return messages;
            }
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }

                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
               
            }
        }
        return null;
    }
}
