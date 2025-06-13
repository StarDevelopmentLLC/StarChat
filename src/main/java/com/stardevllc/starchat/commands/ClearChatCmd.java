package com.stardevllc.starchat.commands;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starcore.api.cmdflags.*;
import com.stardevllc.starcore.api.cmdflags.type.ComplexFlag;
import com.stardevllc.starcore.api.cmdflags.type.PresenceFlag;
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
    
    private static final Flag RANDOMIZE = new PresenceFlag("r", "RANDOMIZE");
    private static final Flag BYPASS_OVERRIDE = new PresenceFlag("bo", "BYPASS_OVERRIDE");
    private static final Flag LINE_AMOUNT = new ComplexFlag("l", "LINE_AMOUNT", null);
    private static final Flag LINE_CHARACTER = new ComplexFlag("c", "LINE_CHARACTER", null);

    private static final CmdFlags flags = new CmdFlags(RANDOMIZE, BYPASS_OVERRIDE, LINE_AMOUNT, LINE_CHARACTER);

    private StarChat plugin;
    
    public ClearChatCmd(StarChat plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("starchat.clearchat")) {
            StarColors.coloredMessage(sender, plugin.getMainConfig().getString("messages.command.nopermission"));
            return true;
        }

        FlagResult flagResult = flags.parse(args);

        int lineAmount;
        if (flagResult.getValue(LINE_AMOUNT) != null) {
            if (sender.hasPermission("starchat.clearchat.flags.amount")) {
                lineAmount = Integer.parseInt((String) flagResult.getValue(LINE_AMOUNT));
            } else {
                StarColors.coloredMessage(sender, "&cYou do not have permission to use the -" + LINE_AMOUNT.id() + " flag, defaulting to config value");
                lineAmount = plugin.getMainConfig().getInt("clearchat.lineamount");
            }
        } else {
            lineAmount = plugin.getMainConfig().getInt("clearchat.lineamount");
        }
        
        String lineChar;
        if (flagResult.getValue(LINE_CHARACTER) != null) {
            if (sender.hasPermission("starchat.clearchat.flags.character")) {
                lineChar = (String) flagResult.getValue(LINE_CHARACTER);
            } else {
                StarColors.coloredMessage(sender, "&cYou do not have permission to use the -" + LINE_CHARACTER.id() + " flag, defaulting to config value");
                lineChar = plugin.getMainConfig().getString("clearchat.character");
            }
        } else {
            lineChar = plugin.getMainConfig().getString("clearchat.character");
        }
        
        boolean randomizeChar;
        if (flagResult.isPresent(RANDOMIZE)) {
            if (sender.hasPermission("starchat.clearchat.flags.randomize")) {
                randomizeChar = true;
            } else {
                StarColors.coloredMessage(sender, "&cYou do not have permission to use the -" + RANDOMIZE.id() + " flag, defaulting to config value");
                randomizeChar = plugin.getMainConfig().getBoolean("clearchat.randomize-character-count");
            }
        } else {
            randomizeChar = plugin.getMainConfig().getBoolean("clearchat.randomize-character-count");
        }

        List<String> lines = new ArrayList<>(lineAmount);
        
        for (int i = 0; i < lineAmount; i++) {
            if (randomizeChar) {
                lines.add(lineChar.repeat(RANDOM.nextInt(30)));
            } else {
                lines.add(lineChar);
            }
        }
        
        String bypassPermission = plugin.getMainConfig().getString("clearchat.bypass-permission");
        
        boolean bypassOverride;
        if (flagResult.isPresent(BYPASS_OVERRIDE)) {
            if (sender.hasPermission("starchat.clearchat.flags.bypassoverride")) {
                bypassOverride = true;
            } else {
                StarColors.coloredMessage(sender, "&cYou do not have permission to use the -" + BYPASS_OVERRIDE.id() + " flag, ignoring");
                bypassOverride = false;
            }
        } else {
            bypassOverride = false;
        }
        
        boolean checkBypass = bypassPermission != null && !bypassPermission.isBlank() && !bypassOverride;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (checkBypass) {
                if (player.hasPermission(bypassPermission)) {
                    StarColors.coloredMessage(player, plugin.getMainConfig().getString("messages.command.clearchat.immune").replace("{actor}", sender.getName()));
                    continue;
                }
            }
            
            lines.forEach(player::sendMessage);
            StarColors.coloredMessage(player, plugin.getMainConfig().getString("messages.command.clearchat.success").replace("{actor}", sender.getName()));
        }
        
        return true;
    }
}
