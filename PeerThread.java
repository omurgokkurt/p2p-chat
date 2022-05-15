package com.company;

import java.io.*;
import java.net.Socket;
import java.util.Calendar;
import java.util.Formatter;
import java.util.concurrent.locks.ReentrantLock;

public class PeerThread extends Thread {
    private BufferedReader bufferedReader;
    private Client peer;
    public Socket socket;
    private ReentrantLock lock;
    private String color;



    public PeerThread(Socket socket, Client peer, ReentrantLock lock, String color) throws IOException {
        this.socket = socket;
        bufferedReader = new BufferedReader((new InputStreamReader(socket.getInputStream())));
        this.peer = peer;
        this.lock = lock;
        peer.peerThreads.add(this);
        this.color = color;

    }



    public void run() {
        boolean flag = true;
        while(flag) {
            try {
                String[] messageInfo = bufferedReader.readLine().split("-`-");
                String username = messageInfo[0];
                String type = messageInfo[1];
                String message = messageInfo[2];
                if (type.equals("0")) {

                    Formatter time = new Formatter();
                    Calendar gfg_calender = Calendar.getInstance();
                    time.format("%tl:%tM", gfg_calender, gfg_calender);
                    System.out.println("["+time+"] " + "["+color+username+Color.ANSI_RESET+"]: " +message);

                    Log my_log = Log.getInstance();
                    my_log.logger.info("["+time+"] " + "["+username+"]: "+message);

                } else if(type.equals("1")) {
                    System.out.println(Color.yellow(username + " wants to join the chat"));
                    Log my_log = Log.getInstance();
                    my_log.logger.info(username + " wants to join the chat");
                } else if(type.equals("2") && !messageInfo[0].split(":")[0].equals(peer.userName)) {
                    String[] userInfo = messageInfo[0].split(":");
                    peer.connect(userInfo[1], userInfo[2]);
                    System.out.println(Color.blue(userInfo[0] + " joined the group"));
                    Log my_log = Log.getInstance();
                    my_log.logger.info(userInfo[0] + " joined the group");

                } else if(type.equals("3")) {
                    System.out.println(Color.red(messageInfo[0] + " left the chat"));
                    Log my_log = Log.getInstance();
                    my_log.logger.info(messageInfo[0] + " left the chat");
                    peer.serverThread.sendMessage(messageInfo[0] + "-`-4-`-" + "LEAVE", messageInfo[0]);
                    flag = false;
                    this.socket.close();

                } else if(type.equals("4") && messageInfo[0].equals(peer.userName)) {
                    flag = false;
                    this.socket.close();


                } else if(type.equals("5")) {
                    if (messageInfo[0].equals(peer.userName)) {
                        Log my_log = Log.getInstance();
                        my_log.logger.info("You have been removed from the chat.");
                        System.out.println(Color.red("You have been removed from the chat. Press " +Color.blue("Enter ") + Color.red("to continue...")));
                        String messageString = peer.userName + "-`-" + "3" + "-`-" +" LEAVING.";
                        peer.serverThread.sendMessage(messageString);
                        peer.serverThread.inGroupChat = false;
                        peer.flag = false;

                    }
                }

            } catch (Exception e){
              //  System.out.println("Peer thread terminated error");
                break;
            }
        }
       // System.out.println("Peer terminated");

    }



}
