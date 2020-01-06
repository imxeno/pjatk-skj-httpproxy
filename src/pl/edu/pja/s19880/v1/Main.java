package pl.edu.pja.s19880.v1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class Main {

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket();
        server.bind(new InetSocketAddress(8080));
        while(true) {
            new ConnectionHandler(server.accept());
        }
    }
}
