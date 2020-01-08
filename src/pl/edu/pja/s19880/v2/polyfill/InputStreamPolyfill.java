package pl.edu.pja.s19880.v2.polyfill;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class InputStreamPolyfill {

    private final InputStream stream;

    public InputStreamPolyfill(InputStream stream) {
        this.stream = stream;
    }

    public byte[] readAllBytes() throws IOException {
        return this.readNBytes(2147483647);
    }

    public byte[] readNBytes(int len) throws IOException {
        if (len < 0) {
            throw new IllegalArgumentException("len < 0");
        } else {
            List<byte[]> bufs = null;
            byte[] result = null;
            int total = 0;
            int remaining = len;

            int n;
            do {
                byte[] buf = new byte[Math.min(remaining, 8192)];

                int nread;
                for (nread = 0; (n = stream.read(buf, nread, Math.min(buf.length - nread, remaining))) > 0; remaining -= n) {
                    nread += n;
                }

                if (nread > 0) {
                    if (2147483639 - total < nread) {
                        throw new OutOfMemoryError("Required array size too large");
                    }

                    total += nread;
                    if (result == null) {
                        result = buf;
                    } else {
                        if (bufs == null) {
                            bufs = new ArrayList<>();
                            bufs.add(result);
                        }

                        bufs.add(buf);
                    }
                }
            } while (n >= 0 && remaining > 0);

            if (bufs == null) {
                if (result == null) {
                    return new byte[0];
                } else {
                    return result.length == total ? result : Arrays.copyOf(result, total);
                }
            } else {
                result = new byte[total];
                int offset = 0;
                remaining = total;

                int count;
                for (Iterator var12 = bufs.iterator(); var12.hasNext(); remaining -= count) {
                    byte[] b = (byte[]) var12.next();
                    count = Math.min(b.length, remaining);
                    System.arraycopy(b, 0, result, offset, count);
                    offset += count;
                }

                return result;
            }
        }
    }
}
