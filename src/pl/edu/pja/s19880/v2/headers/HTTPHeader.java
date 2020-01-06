package pl.edu.pja.s19880.v2.headers;

public class HTTPHeader {
    private String name;
    private String value;

    public HTTPHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public HTTPHeader(String line) {
        String[] temp = line.split(":( |)");
        this.name = temp[0];
        this.value = temp[1];
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
