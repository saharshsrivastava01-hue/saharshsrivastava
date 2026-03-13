package com.goldtrading.ui;

import java.util.List;

/**
 * Utility class for formatting data into console tables.
 */
public class TableFormatter {

    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) return "";

        // Calculate column widths
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, headers.length); i++) {
                if (row[i] != null) {
                    widths[i] = Math.max(widths[i], row[i].length());
                }
            }
        }

        StringBuilder sb = new StringBuilder();

        // Separator line
        String separator = buildSeparator(widths);

        // Header
        sb.append(separator).append("\n");
        sb.append(buildRow(headers, widths)).append("\n");
        sb.append(separator).append("\n");

        // Rows
        for (String[] row : rows) {
            sb.append(buildRow(row, widths)).append("\n");
        }
        sb.append(separator).append("\n");

        return sb.toString();
    }

    private static String buildSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int width : widths) {
            sb.append("-".repeat(width + 2)).append("+");
        }
        return sb.toString();
    }

    private static String buildRow(String[] values, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            String value = (i < values.length && values[i] != null) ? values[i] : "";
            sb.append(String.format(" %-" + widths[i] + "s |", value));
        }
        return sb.toString();
    }

    public static void printBoxed(String title, String content) {
        int width = Math.max(title.length() + 4, 60);
        System.out.println("+" + "=".repeat(width) + "+");
        System.out.printf("| %-" + (width - 2) + "s |\n", "  " + title);
        System.out.println("+" + "=".repeat(width) + "+");
        if (content != null && !content.isEmpty()) {
            for (String line : content.split("\n")) {
                System.out.printf("| %-" + (width - 2) + "s |\n",
                        line.length() > width - 2 ? line.substring(0, width - 2) : line);
            }
            System.out.println("+" + "-".repeat(width) + "+");
        }
    }
}
