package pl.edu.pja.s19880;

public class Logger {
    private Logger() {}
    public static void log(String s) {
        synchronized (System.out) {
            System.out.println(s);
        }
    }
}
