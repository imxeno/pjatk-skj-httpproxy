package pl.edu.pja.s19880.v2;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2 ||
                (args.length == 2 && !args[1].toLowerCase().equals("light") && !args[1].toLowerCase().equals("heavy"))) {
            System.err.println("java -jar proxy.jar <sciezka do pliku konfiguracyjnego> [light/heavy]");
            System.exit(1);
        }
        String configFileName = args[0];
        ProxyConfig.instance = new ProxyConfig(args.length < 2 || !args[1].toLowerCase().equals("light"), configFileName);
        Logger.log("Załadowano plik konfiguracyjny: " + configFileName);
        ServerSocket server = new ServerSocket();
        Logger.log("Tryb: " + (ProxyConfig.instance.heavyMode ? "HEAVY" : "LIGHT"));
        Logger.log("Nasłuchiwanie na porcie: " + ProxyConfig.instance.port);
        Logger.log("NiEbEzPiEcZnE sLowA: " + Arrays.toString(ProxyConfig.instance.unpleasantWords));
        Logger.log("Folder cache: " + ProxyConfig.instance.cacheDir);
        server.bind(new InetSocketAddress(ProxyConfig.instance.port));
        while (true) {
            new ConnectionHandler(server.accept());
        }
    }
}
