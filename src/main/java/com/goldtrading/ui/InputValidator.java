package com.goldtrading.ui;

import java.util.Scanner;

/**
 * Utility class for validating and reading user input.
 */
public class InputValidator {

    private final Scanner scanner;

    public InputValidator(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String readNonEmpty(String prompt) {
        while (true) {
            String value = readString(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("  [!] This field cannot be empty. Please try again.");
        }
    }

    public int readInt(String prompt) {
        while (true) {
            String value = readString(prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid number.");
            }
        }
    }

    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.printf("  [!] Please enter a number between %d and %d.%n", min, max);
        }
    }

    public double readDouble(String prompt) {
        while (true) {
            String value = readString(prompt);
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid decimal number.");
            }
        }
    }

    public double readPositiveDouble(String prompt) {
        while (true) {
            double value = readDouble(prompt);
            if (value > 0) {
                return value;
            }
            System.out.println("  [!] Value must be greater than 0.");
        }
    }

    public boolean readConfirmation(String prompt) {
        String value = readString(prompt + " (y/n): ");
        return value.equalsIgnoreCase("y") || value.equalsIgnoreCase("yes");
    }

    public String readPhone(String prompt) {
        while (true) {
            String value = readString(prompt);
            if (value.isEmpty() || value.matches("\\+?[0-9\\-\\s]{7,15}")) {
                return value;
            }
            System.out.println("  [!] Please enter a valid phone number.");
        }
    }

    public String readEmail(String prompt) {
        while (true) {
            String value = readString(prompt);
            if (value.isEmpty() || value.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                return value;
            }
            System.out.println("  [!] Please enter a valid email address.");
        }
    }
}
