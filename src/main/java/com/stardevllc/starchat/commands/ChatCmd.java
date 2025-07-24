package com.stardevllc.starchat.commands;

import com.stardevllc.starchat.ChatSelector;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starchat.registry.ChannelRegistry;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starcore.config.Configuration;
import com.stardevllc.starlib.dependency.Inject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ChatCmd implements TabExecutor {

    @Inject
    private StarChat plugin;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Configuration pluginConfig = plugin.getMainConfig();
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

        if (!(args.length > 0)) {
            return null;
        }
        
        List<String> channels = new LinkedList<>();
        
        String name = args[0].toLowerCase();

        ChannelRegistry channelRegistry = plugin.getChannelRegistry();
        for (ChatChannel chatChannel : channelRegistry) {
            if (chatChannel.canSendMessages(sender)) {
                if (chatChannel.getName().startsWith(name)) {
                    channels.add(chatChannel.getName());
                }
            }
        }
        
        channels.addAll(plugin.getChatSelectors().keySet());

        Collections.sort(channels);
        return channels;
    }
}