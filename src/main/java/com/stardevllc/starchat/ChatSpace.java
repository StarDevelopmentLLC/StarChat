package com.stardevllc.starchat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public abstract class ChatSpace {
    protected long id; //StarData compatibility
    protected String name, simplifiedName; //Name is the display name and the simplified name is how it is interacted with
    protected String playerFormat, systemFormat; //Player Format is what it looks like when a player sends a message in this space. System format is when a non-player sends a message in this space
    //Logging stuff

    public ChatSpace(String name, String playerFormat, String systemFormat) {
        this.name = name;
        this.playerFormat = playerFormat;
        this.systemFormat = systemFormat;
        this.simplifiedName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name.toLowerCase().replace(" ", "_")));
    }
    
    public abstract void sendMessage(Player player, String message);
    public abstract void sendMessage(String message);

    public String getName() {
        return name;
    }

    public String getSimplifiedName() {
        return simplifiedName;
    }

    public String getPlayerFormat() {
        return playerFormat;
    }

    public String getSystemFormat() {
        return systemFormat;
    }
}