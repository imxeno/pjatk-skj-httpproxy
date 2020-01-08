package pl.edu.pja.s19880.v2;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Pierwszym argumentem powinna być ścieżka do pliku konfiguracyjnego.");
            System.exit(1);
        }
        String configFileName = args[0];
        ProxyConfig.instance = new ProxyConfig(configFileName);
        Logger.log("Załadowano plik konfiguracyjny: " + configFileName);
        ServerSocket server = new ServerSocket();
        Logger.log("Nasłuchiwanie na porcie: " + ProxyConfig.instance.port);
        Logger.log("NiEbEzPiEcZnE sLowA: " + Arrays.toString(ProxyConfig.instance.unpleasantWords));
        Logger.log("Folder cache: " + ProxyConfig.instance.cacheDir);
        server.bind(new InetSocketAddress(ProxyConfig.instance.port));
        while (true) {
            new ConnectionHandler(server.accept());
        }
    }
}
