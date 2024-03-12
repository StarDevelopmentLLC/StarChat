package com.stardevllc.starchat.commands;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.placeholder.DefaultPlaceholders;
import com.stardevllc.starchat.placeholder.PAPIExpansion;
import com.stardevllc.starchat.placeholder.PAPIPlaceholders;
import com.stardevllc.starmclib.color.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class StarChatAdminCmd implements CommandExecutor {
    
    private StarChat plugin;

    public StarChatAdminCmd(StarChat plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender.hasPermission("starchat.command.admin"))) {
            ColorUtils.coloredMessage(sender, "&cYou do not have permission to use that command."); 
            return true;
        }
        
        if (!(args.length > 0)) {
            ColorUtils.coloredMessage(sender, "&cYou must provide a sub-command."); //TODO Print out help when commands done
            return true;
        }
        
        if (args[0].equalsIgnoreCase("save")) {
            plugin.getMainConfig().save();
            ColorUtils.coloredMessage(sender, "&aSaved config.yml successfully.");
        } else if (args[0].equalsIgnoreCase("reload")) {
            //TODO
            ColorUtils.coloredMessage(sender, "&aReloaded config.yyml successfully.");
        } else if (args[0].equalsIgnoreCase("setconsolenameformat") || args[0].equalsIgnoreCase("setcnf")) {
            if (!(args.length > 1)) {
                ColorUtils.coloredMessage(sender, "&cYou must provide a new console name.");
                return true;
            }
            
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }
            
            String consoleName = sb.toString().trim();
            StarChat.setConsoleNameFormat(consoleName);
            ColorUtils.coloredMessage(sender, "&aSet the new console name format to &r" + consoleName);
        } else if (args[0].equalsIgnoreCase("setprivatemessageformat") || args[0].equalsIgnoreCase("setpmf")) {
            if (!(args.length > 1)) {
                ColorUtils.coloredMessage(sender, "&cYou must provide a new private message format.");
                return true;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }

            String privateMessageFormat = sb.toString().trim();
            StarChat.setPrivateMessageFormat(privateMessageFormat);
            ColorUtils.coloredMessage(sender, "&aSet the new private message format to &r" + privateMessageFormat);
        } else if (args[0].equalsIgnoreCase("setuseplaceholderapi") || args[0].equalsIgnoreCase("setupapi")) {
            if (!(args.length > 1)) {
                ColorUtils.coloredMessage(sender, "&cYou must provide a value");
                return true;
            }

            if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("yes")) {
                if (StarChat.isUsePlaceholderAPI()) {
                    if (plugin.getPapiExpansion() != null && plugin.getPapiExpansion().isRegistered()) {
                        ColorUtils.coloredMessage(sender, "&cPlaceholderAPI is already enabled and configured, no need to set it again.");
                        return true;
                    } else {
                        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
                        if (papi != null && papi.isEnabled()) {
                            plugin.setPapiExpansion(new PAPIExpansion(plugin));
                            plugin.getPapiExpansion().register();
                            StarChat.setPlayerPlaceholders(new PAPIPlaceholders());
                            ColorUtils.coloredMessage(sender, "&aStarChat was configured to use PlaceholderAPI but was not able to load hook at startup, however, PlaceholderAPI was detected on this command and hook has been enabled now.");
                        } else {
                            ColorUtils.coloredMessage(sender, "&cStarChat is configured to use PlaceholderAPI, however, it was not detected, so the hook cannot be registered. Please install PlaceholderAPI and restart the server.");
                        }
                    }
                } else {
                    Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
                    if (papi != null && papi.isEnabled()) {
                        plugin.setPapiExpansion(new PAPIExpansion(plugin));
                        plugin.getPapiExpansion().register();
                        StarChat.setPlayerPlaceholders(new PAPIPlaceholders());
                        plugin.getMainConfig().set("use-placeholderapi", true);
                        ColorUtils.coloredMessage(sender, "&aPlaceholderAPI has been detected and hooked into, StarChat will now respect PlaceholderAPI placeholders.");
                    } else {
                        ColorUtils.coloredMessage(sender, "&cPlaceholderAPI is not detected as a plugin, cannot enable PlaceholderAPI support for StarChat. Please install and restart the server.");
                    }
                }
            } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("no")) {
                if (plugin.getPapiExpansion() == null || !plugin.getPapiExpansion().isRegistered()) {
                    ColorUtils.coloredMessage(sender, "&cPlaceholderAPI support is already disabled.");
                    return true;
                }
                
                plugin.getPapiExpansion().unregister();
                plugin.setPapiExpansion(null);
                StarChat.setPlayerPlaceholders(new DefaultPlaceholders());
                plugin.getMainConfig().set("use-placeholderapi", false);
                ColorUtils.coloredMessage(sender, "&Successfully disabled PlaceholderAPI hook and switched to using default placeholder replacements.");
            } else {
                ColorUtils.coloredMessage(sender, "&cYou must provide true, yes, false, or no.");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("setusecolorpermissions") || args[0].equalsIgnoreCase("setucp")) {
            if (!(args.length > 1)) {
                ColorUtils.coloredMessage(sender, "&cYou must provide a value");
                return true;
            }

            if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("yes")) {
                if (StarChat.isUseColorPermissions()) {
                    ColorUtils.coloredMessage(sender, "&cUsage of color permissions is already enabled.");
                    return true;
                }
                StarChat.setUseColorPermissions(true);
                ColorUtils.coloredMessage(sender, "&aYou enabled the use of color based permissions.");
            } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("no")) {
                if (!StarChat.isUseColorPermissions()) {
                    ColorUtils.coloredMessage(sender, "&cUsage of color permissions is already disabled.");
                    return true;
                }
                StarChat.setUseColorPermissions(false);
                ColorUtils.coloredMessage(sender, "&aYou disabled the use of color based permissions.");
            } else {
                ColorUtils.coloredMessage(sender, "&cYou must provide true, yes, false, or no.");
                return true;
            }
        }
        
        return true;
    }
}
