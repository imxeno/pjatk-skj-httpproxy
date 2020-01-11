package pl.edu.pja.s19880.v2;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ProxyConfig {
    public static ProxyConfig instance;
    public final boolean heavyMode;
    public final int port;
    public final String[] unpleasantWords;
    public final String cacheDir;

    public ProxyConfig(boolean heavyMode, String path) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(path));
        this.heavyMode = heavyMode;
        this.port = Integer.parseInt(properties.getProperty("PROXY_PORT"));
        this.unpleasantWords = properties.getProperty("WORDS").split(";");
        this.cacheDir = properties.getProperty("CACHE_DIR").replace("\"", "");
    }


}
