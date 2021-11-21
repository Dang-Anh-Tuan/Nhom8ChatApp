/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;

/**
 *
 * @author ADMIN
 */
public class Message implements Serializable{
    private int id;
    private User from;
    private User to;
    private String msg;

    public Message() {
    }

    public Message(int id, User from, User to, String msg) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.msg = msg;
    }
    
    public Message(User from, User to, String msg){
        this.from = from;
        this.to = to;
        this.msg = msg;
    }

      public Message(int id, String msg){
        this.id = id;
        this.msg = msg;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public User getFrom() {
        return from;
    }
    
 
    public void setTo(User to) {
        this.to = to;
    }

    public User getTo() {
        return to;
    }

    
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    public String toMsg(){
        return to.getUsername() + " : " + msg;
    }
    
}
