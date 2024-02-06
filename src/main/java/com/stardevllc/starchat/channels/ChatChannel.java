package com.stardevllc.starchat.channels;

import com.stardevllc.starchat.ChatSpace;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starmclib.color.ColorUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ChatChannel extends ChatSpace {
    protected transient YamlDocument config; //Config to store information as channels are mainly config/command controlled, transient modifier allows StarData to ignore this field without having to depend on StarData directly
    
    protected String viewPermission = ""; //Permission needed by players in order to view messages in this channel
    protected String sendPermission = ""; //Permission needed by players in order to send messages in this channel
    
    protected String playerDisplayNameFormat = ""; //Format for player display names in this channel.
    
    public ChatChannel(String name, File configFile) {
        super(name);
        try {
            config = YamlDocument.create(configFile, GeneralSettings.builder().setUseDefaults(true).build(), LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
        } catch (IOException e) {}
    }
    
    protected void createDefaults() {
        if (!config.contains("name")) {
            config.set("name", this.name);
            config.getBlock("name").addComment("It is recommended to not change this name in the file and instead use commands.");
        }
        
        if (!config.contains("formats.sender")) {
            config.set("formats.sender", senderFormat);
            config.getBlock("formats.sender").addComment("The format used when a Player or the Console sends a message in this channel.");
        }
        
        if (!config.contains("formats.system")) {
            config.set("formats.system", systemFormat);
            config.getBlock("formats.system").addComment("The format used when channel related messages are sent.");
        }
        
        if (!config.contains("permissions.view")) {
            config.set("permissions.view", viewPermission);
            config.getBlock("permissions.view").addComment("The permission that is required to have in order for a player to see messages in this channel.");
        }
        
        if (!config.contains("permissions.send")) {
            config.set("permissions.send", sendPermission);
            config.getBlock("permissions.send").addComment("The permission that is required to have in order for a player to send messages in this channel.");
        }
        
        if (!config.contains("formats.playerdisplayname")) {
            config.set("formats.playerdisplayname", playerDisplayNameFormat);
            config.getBlock("formats.playerdisplayname").addComments(List.of("The format used for formatting a player's name for the {displayname} argument.", "You can have {prefix}, {suffix} and {name}"));
        }
        
        if (!config.contains("settings.affectedbypunishments")) {
            config.set("settings.affectedbypunishments", affectedByPunishments);
            config.getBlock("settings.affectedbypunishments").addComment("This controls if this channel is affected by the AsyncPlayerChatEvent being cancelled by other plugins (Most likely punishment plugins)");
        }

        try {
            config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void loadSettings() {
        this.senderFormat = config.getString("formats.sender");
        this.systemFormat = config.getString("formats.system");
        this.viewPermission = config.getString("permissions.view");
        this.sendPermission = config.getString("permissions.send");
        this.playerDisplayNameFormat = config.getString("formats.playerdisplayname");
        this.affectedByPunishments = config.getBoolean("settings.affectedbypunishments");
    }

    @Override
    public void sendMessage(CommandSender sender, String message) {
        String formattedMessage = "";
        
        if (sender == null) {
            formattedMessage = systemFormat;
        } else if (sender instanceof ConsoleCommandSender) {
            formattedMessage = senderFormat.replace("{displayname}", StarChat.consoleNameFormat);
        } else if (sender instanceof Player player) {
            if (!sendPermission.isEmpty() && !player.hasPermission(sendPermission)) {
                player.sendMessage(ColorUtils.color("&cYou do not have permission to send messages in " + getName()));
                return;
            }
            
            String displayName;
            if (this.playerDisplayNameFormat == null || this.playerDisplayNameFormat.isEmpty()) {
                displayName = player.getDisplayName();
            } else {
                displayName = this.playerDisplayNameFormat;
                displayName = displayName.replace("{prefix}", StarChat.vaultChat.getPlayerPrefix(player));
                displayName = displayName.replace("{name}", player.getName());
                displayName = displayName.replace("{suffix}", StarChat.vaultChat.getPlayerSuffix(player));
            }
            
            formattedMessage = senderFormat.replace("{displayname}", displayName);
        }
        
        formattedMessage = formattedMessage.replace("{message}", message);
        formattedMessage = ColorUtils.color(formattedMessage);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (viewPermission.isEmpty() || player.hasPermission(viewPermission)) {
                player.sendMessage(formattedMessage);
            }
        }
    }
}