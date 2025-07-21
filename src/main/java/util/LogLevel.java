package util;

import lombok.Getter;

@Getter
public enum LogLevel {
    ALL(500, AnsiColor.RESET),
    RIDICULOUS(5, AnsiColor.PURPLE),
    DEBUG(4, AnsiColor.PURPLE_BRIGHT),
    UNNECESSARY(3, AnsiColor.CYAN),
    IO(2, AnsiColor.CYAN_BRIGHT),
    INFO(1, AnsiColor.GREEN),
    QUIET(0, AnsiColor.RESET),
    WARN(-1, AnsiColor.RED),
    ERROR(-2, AnsiColor.RED_BOLD_BRIGHT),
    IGNORE(-3, AnsiColor.RESET);

    private final int level;
    private final String textColor;

    LogLevel(int level, String textColor) {
        this.level = level;
        this.textColor = textColor;
    }
}
