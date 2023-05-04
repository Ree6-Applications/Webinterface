package de.presti.ree6.backend.utils;

import java.util.Base64;

/**
 * A utility class used to handle session easier.
 */
public class SessionUtil {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     * @throws IllegalStateException it is a utility class.
     */
    private SessionUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get the Identifier out of the Cookie-Value.
     *
     * @param identifier the encoded Identifier.
     * @return the decoded Identifier.
     */
    public static String getIdentifier(String identifier) {
        try {
            identifier = new String(Base64.getDecoder().decode(identifier));
            return identifier;
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * Check if a String is a valid identifier.
     *
     * @param identifier the "identifier".
     * @return true, if it is an invalid identifier | false, if not.
     */
    public static boolean checkIdentifier(String identifier) {
        return identifier == null || identifier.equals("-1");
    }
}
