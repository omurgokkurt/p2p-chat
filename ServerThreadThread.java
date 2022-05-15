package com.company;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class ServerThreadThread extends Thread{
    private ServerThread serverThread;
    public Socket socket;
    private PrintWriter printWriter;
    private String userInfo;
    private ReentrantLock lock;

    public ServerThreadThread(Socket socket, ServerThread serverThread, String userInfo, ReentrantLock lock) {
        this.serverThread = serverThread;
        this.socket = socket;
        this.userInfo = userInfo;
        this.lock = lock;
    }

    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.printWriter = new PrintWriter(socket.getOutputStream(), true);

            while(true) {
                String message = bufferedReader.readLine();
                if (message == null) {
                    lock.lock();

                    socket.close();
                    serverThread.getclientThreads().remove(this);
                   // System.out.println(userInfo + " left the server");

                    lock.unlock();

                    break;
                } else {
                    serverThread.sendMessage(bufferedReader.readLine());
                }

            }
        } catch (Exception e) {
            lock.lock();

            serverThread.getclientThreads().remove(this);
          //  System.out.println(userInfo + " left the server ERROR");

            lock.unlock();

        }
    }

    public PrintWriter getPrintWriter() {return printWriter;}

    public String getUserInfo() {
        return userInfo;
    }
}
