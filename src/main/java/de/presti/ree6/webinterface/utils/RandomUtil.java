package de.presti.ree6.webinterface.utils;

import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {

    public static String getRandomBase64String() {
        Random random = ThreadLocalRandom.current();
        byte[] randomBytes = new byte[128];
        random.nextBytes(randomBytes);
        return Base64.getUrlEncoder().encodeToString(randomBytes);
    }

}
