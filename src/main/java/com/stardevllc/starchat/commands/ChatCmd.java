package com.stardevllc.starchat.commands;

import com.stardevllc.starchat.ChatSelector;
import com.stardevllc.starchat.ChatSpace;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starmclib.color.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatCmd implements CommandExecutor {

    private StarChat plugin;

    public ChatCmd(StarChat plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.color("&cOnly players can use that command."));
            return true;
        }

        if (!(args.length > 0)) {
            sender.sendMessage(ColorUtils.color("&cUsage: /" + label + " <channelName>"));
            return true;
        }

        String channelName = args[0].toLowerCase();

        ChatSpace chatSpace;
        String nameOverride = "";

        chatSpace = plugin.getChannelRegistry().get(channelName);
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
            sender.sendMessage(ColorUtils.color("&cSorry, but &e" + channelName + "&c is not a registered chat space."));
            return true;
        }

        if (chatSpace instanceof ChatChannel chatChannel) {
            String sendPermission = chatChannel.getSendPermission();
            if (!sendPermission.isEmpty() && !player.hasPermission(sendPermission)) {
                sender.sendMessage(ColorUtils.color("&cYou do not have permission to send messages in " + chatSpace.getName() + "."));
                return true;
            }
        } else if (chatSpace instanceof ChatRoom chatRoom) {
            if (!chatRoom.isMember(player.getUniqueId())) {
                sender.sendMessage(ColorUtils.color("&cYou are not a member of " + chatRoom.getName()));
                return true;
            }
        }

        plugin.setPlayerFocus(player, chatSpace);
        String spaceName = chatSpace.getName();
        if (nameOverride != null && !nameOverride.isEmpty()) {
            spaceName = nameOverride;
        }
        sender.sendMessage(ColorUtils.color("&aSet your chat focus to &b" + spaceName + "."));
        return true;
    }
}
