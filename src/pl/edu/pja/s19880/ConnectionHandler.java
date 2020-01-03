package pl.edu.pja.s19880;

import pl.edu.pja.s19880.html.HTMLEntity;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ConnectionHandler implements Runnable {
    public final Socket socket;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            this.handle();
        } catch (Exception e) {
        }
    }

    public void handle() throws Exception {
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        HTMLEntity htmlEntity = new HTMLEntity(this);
        while(true) {
            int available = input.available();
            if(available > 0) {
                byte[] bytes = new byte[available];
                input.read(bytes);
                htmlEntity.append(new String(bytes, StandardCharsets.UTF_8));
            }
            if(htmlEntity.parse()) {
                htmlEntity.process();
                htmlEntity = new HTMLEntity(this);
            }
            Thread.sleep(10);
        }
    }
}
