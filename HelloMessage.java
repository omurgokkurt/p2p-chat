package com.company;

import java.io.Serializable;
import java.net.InetAddress;

public class HelloMessage implements Serializable {

    private String userName;
    private String message="HELLO";

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
