package com.stardevllc.starchat;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class ChatSpace {
    protected long id; //StarData compatibility
    protected String name, simplifiedName; //Name is the display name and the simplified name is how it is interacted with
    protected String senderFormat = "", systemFormat = ""; //Player Format is what it looks like when a player sends a message in this space. System format is when a non-player sends a message in this space
    protected String playerDisplayNameFormat = ""; //Format for player display names in this channel.
    protected boolean affectedByPunishments = true; //If this space is affected by punishments.
    //Logging stuff

    public ChatSpace(String name) {
        this.name = name;
        this.simplifiedName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name.toLowerCase().replace(" ", "_")));
    }

    public ChatSpace(String name, String senderFormat, String systemFormat) {
        this(name);
        this.senderFormat = senderFormat;
        this.systemFormat = systemFormat;
    }
    
    public abstract void sendMessage(CommandSender sender, String message);
    
    public void sendMessage(String message) {
        sendMessage(null, message);
    }

    public String getName() {
        return name;
    }

    public String getSimplifiedName() {
        return simplifiedName;
    }

    public String getSenderFormat() {
        return senderFormat;
    }

    public String getSystemFormat() {
        return systemFormat;
    }

    public boolean isAffectedByPunishments() {
        return affectedByPunishments;
    }

    public String getPlayerDisplayNameFormat() {
        return playerDisplayNameFormat;
    }
    
    protected String formatPlayerDisplayName(Player player) {
        String displayName;
        if (this.playerDisplayNameFormat == null || this.playerDisplayNameFormat.isEmpty()) {
            displayName = player.getDisplayName();
        } else {
            displayName = this.playerDisplayNameFormat;
            displayName = displayName.replace("{prefix}", StarChat.vaultChat.getPlayerPrefix(player));
            displayName = displayName.replace("{name}", player.getName());
            displayName = displayName.replace("{suffix}", StarChat.vaultChat.getPlayerSuffix(player));
        }
        return displayName;
    }
}