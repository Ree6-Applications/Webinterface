package de.presti.ree6.webinterface.utils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Class used for Random Utilities or Utilities which use a Random Algorithm.
 */
public class RandomUtil {

    /**
     * Constructor for the Random Utility class.
     */
    private RandomUtil() {
        throw new IllegalStateException("Utility class");
    }

    // A Secure Random to create actually secure Random data.
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Create a new 126 Byte Base64 String.
     * @return {@link String} A 126 Byte Base64 String.
     */
    public static String getRandomBase64String() {
        byte[] randomBytes = new byte[128];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().encodeToString(randomBytes);
    }

}
