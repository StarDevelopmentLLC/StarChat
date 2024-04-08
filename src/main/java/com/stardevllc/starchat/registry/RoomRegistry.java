package com.stardevllc.starchat.registry;

import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starlib.registry.StringRegistry;
import org.bukkit.ChatColor;

public class RoomRegistry extends StringRegistry<ChatRoom> {
    public RoomRegistry() {
        super(string -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string.toLowerCase().replace(" ", "_"))), ChatRoom::getName);
    }
}
