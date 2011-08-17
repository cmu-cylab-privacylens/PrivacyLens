
package ch.SWITCH.aai.uApprove;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.core.io.Resource;

public class Util {

    private static MessageDigest sha256;

    static {
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException e) {
            throw new UApproveException(e);
        }
    }

    /** Default constructor for utility classes is private. */
    private Util() {
    }

    /**
     * Reads a resource into a String.
     * 
     * @param resource The resource.
     * @return Returns the content of the resource.
     * @throws IOException in case of failure.
     */
    public static String readResource(final Resource resource) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line + "\n");
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

    /**
     * Creates a fingerprint.
     * 
     * @param input The input String.
     * @return Returns a fingerprint.
     */
    public static String fingerprint(final String input) {
        return new String(Hex.encode(sha256.digest(input.getBytes())));
    }
}
