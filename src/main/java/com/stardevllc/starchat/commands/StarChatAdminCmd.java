package com.stardevllc.starchat.commands;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starchat.placeholder.DefaultPlaceholders;
import com.stardevllc.starchat.placeholder.PAPIExpansion;
import com.stardevllc.starchat.placeholder.PAPIPlaceholders;
import com.stardevllc.starchat.pm.PrivateMessage;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starmclib.Config;
import com.stardevllc.starmclib.actor.Actor;
import com.stardevllc.starmclib.actor.PlayerActor;
import com.stardevllc.starmclib.color.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class StarChatAdminCmd implements CommandExecutor {

    private StarChat plugin;
    private Config pluginConfig;

    public StarChatAdminCmd(StarChat plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getMainConfig();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender.hasPermission("starchat.command.admin"))) {
            ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
            return true;
        }

        if (!(args.length > 0)) {
            ColorUtils.coloredMessage(sender, "&cYou must provide a sub-command."); //TODO Print out help when commands done
            return true;
        }

        if (args[0].equalsIgnoreCase("save")) {
            if (!sender.hasPermission("starchat.command.admin.save")) {
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            plugin.getMainConfig().save();
            ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.savesuccess"));
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("starchat.command.admin.reload")) {
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }

            plugin.reload(false);
            ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.reloadsuccess"));
        } else if (args[0].equalsIgnoreCase("setconsolenameformat") || args[0].equalsIgnoreCase("setcnf")) {
            if (!sender.hasPermission("starchat.command.admin.setconsolenameformat")) {
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
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
            ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setconsolename").replace("{NEWNAME}", consoleName));
        } else if (args[0].equalsIgnoreCase("setprivatemessageformat") || args[0].equalsIgnoreCase("setpmf")) {
            if (!sender.hasPermission("starchat.command.admin.setprivatemessageformat")) {
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
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
            sender.sendMessage(ColorUtils.color("&aSet the new private message format to &r") + privateMessageFormat);
        } else if (args[0].equalsIgnoreCase("setuseplaceholderapi") || args[0].equalsIgnoreCase("setupapi")) {
            if (!sender.hasPermission("starchat.command.admin.setuseplaceholderapi")) {
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            if (!(args.length > 1)) {
                ColorUtils.coloredMessage(sender, "&cYou must provide a value");
                return true;
            }

            if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("yes")) {
                if (StarChat.isUsePlaceholderAPI()) {
                    if (plugin.getPapiExpansion() != null && plugin.getPapiExpansion().isRegistered()) {
                        ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.alreadyconfigandenabled"));
                        return true;
                    } else {
                        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
                        if (papi != null && papi.isEnabled()) {
                            plugin.setPapiExpansion(new PAPIExpansion(plugin));
                            plugin.getPapiExpansion().register();
                            StarChat.setPlayerPlaceholders(new PAPIPlaceholders());
                            ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.configbutnotenabled"));
                        } else {
                            ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.configbutnotdetected"));
                        }
                    }
                } else {
                    Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
                    if (papi != null && papi.isEnabled()) {
                        plugin.setPapiExpansion(new PAPIExpansion(plugin));
                        plugin.getPapiExpansion().register();
                        StarChat.setPlayerPlaceholders(new PAPIPlaceholders());
                        plugin.getMainConfig().set("use-placeholderapi", true);
                        StarChat.setUsePlaceholderAPI(true);
                        ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.detectedandenabled"));
                    } else {
                        ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.notdetectednotenabled"));
                    }
                }
            } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("no")) {
                if (plugin.getPapiExpansion() == null || !plugin.getPapiExpansion().isRegistered()) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setuseapi.alreadydisabled"));
                    return true;
                }

                plugin.getPapiExpansion().unregister();
                plugin.setPapiExpansion(null);
                StarChat.setPlayerPlaceholders(new DefaultPlaceholders());
                plugin.getMainConfig().set("use-placeholderapi", false);
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.disabledsuccess"));
            } else {
                ColorUtils.coloredMessage(sender, "&cYou must provide true, yes, false, or no.");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("setusecolorpermissions") || args[0].equalsIgnoreCase("setucp")) {
            if (!sender.hasPermission("starchat.command.admin.setusecolorpermissions")) {
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            if (!(args.length > 1)) {
                ColorUtils.coloredMessage(sender, "&cYou must provide a value");
                return true;
            }

            if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("yes")) {
                if (StarChat.isUseColorPermissions()) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusecolorperms.alreadyenabled"));
                    return true;
                }
                StarChat.setUseColorPermissions(true);
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusecolorperms.enabled"));
            } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("no")) {
                if (!StarChat.isUseColorPermissions()) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusecolorperms.alreadydisabled"));
                    return true;
                }
                StarChat.setUseColorPermissions(false);
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusecolorperms.disabled"));
            } else {
                ColorUtils.coloredMessage(sender, "&cYou must provide true, yes, false, or no.");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("starchat.command.admin.list")) {
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            if (!(args.length > 1)) {
                ColorUtils.coloredMessage(sender, "&cUsage: /" + label + " list <all|channels|rooms|conversations>");
                return true;
            }

            if (args[1].equalsIgnoreCase("all")) {
                if (!sender.hasPermission("starchat.command.admin.list.all")) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.list.all.header"));
                if (sender.hasPermission("starchat.command.admin.list.channels")) {
                    listChannels(sender);
                }

                if (sender.hasPermission("starchat.command.admin.list.rooms")) {
                    listRooms(sender);
                }

                if (sender.hasPermission("starchat.command.admin.list.conversations")) {
                    listConversations(sender);
                }
            } else if (args[1].equalsIgnoreCase("channels")) {
                if (!(sender.hasPermission("starchat.command.admin.list.channels") || sender.hasPermission("starchat.command.admin.list.all"))) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.list.channels.header"));
                listChannels(sender);
            } else if (args[1].equalsIgnoreCase("rooms")) {
                if (!(sender.hasPermission("starchat.command.admin.list.rooms") || sender.hasPermission("starchat.command.admin.list.all"))) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.list.rooms.header"));
                listRooms(sender);
            } else if (args[1].equalsIgnoreCase("conversations")) {
                if (!(sender.hasPermission("starchat.command.admin.list.conversations") || sender.hasPermission("starchat.command.admin.list.all"))) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.admin.list.conversations.header"));
                listConversations(sender);
            }
        } else if (args[0].equalsIgnoreCase("setplayerchatfocus") || args[0].equalsIgnoreCase("setplayerfocus") || args[0].equalsIgnoreCase("setfocus")) {
            if (!(sender.hasPermission("starchat.command.admin.setplayerchatfocus"))) {
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }

            if (!(args.length > 2)) {
                ColorUtils.coloredMessage(sender, "&cUsage: /" + label + " " + args[0] + " <player> <chatspace>");
                return true;
            }

            Actor target = Actor.create(args[1]);
            if (target == null) {
                ColorUtils.coloredMessage(sender, "&cInvalid target, are they online?");
                return true;
            }

            if (!target.isPlayer()) {
                ColorUtils.coloredMessage(sender, "&cTarget must be a player.");
                return true;
            }

            if (!target.isOnline()) {
                ColorUtils.coloredMessage(sender, "&cTarget must be online to set the chat focus.");
                return true;
            }

            Player targetPlayer = ((PlayerActor) target).getPlayer();

            ChatChannel chatChannel = plugin.getChannelRegistry().get(args[2]);
            if (chatChannel == null) {
                ColorUtils.coloredMessage(sender, "&cThat is not a valid channel.");
                return true;
            }

            plugin.setPlayerFocus(targetPlayer, chatChannel);
            ColorUtils.coloredMessage(sender, "&eYou set &b" + targetPlayer.getName() + "'s &echat focus to &d" + chatChannel.getName());
            ColorUtils.coloredMessage(targetPlayer, "&eYour chat focus was changed to &d" + chatChannel.getName() + " &eby &b" + Actor.create(sender).getName());
        } else if (args[0].equalsIgnoreCase("channel")) {
            if (!(sender.hasPermission("starchat.command.admin.channel"))) {
                ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }

            if (!(args.length > 2)) {
                ColorUtils.coloredMessage(sender, "&cUsage: /" + label + " channel <[channelName]|create|delete> <args>");
                return true;
            }

            if (args[1].equalsIgnoreCase("create")) {
                if (!(sender.hasPermission("starchat.command.admin.channel.create"))) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }

                String channelName = sb.toString().trim();

                File file = new File(plugin.getDataFolder() + File.separator + "channels" + File.separator + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', channelName)).toLowerCase().replace(" ", "_") + ".yml");
                ChatChannel chatChannel = new ChatChannel(plugin, channelName, file);
                plugin.getChannelRegistry().register(chatChannel.getSimplifiedName(), chatChannel);
                ColorUtils.coloredMessage(sender, "&aCreated a new channel called " + channelName);
                return true;
            }

            ChatChannel chatChannel = plugin.getChannelRegistry().get(args[1]);
            if (chatChannel == null) {
                ColorUtils.coloredMessage(sender, "&cThat is not a registered chat channel.");
                return true;
            }

            if (args[1].equalsIgnoreCase("delete")) {
                if (!(sender.hasPermission("starchat.command.admin.channel.delete"))) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }

                if (!chatChannel.getPlugin().getName().equalsIgnoreCase(plugin.getName())) {
                    ColorUtils.coloredMessage(sender, "&cYou can only delete chat channels owned by StarChat.");
                    return true;
                }

                chatChannel.getFile().delete();
                plugin.getChannelRegistry().deregister(chatChannel.getSimplifiedName());
                ColorUtils.coloredMessage(sender, "&eYou deleted the chat channel &b" + chatChannel.getName());
            }

            if (!chatChannel.getPlugin().getName().equalsIgnoreCase(plugin.getName())) {
                ColorUtils.coloredMessage(sender, "&cYou can only modify chat channels owned by StarChat.");
                return true;
            }

            if (!(args.length > 3)) {
                ColorUtils.coloredMessage(sender, "&cUsage: /" + label + " " + args[0] + " " + args[1] + " <subcommand> <arguments>");
                return true;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }

            String value = sb.toString().trim();

            if (args[2].equalsIgnoreCase("setname")) {
                if (!(sender.hasPermission("starchat.command.admin.channel.setname"))) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                String oldName = chatChannel.getName();
                plugin.getChannelRegistry().deregister(chatChannel.getSimplifiedName());
                chatChannel.setName(value);
                plugin.getChannelRegistry().register(chatChannel.getSimplifiedName(), chatChannel);
                ColorUtils.coloredMessage(sender, "&eSet &b" + oldName + "'s &enew name to &d" + chatChannel.getName());
            } else if (args[2].equalsIgnoreCase("setsenderformat")) {
                if (!(sender.hasPermission("starchat.command.admin.channel.setsenderformat"))) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                chatChannel.setSenderFormat(value);
                sender.sendMessage(ColorUtils.color("&eSet &b" + chatChannel.getSimplifiedName() + "'s &esender format to: &r") + chatChannel.getSenderFormat());
            } else if (args[2].equalsIgnoreCase("setsystemformat")) {
                if (!(sender.hasPermission("starchat.command.admin.channel.setsystemformat"))) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                chatChannel.setSystemFormat(value);
                sender.sendMessage(ColorUtils.color("&eSet &b" + chatChannel.getSimplifiedName() + "'s &esystem format to: &r") + chatChannel.getSystemFormat());
            } else if (args[2].equalsIgnoreCase("setdisplaynameformat")) {
                if (!(sender.hasPermission("starchat.command.admin.channel.setdisplaynameformat"))) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                chatChannel.setPlayerDisplayNameFormat(value);
                sender.sendMessage(ColorUtils.color("&eSet &b" + chatChannel.getSimplifiedName() + "'s &esystem format to: &r") + chatChannel.getPlayerDisplayNameFormat());
            } else if (args[2].equalsIgnoreCase("setaffectedbypunishments")) {
                if (!(sender.hasPermission("starchat.command.admin.channel.setaffectedbypunishments"))) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                boolean abpValue = Boolean.parseBoolean(value);
                chatChannel.setAffectedByPunishments(abpValue);
                ColorUtils.coloredMessage(sender, "&eSet &b" + chatChannel.getSimplifiedName() + "'s &eaffected by punishments value to: &d" + chatChannel.isAffectedByPunishments());
            } else if (args[2].equalsIgnoreCase("setviewpermission")) {
                if (!(sender.hasPermission("starchat.command.admin.channel.setviewpermission"))) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                chatChannel.setViewPermission(value);
                ColorUtils.coloredMessage(sender, "&eSet &b" + chatChannel.getSimplifiedName() + "'s &eview permission to &d" + value);
            } else if (args[2].equalsIgnoreCase("setsendpermission")) {
                if (!(sender.hasPermission("starchat.command.admin.channel.setsendpermission"))) {
                    ColorUtils.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                chatChannel.setSendPermission(value);
                ColorUtils.coloredMessage(sender, "&eSet &b" + chatChannel.getSimplifiedName() + "'s &esend permission to &d" + value);
            } else {
                ColorUtils.coloredMessage(sender, "&cInvalid subcommand.");
                return true;
            }

            chatChannel.saveConfig();
        }
        return true;
    }

    private void listChannels(CommandSender sender) {
        for (ChatChannel chatChannel : plugin.getChannelRegistry()) {
            ColorUtils.coloredMessage(sender, " &8- &eChannel &b" + chatChannel.getSimplifiedName() + " &eowned by the plugin &d" + chatChannel.getPlugin().getName());
        }
    }

    private void listRooms(CommandSender sender) {
        for (ChatRoom chatRoom : plugin.getRoomRegistry()) {
            ColorUtils.coloredMessage(sender, " &8- &eRoom &b" + chatRoom.getSimplifiedName() + " &eowned by the plugin &d" + chatRoom.getPlugin().getName());
        }
    }

    private void listConversations(CommandSender sender) {
        for (PrivateMessage privateMessage : plugin.getPrivateMessages()) {
            ColorUtils.coloredMessage(sender, " &8- &eConversation between &b" + privateMessage.getActor1().getName() + " &end &b" + privateMessage.getActor2().getName());
        }
    }
}
