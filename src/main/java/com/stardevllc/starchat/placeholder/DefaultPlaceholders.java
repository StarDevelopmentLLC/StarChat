package com.stardevllc.starchat.placeholder;

import com.stardevllc.starchat.StarChat;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DefaultPlaceholders implements PlayerPlaceholders {
    
    private Chat vaultChat = StarChat.getVaultChat();
    
    public String setPlaceholders(Player player, String text) {
        String replacement = text.replace("{prefix}", vaultChat.getPlayerPrefix(player));
        replacement = replacement.replace("{name}", player.getName());
        replacement = replacement.replace("{suffix}", vaultChat.getPlayerSuffix(player));
        return replacement;
    }

    public List<String> setPlaceholders(Player player, List<String> text) {
        List<String> replacement = new ArrayList<>();
        for (String str : text) {
            replacement.add(setPlaceholders(player, str));
        }
        return replacement;
    }

    public String setPlaceholders(OfflinePlayer player, String text) {
        String replacement = text.replace("{prefix}", vaultChat.getPlayerPrefix(null, player));
        replacement = replacement.replace("{name}", player.getName());
        replacement = replacement.replace("{suffix}", vaultChat.getPlayerSuffix(null, player));
        return replacement;
    }

    public List<String> setPlaceholders(OfflinePlayer player, List<String> text) {
        List<String> replacement = new ArrayList<>();
        for (String str : text) {
            replacement.add(setPlaceholders(player, str));
        }
        return replacement;
    }
}
