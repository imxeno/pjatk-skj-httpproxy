package pl.edu.pja.s19880.html;

import pl.edu.pja.s19880.ConnectionHandler;
import pl.edu.pja.s19880.Logger;
import pl.edu.pja.s19880.html.headers.HTTPHeader;
import pl.edu.pja.s19880.html.headers.HTTPHeaderMap;

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
    public static String LINE_END = "\r\n";
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
        String[] arr = raw.toString().split(LINE_END, -1);
        for(int i = 1; i < arr.length; i++) {
            if(!arr[i].equals("")) continue;
            message = arr[0];
            headers = new HTTPHeaderMap(String.join("\r\n", Arrays.copyOfRange(arr, 1, i)));
            if(headers.get("Content-Length") != null) {
                int contentLength = Integer.parseInt(headers.get("Content-Length").value());
                if(arr.length < i + 1) return false;
                body = String.join("\r\n", Arrays.copyOfRange(arr, i + 1, arr.length)).substring(0, contentLength);
                return body.length() == contentLength;
            }
            return true;
        }
        return false;
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
                        if(proxied.headers.containsKey("Content-Type") && proxied.headers.get("Content-Type").value().contains("text/html")) {
                            int headIndex = proxied.body.toLowerCase().indexOf("</head>");
                            proxied.body = proxied.body.substring(0, headIndex) + "<script>" + getUnpleasantWordFilterScript() + "</script>" + proxied.body.substring(headIndex);
                            proxied.headers.put(new HTTPHeader("Content-Length", "" + proxied.body.getBytes(StandardCharsets.UTF_8).length));
                        }
                        byte[] content = proxied.toString().getBytes(StandardCharsets.UTF_8);
                        this.connectionHandler.socket.getOutputStream().write(content);
                        this.connectionHandler.socket.getOutputStream().flush();
                        this.connectionHandler.socket.getOutputStream().close();
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
