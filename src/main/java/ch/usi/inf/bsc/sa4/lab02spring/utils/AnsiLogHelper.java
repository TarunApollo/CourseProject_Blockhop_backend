package ch.usi.inf.bsc.sa4.lab02spring.utils;

/// ANSI helpers for console log formatting
public final class AnsiLogHelper {

    /// ANSI reset sequence that clears active text formatting.
    private static final String RESET = "\u001B[0m";
    /// ANSI sequence for green foreground text.
    private static final String ANSI_GREEN = "\u001B[32m";
    /// ANSI sequence for red foreground text.
    private static final String ANSI_RED = "\u001B[31m";
    /// ANSI sequence for yellow foreground text.
    private static final String ANSI_YELLOW = "\u001B[33m";
    /// ANSI sequence for cyan foreground text.
    private static final String ANSI_CYAN = "\u001B[36m";
    /// ANSI sequence for bold text.
    private static final String ANSI_BOLD = "\u001B[1m";

    /// Prevents instantiation of this utility class.
    private AnsiLogHelper() {
    }

    /// Formats text with a cyan foreground color.
    /// @param text the text to format
    /// @return the text wrapped in ANSI cyan formatting
    public static String cyan(final String text) {
        return wrap(ANSI_CYAN, text);
    }

    /// Formats text with a green foreground color.
    /// @param text the text to format
    /// @return the text wrapped in ANSI green formatting
    public static String green(final String text) {
        return wrap(ANSI_GREEN, text);
    }

    /// Formats text with a red foreground color.
    /// @param text the text to format
    /// @return the text wrapped in ANSI red formatting
    public static String red(final String text) {
        return wrap(ANSI_RED, text);
    }

    /// Formats text with a yellow foreground color.
    /// @param text the text to format
    /// @return the text wrapped in ANSI yellow formatting
    public static String yellow(final String text) {
        return wrap(ANSI_YELLOW, text);
    }

    /// Formats text with bold styling.
    /// @param text the text to format
    /// @return the text wrapped in ANSI bold formatting
    public static String bold(final String text) {
        return wrap(ANSI_BOLD, text);
    }

    private static String wrap(final String color, final String text) {
        return color + text + RESET;
    }
}
