package com.stardevllc.starchat.commands;

import com.stardevllc.actors.Actor;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.pm.PrivateMessage;
import com.stardevllc.starcore.color.ColorHandler;
import com.stardevllc.starcore.utils.cmdflags.CmdFlags;
import com.stardevllc.starcore.utils.cmdflags.Flag;
import com.stardevllc.starcore.utils.cmdflags.ParseResult;
import com.stardevllc.starcore.utils.cmdflags.type.PresenceFlag;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MessageCmd implements TabExecutor {
    
    private StarChat plugin;
    
    protected static final Flag FOCUS = new PresenceFlag("f", "FOCUS");

    protected static final CmdFlags flags = new CmdFlags(FOCUS);

    public MessageCmd(StarChat plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender.hasPermission("starchat.command.message"))) {
            ColorHandler.getInstance().coloredMessage(sender, plugin.getMainConfig().getString("messages.command.nopermission"));
            return true;
        }

        ParseResult flagResult = flags.parse(args);
        args = flagResult.args();
        
        if (!(args.length >= 2)) {
            sender.sendMessage(ColorHandler.getInstance().color("&cUsage: /" + label + " <target> <message>"));
            return true;
        }

        Actor senderActor = Actor.create(sender);
        Actor targetActor = Actor.create(args[0]);

        if (targetActor == null) {
            sender.sendMessage(ColorHandler.getInstance().color("&cInvalid target. They must be online, or the console."));
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
        if (!(sender.hasPermission("starchat.command.message"))) {
            return null;
        }
        
        if (!(sender instanceof Player player)) {
            return null;
        }
        
        if (args.length == 1) {
            String target = args[0].toLowerCase();
            
            List<String> players = new LinkedList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getUniqueId().equals(player.getUniqueId())) {
                    players.add(p.getName());
                }
            }

            players.removeIf(playerName -> !playerName.toLowerCase().startsWith(target));

            Collections.sort(players);
            return players;
        } else if (args.length >= 2) {
            return List.of("<message>");
        }
        
        return null;
    }
}
