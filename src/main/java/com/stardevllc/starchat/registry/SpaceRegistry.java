package com.stardevllc.starchat.registry;

import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starlib.objects.registry.Registry;
import org.bukkit.ChatColor;

public class SpaceRegistry extends Registry<String, ChatSpace> {
    public SpaceRegistry() {
        super(null, string -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string.toLowerCase().replace(" ", "_"))), ChatSpace::getName, null, null);
    }
}
