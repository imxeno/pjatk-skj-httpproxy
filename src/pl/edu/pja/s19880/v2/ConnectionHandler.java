package pl.edu.pja.s19880.v2;

import pl.edu.pja.s19880.v2.headers.HTTPHeader;
import pl.edu.pja.s19880.v2.headers.HTTPHeaderMap;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ConnectionHandler implements Runnable {
    private final Socket socket;
    private final DataInputStream input;
    private final OutputStream output;

    public ConnectionHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.output = socket.getOutputStream();
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                HTTPEntity entity = parseHTTP(input);
                Logger.log(entity.getMessage());
                if(entity.getMessage().toLowerCase().contains("connect")) {
                    socket.close();
                    return;
                }
                proxy(entity);
                socket.close();
                return;
            }
        } catch (IOException | InterruptedException e) {
            if(e.getMessage().contains("Stream closed")) return;
            e.printStackTrace();
        }
    }
    public static HTTPEntity parseHTTP(DataInputStream stream) throws IOException, InterruptedException {
        System.out.println(stream.available());
        String message = stream.readLine();
        HTTPHeaderMap headers = new HTTPHeaderMap();
        String temp = stream.readLine();
        while(temp != null && !temp.isEmpty()) {
            headers.put(new HTTPHeader(temp));
            temp = stream.readLine();
        }
        byte[] body = new byte[0];
        HTTPHeader contentLength = headers.get("Content-Length");
        if(contentLength != null) {
            int contentLengthValue = Integer.parseInt(contentLength.value());
            while(stream.available() < contentLengthValue) {
                Thread.sleep(10);
            }
            body = new byte[contentLengthValue];
            stream.read(body);
        }
        return new HTTPEntity(message, headers, body);
    }

    private void proxy(HTTPEntity httpEntity) throws InterruptedException {
        String message = httpEntity.getMessage();
        Logger.log("(REQ) " + message);
        String lowerCaseMethod = message.split(" ")[0].toLowerCase();
        httpEntity.getHeaders().put(new HTTPHeader("Accept-Encoding", "identity")); // we prefer plaintext
        httpEntity.getHeaders().remove("If-None-Match"); // we don't need caches
        httpEntity.getHeaders().remove("If-Modified-Since"); // we don't need caches
        try {
            if(!lowerCaseMethod.equals("get") && !lowerCaseMethod.equals("post")) {
                socket.close();
                return;
            }
            Socket s = new Socket();
            s.connect(new InetSocketAddress(InetAddress.getByName(httpEntity.getHeaders().get("host").value()), 80));
            Logger.log("(PRX) Connected socket!");
            s.getOutputStream().write(httpEntity.getBytes());
            HTTPEntity proxied = parseHTTP(new DataInputStream(new BufferedInputStream(s.getInputStream())));
            Logger.log("(RES) " + proxied.getMessage());
            proxied.getHeaders().put(new HTTPHeader("X-Proxy-Author", "Piotr Adamczyk | s19880"));
            proxied.getHeaders().put(new HTTPHeader("X-This-Proxy", "is very offensive to me 🎅"));
            /*if(proxied.getHeaders().containsKey("Content-Type") && proxied.getHeaders().get("Content-Type").value().contains("text/html")) {
                int headIndex = proxied.body.toLowerCase().indexOf("</head>");
                proxied.body = proxied.body.substring(0, headIndex) + "<script>" + getUnpleasantWordFilterScript() + "</script>" + proxied.body.substring(headIndex);
                proxied.headers.put(new pl.edu.pja.s19880.v1.html.headers.HTTPHeader("Content-Length", "" + proxied.body.getBytes(StandardCharsets.UTF_8).length));
            }
            byte[] content = proxied.toString().getBytes(StandardCharsets.UTF_8);*/
            output.write(proxied.getBytes());
            output.flush();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
