package com.stardevllc.starchat.placeholder;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.hooks.VaultHook;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DefaultPlaceholders implements PlaceholderHandler {
    
    public String setPlaceholders(Player player, String text) {
        return setPlaceholders((OfflinePlayer) player, text);
    }

    public List<String> setPlaceholders(Player player, List<String> text) {
        List<String> replacement = new ArrayList<>();
        for (String str : text) {
            replacement.add(setPlaceholders(player, str));
        }
        return replacement;
    }

    public String setPlaceholders(OfflinePlayer player, String text) {
        String prefix = "", suffix = "";

        VaultHook vaultHook = StarChat.getInstance().getVaultHook();
        if (vaultHook != null) {
            Chat chat = vaultHook.getChat();
            prefix = chat.getPlayerPrefix(null, player);
            suffix = chat.getPlayerSuffix(null, player);
        }

        return text.replace("{prefix}", prefix).replace("{name}", player.getName()).replace("{suffix}", suffix);
    }

    public List<String> setPlaceholders(OfflinePlayer player, List<String> text) {
        List<String> replacement = new ArrayList<>();
        for (String str : text) {
            replacement.add(setPlaceholders(player, str));
        }
        return replacement;
    }
}
