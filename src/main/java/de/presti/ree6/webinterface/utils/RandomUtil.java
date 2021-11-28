package de.presti.ree6.webinterface.utils;

import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class used for Random Utilities or Utilities which use a Random Algorithm.
 */
public class RandomUtil {

    /**
     * Create a new 126 Byte Base64 String.
     * @return {@link String} A 126 Byte Base64 String.
     */
    public static String getRandomBase64String() {
        Random random = ThreadLocalRandom.current();
        byte[] randomBytes = new byte[128];
        random.nextBytes(randomBytes);
        return Base64.getUrlEncoder().encodeToString(randomBytes);
    }

}
