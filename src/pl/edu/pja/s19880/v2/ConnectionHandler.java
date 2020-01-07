package pl.edu.pja.s19880.v2;

import pl.edu.pja.s19880.v2.headers.HTTPHeader;
import pl.edu.pja.s19880.v2.headers.HTTPHeaderMap;
import pl.edu.pja.s19880.v2.polyfill.InputStreamPolyfill;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
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

    @SuppressWarnings("deprecated")
    public static HTTPEntity parseHTTP(DataInputStream stream) throws IOException, InterruptedException {
        String message = stream.readLine();
        HTTPHeaderMap headers = new HTTPHeaderMap();
        String temp = stream.readLine();
        while(temp != null && !temp.isEmpty()) {
            headers.put(new HTTPHeader(temp));
            temp = stream.readLine();
        }
        byte[] body = new byte[0];
        HTTPHeader contentLength = headers.get("Content-Length");
        if (contentLength != null) {
            int readBytes = 0;
            int contentLengthValue = Integer.parseInt(contentLength.value());
            body = new byte[contentLengthValue];
            while (readBytes < contentLengthValue) {
                readBytes += stream.read(body, readBytes, contentLengthValue - readBytes);
                Thread.sleep(1);
            }
        } else if (stream.available() > 0) {
            body = new InputStreamPolyfill(stream).readAllBytes();
        }
        if (message == null) throw new IOException("DataInputStream returned null");
        return new HTTPEntity(message, headers, body);
    }

    @Override
    public void run() {
        try {
            HTTPEntity entity = parseHTTP(input);
            if (entity.getMessage().toLowerCase().contains("connect")) {
                Logger.log("(TUN) " + entity.getMessage());
                proxyHTTPS(entity);
                socket.close();
                return;
            }
            proxyHTTP(entity);
            socket.close();
        } catch (IOException | InterruptedException e) {
            if (e.getMessage().contains("Stream closed") || e.getMessage().contains("DataInputStream")) return;
            e.printStackTrace();
        }
    }

    private void proxyHTTP(HTTPEntity httpEntity) throws InterruptedException {
        String message = httpEntity.getMessage();
        httpEntity.setMessage(message.replace("HTTP/1.1", "HTTP/1.0"));
        Logger.log("(REQ) " + message);
        String lowerCaseMethod = message.split(" ")[0].toLowerCase();
        httpEntity.getHeaders().put(new HTTPHeader("Accept-Encoding", "identity")); // we prefer plaintext
        httpEntity.getHeaders().remove("If-None-Match"); // we don't need caches
        httpEntity.getHeaders().remove("If-Modified-Since"); // we don't need caches
        try {
            if (!lowerCaseMethod.equals("get") && !lowerCaseMethod.equals("post")) {
                socket.close();
                return;
            }
            Socket s = new Socket();
            s.connect(new InetSocketAddress(InetAddress.getByName(httpEntity.getHost()), httpEntity.getPort()));
            Logger.log("(PRX) Connected socket!");
            s.getOutputStream().write(httpEntity.getBytes());
            HTTPEntity proxied = parseHTTP(new DataInputStream(new BufferedInputStream(s.getInputStream())));
            Logger.log("(RES) " + proxied.getMessage());
            proxied.getHeaders().put(new HTTPHeader("X-Proxy-Author", "Piotr Adamczyk | s19880"));
            proxied.getHeaders().put(new HTTPHeader("X-This-Proxy", "is very offensive to me 🎅"));
            if (proxied.getHeaders().containsKey("Content-Type") && proxied.getHeaders().get("Content-Type").value().contains("text/html")) {
                String body = new String(proxied.getBody(), StandardCharsets.UTF_8);
                int headIndex = body.toLowerCase().indexOf("</head>");
                if (headIndex != -1) {
                    body = body.substring(0, headIndex) + "<script>" + HTTPEntity.getUnpleasantWordFilterScript() + "</script>" + body.substring(headIndex);
                    proxied.setBody(body.getBytes(StandardCharsets.UTF_8));
                    proxied.getHeaders().put(new HTTPHeader("Content-Length", "" + proxied.getBody().length));
                }
            }
            output.write(proxied.getBytes());
            output.flush();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void proxyHTTPS(HTTPEntity httpEntity) throws IOException, InterruptedException {
        Socket s = new Socket();
        s.connect(new InetSocketAddress(InetAddress.getByName(httpEntity.getHost()), httpEntity.getPort()));
        output.write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
        long lastTransmission = System.currentTimeMillis();
        while (!socket.isClosed()) {
            if (input.available() > 0) {
                int len = input.available();
                byte[] temp = new byte[len];
                input.read(temp, 0, len);
                s.getOutputStream().write(temp);
                lastTransmission = System.currentTimeMillis();
            }
            if (s.getInputStream().available() > 0) {
                int len = s.getInputStream().available();
                byte[] temp = new byte[len];
                s.getInputStream().read(temp, 0, len);
                output.write(temp);
                lastTransmission = System.currentTimeMillis();
            }
            if (lastTransmission + 10000L < System.currentTimeMillis()) break;
            Thread.sleep(1);
        }
        s.close();
    }
}
