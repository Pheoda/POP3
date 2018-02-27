package client;

import server.Connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Application extends Connection {

    public Application(InetAddress ia, int port) throws IOException {
        super(new Socket(ia, port));
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Adresse IP serveur : ");
        String ip = sc.nextLine();
        System.out.print("Port serveur : ");
        int port = sc.nextInt();
        try {
            Application c = new Application(InetAddress.getByName(ip), port);
            new Thread(c).start();
        } catch (IOException ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void printEmail() {

    }

    @Override
    public void run() {
        boolean loop = true;

        Scanner sc = new Scanner(System.in);
        while (loop) {
            try {
                System.out.println("Client Started !");
                String cmd = sc.nextLine();
                byte[] data = cmd.getBytes();
                out.write(data);
                out.flush();

                String[] cmd_tab = cmd.split("\\s+");

                switch (cmd_tab[0].toUpperCase()) {
                    case "RETR":
                        System.out.println("retr");
                        printEmail();
                        break;
                    case "QUIT":
                        System.out.println("quit");
                        loop = false;
                        break;
                    default:
                        String[] response = readCommand();
                        System.out.println("default");
                        for (String resp : response) {
                            if (resp.equals("-ERR"))
                                System.out.println("Erreur :");
                            else if (!resp.equals("+OK"))
                                System.out.println(resp);
                        }
                }

            } catch (IOException ex) {
                Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


}
