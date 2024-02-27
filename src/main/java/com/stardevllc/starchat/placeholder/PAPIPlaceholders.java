package com.stardevllc.starchat.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class PAPIPlaceholders implements PlayerPlaceholders {
    
    private DefaultPlaceholders defaultPlaceholders = new DefaultPlaceholders();
    
    public String setPlaceholders(Player player, String text) {
        return defaultPlaceholders.setPlaceholders(player, PlaceholderAPI.setPlaceholders(player, text));
    }
    
    public List<String> setPlaceholders(Player player, List<String> text) {
        return defaultPlaceholders.setPlaceholders(player, PlaceholderAPI.setPlaceholders(player, text));
    }

    public String setPlaceholders(OfflinePlayer player, String text) {
        return defaultPlaceholders.setPlaceholders(player, PlaceholderAPI.setPlaceholders(player, text));
    }

    public List<String> setPlaceholders(OfflinePlayer player, List<String> text) {
        return defaultPlaceholders.setPlaceholders(player, PlaceholderAPI.setPlaceholders(player, text));
    }
}