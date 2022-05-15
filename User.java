package com.company;

import java.net.Socket;
import java.net.SocketAddress;
import java.util.Objects;

public class User {

    private String userName;
    private String password;
    private Boolean isOnline = false;
    private SocketAddress remoteSocketAdress;
    private Socket activeTCPSocket;
    private Long lastConnectionTimeStamp;
    private String userServerPort;

    public User() {
    }

    public User(String userName, String password, SocketAddress ipAdress) {
        this.userName = userName;
        this.password = password;
        this.remoteSocketAdress = ipAdress;

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }

    public SocketAddress getRemoteSocketAdress() {
        return remoteSocketAdress;
    }

    public void setRemoteSocketAdress(SocketAddress remoteSocketAdress) {
        this.remoteSocketAdress = remoteSocketAdress;
    }

    public Long getLastConnectionTimeStamp() {
        return lastConnectionTimeStamp;
    }

    public void setLastConnectionTimeStamp(Long lastConnectionTimeStamp) {
        this.lastConnectionTimeStamp = lastConnectionTimeStamp;
    }

    public Socket getActiveTCPSocket() {
        return activeTCPSocket;
    }

    public void setActiveTCPSocket(Socket activeTCPSocket) {
        this.activeTCPSocket = activeTCPSocket;
    }

    public String getUserServerPort() {
        return userServerPort;
    }

    public void setUserServerPort(String userServerPort) {
        this.userServerPort = userServerPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userName.equals(user.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName);
    }
}
