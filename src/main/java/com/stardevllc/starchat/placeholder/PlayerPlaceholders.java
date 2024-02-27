package com.stardevllc.starchat.placeholder;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public interface PlayerPlaceholders {
    String setPlaceholders(Player player, String text);
    List<String> setPlaceholders(Player player, List<String> text);
    String setPlaceholders(OfflinePlayer player, String text);
    List<String> setPlaceholders(OfflinePlayer player, List<String> text);
}
