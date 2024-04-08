package com.stardevllc.starchat.placeholder;

import com.stardevllc.starchat.StarChat;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PAPIExpansion extends PlaceholderExpansion {
    
    private StarChat plugin;

    public PAPIExpansion(StarChat plugin) {
        this.plugin = plugin;
    }

    // %starchat_space% - Player's current chat space identifier
    // %starchat_space_display% - Playyer's current chat space displayname
    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params.equalsIgnoreCase("space")) {
            return plugin.getPlayerFocus(player).getName().toLowerCase().replace(" ", "_");
        } else if (params.equalsIgnoreCase("space_display")) {
            return plugin.getPlayerFocus(player).getName();
        }
        
        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

    public String getIdentifier() {
        return "starchat";
    }

    public String getAuthor() {
        return "Firestar311";
    }

    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
}
