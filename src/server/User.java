package server;

public class User {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean userExists(String username, String password) {
        // Password needs to be MD5 !
        return this.username == username && this.password == password;
    }

}