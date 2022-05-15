package com.company;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class CentralRegistryServer {


    private ServerSocket serverSocket;
    private DatagramSocket datagramSocket;
    private byte[] buffer=new byte[1024];


    public CentralRegistryServer(ServerSocket serverSocket,DatagramSocket datagramSocket){

        this.serverSocket=serverSocket;
        this.datagramSocket=datagramSocket;

    }

    public void startRegistryServer(){

        try {

            while(!serverSocket.isClosed()){

                System.out.println("Server is ready to accept TCP connections!");
                Log my_log = Log.getInstance();
                my_log.logger.info("Server is ready to accept TCP connections!");
                Socket socket=serverSocket.accept();
                System.out.println("A new client has connected!");
                my_log.logger.info("A new client has connected!");

                ClientHandler ch=new ClientHandler(socket);

                Thread thread= new Thread(ch);

                thread.start();



            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startUDPServer(){

        System.out.println("Server is ready for UDP messages!");
        Log my_log = Log.getInstance();
        my_log.logger.info("Server is ready for UDP messages!");
        while (true){

            try {
                DatagramPacket datagramPacket=new DatagramPacket(buffer,buffer.length);
                datagramSocket.receive(datagramPacket);
                ClientHandler clientHandler=new ClientHandler(datagramPacket,datagramSocket);
                Thread thread=new Thread(clientHandler);
                thread.start();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void closeServerSocket(){

        if (this.serverSocket!=null){

            try {
                this.serverSocket.close();
                System.out.println("Server socket closed!");
                Log my_log = Log.getInstance();
                my_log.logger.info("Server socket closed!");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    public static void main(String args[]) throws IOException {

        ServerSocket serverSocket=new ServerSocket(1234);
        DatagramSocket datagramSocket=new DatagramSocket(1235);
        CentralRegistryServer centralRegistryServer=new CentralRegistryServer(serverSocket,datagramSocket);

        new Thread(new Runnable() {
            @Override
            public void run() {
                centralRegistryServer.startRegistryServer();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                centralRegistryServer.startUDPServer();
            }
        }).start();

    }


}
