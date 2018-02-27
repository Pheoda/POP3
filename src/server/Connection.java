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

    private User[] listUsers = {
            new User("user", "pass"),
            new User("user2", "pass")
    };

    private User currentUser = null;

    public enum State {AUTHORIZATION, TRANSACTION};

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

            sendMessage("+OK POP3 server ready");

            state  = State.AUTHORIZATION;
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
                e.printStackTrace();
            }
        }while(character != -1 && !end);

        return request.split("\\s+");
    }


    private void sendMessage(String message) {
        try {
            out.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        boolean run = true;
        while(run) {
            String[] clientMessage = readCommand();
            System.out.println("received=");
            for(int i = 0; i < clientMessage.length; i++)
                System.out.println(clientMessage[i]);

            if(clientMessage.length > 0) {
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
                                }
                                else
                                    sendMessage("-ERR user or password false");
                            }
                            else
                                sendMessage("-ERR wrong number of parameters (" + clientMessage.length + ")");
                        }
                        break;
                    case "STAT":
                        if (state == State.TRANSACTION) {
                            sendMessage("+OK " +  currentUser.getNbMessages() + " " + currentUser.getSizeMessage());
                        }
                        break;
                    case "RETR":
                        if (state == State.TRANSACTION) {
                            if(clientMessage.length > 1) {
                                // Gérer les NumberFormatException lors du parsing !!
                                int messageNumber = Integer.parseInt(clientMessage[1]);

                                if(messageNumber < 0 || messageNumber >= currentUser.getNbMessages()) {
                                    sendMessage("-ERR message " + messageNumber + " doesn't exist");
                                }
                                else {
                                    sendMessage("+OK " + currentUser.getSizeMessage(messageNumber));
                                    sendMessage(currentUser.getMessage(messageNumber));
                                    sendMessage(".\r\n");
                                }

                            }
                            else
                                sendMessage("-ERR wrong number of parameters (" + clientMessage.length + ")");
                        }
                        break;
                }
            }
        }
    }
}
