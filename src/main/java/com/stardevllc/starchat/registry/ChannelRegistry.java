package com.stardevllc.starchat.registry;

import com.stardevllc.registry.StringRegistry;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.channels.ChatChannel;
import org.bukkit.ChatColor;

public class ChannelRegistry extends StringRegistry<ChatChannel> {
    
    private StarChat plugin;
    
    public ChannelRegistry(StarChat starChat) {
        super(null, string -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string.toLowerCase().replace(" ", "_"))), ChatChannel::getName, null, null);
        this.plugin = starChat;
    }

    @Override
    public ChatChannel register(String key, ChatChannel object) {
        super.register(key, object);
        plugin.getSpaceRegistry().register(key, object);
        return object;
    }

    @Override
    public ChatChannel unregister(String key) {
        plugin.getSpaceRegistry().unregister(key);
        return super.unregister(key);
    }
}
