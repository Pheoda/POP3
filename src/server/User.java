package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class User {
    private String username;
    private String password;

    private int nbMessages = 0;

    private BufferedReader reader;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        try {
            this.reader = new BufferedReader(new FileReader(this.username + ".txt"));
            String line;
            while((line = reader.readLine()) != null) {
                // Stocker string line dans Messages[nbMessages] pour les restituer ensuite
                if(line == ".\n")
                    nbMessages++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getNbMessages() {
        return nbMessages;
    }

    public boolean userExists(String username, String password) {
        // Password needs to be MD5 in POP3S !
        return this.username.equals(username) && this.password.equals(password);
    }

}