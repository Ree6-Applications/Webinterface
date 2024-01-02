package de.presti.ree6.backend.utils.data;

/**
 * Util class to make it easier to convert DataTyps to other ones.
 * This is mainly being used because Springboot is a bitch when handling longs,
 * inside Path Variables.
 */
public class ConverterUtil {

    public static long convertStringToLong(String string) {
        long newValue = -1;
        try {
            newValue = Long.parseLong(string);
        } catch (NumberFormatException ignored) {
            throw new IllegalArgumentException("The given String is not a valid Long!");
        }
        return newValue;
    }

}
