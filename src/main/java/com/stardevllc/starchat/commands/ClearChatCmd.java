package com.stardevllc.starchat.commands;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starcore.color.ColorHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClearChatCmd implements CommandExecutor {

    private static final Random RANDOM = new Random();
    private StarChat plugin;
    
    public ClearChatCmd(StarChat plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender.hasPermission("starchat.clearchat"))) {
            ColorHandler.getInstance().coloredMessage(sender, plugin.getMainConfig().getString("messages.command.nopermission"));
            return true;
        }
        
        int lineAmount = plugin.getMainConfig().getInt("clearchat.lineamount");
        String lineChar = plugin.getMainConfig().getString("clearchat.character");
        boolean randomizeChar = plugin.getMainConfig().getBoolean("clearchat.randomize-character-count");

        List<String> lines = new ArrayList<>(lineAmount);
        
        for (int i = 0; i < lineAmount; i++) {
            if (randomizeChar) {
                lines.add(lineChar.repeat(RANDOM.nextInt(30)));
            } else {
                lines.add(lineChar);
            }
        }
        
        String bypassPermission = plugin.getMainConfig().getString("clearchat.bypass-permission");
        boolean checkBypass = bypassPermission != null && !bypassPermission.isBlank();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (checkBypass) {
                if (player.hasPermission(bypassPermission)) {
                    ColorHandler.getInstance().coloredMessage(player, plugin.getMainConfig().getString("messages.command.clearchat.immune").replace("{actor}", sender.getName()));
                    continue;
                }
            }
            
            lines.forEach(player::sendMessage);
            ColorHandler.getInstance().coloredMessage(player, plugin.getMainConfig().getString("messages.command.clearchat.success").replace("{actor}", sender.getName()));
        }
        
        return true;
    }
}
