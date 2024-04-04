package com.stardevllc.starchat.commands;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.pm.PrivateMessage;
import com.stardevllc.starcore.utils.actor.Actor;
import com.stardevllc.starcore.utils.color.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MessageCmd implements CommandExecutor {
    
    private StarChat plugin;

    public MessageCmd(StarChat plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender.hasPermission("starchat.command.message"))) {
            ColorUtils.coloredMessage(sender, plugin.getMainConfig().getString("messages.command.nopermission"));
            return true;
        }
        
        if (!(args.length >= 2)) {
            sender.sendMessage(ColorUtils.color("&cUsage: /" + label + " <target> <message>"));
            return true;
        }

        Actor senderActor = Actor.create(sender);
        Actor targetActor = Actor.create(args[0]);

        if (targetActor == null) {
            sender.sendMessage(ColorUtils.color("&cInvalid target. They must be online, or the console."));
            return true;
        }

        PrivateMessage privateMessage = plugin.getPrivateMessage(senderActor, targetActor);
        if (privateMessage == null) {
            privateMessage = new PrivateMessage(plugin, senderActor, targetActor, plugin.getMainConfig().getString("private-msg-format"));
            plugin.addPrivateMessage(privateMessage);
        }

        StringBuilder msgBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            msgBuilder.append(args[i]).append(" ");
        }

        privateMessage.sendMessage(sender, msgBuilder.toString().trim());
        plugin.assignLastMessage(sender, msgBuilder, privateMessage, senderActor, targetActor);
        return true;
    }
}
