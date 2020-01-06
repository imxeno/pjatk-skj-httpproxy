package pl.edu.pja.s19880.v1.html.headers;

public class HTTPHeader {
    private String name;
    private String value;

    public HTTPHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String value() {
        return value;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", name(), value());
    }
}
