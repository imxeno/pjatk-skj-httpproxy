package pl.edu.pja.s19880;

import java.util.HashMap;
import java.util.Map;

public class HTMLEntity {
    private Map<String, String> headers = new HashMap<>();
    private String content;
    private StringBuilder raw = new StringBuilder();

    boolean append(String data) {
        raw.append(data);
        return true;
    }
}
