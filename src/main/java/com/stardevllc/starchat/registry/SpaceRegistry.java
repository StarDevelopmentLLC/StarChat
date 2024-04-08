package com.stardevllc.starchat.registry;

import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starlib.registry.StringRegistry;
import org.bukkit.ChatColor;

public class SpaceRegistry extends StringRegistry<ChatSpace> {
    public SpaceRegistry() {
        super(string -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string.toLowerCase().replace(" ", "_"))), ChatSpace::getName);
    }
}
