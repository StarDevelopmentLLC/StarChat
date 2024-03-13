package com.stardevllc.starchat;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class ChatSpace {
    protected long id; //StarData compatibility
    protected JavaPlugin plugin; //Plugin that owns the space
    protected String name, simplifiedName; //Name is the display name and the simplified name is how it is interacted with
    protected String senderFormat = "", systemFormat = ""; //Player Format is what it looks like when a player sends a message in this space. System format is when a non-player sends a message in this space
    protected String playerDisplayNameFormat = ""; //Format for player display names in this channel.
    protected boolean affectedByPunishments = true; //If this space is affected by punishments.
    //Logging stuff

    public ChatSpace(JavaPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.simplifiedName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name.toLowerCase().replace(" ", "_")));
    }

    public ChatSpace(JavaPlugin plugin, String name, String senderFormat, String systemFormat) {
        this(plugin, name);
        this.senderFormat = senderFormat;
        this.systemFormat = systemFormat;
    }
    
    public abstract void sendMessage(CommandSender sender, String message);
    
    public void sendMessage(String message) {
        sendMessage(null, message);
    }

    public void setSenderFormat(String senderFormat) {
        this.senderFormat = senderFormat;
    }

    public void setSystemFormat(String systemFormat) {
        this.systemFormat = systemFormat;
    }

    public void setPlayerDisplayNameFormat(String playerDisplayNameFormat) {
        this.playerDisplayNameFormat = playerDisplayNameFormat;
    }

    public void setAffectedByPunishments(boolean affectedByPunishments) {
        this.affectedByPunishments = affectedByPunishments;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.simplifiedName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name.toLowerCase().replace(" ", "_")));
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
        if (this.playerDisplayNameFormat == null || this.playerDisplayNameFormat.isEmpty()) {
            return player.getDisplayName();
        } else {
            return StarChat.getPlayerPlaceholders().setPlaceholders(player, this.playerDisplayNameFormat);
        }
    }
}