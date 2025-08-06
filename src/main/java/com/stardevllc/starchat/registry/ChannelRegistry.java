package com.stardevllc.starchat.registry;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starlib.registry.RegistryObject;
import com.stardevllc.starlib.registry.StringRegistry;
import org.bukkit.ChatColor;

public class ChannelRegistry extends StringRegistry<ChatChannel> {
    
    private StarChat plugin;
    
    public ChannelRegistry(StarChat starChat) {
        super(null, string -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string.toLowerCase().replace(" ", "_"))), ChatChannel::getName, null, null);
        this.plugin = starChat;
    }

    @Override
    public RegistryObject<String, ChatChannel> register(String key, ChatChannel object) {
        plugin.getSpaceRegistry().register(key, object);
        return super.register(key, object);
    }

    @Override
    public ChatChannel unregister(String key) {
        plugin.getSpaceRegistry().unregister(key);
        return super.unregister(key);
    }
}
