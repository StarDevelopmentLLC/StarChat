package com.stardevllc.starchat.registry;

import com.stardevllc.starchat.ChatSpace;
import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starlib.registry.StringRegistry;
import org.bukkit.ChatColor;

public class ChannelRegistry extends StringRegistry<ChatChannel> {
    public ChannelRegistry() {
        super(string -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string.toLowerCase().replace(" ", "_"))), ChatSpace::getName);
    }
}
