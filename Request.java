package com.company;

import java.io.Serializable;

public class Request implements Serializable {

    private long id;
    private String userName;
    private String password;
    private String clientServerPortNumber;
    private String searchedUser;

    /*
    * 1-register
    * 2-login
    * 3-hello
    * 4-message
    * */
    private int requestType;



    public Request(String userName, String password,int requestType, String clientServerPortNumber) {
        this.userName = userName;
        this.password = password;
        this.requestType=requestType;
        this.clientServerPortNumber = clientServerPortNumber;

    }
    public Request(String userName, int requestType) {
        this.requestType=requestType;
        this.searchedUser = userName;

    }

    public String getSearchedUser() {
        return searchedUser;
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

    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public long getId() {
        return id;
    }

    public String getClientServerPortNumber() {
        return clientServerPortNumber;
    }
}
