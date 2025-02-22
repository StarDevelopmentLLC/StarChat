package com.stardevllc.starchat.registry;

import com.stardevllc.registry.StringRegistry;
import com.stardevllc.starchat.space.ChatSpace;
import org.bukkit.ChatColor;

public class SpaceRegistry extends StringRegistry<ChatSpace> {
    public SpaceRegistry() {
        super(null, string -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string.toLowerCase().replace(" ", "_"))), ChatSpace::getName, null, null);
    }
}
