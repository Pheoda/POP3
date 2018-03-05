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
        String [] str_tab;
        boolean mailFinished = false;
        do {
            str_tab = readCommand();
            if (str_tab.length != 0) {
                for (String s : str_tab) {
                    System.out.print(s + " ");
                }
                System.out.println();
            }
            if(str_tab.length > 0)
                mailFinished = str_tab[0].equals(".");
        } while (!mailFinished);
    }

    @Override
    public void run() {
        boolean loop = true;

        Scanner sc = new Scanner(System.in);
        while (loop) {
            String[] response = readCommand();
            for (String re : response)
                System.out.print(re + " ");

            try {
                String cmd = sc.nextLine();
                byte[] data = (cmd+"\r\n").getBytes();
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
                        break;
                }

            } catch (IOException ex) {
                Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}