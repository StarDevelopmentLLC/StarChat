package com.stardevllc.starchat.commands;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.pm.PrivateMessage;
import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starmclib.actors.Actor;
import com.stardevllc.starmclib.actors.PlayerActor;
import com.stardevllc.starmclib.cmdflags.FlagResult;
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
        if (!sender.hasPermission("starchat.command.reply")) {
            StarColors.coloredMessage(sender, plugin.getMainConfig().getString("messages.command.nopermission"));
            return true;
        }

        FlagResult flagResult = flags.parse(args);
        args = flagResult.args();
        
        if (args.length == 0) {
            sender.sendMessage(StarColors.color("&cUsage: /" + label + " <message>"));
            sender.sendMessage(StarColors.color("&cUsage: /" + label + " <target> <message>"));
            return true;
        }

        Actor senderActor = Actor.create(sender);
        Actor targetActor = Actor.create(args[0]);
        
        if (targetActor != null && !senderActor.canSee(targetActor) && !senderActor.hasPermission("starchat.privatemessage.visibility.bypass")) {
            sender.sendMessage(StarColors.color(plugin.getMainConfig().getString("messages.command.invalidtarget")));
            return true;
        }

        PrivateMessage privateMessage;
        
        int msgStart;
        if (targetActor != null) {
            privateMessage = plugin.getPrivateMessage(senderActor, targetActor);
            if (privateMessage == null) {
                sender.sendMessage(StarColors.color(plugin.getMainConfig().getString("messages.command.reply.noopenconversation").replace("{target}", targetActor.getName())));
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
                StarColors.coloredMessage(sender, plugin.getMainConfig().getString("messages.command.reply.noactiveconversations"));
                return true;
            }

            
            if (privateMessage.getActor1().equals(senderActor)) {
                targetActor = privateMessage.getActor2();
            } else {
                targetActor = privateMessage.getActor1();
            }
            
            if (!senderActor.canSee(targetActor) && !senderActor.hasPermission("starchat.privatemessage.visibility.bypass")) {
                StarColors.coloredMessage(sender, plugin.getMainConfig().getString("messages.command.invalidtarget"));
                return true;
            }

            msgStart = 0;
        }

        StringBuilder msgBuilder = new StringBuilder();
        for (int i = msgStart; i < args.length; i++) {
            msgBuilder.append(args[i]).append(" ");
        }
        
        ChatContext context = new ChatContext(sender, msgBuilder.toString().trim());
        privateMessage.sendMessage(context);
        privateMessage.sendToConsole(context.getFinalMessage());
        plugin.assignLastMessage(sender, msgBuilder, privateMessage, senderActor, targetActor);
        if (flagResult.isPresent(FOCUS)) {
            if (sender instanceof Player player) {
                plugin.setPlayerFocus(player, privateMessage);
                String spaceName = "Private (" + targetActor.getName() + ")";
                sender.sendMessage(StarColors.color(plugin.getConfig().getString("messages.command.chat.setfocus").replace("{SPACE}", spaceName)));
            }
        }
        return true;
    }
    
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("starchat.command.reply")) {
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
                if (lastMessage.getActor1().canSee(lastMessage.getActor2()) || lastMessage.getActor1().hasPermission("starchat.privatemessage.visibility.bypass")) {
                    return List.of(lastMessage.getActor2().getName());
                }
            } else {
                if (lastMessage.getActor2().canSee(lastMessage.getActor1()) || lastMessage.getActor2().hasPermission("starchat.privatemessage.visibility.bypass")) {
                    return List.of(lastMessage.getActor1().getName());
                }
            }
        } else if (args.length >= 2) {
            return List.of("<message>");
        }
        
        return null;
    }
}
