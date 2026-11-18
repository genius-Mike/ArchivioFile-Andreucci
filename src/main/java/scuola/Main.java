package scuola;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(3000);
        while(true){
            ClientThread cli = new ClientThread(ss.accept());
            cli.start();
        }
        
    }
}