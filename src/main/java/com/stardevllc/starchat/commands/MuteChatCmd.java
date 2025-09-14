package com.stardevllc.starchat.commands;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starlib.dependency.Inject;
import com.stardevllc.starmclib.actors.Actor;
import com.stardevllc.starmclib.actors.Actors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MuteChatCmd implements CommandExecutor {
    @Inject
    private StarChat plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("starchat.mutechat")) {
            StarColors.coloredMessage(sender, plugin.getMainConfig().getString("messages.command.nopermission"));
            return true;
        }

        Actor actor = Actors.create(sender);
        
        StringBuilder reason = new StringBuilder();

        for (String arg : args) {
            reason.append(arg).append(" ");
        }
        
        if (!reason.isEmpty()) {
            reason.deleteCharAt(reason.length() - 1);
        }
        
        if (!plugin.getMuteChat().isMuted()) {
            plugin.getMuteChat().mute(actor, reason.toString());
        } else {
            plugin.getMuteChat().unmute();
        }
        return true;
    }
}
