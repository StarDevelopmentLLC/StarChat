package com.stardevllc.starchat.commands;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.pm.PrivateMessage;
import com.stardevllc.starcore.utils.actor.Actor;
import com.stardevllc.starcore.utils.actor.PlayerActor;
import com.stardevllc.starcore.utils.color.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReplyCmd implements CommandExecutor {
    
    private StarChat plugin;

    public ReplyCmd(StarChat plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender.hasPermission("starchat.command.reply"))) {
            ColorUtils.coloredMessage(sender, plugin.getMainConfig().getString("messages.command.nopermission"));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ColorUtils.color("&cUsage: /" + label + " <message>"));
            sender.sendMessage(ColorUtils.color("&cUsage: /" + label + " <target> <message>"));
            return true;
        }

        Actor senderActor = Actor.create(sender);
        Actor targetActor = Actor.create(args[0]);

        PrivateMessage privateMessage;

        int msgStart;
        if (targetActor != null) {
            privateMessage = plugin.getPrivateMessage(senderActor, targetActor);
            if (privateMessage == null) {
                sender.sendMessage(ColorUtils.color("&cYou do not have a conversation open with " + targetActor.getName()));
                return true;
            }
            msgStart = 1;
        } else {
            if (senderActor instanceof PlayerActor playerActor) {
                privateMessage = plugin.getLastMessage(playerActor.getUniqueId());
            } else {
                privateMessage = plugin.getConsoleLastMessage();
            }

            if (privateMessage.getActor1().equals(senderActor)) {
                targetActor = privateMessage.getActor2();
            } else {
                targetActor = privateMessage.getActor1();
            }

            msgStart = 0;
        }

        if (privateMessage == null) {
            sender.sendMessage(ColorUtils.color("&cYou do not have a message to reply to."));
            return true;
        }

        StringBuilder msgBuilder = new StringBuilder();
        for (int i = msgStart; i < args.length; i++) {
            msgBuilder.append(args[i]).append(" ");
        }

        privateMessage.sendMessage(new ChatContext(sender, msgBuilder.toString().trim()));
        plugin.assignLastMessage(sender, msgBuilder, privateMessage, senderActor, targetActor);
        return true;
    }
}
