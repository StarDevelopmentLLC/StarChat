package com.stardevllc.starchat.channels;

import com.stardevllc.starchat.ChatSpace;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starcore.Config;
import com.stardevllc.starmclib.color.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class ChatChannel extends ChatSpace {
    protected transient Config config; //Config to store information as channels are mainly config/command controlled, transient modifier allows StarData to ignore this field without having to depend on StarData directly

    protected String viewPermission = ""; //Permission needed by players in order to view messages in this channel
    protected String sendPermission = ""; //Permission needed by players in order to send messages in this channel

    public ChatChannel(String name, File configFile) {
        super(name);
        config = new Config(configFile);
    }

    protected void createDefaults() {
        config.addDefault("name", this.name, "The name of the channel.", "It is recommended to not change this name in the file and instead use commands.");
        config.addDefault("formats.sender", senderFormat, "The format used when a Player or the Console sends a chat message in this channel.");
        config.addDefault("formats.system", systemFormat, "The format used when channel related messages are sent.");
        config.addDefault("permissions.view", viewPermission, "The permission that is required to have in order for a player to receive messages from this channel.");
        config.addDefault("permssions.send", sendPermission, "The permission that is required to have in order for a player to send messages in this channel.");
        config.addDefault("formats.playerdisplayname", playerDisplayNameFormat, "The format used for formatting a player's name for the {displayname} variable.", "You can have {prefix}, {suffix} and {name}");
        config.addDefault("settings.affectedbypunishments", affectedByPunishments, "Control flag to allow messages to still be sent in this channel, even if the chat event is cancelled.");
        config.save();
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
            formattedMessage = senderFormat.replace("{displayname}", StarChat.getConsoleNameFormat());
        } else if (sender instanceof Player player) {
            if (!sendPermission.isEmpty() && !player.hasPermission(sendPermission)) {
                player.sendMessage(ColorUtils.color("&cYou do not have permission to send messages in " + getName()));
                return;
            }

            formattedMessage = senderFormat.replace("{displayname}", formatPlayerDisplayName(player));
        }

        formattedMessage = formattedMessage.replace("{message}", message);
        formattedMessage = ColorUtils.color(formattedMessage);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (viewPermission.isEmpty() || player.hasPermission(viewPermission)) {
                player.sendMessage(formattedMessage);
            }
        }
    }

    public String getViewPermission() {
        return viewPermission;
    }

    public String getSendPermission() {
        return sendPermission;
    }
}