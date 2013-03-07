package batch.demo.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Some file utilities.
 *
 * @author Stephane Nicoll
 */
public class FileUtil {

    /**
     * Returns the number of lines found in the specified stream.
     * <p/>
     * The caller is responsible to close the stream.
     *
     * @param in the input stream to use
     * @return the number of lines found in the stream
     * @throws IOException if an error occurred
     */
    public static long countLines(InputStream in) throws IOException {
        final InputStream is = new BufferedInputStream(in);
        byte[] c = new byte[1024];
        long count = 0;
        int readChars;
        while ((readChars = is.read(c)) != -1) {
            for (int i = 0; i < readChars; ++i) {
                // We're dealing with the char here, it's \n on Unix and \r\n on Windows                
                if (c[i] == '\n')
                    ++count;
            }
        }
        // Last line
        if (count > 0) {
            count++;
        }
        return count;
    }
}
