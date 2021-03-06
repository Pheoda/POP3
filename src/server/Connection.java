package server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Connection implements Runnable {

    public final static int CR = 13;
    public final static int LF = 10;

    boolean run = true;

    private User[] listUsers = {
            new User("user", "pass"),
            new User("user2", "pass")
    };

    private User currentUser = null;

    public enum State {AUTHORIZATION, TRANSACTION}

    ;

    protected Socket socket;
    protected InputStream in;
    protected OutputStream out;
    protected BufferedInputStream bufIn;

    protected State state;

    public Connection(Socket connexion) {
        socket = connexion;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            bufIn = new BufferedInputStream(in);
            state = State.AUTHORIZATION;
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected String[] readCommand() {
        int character = -1;
        boolean end = false, crReceived = false;
        String request = "";
        do {
            try {
                character = bufIn.read();

                request += (char) character;

                end = crReceived && character == LF;

                crReceived = (character == CR);

            } catch (IOException e) {
                run = false;
            }
        } while (character != -1 && !end);

        return request.split("\\s+");
    }


    private void sendMessage(String message) {
        try {
            out.write((message + "\r\n").getBytes());
            System.out.println("Said : " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendError() {
        this.sendMessage("-ERR commande impossible");
    }

    @Override
    public void run() {
        sendMessage("+OK POP3 server ready");
        while (run) {
            String[] clientMessage = readCommand();
            System.err.print("Received : ");
            for (int i = 0; i < clientMessage.length; i++)
                System.err.print(clientMessage[i] + " ");
            System.err.println();
            if (clientMessage.length > 0) {
                String command = clientMessage[0].toUpperCase();

                switch (command) {
                    case "QUIT":
                        // Destroy object
                        sendMessage("+OK client closing");
                        run = false;
                        break;
                    case "APOP":
                        // Check list users
                        if (state == State.AUTHORIZATION) {
                            if (clientMessage.length == 3) {
                                for (User u : listUsers) {
                                    if (u.userExists(clientMessage[1], clientMessage[2])) {
                                        currentUser = u;
                                        break;
                                    }
                                }
                                // User reconnu ou non
                                if (currentUser != null) {
                                    state = State.TRANSACTION;
                                    sendMessage("+OK user connected");
                                } else
                                    sendMessage("-ERR user or password false");
                            } else
                                sendMessage("-ERR wrong number of parameters (" + clientMessage.length + ")");
                        } else {
                            sendError();
                        }
                        break;
                    case "STAT":
                        if (state == State.TRANSACTION) {
                            sendMessage("+OK " + currentUser.getNbMessages() + " " + currentUser.getSizeMessage());
                        } else {
                            sendError();
                        }
                        break;
                    case "RETR":
                        if (state == State.TRANSACTION) {
                            if (clientMessage.length > 1) {
                                int messageNumber = Integer.parseInt(clientMessage[1]);

                                if (messageNumber < 0 || messageNumber >= currentUser.getNbMessages()) {
                                    sendMessage("-ERR message " + messageNumber + " doesn't exist");
                                } else {
                                    sendMessage("+OK " + currentUser.getSizeMessage(messageNumber));
                                    sendMessage(currentUser.getMessage(messageNumber));
                                }

                            } else
                                sendMessage("-ERR wrong number of parameters (" + clientMessage.length + ")");
                        } else {
                            sendError();
                        }
                        break;
                    default:
                        sendError();
                }
            }
        }
    }
}
