package io.xeros.content.casino;

/**
 * Utility methods for casino commands.
 */
public class CasinoCmdUtil {

    /**
     * Parse an amount string that supports k/m/b suffixes.
     * Examples: "100k" = 100,000, "1.5m" = 1,500,000, "1b" = 1,000,000,000
     */
    public static int parseAmount(String input) throws NumberFormatException {
        input = input.toLowerCase().replace(",", "").replace(" ", "");
        double multiplier = 1;
        if (input.endsWith("k")) {
            multiplier = 1_000;
            input = input.substring(0, input.length() - 1);
        } else if (input.endsWith("m")) {
            multiplier = 1_000_000;
            input = input.substring(0, input.length() - 1);
        } else if (input.endsWith("b")) {
            multiplier = 1_000_000_000;
            input = input.substring(0, input.length() - 1);
        }
        long result = (long) (Double.parseDouble(input) * multiplier);
        if (result > Integer.MAX_VALUE || result < 1) {
            throw new NumberFormatException("Amount out of range");
        }
        return (int) result;
    }
}
