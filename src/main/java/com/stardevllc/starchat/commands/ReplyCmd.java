package com.stardevllc.starchat.commands;

import com.stardevllc.actors.Actor;
import com.stardevllc.actors.PlayerActor;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.pm.PrivateMessage;
import com.stardevllc.starcore.color.ColorHandler;
import com.stardevllc.starcore.utils.cmdflags.ParseResult;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

import static com.stardevllc.starchat.commands.MessageCmd.FOCUS;
import static com.stardevllc.starchat.commands.MessageCmd.flags;

public class ReplyCmd implements TabExecutor {
    
    private StarChat plugin;

    public ReplyCmd(StarChat plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender.hasPermission("starchat.command.reply"))) {
            ColorHandler.getInstance().coloredMessage(sender, plugin.getMainConfig().getString("messages.command.nopermission"));
            return true;
        }

        ParseResult flagResult = flags.parse(args);
        args = flagResult.args();
        
        if (args.length == 0) {
            sender.sendMessage(ColorHandler.getInstance().color("&cUsage: /" + label + " <message>"));
            sender.sendMessage(ColorHandler.getInstance().color("&cUsage: /" + label + " <target> <message>"));
            return true;
        }

        Actor senderActor = Actor.create(sender);
        Actor targetActor = Actor.create(args[0]);

        PrivateMessage privateMessage;
        
        int msgStart;
        if (targetActor != null) {
            privateMessage = plugin.getPrivateMessage(senderActor, targetActor);
            if (privateMessage == null) {
                sender.sendMessage(ColorHandler.getInstance().color("&cYou do not have a conversation open with " + targetActor.getName()));
                return true;
            }
            msgStart = 1;
        } else {
            if (senderActor instanceof PlayerActor playerActor) {
                privateMessage = plugin.getLastMessage(playerActor.getUniqueId());
            } else {
                privateMessage = plugin.getConsoleLastMessage();
            }

            if (privateMessage == null) {
                ColorHandler.getInstance().coloredMessage(sender, "&cYou do not have any active conversations.");
                return true;
            }

            if (privateMessage.getActor1().equals(senderActor)) {
                targetActor = privateMessage.getActor2();
            } else {
                targetActor = privateMessage.getActor1();
            }

            msgStart = 0;
        }

        StringBuilder msgBuilder = new StringBuilder();
        for (int i = msgStart; i < args.length; i++) {
            msgBuilder.append(args[i]).append(" ");
        }

        privateMessage.sendMessage(new ChatContext(sender, msgBuilder.toString().trim()));
        plugin.assignLastMessage(sender, msgBuilder, privateMessage, senderActor, targetActor);
        if (flagResult.isPresent(FOCUS)) {
            if (sender instanceof Player player) {
                plugin.setPlayerFocus(player, privateMessage);
                String spaceName = "Private (" + targetActor.getName() + ")";
                sender.sendMessage(ColorHandler.getInstance().color(plugin.getConfig().getString("messages.command.chat.setfocus").replace("{SPACE}", spaceName)));
            }
        }
        return true;
    }
    
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender.hasPermission("starchat.command.reply"))) {
            return null;
        }

        if (!(sender instanceof Player player)) {
            return null;
        }
        
        if (args.length == 1) {
            PrivateMessage lastMessage = plugin.getLastMessage(player.getUniqueId());
            if (lastMessage == null) {
                return null;
            }
            
            if (lastMessage.getActor1().equals(player)) {
                return List.of(lastMessage.getActor2().getName());
            } else {
                return List.of(lastMessage.getActor1().getName());
            }
        } else if (args.length >= 2) {
            return List.of("<message>");
        }
        
        return null;
    }
}
