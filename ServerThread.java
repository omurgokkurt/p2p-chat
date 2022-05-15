package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ServerThread extends Thread {
    private  ServerSocket serverSocket;
    public List<ServerThreadThread> clientThreads = new ArrayList<ServerThreadThread>();


    private ReentrantLock lock;


    boolean gettingRequest = false;
    private String response = "wait";
    private String clientUserName;
    private String clientHostName;
    private String clientPortNumber;
    private String othersInfo;
    public boolean inGroupChat = false;
    private List<String> clients = new ArrayList<String>();

    public ServerThread(ServerSocket serverSocket, ReentrantLock lock) throws IOException {
        this.serverSocket = serverSocket;
        this.lock = lock;
    }

    public void run() {
        try {
            while (true) {
                Socket incomingRequest = serverSocket.accept();
                gettingRequest = true;
                PrintWriter out = new PrintWriter(incomingRequest.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(incomingRequest.getInputStream()));

                String requestString = in.readLine();
                String[] request = requestString.split("-`-");

                if (request[0].equals("CHAT_REQUEST")) {
                    System.out.println(Color.yellow("> " + request[1] +" sent a chat request.\n") + Color.blue("/accept") +" or "+ Color.red("/reject"));
                    Log my_log = Log.getInstance();
                    my_log.logger.info(request[1] +" sent a chat request.");
                    this.clientUserName = request[1];
                    sendMessage(clientUserName + "-`-" +"1"+"-`-" + "message");
                    this.clientHostName = request[2];
                    this.clientPortNumber = request[3];
                    while(response.equals("wait")) {this.sleep(20);}
                    if(response.equals("y")) {
                        if (clientThreads.size() == 0) {
                            out.println("OK 1");

                        } else {
                            out.println("OK_GROUP "+ getAllClients());
                            sendMessage(clientUserName + ":" +clientHostName+":" +clientPortNumber +"-`-" +"2"+"-`-" + "message");
                        }

                        ServerThreadThread serverThreadThread = new ServerThreadThread(incomingRequest, this, request[1]+":"+request[2]+":"+request[3], lock);
                        clientThreads.add(serverThreadThread);
                        serverThreadThread.start();
                        this.gettingRequest = false;
                        this.response = "wait";


                        this.clientUserName = "";
                        this.clientHostName = "";
                        this.clientPortNumber = "";
                    } else {
                        out.println("REJECT");
                        this.response = "wait";
                        this.gettingRequest = false;
                        this.clientUserName = "";
                        this.clientHostName = "";
                        this.clientPortNumber = "";
                        incomingRequest.close();

                    }

                } else if (request[0].equals("GROUP_INVITE") && !inGroupChat) {
                    System.out.println(Color.yellow("> " + request[1] +" invited you to a group chat.\n") + Color.blue("/join") +" or "+ Color.red("/reject"));
                    Log my_log = Log.getInstance();
                    my_log.logger.info(request[1] +" sent a group invite.");
                    this.clientUserName = request[1];
                    this.clientHostName = request[2];
                    this.clientPortNumber = request[3];
                    this.othersInfo = request[4];
                    while(response.equals("wait")) {this.sleep(20);}
                    if(response.equals("y")) {
                        out.println("JOIN");
                        ServerThreadThread serverThreadThread = new ServerThreadThread(incomingRequest, this, request[1]+":"+request[2]+":"+request[3], lock);
                        clientThreads.add(serverThreadThread);
                        serverThreadThread.start();
                        this.gettingRequest = false;
                        this.response = "wait";
                        this.clientUserName = "";
                        this.clientHostName = "";
                        this.clientPortNumber = "";
                        this.othersInfo = "";
                    } else {
                        out.println("REJECT");
                        this.response = "wait";
                        this.gettingRequest = false;
                        this.clientUserName = "";
                        this.clientHostName = "";
                        this.clientPortNumber = "";
                        this.othersInfo = "";
                        incomingRequest.close();
                    }

                } else if (request[0].equals("ACCEPT") || request[0].equals("BYPASS")) {
                    ServerThreadThread serverThreadThread = new ServerThreadThread(incomingRequest, this, request[1]+":"+request[2]+":"+request[3], lock);
                    clientThreads.add(serverThreadThread);
                    serverThreadThread.start();
                    gettingRequest = false;

                }  else {
                    out.println("REJECT");
                    this.response = "wait";
                    this.gettingRequest = false;
                    this.clientUserName = "";
                    this.clientHostName = "";
                    this.clientPortNumber = "";
                    this.othersInfo = "";
                    incomingRequest.close();
                }

                if (getAllUserNames().size() > 1) {
                    this.inGroupChat = true;
                } else {
                    inGroupChat = false;
                }
            }
        } catch (Exception e) {e.printStackTrace(); }
    }

    String getAllClients()                              {
        lock.lock();
        List<String> allClients = new ArrayList<>();
        for (ServerThreadThread t : clientThreads)      {
            allClients.add(t.getUserInfo());
                                                        }
        lock.unlock();
        return String.join("-", allClients);   }
    ServerThreadThread findUser(String username)        {
        lock.lock();
        for (ServerThreadThread t : clientThreads)      {
            if(t.getUserInfo().equals(username))        {
                lock.unlock();
                return t;
                                                        }
                                                        }
        lock.unlock();
        return null;
                                                        }

    List<String> getAllUserNames() {
        lock.lock();
        List<String> allClients = new ArrayList<String>();
        for (ServerThreadThread t : clientThreads) {
            allClients.add(t.getUserInfo().split(":")[0]);
        }
        lock.unlock();
        return allClients;
    }

    void sendMessage(String message) {
        lock.lock();
        try {

            for (Iterator<ServerThreadThread> iterator = clientThreads.iterator(); iterator.hasNext();) {
                ServerThreadThread t = iterator.next();
                t.getPrintWriter().println(message);
            }
        } catch(Exception e) {e.printStackTrace();}
        lock.unlock();
    }

    void sendMessage(String message, String username) {
        lock.lock();
        ServerThreadThread t = findUser(username);
        if (t != null) t.getPrintWriter().println(message);
        lock.unlock();
    }

    public boolean isGettingRequest() {
        return gettingRequest;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getOthersInfo() {
        return othersInfo;
    }

    public String getClientHostName() {
        return clientHostName;
    }

    public String getClientPortNumber() {
        return clientPortNumber;
    }

    public List<ServerThreadThread> getclientThreads() { return clientThreads;}

}
