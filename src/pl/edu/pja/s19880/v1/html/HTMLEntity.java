package pl.edu.pja.s19880.v1.html;

import pl.edu.pja.s19880.v1.ConnectionHandler;
import pl.edu.pja.s19880.v1.Logger;
import pl.edu.pja.s19880.v1.html.headers.HTTPHeader;
import pl.edu.pja.s19880.v1.html.headers.HTTPHeaderMap;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class HTMLEntity {
    public static final String LINE_END = "\r\n";
    public static final String HEADER_SEPARATOR = LINE_END + LINE_END;
    private final ConnectionHandler connectionHandler;
    private StringBuilder raw = new StringBuilder();
    public String message;
    private HTTPHeaderMap headers;
    private String body;

    public HTMLEntity(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public boolean append(String data) {
        raw.append(data);
        return true;
    }

    public boolean parse() {
        try {
            String[] payload = raw.toString().split(HEADER_SEPARATOR, 2);
            if (payload.length < 1) return false;
            String[] headerPayload = payload[0].split(LINE_END);
            if (headerPayload.length < 2) return false;
            message = headerPayload[0];
            headers = new HTTPHeaderMap(Arrays.copyOfRange(headerPayload, 1, headerPayload.length));
            if (headers.get("Content-Length") != null) {
                int contentLength = Integer.parseInt(headers.get("Content-Length").value());
                if(payload.length < 2) return false;
                if(payload[1].length() < contentLength) return false;
                body = payload[1].substring(0, contentLength);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append(LINE_END)
                .append(headers.toString()).append(LINE_END).append(body);
        return sb.toString();
    }

    public void process() {
        Logger.log("(REQ) " + this.message);
        String lowerCaseMethod = message.split(" ")[0].toLowerCase();
        headers.put(new HTTPHeader("Accept-Encoding", "identity")); // we prefer plaintext
        headers.remove("If-None-Match"); // we don't need caches
        headers.remove("If-Modified-Since"); // we don't need caches
        try {
            if(!lowerCaseMethod.equals("get") && !lowerCaseMethod.equals("post")) {
                this.connectionHandler.socket.close();
                return;
            }
            Socket s = new Socket();
            s.connect(new InetSocketAddress(InetAddress.getByName(headers.get("host").value()), 80));
            s.getOutputStream().write(this.toString().getBytes(StandardCharsets.UTF_8));
            InputStream input = s.getInputStream();
            HTMLEntity proxied = new HTMLEntity(null);
            Logger.log("(PRX) Connected socket!");
            while(true) {
                int available = input.available();
                if(available > 0) {
                    byte[] bytes = new byte[available];
                    input.read(bytes);
                    proxied.append(new String(bytes, StandardCharsets.UTF_8));
                    Logger.log("(PRX) Reading bytes");
                    if (proxied.parse()) {
                        Logger.log("(RES) " + proxied.message);
                        proxied.headers.put(new HTTPHeader("X-Proxy-Author", "Piotr Adamczyk | s19880"));
                        proxied.headers.put(new HTTPHeader("X-This-Proxy", "is very offensive to me 🎅"));
                        if(proxied.headers.containsKey("Content-Type") && proxied.headers.get("Content-Type").value().contains("text/html")) {
                            int headIndex = proxied.body.toLowerCase().indexOf("</head>");
                            proxied.body = proxied.body.substring(0, headIndex) + "<script>" + getUnpleasantWordFilterScript() + "</script>" + proxied.body.substring(headIndex);
                            proxied.headers.put(new HTTPHeader("Content-Length", "" + proxied.body.getBytes(StandardCharsets.UTF_8).length));
                        }
                        byte[] content = proxied.toString().getBytes(StandardCharsets.UTF_8);
                        this.connectionHandler.socket.getOutputStream().write(content);
                        this.connectionHandler.socket.getOutputStream().flush();
                        s.close();
                        return;
                    }
                }
                Thread.sleep(10);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getUnpleasantWordFilterScript() {
        try {
            return new String(Files.readAllBytes(Paths.get("src/pl/edu/pja/s19880/unpleasantWordFilter.js")));
        } catch (IOException e) {
            return "alert('error!');";
        }
    }
}
