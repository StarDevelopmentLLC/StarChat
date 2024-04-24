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
    public void register(String key, ChatRoom object) {
        super.register(key, object);
        this.plugin.getSpaceRegistry().register(key, object);
    }

    @Override
    public void deregister(String key) {
        super.deregister(key);
        this.plugin.getSpaceRegistry().deregister(key);
    }
}
