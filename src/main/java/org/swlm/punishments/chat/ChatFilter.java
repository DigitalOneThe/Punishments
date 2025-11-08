package org.swlm.punishments.chat;

import org.bukkit.entity.Player;
import org.swlm.punishments.Punishments;

import java.util.Map;

public class ChatFilter {

    private final Punishments plugin;
    private final Map<String, ViolationLevel> blackListWords;

    public ChatFilter(Punishments plugin, Map<String, ViolationLevel> blackListWords) {
        this.plugin = plugin;
        this.blackListWords = blackListWords;
    }

    protected Map<String, ViolationLevel> getBlackListWords() {
        return blackListWords;
    }

    public ViolationLevel getViolationLevelFromMessage(String message) {
        String lowerMessage = message.toLowerCase();
        ViolationLevel maxLevel = null;

        for (Map.Entry<String, ViolationLevel> entry : blackListWords.entrySet()) {
            String bannedText = entry.getKey();
            if (lowerMessage.contains(bannedText)) {
                ViolationLevel currentLevel = entry.getValue();
                if (maxLevel == null || currentLevel.ordinal() > maxLevel.ordinal()) {
                    maxLevel = currentLevel;
                }
            }
        }
        return maxLevel;
    }

    public void executePunish(Player player, ViolationLevel violationLevel) {
        switch (violationLevel) {
            case VIOLATION_LEVEL_NONE: {
                // warning!
                break;
            }

            case VIOLATION_LEVEL_MILD: {
                // mute
                break;
            }

            case VIOLATION_LEVEL_SEVERE: {
                // ban
                break;
            }
        }
    }

    public boolean hasMessageBlacklistWords(String message) {
        return getViolationLevelFromMessage(message) != null;
    }

    protected Punishments getPlugin() {
        return plugin;
    }
}
