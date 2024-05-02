package com.stardevllc.starchat.registry;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starlib.registry.StringRegistry;
import org.bukkit.ChatColor;

public class RoomRegistry extends StringRegistry<ChatRoom> {
    
    private StarChat plugin;
    
    public RoomRegistry(StarChat starChat) {
        super(string -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string.toLowerCase().replace(" ", "_"))), ChatRoom::getName);
        this.plugin = starChat;
    }

    @Override
    public ChatRoom register(String key, ChatRoom object) {
        this.plugin.getSpaceRegistry().register(key, object);
        return super.register(key, object);
    }

    @Override
    public ChatRoom unregister(String key) {
        this.plugin.getSpaceRegistry().unregister(key);
        return super.unregister(key);
    }
}
