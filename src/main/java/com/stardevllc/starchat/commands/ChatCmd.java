package com.stardevllc.starchat.commands;

import com.stardevllc.StarColors;
import com.stardevllc.config.file.FileConfig;
import com.stardevllc.starchat.ChatSelector;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starlib.injector.Inject;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class ChatCmd implements TabExecutor {

    @Inject
    private StarChat plugin;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        FileConfig pluginConfig = plugin.getMainConfig();
        if (!sender.hasPermission("starchat.command.chat")) {
            StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
            return true;
        }
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage(StarColors.color(pluginConfig.getString("messages.command.onlyplayers")));
            return true;
        }
        
        if (!(args.length > 0)) {
            sender.sendMessage(StarColors.color("&cUsage: /" + label + " <channelName>"));
            return true;
        }
        
        String channelName = args[0].toLowerCase();
        
        String nameOverride = "";
        
        ChatSpace chatSpace = plugin.getChannelRegistry().get(channelName);
        if (chatSpace == null) {
            chatSpace = plugin.getRoomRegistry().get(channelName);
        }

        if (chatSpace == null) {
            ChatSelector selector = plugin.getChatSelectors().get(channelName);
            if (selector != null) {
                ChatSelector.ChatSelection selection = selector.getSelection(player, args);
                if (selection != null) {
                    chatSpace = selection.space();
                    nameOverride = selection.nameOverride();
                }
            }
        }

        if (chatSpace == null) {
            sender.sendMessage(StarColors.color(pluginConfig.getString("messages.chatspace.notexist").replace("{PROVIDED}", channelName)));
            return true;
        }
        

        if (chatSpace instanceof ChatChannel chatChannel) {
            String sendPermission = chatChannel.getSendPermission();
            if (!sendPermission.isEmpty() && !player.hasPermission(sendPermission)) {
                sender.sendMessage(StarColors.color(pluginConfig.getString("messages.channel.nosendpermission").replace("{CHANNEL}", chatChannel.getName())));
                return true;
            }
        } else if (chatSpace instanceof ChatRoom chatRoom) {
            if (!chatRoom.isMember(player.getUniqueId())) {
                sender.sendMessage(StarColors.color(pluginConfig.getString("messages.room.notamember").replace("{ROOM}", chatRoom.getName())));
                return true;
            }
        }

        plugin.setPlayerFocus(player, chatSpace);
        String spaceName = chatSpace.getName();
        if (nameOverride != null && !nameOverride.isEmpty()) {
            spaceName = nameOverride;
        }
        sender.sendMessage(StarColors.color(pluginConfig.getString("messages.command.chat.setfocus").replace("{SPACE}", spaceName)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("starchat.command.chat")) {
            return null;
        }
        
        if (!(sender instanceof Player)) {
            return null;
        }
        
        if (args.length != 1) {
            return null;
        }
        
        List<String> channels = new LinkedList<>();
        channels.addAll(plugin.getChannelRegistry().keySet().stream().map(k -> k.toString().toLowerCase()).toList());
        channels.addAll(plugin.getChatSelectors().keySet().stream().map(String::toLowerCase).toList());
        
        String name = args[0].toLowerCase();
        channels.removeIf(s -> !s.startsWith(name));
        Collections.sort(channels);
        return channels;
    }
}