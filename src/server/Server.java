/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import dao.MessageDAO;
import dao.UserDAO;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import model.Message;
import model.User;

/**
 *
 * @author ADMIN
 */
public class Server {

    private static final int PORT = 9999;

    private static HashMap<String, DataOutputStream> writers = new HashMap<String, DataOutputStream>();

    private static HashMap<String, ObjectOutputStream> writersObject = new HashMap<String, ObjectOutputStream>();

    public static void main(String[] args) throws Exception {
        System.out.println("Server run");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {

        private String name = "unknown";
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        private ObjectOutputStream objectOutputStream;
        private ObjectInputStream objectInputStream;
        private UserDAO userDAO;
        private MessageDAO messageDAO;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());

                userDAO = new UserDAO();
                messageDAO = new MessageDAO();

                while (true) {

                    String input = in.readUTF();
                    //===================== Register ============================
                    if (input.equals("__REGISTER")) {
                        String reg_username = in.readUTF();
                        String reg_password = in.readUTF();

                        List<User> users = userDAO.findAll();

                        boolean checkExist = false;

                        for (User user : users) {
                            if (user.getUsername().equals(reg_username)) {
                                checkExist = true;
                                break;
                            }
                        }

                        if (!checkExist) {
                            userDAO.saveUser(new User(reg_username, reg_password));
                            out.writeUTF("__REGISTER_SUCCESS");
                        } else {
                            out.writeUTF("__REGISTER_UNSUCCESS");
                        }
                    } 

                    //=================== AND Register ============================
                    
                    
                    
                    //===================== Login ============================
                    else if (input.equals("__LOGIN")) {
                        String log_username = in.readUTF();
                        String log_password = in.readUTF();

                        List<User> users = userDAO.findAll();
                        System.out.println("user");

                        boolean isCorrect = false;

                        for (User user : users) {
                            if (user.getUsername().equals(log_username)
                                    && user.getPassword().equals(log_password)) {
                                isCorrect = true;
                                break;
                            }
                        }

                        if (isCorrect) {
                            name = log_username;
                            List<String> clientsOnline = new ArrayList<>();

                            // add new writer for new client online
                            writers.put(name, out);
                            writersObject.put(name, objectOutputStream);
                            out.writeUTF("__LOGIN_SUCCESS");

                            // update new list useronline to any client
                            Iterator iteratorWriter = writers.entrySet().iterator();
                            while (iteratorWriter.hasNext()) {
                                Map.Entry me2 = (Map.Entry) iteratorWriter.next();
                                DataOutputStream o = (DataOutputStream) me2.getValue();
                                clientsOnline.add(me2.getKey().toString());
                                o.writeUTF("__UPDATE");
                            }

                            Iterator iterator = writersObject.entrySet().iterator();
                            while (iterator.hasNext()) {
                                Map.Entry me2 = (Map.Entry) iterator.next();
                                System.out.println(me2.getValue());
                                ObjectOutputStream o = (ObjectOutputStream) me2.getValue();
                                o.writeUnshared(clientsOnline);
                            }

                        } else {
                            out.writeUTF("__LOGIN_UNSUCCESS");
                        }
                    } 

                    
                    
                    //=================== AND Login ============================
                    
                    
                    
                    
                    //===================== Chat all ============================
                    else if (input.startsWith("__CHAT_ALL")) {
                        // get message
                        String msg = in.readUTF();

                        // send all
                        Iterator iterator = writers.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry me2 = (Map.Entry) iterator.next();
                            DataOutputStream o = (DataOutputStream) me2.getValue();
                            o.writeUTF("__CHAT_ALL" + name + ": " + msg);
                        }
                    } 


                    //=================== AND Chat all ============================
                    
                    
                    
                    //================== Chat solo ( thì thầm ) ============================
                    else if (input.startsWith("__WHISPER")) {

                        // get client send to
                        String clientSendTo = in.readUTF();

                        // get message
                        String msg = in.readUTF();

                        // send msg to 2 client chat solo
                        writers.get(name).writeUTF("__WHISPER" + name + ":" + msg);
                        writers.get(clientSendTo).writeUTF("__WHISPER" + name + ":" + msg);

                    } 


                    //================== AND Chat solo ( thì thầm ) ============================
                    
                    
                    
                    //================== Chat solo ( phòng riêng ) ============================
                    else if (input.startsWith("__REQUEST_SOLO")) {
                        String nameWanaChatTo = in.readUTF();
                        String nameRequest = in.readUTF();
                        DataOutputStream outNameWanaChatTo = writers.get(nameWanaChatTo);
                        outNameWanaChatTo.writeUTF("__INVITE_CHAT");
                        outNameWanaChatTo.writeUTF(nameRequest);

                    } else if (input.startsWith("__ACCEPT_INVITATION")) {
                        String nameWanaChatTo = in.readUTF();
                        String nameRequest = in.readUTF();
                        DataOutputStream outNameRequest = writers.get(nameRequest);
                        outNameRequest.writeUTF("__ACCEPTED");
                        outNameRequest.writeUTF(nameWanaChatTo);
                        
                        
                        // send history chat
                        writers.get(nameWanaChatTo).writeUTF("__HISTORY_CHAT");
                        outNameRequest.writeUTF("__HISTORY_CHAT");
                        List<Message> messages = messageDAO.findPrivateChat(nameWanaChatTo, nameRequest);
                        writersObject.get(nameWanaChatTo).writeUnshared(messages);
                        writersObject.get(nameRequest).writeUnshared(messages);

                    } else if (input.startsWith("__DISAGREE_INVITATION")) {
                        String nameWanaChatTo = in.readUTF();
                        String nameRequest = in.readUTF();
                        DataOutputStream outNameRequest = writers.get(nameRequest);
                        outNameRequest.writeUTF("__UNACCEPTED");
                        outNameRequest.writeUTF(nameWanaChatTo);

                    } else if (input.startsWith("__OUT_SOLO")) {
                        String nameRequest = in.readUTF();
                        String nameWanaChatTo = in.readUTF();
                        DataOutputStream outNameRequest = writers.get(nameRequest);
                        DataOutputStream outNameWanaChatTo = writers.get(nameWanaChatTo);
                        outNameRequest.writeUTF("__OUT_SOLO");
                        outNameWanaChatTo.writeUTF("__OUT_SOLO");

                    } else if (input.startsWith("__CHAT_SOLO")) {
                        String nameRequest = in.readUTF();
                        String nameWanaChatTo = in.readUTF();
                        DataOutputStream outNameRequest = writers.get(nameRequest);
                        DataOutputStream outNameWanaChatTo = writers.get(nameWanaChatTo);
                        String msgContent = in.readUTF();
                        
                        // save db
                        User from = userDAO.findByUsername(nameRequest);
                        User to = userDAO.findByUsername(nameWanaChatTo);
                        messageDAO.saveMessage(new Message(from, to, msgContent));
                        
                        // send to client
                        String msgSend = ("__CHAT_SOLO " + nameRequest + ": " + msgContent);
                        outNameRequest.writeUTF(msgSend);
                        outNameWanaChatTo.writeUTF(msgSend);
                    } 


                    //=================== AND Chat solo (phòng riêng)============================
                    
                    
                    
                    //------------------ Exit app ------------------------------
                    else if (input.startsWith("__CLOSE")) {
                        writers.remove(name);
                        writersObject.remove(name);
                    } 

                    //------------------AND Exit app ------------------------------
                    
                    
                    
                    //================== Update ============================
                    else if (input.startsWith("__UPDATE")) {
                        List<String> clientsOnline = new ArrayList<>();
                        Iterator iteratorWriter = writers.entrySet().iterator();
                        while (iteratorWriter.hasNext()) {
                            Map.Entry me2 = (Map.Entry) iteratorWriter.next();
                            DataOutputStream o = (DataOutputStream) me2.getValue();
                            clientsOnline.add(me2.getKey().toString());
                            o.writeUTF("__UPDATE");
                        }

                        Iterator iterator = writersObject.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry me2 = (Map.Entry) iterator.next();
                            System.out.println(me2.getValue());
                            ObjectOutputStream o = (ObjectOutputStream) me2.getValue();

                            o.writeUnshared(clientsOnline);

                        }
                    }

                    //================== End Update ============================
                }

            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
