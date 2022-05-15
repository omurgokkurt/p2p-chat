package com.company;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientHandler implements Runnable {

    private static List<User> registeredUsers = new ArrayList<>();
    public static  final String NOTFOUNDMESSAGE="NOT_FOUND";
    public static final  String UNAUTHORIZEDMESSAGE="UNAUTHORIZED";
    public static final String OFFLINEMESSAGE="OFFLINE";
    public static final long TIMEOUT= 20*1000;
    private User user;
    private Socket socket;
    private DatagramPacket datagramPacket;
    private ObjectOutputStream objectOutputStream;
    private DatagramSocket datagramSocket;


    public ClientHandler(Socket socket) {

        this.socket = socket;

        try {
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ClientHandler (DatagramPacket datagramPacket, DatagramSocket datagramSocket){

        this.datagramPacket=datagramPacket;
        this.datagramSocket=datagramSocket;

    }

    //search user by username in static arraylist
    private int lookForUser(String userName) {

        for (User registeredUser : ClientHandler.registeredUsers) {
            System.out.println(ClientHandler.registeredUsers.indexOf(registeredUser));
            if (registeredUser.getUserName().equals(userName)) {
                return ClientHandler.registeredUsers.indexOf(registeredUser);
            }
        }
        return -1;

    }

    @Override
    public void run() {
        if (this.socket!=null&&this.datagramPacket==null) {
            // get the input stream from the connected socket
            InputStream inputStream = null;
            try {
                inputStream = socket.getInputStream();

                // create a DataInputStream so we can read data from it.
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                // read the list of messages from the socket
                Request request = (Request) objectInputStream.readObject();

                if (request != null) {

                    if (request.getRequestType() == 1) { registerProcess(request);}
                    else if (request.getRequestType() == 2) { loginProcess(request);}
                    else if (request.getRequestType() == 3) { search(request.getSearchedUser());}

                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }else if(this.socket==null&&this.datagramPacket!=null){

            byte[] data=datagramPacket.getData();

            ByteArrayInputStream in=new ByteArrayInputStream(data);
            InetAddress ipAdress=datagramPacket.getAddress();
            int port=datagramPacket.getPort();
            try {
                ObjectInputStream is=new ObjectInputStream(in);
                HelloMessage helloMessage=(HelloMessage) is.readObject();
                int userIndex = lookForUser(helloMessage.getUserName());
                if (userIndex==-1){

                    byte[] response=NOTFOUNDMESSAGE.getBytes(StandardCharsets.UTF_8);
                    datagramPacket=new DatagramPacket(response,response.length,ipAdress,port);
                    datagramSocket.send(datagramPacket);

                }
                else if(!ClientHandler.registeredUsers.get(userIndex).getOnline()){
                    byte[] response=UNAUTHORIZEDMESSAGE.getBytes(StandardCharsets.UTF_8);
                    datagramPacket=new DatagramPacket(response,response.length,ipAdress,port);
                    datagramSocket.send(datagramPacket);
                }
                else{
                    System.out.println("User: "+helloMessage.getUserName()+" says hello!");
                    Log my_log = Log.getInstance();
                    my_log.logger.info("User: "+helloMessage.getUserName()+" says hello!");
                    ClientHandler.registeredUsers.get(userIndex).setLastConnectionTimeStamp(new Date().getTime());
                    byte[] response="HELLO".getBytes(StandardCharsets.UTF_8);
                    datagramPacket=new DatagramPacket(response,response.length,ipAdress,port);
                    datagramSocket.send(datagramPacket);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    private void search(String userName) throws IOException {
        int userIndex = lookForUser(userName);
        Response response = new Response();
        response.setResponseType(3);
        if (userIndex == -1) {
            response.setResponseMessage(NOTFOUNDMESSAGE);
        } else if (!ClientHandler.registeredUsers.get(lookForUser(userName)).getOnline()) {
            response.setResponseMessage(OFFLINEMESSAGE);
        } else {
            String ip = ClientHandler.registeredUsers.get(lookForUser(userName)).getRemoteSocketAdress().toString().split(":")[0];
            String serverPortNumber = ClientHandler.registeredUsers.get(lookForUser(userName)).getUserServerPort();
            response.setResponseMessage(ip + ":" + serverPortNumber);
        }
        objectOutputStream.writeObject(response);
    }


    private void loginProcess(Request request) {

        int userIndex=lookForUser(request.getUserName());

        if (userIndex!=-1 && ClientHandler.registeredUsers.get(userIndex).getPassword().equals(request.getPassword()) && !ClientHandler.registeredUsers.get(userIndex).getOnline()){

            ClientHandler.registeredUsers.get(userIndex).setOnline(true);
            ClientHandler.registeredUsers.get(userIndex).setLastConnectionTimeStamp(new Date().getTime());
            ClientHandler.registeredUsers.get(userIndex).setActiveTCPSocket(socket);
            ClientHandler.registeredUsers.get(userIndex).setUserServerPort(request.getClientServerPortNumber());
            System.out.println(Color.green("User : "+ request.getUserName()+" is online now!"));
            Log my_log = Log.getInstance();
            my_log.logger.info("User : "+ request.getUserName()+" is online now!");
            Response response=new Response();
            response.setRequestId(request.getId());
            response.setResponseMessage("SUCCESSFUL");
            response.setResponseType(2);
            System.out.println(Color.green("User:"+request.getUserName()+" logged in."));
            my_log.logger.info("User:"+request.getUserName()+" logged in.");

            try {
                objectOutputStream.writeObject(response);
                objectOutputStream.flush();


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(ClientHandler.registeredUsers.get(userIndex).getActiveTCPSocket().isConnected()){
                            try {
                                Thread.sleep(TIMEOUT);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (ClientHandler.registeredUsers.get(userIndex).getOnline()&&
                                    new Date().getTime()-ClientHandler.registeredUsers.get(userIndex).getLastConnectionTimeStamp()>TIMEOUT){
                                ClientHandler.registeredUsers.get(userIndex).setOnline(false);
                                System.out.println(Color.red("User "+ClientHandler.registeredUsers.get(userIndex).getUserName()+" turned offline due to timeout"));
                                Log my_log = Log.getInstance();
                                my_log.logger.info("User "+ClientHandler.registeredUsers.get(userIndex).getUserName()+" turned offline due to timeout");
                                try {
                                    socket.close();
                                    break;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }
                }).start();


            } catch (IOException  e) {
                e.printStackTrace();
            }

        } else if (userIndex!=-1 && ClientHandler.registeredUsers.get(userIndex).getPassword().equals(request.getPassword()) && ClientHandler.registeredUsers.get(userIndex).getOnline()) {
            Response response=new Response();
            response.setRequestId(request.getId());
            response.setResponseType(2);
            response.setResponseMessage("ALREADY_LOGGED_IN");
            Log my_log = Log.getInstance();
            my_log.logger.info("ALREADY_LOGGED_IN");
            try {
                objectOutputStream.writeObject(response);
                objectOutputStream.flush();
                objectOutputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            Response response=new Response();
            response.setRequestId(request.getId());
            response.setResponseType(2);
            response.setResponseMessage("INVALID_USERNAME_OR_PASSWORD");
            Log my_log = Log.getInstance();
            my_log.logger.info("INVALID_USERNAME_OR_PASSWORD");
            try {
                objectOutputStream.writeObject(response);
                objectOutputStream.flush();
                objectOutputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void registerProcess(Request request) {

        int userIndex = lookForUser(request.getUserName());

        //if user is already registered:
        if (userIndex != -1) {

            if (this.socket != null && this.objectOutputStream != null) {

                try {

                    Response response = new Response();
                    response.setResponseType(1);
                    response.setRequestId(request.getId());
                    response.setResponseMessage("ALREADY_REGISTERED");
                    Log my_log = Log.getInstance();
                    my_log.logger.info("ALREADY_REGISTERED");
                    objectOutputStream.writeObject(response);
                    objectOutputStream.flush();
                    objectOutputStream.close();
                    socket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {

            User newUser = new User(request.getUserName(), request.getPassword(), this.socket.getRemoteSocketAddress());
            ClientHandler.registeredUsers.add(newUser);
            Response response = new Response();
            response.setRequestId(request.getId());
            response.setResponseType(1);
            response.setResponseMessage("SUCCESSFUL");
            try {
                objectOutputStream.writeObject(response);
                objectOutputStream.flush();
                objectOutputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("new user:"+request.getUserName()+" registered!");
            Log my_log = Log.getInstance();
            my_log.logger.info("new user:"+request.getUserName()+" registered!");
        }
    }
}
