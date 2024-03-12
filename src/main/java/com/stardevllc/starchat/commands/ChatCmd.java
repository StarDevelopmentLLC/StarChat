package com.stardevllc.starchat.commands;

import com.stardevllc.starchat.ChatSelector;
import com.stardevllc.starchat.ChatSpace;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starchat.pm.PrivateMessage;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starmclib.actor.Actor;
import com.stardevllc.starmclib.actor.PlayerActor;
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

        Actor senderActor = new PlayerActor(player);
        String channelName = args[0].toLowerCase();

        ChatSpace chatSpace;
        String nameOverride = "";
        if (channelName.equalsIgnoreCase("private")) {
            if (args.length >= 2) {
                Actor targetActor = Actor.create(args[1]);
                if (targetActor == null) {
                    sender.sendMessage(ColorUtils.color("&cInvalid target."));
                    return true;
                }

                chatSpace = plugin.getPrivateMessage(senderActor, targetActor);
                if (chatSpace == null) {
                    sender.sendMessage(ColorUtils.color("You do not have a private conversation with " + targetActor.getName()));
                    return true;
                }
                nameOverride = "Private (" + targetActor.getName() + ")";
            } else {
                PrivateMessage privateMessage = plugin.getLastMessage(((Player) sender).getUniqueId());
                chatSpace = privateMessage;
                if (chatSpace == null) {
                    sender.sendMessage(ColorUtils.color("&cYou do not have a last conversation to use as a focus."));
                    return true;
                }

                Actor other = privateMessage.getActor1().equals(senderActor) ? privateMessage.getActor2() : privateMessage.getActor1();
                nameOverride = "Private (" + other.getName() + ")";
            }
        } else {
            chatSpace = plugin.getChannelRegistry().get(channelName);
            if (chatSpace == null) {
                chatSpace = plugin.getRoomRegistry().get(channelName);
            }
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
