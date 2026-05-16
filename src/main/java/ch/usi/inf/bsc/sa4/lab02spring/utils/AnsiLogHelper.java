package ch.usi.inf.bsc.sa4.lab02spring.utils;

/// ANSI helpers for console log formatting
/// 
/// 
public final class AnsiLogHelper {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    private AnsiLogHelper() {
    }

    public static String cyan(final String text) {
        return wrap(CYAN, text);
    }

    public static String green(final String text) {
        return wrap(GREEN, text);
    }

    public static String red(final String text) {
        return wrap(RED, text);
    }

    public static String yellow(final String text) {
        return wrap(YELLOW, text);
    }

    public static String bold(final String text) {
        return wrap(BOLD, text);
    }

    private static String wrap(final String color, final String text) {
        return color + text + RESET;
    }
}
