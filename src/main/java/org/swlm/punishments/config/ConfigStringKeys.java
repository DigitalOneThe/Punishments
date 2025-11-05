package org.swlm.punishments.config;

public enum ConfigStringKeys {
    // Database section
    DATABASE_USER("database.user"),
    DATABASE_PASSWORD("database.password"),
    DATABASE_NAME("database.name"),
    DATABASE_HOST("database.host"),
    DATABASE_PORT("database.port"),

    // Limits section
    LIMITS_OVERRIDES("limits.overrides"),
    LIMITS_COMMANDS("limits.commands"),

    // Error messages section
    ERROR_MESSAGES_ERROR_IN_PLUGIN("error-messages.error-in-plugin"),
    ERROR_MESSAGES_ERROR_IN_DATABASE("error-messages.error-in-database"),

    // Warning messages section
    WARNING_MESSAGES_FAILED_ATTEMPT_NOT_FOUND("warning-messages.failed-attempt.not-found"),
    WARNING_MESSAGES_FAILED_ATTEMPT_NOT_BANNED("warning-messages.failed-attempt.not-banned"),
    WARNING_MESSAGES_FAILED_ATTEMPT_NOT_WARNED("warning-messages.failed-attempt.not-warned"),
    WARNING_MESSAGES_FAILED_ATTEMPT_HAS_ALREADY_BANNED("warning-messages.failed-attempt.has-already-banned"),
    WARNING_MESSAGES_FAILED_ATTEMPT_HAS_ALREADY_WARNED("warning-messages.failed-attempt.has-already-warned"),
    WARNING_MESSAGES_FAILED_ATTEMPT_FAILED_UNBAN("warning-messages.failed-attempt.failed-unban"),
    WARNING_MESSAGES_FAILED_ATTEMPT_FAILED_UNWARN("warning-messages.failed-attempt.failed-unwarn"),
    WARNING_MESSAGES_FAILED_ATTEMPT_FAILED_BAN("warning-messages.failed-attempt.failed-ban"),
    WARNING_MESSAGES_FAILED_ATTEMPT_FAILED_WARN("warning-messages.failed-attempt.failed-warn"),
    WARNING_MESSAGES_FAILED_ATTEMPT_EXCEEDED_BAN_LIMIT("warning-messages.failed-attempt.exceeded-ban-limit"),
    WARNING_MESSAGES_FAILED_ATTEMPT_EMPTY_LOGS("warning-messages.failed-attempt.empty-logs"),
    WARNING_MESSAGES_FAILED_ATTEMPT_NOT_PERMISSION("warning-messages.failed-attempt.not-permission"),

    // Broadcast messages section
    BROADCAST_MESSAGES_BAN("broadcast-messages.ban"),
    BROADCAST_MESSAGES_TEMPBAN("broadcast-messages.tempban"),
    BROADCAST_MESSAGES_UNBAN("broadcast-messages.unban"),
    BROADCAST_MESSAGES_UNWARN("broadcast-messages.unwarn"),
    BROADCAST_MESSAGES_WARN("broadcast-messages.warn"),

    // Window messages section
    WINDOW_MESSAGES_BAN("window-messages.ban"),
    WINDOW_MESSAGES_TEMPBAN("window-messages.tempban"),

    // Messages section
    MESSAGES_ROLLBACK("messages.rollback"),
    MESSAGES_EDIT("messages.edit"),

    // Lore section
    LORE_LOGGING("lore.logging"),

    // Command arguments section
    COMMAND_ARGUMENTS_BAN("command-arguments.ban"),
    COMMAND_ARGUMENTS_TEMPBAN("command-arguments.tempban"),
    COMMAND_ARGUMENTS_UNBAN("command-arguments.unban"),
    COMMAND_ARGUMENTS_WARN("command-arguments.warn"),
    COMMAND_ARGUMENTS_UNWARN("command-arguments.unwarn"),
    COMMAND_ARGUMENTS_HISTORY("command-arguments.history"),
    COMMAND_ARGUMENTS_PUNISHMENT("command-arguments.punishment"),
    COMMAND_ARGUMENTS_PUNISHMENT_ROLLBACK("command-arguments.punishment-rollback"),
    COMMAND_ARGUMENTS_PUNISHMENT_EDIT("command-arguments.punishment-edit"),
    COMMAND_ARGUMENTS_PUNISHMENT_LOGS("command-arguments.punishment-logs");

    private final String name;

    ConfigStringKeys(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
