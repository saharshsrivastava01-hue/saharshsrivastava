package com.goldtrader.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtil {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    private CurrencyUtil() {
        // Utility class
    }

    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatWeight(BigDecimal weight) {
        if (weight == null) return "0.000 g";
        return weight.setScale(3, RoundingMode.HALF_UP) + " g";
    }

    public static BigDecimal parseCurrency(String text) {
        try {
            String cleaned = text.replaceAll("[^\\d.]", "");
            return new BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public static BigDecimal parseWeight(String text) {
        try {
            String cleaned = text.replaceAll("[^\\d.]", "");
            return new BigDecimal(cleaned).setScale(3, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
