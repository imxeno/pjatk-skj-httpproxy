package pl.edu.pja.s19880.v2;


import pl.edu.pja.s19880.v2.headers.HTTPHeaderMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HTTPEntity {
    public static final String LINE_END = "\r\n";
    private String message;
    private HTTPHeaderMap headers;
    private byte[] body;

    public HTTPEntity(String message, HTTPHeaderMap headers, byte[] body) {
        this.message = message;
        this.headers = headers;
        this.body = body;
    }

    public static String getUnpleasantWordFilterScript() {
        try {
            return new String(Files.readAllBytes(Paths.get("src/pl/edu/pja/s19880/v2/unpleasantWordFilter.js")));
        } catch (IOException e) {
            return "alert('error!');";
        }
    }

    public String getMessage() {
        return message;
    }
    public HTTPHeaderMap getHeaders() {
        return headers;
    }

    public byte[] getBytes() {
        try {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ba.write(message.getBytes(StandardCharsets.UTF_8));
            ba.write(LINE_END.getBytes(StandardCharsets.UTF_8));
            ba.write(headers.toString().getBytes(StandardCharsets.UTF_8));
            ba.write(LINE_END.getBytes(StandardCharsets.UTF_8));
            ba.write(body);
            return ba.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
