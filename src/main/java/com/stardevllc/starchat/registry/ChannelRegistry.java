package com.stardevllc.starchat.registry;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starlib.registry.StringRegistry;
import org.bukkit.ChatColor;

public class ChannelRegistry extends StringRegistry<ChatChannel> {
    
    private StarChat plugin;
    
    public ChannelRegistry(StarChat starChat) {
        super(string -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string.toLowerCase().replace(" ", "_"))), ChatChannel::getName);
        this.plugin = starChat;
    }

    @Override
    public void register(String key, ChatChannel object) {
        super.register(key, object);
        plugin.getSpaceRegistry().register(key, object);
    }

    @Override
    public void deregister(String key) {
        super.deregister(key);
        plugin.getSpaceRegistry().deregister(key);
    }
}
