package pl.edu.pja.s19880;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionHandler implements Runnable {
    private final Socket socket;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            this.handle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public void handle() throws Exception {
            System.out.println("Connected");
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            String request = "";
            while(true) {
                int available = input.available();
                if(available > 0) {
                    byte[] bytes = new byte[available];
                    input.read(bytes);
                    request += new String(bytes, StandardCharsets.UTF_8);
                }
                if(request.endsWith("\r\n\r\n")) {
                    proxyRequest(request);
                    request = "";
                }
                Thread.sleep(10);
            }
        }

    private void proxyRequest(String request) throws IOException, InterruptedException {
        List<String> req = Arrays.asList(request.split("\r\n"));
        req.forEach(r -> System.out.println("REQ " + r));
        Map<String, String> headers = new HashMap<>();
        for(int i = 1; i < req.size() ; i++) {
            String r = req.get(i);
            String[] s = r.split(": ", 2);
            headers.put(s[0].toLowerCase(), s[1]);
        }
        headers.forEach((k, v) -> System.out.println("HEAD (" + k + ") " + v));
        Socket s = new Socket();
        s.connect(new InetSocketAddress(InetAddress.getByName(headers.get("host")), 80));
        s.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));
        InputStream input = s.getInputStream();
        StringBuilder proxied = new StringBuilder();
        while(true) {
            int available = input.available();
            if(available > 0) {
                byte[] bytes = new byte[available];
                input.read(bytes);
                proxied.append(new String(bytes, StandardCharsets.UTF_8));
            }
            if(proxied.toString().endsWith("\r\n")) {
                System.out.println("req");
                this.socket.getOutputStream().write(proxied.toString().getBytes(StandardCharsets.UTF_8));
                this.socket.getOutputStream().flush();
                this.socket.close();
                System.out.println("START: " + proxied + " END");
                return;
            }
            Thread.sleep(10);
        }
    }
}
