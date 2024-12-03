package com.stardevllc.starchat.commands;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starcore.color.ColorHandler;
import com.stardevllc.starcore.utils.cmdflags.CmdFlags;
import com.stardevllc.starcore.utils.cmdflags.Flag;
import com.stardevllc.starcore.utils.cmdflags.type.ComplexFlag;
import com.stardevllc.starcore.utils.cmdflags.type.PresenceFlag;
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
    
    private static final Flag RANDOMIZE = new PresenceFlag("r", "RANDOMIZE");
    private static final Flag BYPASS_OVERRIDE = new PresenceFlag("bo", "BYPASS_OVERRIDE");
    private static final Flag LINE_AMOUNT = new ComplexFlag("l", "LINE_AMOUNT", null);
    private static final Flag LINE_CHARACTER = new ComplexFlag("c", "LINE_CHARACTER", null);
    
    public ClearChatCmd(StarChat plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender.hasPermission("starchat.clearchat"))) {
            ColorHandler.getInstance().coloredMessage(sender, plugin.getMainConfig().getString("messages.command.nopermission"));
            return true;
        }

        CmdFlags flags = new CmdFlags(RANDOMIZE, BYPASS_OVERRIDE, LINE_AMOUNT, LINE_CHARACTER);
        flags.parse(args);
        
        int lineAmount;
        if (flags.getFlagValues().get(LINE_AMOUNT) != null) {
            if (sender.hasPermission("starchat.clearchat.flags.amount")) {
                lineAmount = Integer.parseInt((String) flags.getFlagValues().get(LINE_AMOUNT));
            } else {
                ColorHandler.getInstance().coloredMessage(sender, "&cYou do not have permission to use the -" + LINE_AMOUNT.id() + " flag, defaulting to config value");
                lineAmount = plugin.getMainConfig().getInt("clearchat.lineamount");
            }
        } else {
            lineAmount = plugin.getMainConfig().getInt("clearchat.lineamount");
        }
        
        String lineChar;
        if (flags.getFlagValues().get(LINE_CHARACTER) != null) {
            if (sender.hasPermission("starchat.clearchat.flags.character")) {
                lineChar = (String) flags.getFlagValues().get(LINE_CHARACTER);
            } else {
                ColorHandler.getInstance().coloredMessage(sender, "&cYou do not have permission to use the -" + LINE_CHARACTER.id() + " flag, defaulting to config value");
                lineChar = plugin.getMainConfig().getString("clearchat.character");
            }
        } else {
            lineChar = plugin.getMainConfig().getString("clearchat.character");
        }
        
        boolean randomizeChar;
        if ((boolean) flags.getFlagValues().get(RANDOMIZE)) {
            if (sender.hasPermission("starchat.clearchat.flags.randomize")) {
                randomizeChar = (boolean) flags.getFlagValues().get(RANDOMIZE);
            } else {
                ColorHandler.getInstance().coloredMessage(sender, "&cYou do not have permission to use the -" + RANDOMIZE.id() + " flag, defaulting to config value");
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
        if ((boolean) flags.getFlagValues().get(BYPASS_OVERRIDE)) {
            if (sender.hasPermission("starchat.clearchat.flags.bypassoverride")) {
                bypassOverride = (boolean) flags.getFlagValues().get(BYPASS_OVERRIDE);
            } else {
                ColorHandler.getInstance().coloredMessage(sender, "&cYou do not have permission to use the -" + BYPASS_OVERRIDE.id() + " flag, ignoring");
                bypassOverride = false;
            }
        } else {
            bypassOverride = false;
        }
        
        boolean checkBypass = bypassPermission != null && !bypassPermission.isBlank() && !bypassOverride;
        
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
