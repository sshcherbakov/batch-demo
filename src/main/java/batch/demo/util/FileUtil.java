package batch.demo.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

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
     * Up to 5 times faster than using BufferedReader and up to 2 times faster 
     * than LineNumberReader probably because of missing synchronization.
     * 
     * @param in the input stream to use
     * @return the number of lines found in the stream
     * @throws IOException if an error occurred
     */    
    public static long countLines(InputStream in) throws IOException {
        final InputStream is = new BufferedInputStream(in);
        byte[] c = new byte[4096];
        long count = 0;
        int readChars;
        byte lastChar = 0;
        boolean contentExists = false;
        while ((readChars = is.read(c)) != -1) {
            for (int i = 0; i < readChars; ++i) {
            	contentExists = true;
            	lastChar = c[i];
                // We're dealing with the char here, it's \n on Unix and \r\n on Windows                
                if (c[i] == '\n')
                    ++count;
            }
        }
        // Last line
        if ( (count > 0 && lastChar != '\n') || 						// <-- last line is not empty but is not terminated by '\n'
        	(count == 0 && lastChar != '\n' && contentExists) ) {		// <-- the first line is the last line and it's not terminated by '\n'
            count++;
        }
        return count;
    }

}
