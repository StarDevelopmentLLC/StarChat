package com.stardevllc.starchat.registry;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starlib.objects.registry.Registry;
import com.stardevllc.starlib.objects.registry.RegistryObject;
import org.bukkit.ChatColor;

public class RoomRegistry extends Registry<String, ChatRoom> {
    
    private StarChat plugin;
    
    public RoomRegistry(StarChat starChat) {
        super(null, string -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string.toLowerCase().replace(" ", "_"))), ChatRoom::getName, null, null);
        this.plugin = starChat;
    }

    @Override
    public RegistryObject<String, ChatRoom> register(String key, ChatRoom object) {
        this.plugin.getSpaceRegistry().register(key, object);
        return super.register(key, object);
    }

    @Override
    public boolean unregister(String key) {
        this.plugin.getSpaceRegistry().unregister(key);
        return super.unregister(key);
    }
}
