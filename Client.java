package com.company;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Client {
    // ip and ports of the registry server:
    private static final String SERVER_HOSTADRESS = "localhost";
    private static int SERVER_TCPPORT = 1234;
    private static int SERVER_UDPPORT = 1235;
    private Socket socket;
    private DatagramSocket datagramSocket;
    // ip and port for the client:
    private String HOSTADRESS = "localhost";
    private String portNumber;
    public String userName;
    private String password;
    public ServerThread serverThread;
    private boolean isLoggedIn;
    private byte[] buffer = new byte[1024];
    private ReentrantLock lock = new ReentrantLock();
    public List<PeerThread> peerThreads = new ArrayList<PeerThread>();
    public Color colorPicker = new Color();
    public boolean readyToCreateServer = false;
    public BufferedReader bufferedReaderC;
    boolean flag;


    public Client() throws SocketException {
        datagramSocket = new DatagramSocket();

    }

    private void initializeNewSocket() throws IOException {
        this.socket = new Socket(SERVER_HOSTADRESS, SERVER_TCPPORT);


    }

    private void register(String userName, String password) throws IOException {
        initializeNewSocket();

        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            listenForMessage(this.socket, objectOutputStream, objectInputStream);
            Request request = new Request(userName, password, 1, portNumber);
            if (this.socket != null && !this.socket.isClosed() && this.socket.isConnected() && objectOutputStream != null) {

                try {
                    objectOutputStream.writeObject(request);
                    objectOutputStream.flush();
                    this.userName = userName;
                    this.password = password;

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void login(String userName, String password) throws IOException {

        initializeNewSocket();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        listenForMessage(this.socket, objectOutputStream, objectInputStream);
        Request request = new Request(userName, password, 2, portNumber);
        if (this.socket != null && !this.socket.isClosed() && this.socket.isConnected() && objectOutputStream != null) {

            try {
                objectOutputStream.writeObject(request);
                objectOutputStream.flush();
                this.userName = userName;
                this.password = password;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }



    private String searchFor(String user) throws IOException, ClassNotFoundException {
        Log my_log = Log.getInstance();
        my_log.logger.info("Searching for: "+user);
        initializeNewSocket();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        Request request = new Request(user, 3);
        objectOutputStream.writeObject(request);
        Response response = (Response) objectInputStream.readObject();
        return response.getResponseMessage();
    }

    private void listenForMessage(Socket socket, ObjectOutputStream oos, ObjectInputStream ois) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                while (!socket.isClosed() && socket.isConnected()) {
                    try {
                        response = (Response) ois.readObject();

                        if (response.getResponseType() == 1) {
                            if (response.getResponseMessage().equals("ALREADY_REGISTERED")) {
                                System.out.println(Color.red("Username is taken."));
                                Log my_log = Log.getInstance();
                                my_log.logger.info("Server responded ALREADY_REGISTERED, Register failed");
                            } else {
                                System.out.println(Color.green("Register successful."));
                                Log my_log = Log.getInstance();
                                my_log.logger.info("Register successful");
                            }
                            socket.close();
                            readyToCreateServer = true;
                        } else if (response.getResponseType() == 2) {

                            if (response.getResponseMessage().equals("SUCCESSFUL")) {
                                System.out.println(Color.green("Login successful."));
                                Log my_log = Log.getInstance();
                                my_log.logger.info("Login successful.");
                                isLoggedIn = true;
                                readyToCreateServer = true;
                                while (isLoggedIn) {
                                    HelloMessage helloMessage = new HelloMessage();
                                    helloMessage.setUserName(userName);
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                                    objectOutputStream.writeObject(helloMessage);
                                    byte[] objectBuffer = byteArrayOutputStream.toByteArray();
                                    DatagramPacket datagramPacket = new DatagramPacket(objectBuffer, objectBuffer.length, InetAddress.getByName(SERVER_HOSTADRESS), SERVER_UDPPORT);
                                    DatagramPacket receivedDatagramPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(SERVER_HOSTADRESS), SERVER_UDPPORT);
                                    datagramSocket.send(datagramPacket);
                                    datagramSocket.receive(receivedDatagramPacket);
                                    String messageFromServer = new String(receivedDatagramPacket.getData(), 0, receivedDatagramPacket.getLength());
//                                    System.out.println(messageFromServer);
                                    Thread.sleep(6 * 1000);
                                }

                            } else {
                                if (response.getResponseMessage().equals("ALREADY_LOGGED_IN")) {
                                    Log my_log = Log.getInstance();
                                    my_log.logger.info("Server responded ALREADY_LOGGED_IN, Login failed");
                                    System.out.println(Color.red("This account is already logged in."));
                                } else {
                                    System.out.println(Color.red("Invalid username or password."));
                                    Log my_log = Log.getInstance();
                                    my_log.logger.info("Server responded INVALID_USERNAME_OR_PASSWORD, Login failed");
                                }
                                isLoggedIn = false;
                                readyToCreateServer = true;
                                socket.close();
                            }

                        }

                    } catch (EOFException e) {

                    } catch (IOException e) {
                        e.printStackTrace();

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        }).start();
    }

    // Send a request with given type to a user
    public boolean request(String user, String type) throws IOException, ClassNotFoundException {
        String foundUser = searchFor(user); // Request to server. Returns "ip:port" if successful
        if (foundUser.equals("NOT_FOUND")) {
            System.out.println(Color.red("User does not exists. "));
            Log my_log = Log.getInstance();
            my_log.logger.info("User does not exists.");
            return false;
        } else if (foundUser.equals("OFFLINE")) {
            System.out.println(Color.red("User is currently offline."));
            Log my_log = Log.getInstance();
            my_log.logger.info("User is currently offline.");
            return false;
        }


        String ip = "localhost"; // Testing on the local machine
        String port = foundUser.split(":")[1];
        Socket newSocket = null;
        newSocket = new Socket(ip, Integer.valueOf(port));
        PrintWriter out = new PrintWriter(newSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));
        if (type.equals("CHAT_REQUEST")){ // Regular chat request
            out.println(type + "-`-" + userName + "-`-" + HOSTADRESS + "-`-" + portNumber);
            Log my_log = Log.getInstance();
            my_log.logger.info("sent CHAT_REQUEST to " + foundUser);
            System.out.println(Color.yellow("Sent a request to " + foundUser));
        } else if (type.equals("GROUP_INVITE")) { // This is sent by typing "/invite username" while you are already in chat
            out.println(type + "-`-" + userName + "-`-" + HOSTADRESS + "-`-" + portNumber + "-`-" + serverThread.getAllClients());
            Log my_log = Log.getInstance();
            my_log.logger.info("sent GROUP_INVITE to " + foundUser);
            System.out.println(Color.yellow("Sent an invite to " + foundUser));
        }
        String response = in.readLine(); // Waiting until contact responds to our CHAT_REQUEST or GROUP_INVITE

        if (response.split(" ")[0].equals("OK")) { // This is the approving message when neither you nor the contact is in another chat/group
            System.out.println(Color.green(user + " accepted your request"));
            Log my_log = Log.getInstance();
            my_log.logger.info(foundUser + " responds OK");
            new PeerThread(newSocket, this, lock, colorPicker.assignColor()).start();
            return true;
        } else if (response.split(" ")[0].equals("OK_GROUP")) { // This is the approving message when you are not in a conversation with anyone but the contact is.
            System.out.println(Color.green(user + " accepted your request"));
            Log my_log = Log.getInstance();
            my_log.logger.info(foundUser + " responds OK_GROUP");
            new PeerThread(newSocket, this, lock, colorPicker.assignColor()).start();
            // Contact will respond with the information of his/her contact list.
            // Also he/she will send your information to these peers thus everyone will be connected to each other
            String[] userInfo = response.split(" ")[1].split("-");
            for (String s : userInfo) {
                connect(s.split(":")[1], s.split(":")[2]);
            }
            return true;
        } else if (response.split(" ")[0].equals("JOIN")) { // This is the accepting response when you send a GROUP_INVITE with /invite
            Log my_log = Log.getInstance();
            my_log.logger.info(foundUser + " responds JOIN");
            System.out.println(Color.green(user + " accepted your invite"));
            serverThread.sendMessage(user + ":" +ip+":" +port +"-`-" +"2"+"-`-" + "message");
            new PeerThread(newSocket, this, lock, colorPicker.assignColor()).start();
            return true;
        } else {
            System.out.println(Color.red(user +" declined your request."));
            Log my_log = Log.getInstance();
            my_log.logger.info(foundUser + " responds REJECT");
            out.close();
            in.close();
            newSocket.close();
            return false;
        }

    }


    public void updateListenToPeers() throws Exception {
        while (true) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

            if (!serverThread.gettingRequest) System.out.println(Color.blue("Enter contact username:"));
            String input = bufferedReader.readLine();

            if (!serverThread.isGettingRequest()) {
                // request() will return true if request is accepted, this breaks the loop and goes to communicate()
                if (request(input, "CHAT_REQUEST")) break;

            } else if (input.equals("/accept")) {
                Log my_log = Log.getInstance();
                my_log.logger.info("Request accepted.");
                connect(serverThread.getClientHostName(), serverThread.getClientPortNumber());
                serverThread.setResponse("y");
                break;
            } else if (input.equals("/join")) {
                Log my_log = Log.getInstance();
                my_log.logger.info("Request accepted.");
                serverThread.setResponse("y");
                connect(serverThread.getClientHostName(), serverThread.getClientPortNumber());
                String[] userInfo = serverThread.getOthersInfo().split("-");
                for (String s : userInfo) {
                    connect(s.split(":")[1], s.split(":")[2]);
                }

                break;
            }  else {
                Log my_log = Log.getInstance();
                my_log.logger.info("Request rejected.");
                serverThread.setResponse("n");
                serverThread.gettingRequest = false;
            }
        }

        // Wait until serverThread completes establishing connection and stores the required information
        while (serverThread.isGettingRequest()) Thread.sleep(50);
        communicate();
    }

    public void connect(String clientName, String clientPortNumber) {
        try {
            Socket newSocket = new Socket(clientName, Integer.parseInt(clientPortNumber));
            PrintWriter out = new PrintWriter(newSocket.getOutputStream(), true);
            out.println("BYPASS" + "-`-" + userName + "-`-" + HOSTADRESS + "-`-" + portNumber);
            new PeerThread(newSocket, this, lock, colorPicker.assignColor()).start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void communicate() {
        System.out.println("> You can use commands: " + Color.blue("/invite username ") +Color.green( "/remove username ") + Color.blue("/who ") + Color.red("/exit"));
        this.bufferedReaderC = new BufferedReader(new InputStreamReader(System.in));
        flag = true;

        try {
            List<String> others = serverThread.getAllUserNames();

            if (others.size() == 1) System.out.print(Color.blue("Chat with "));
            if (others.size() > 1) System.out.print(Color.blue("Group chat with "));
            System.out.print("[" + Color.ANSI_BLUE + String.join(Color.ANSI_RESET+"] [" + Color.ANSI_BLUE, others) + Color.ANSI_RESET + "]\n");

            Log my_log = Log.getInstance();
            my_log.logger.info("Entered the chat with: " + String.join(", ", others));

            while (flag) {
                if (serverThread.clientThreads.size() == 0) break;
                String message = bufferedReaderC.readLine();
                String[] splitMessage = message.split(" ");

                if (!flag) break;
                if (message.equals("/exit")) {
                    my_log.logger.info("Leaving the chat.");
                    String messageString = userName + "-`-" + "3" + "-`-" +"LEAVING";
                    serverThread.sendMessage(messageString);
                    flag = false;
                    serverThread.inGroupChat = false;

                } else if (message.equals("/who")) {
                    others = serverThread.getAllUserNames();
                    System.out.print("[" + Color.ANSI_BLUE + String.join(Color.ANSI_RESET+"] [" + Color.ANSI_BLUE, others) + Color.ANSI_RESET + "]\n");

                } else if (splitMessage[0].equals("/who") && (splitMessage.length > 1)) {
                    ServerThreadThread serverThreadThread = serverThread.findUser(splitMessage[1]);
                    if (serverThreadThread != null) {
                        String[] userInfo = serverThreadThread.getUserInfo().split(":");
                        System.out.print("[username: " + Color.yellow(userInfo[0]) + " ip: "+ Color.yellow(userInfo[1]) + " port: " + Color.yellow(userInfo[2]) + "]\n");
                    }


                }else if (splitMessage[0].equals("/invite") && (splitMessage.length > 1)) {
                    String messageString = userName + "-`-" + "0" + "-`-" + Color.blue(message);
                    serverThread.sendMessage(messageString);
                    request(splitMessage[1], "GROUP_INVITE");


                } else if (splitMessage[0].equals("/remove") && (splitMessage.length > 1)) {
                    serverThread.sendMessage(splitMessage[1] + "-`-5-`-" + userName, splitMessage[1]);
                    Thread.sleep(100);


                } else if (serverThread.gettingRequest && message.equals("/accept")) {
                    connect(serverThread.getClientHostName(), serverThread.getClientPortNumber());
                    serverThread.setResponse("y");
                    String messageString = userName + "-`-" + "0" + "-`-" + Color.blue(message);
                    serverThread.sendMessage(messageString);

                } else if (serverThread.gettingRequest && message.equals("/reject")) {
                    serverThread.setResponse("n");
                    String messageString = userName + "-`-" + "0" + "-`-" + Color.red(message);
                    serverThread.sendMessage(messageString);

                } else if (!message.isBlank()) {
                    String messageString = userName + "-`-" + "0" + "-`-" + message;
                    serverThread.sendMessage(messageString);
                }
            } this.updateListenToPeers();

        } catch (Exception e) { }

    }


    public static void main(String[] args) throws IOException, InterruptedException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        Client client = new Client();

        System.out.println(Color.blue("\nWelcome to the Chat Program! First register if you are not registered, Then login with your username and password."));
        Log my_log = Log.getInstance();
        my_log.logger.info("Program started.");
        while (true) {

            System.out.println("[1] Login  [2] Register  [3] Exit");
            String choice = bufferedReader.readLine();
            if (choice.equals("1")) {
                System.out.println(Color.blue("Enter username and password:"));

                ServerSocket clientServerSocket = new ServerSocket(0); //portnumber = 0 finds an available port
                client.portNumber = Integer.toString(clientServerSocket.getLocalPort());

                String[] userInfo = bufferedReader.readLine().split(" ");
                client.login(userInfo[0], userInfo[1]);

                while (!client.readyToCreateServer) { Thread.sleep(10);} // Wait until Registry Server responds
                client.readyToCreateServer = false;

                if (client.isLoggedIn) {
                    client.serverThread = new ServerThread(clientServerSocket, client.lock);
                    client.serverThread.start();
                    try {
                        client.updateListenToPeers();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    clientServerSocket.close();
                    continue;
                }


                break;
            } else if (choice.equals("2")) {
                System.out.println(Color.blue("Enter username and password:"));
                String[] userInfo = bufferedReader.readLine().split(" ");
                client.register(userInfo[0], userInfo[1]);
                while (!client.readyToCreateServer) { Thread.sleep(10);} // Wait until Registry Server responds
                client.readyToCreateServer = false;

            } else if(choice.equals("3")){

                System.exit(0);

            }
        }

    }

}
