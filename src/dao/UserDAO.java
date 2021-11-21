/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import model.User;

/**
 *
 * @author ADMIN
 */
public class UserDAO {

    // for file
    private final String file = "src/files/user.txt";

    // for db
    Connection con;
    String hostName = "localhost";
    String dbName = "chatapp";
    String userName = "root";
    String password = "123456";

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

    // for file 
//    public List<User> findAll() {
//        Scanner scanner;
//        List<User> list = new ArrayList<>();
//        try {
//            scanner = new Scanner(new File(file));
//            while(scanner.hasNextLine()){
//                String line = scanner.nextLine();
//                String data[] = line.split(" ");
//                list.add(new User(data[0], data[1]));
//            }
//
//            scanner.close();
//        } catch (IOException e) {
//
//        }
//        return list;
//    }
//
//    public void saveUser(User user) {
//        try {
//            Writer output;
//            output = new BufferedWriter(new FileWriter(file, true));
//            output.append(user.getUsername() + " " + user.getPassword() + "\n");
//            output.close();
//        } catch (IOException e) {
//            System.out.println(e);
//        }
//    }
    public int saveUser(User user) {
        try {
            getCon();
            if (this.con != null) {
                PreparedStatement preparedStatement
                        = this.con.prepareStatement("INSERT INTO `chatapp`.`user` (`username`, `password`) VALUES (? , ?);");
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getPassword());
                int result = preparedStatement.executeUpdate();
                preparedStatement.close();
                disconnect();
                return result;
            }

        } catch (SQLException ex) {

        }
        return -1;
    }

    public ArrayList<User> findAll() throws RemoteException {
        ArrayList<User> users = new ArrayList<User>();
        try {
            getCon();
            if (this.con != null) {
                String sql = "Select * from user";
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    users.add(new User(rs.getString(1), rs.getString(2)));
                }
                disconnect();
                return users;
            } else {
                System.out.println("connect fail");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public User findByUsername(String userName) {
        User user = null;
        try {
            getCon();
            if (this.con != null) {
                String sql = "Select * from user WHERE username = ? ;";
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1, userName);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    user = new User(rs.getString(1), rs.getString(2));
                }
                disconnect();
                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return user;
    }
}
