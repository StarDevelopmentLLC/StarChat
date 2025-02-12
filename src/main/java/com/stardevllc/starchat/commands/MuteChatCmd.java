package com.stardevllc.starchat.commands;

import com.stardevllc.actors.Actor;
import com.stardevllc.colors.StarColors;
import com.stardevllc.starchat.StarChat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MuteChatCmd implements CommandExecutor {
    private StarChat plugin;

    public MuteChatCmd(StarChat plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("starchat.mutechat")) {
            StarColors.coloredMessage(sender, plugin.getMainConfig().getString("messages.command.nopermission"));
            return true;
        }

        Actor actor = Actor.create(sender);
        
        StringBuilder reason = new StringBuilder();

        for (String arg : args) {
            reason.append(arg).append(" ");
        }
        
        if (!reason.isEmpty()) {
            reason.deleteCharAt(reason.length() - 1);
        }

        plugin.getMuteChat().mute(actor, reason.toString());
        return true;
    }
}
