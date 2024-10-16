package com.stardevllc.starchat.commands;

import com.stardevllc.converter.string.StringConverters;
import com.stardevllc.helper.ReflectionHelper;
import com.stardevllc.observable.Property;
import com.stardevllc.property.BooleanProperty;
import com.stardevllc.property.StringProperty;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starchat.placeholder.DefaultPlaceholders;
import com.stardevllc.starchat.placeholder.PAPIExpansion;
import com.stardevllc.starchat.placeholder.PAPIPlaceholders;
import com.stardevllc.starchat.pm.PrivateMessage;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starcore.actor.Actor;
import com.stardevllc.starcore.actor.PlayerActor;
import com.stardevllc.starcore.color.ColorHandler;
import com.stardevllc.starcore.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class StarChatAdminCmd implements TabExecutor {

    private StarChat plugin;
    private Config pluginConfig;

    public StarChatAdminCmd(StarChat plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getMainConfig();
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender.hasPermission("starchat.command.admin"))) {
            return null;
        }

        if (args.length == 0) {
            return null;
        }

        List<String> completions = new LinkedList<>();
        String arg = "";
        boolean noSort = false;
        if (args.length == 1) {
            completions.addAll(List.of("save", "reload", "setconsolenameformat", "setprivatemessageformat", "setuseplaceholderapi", "setusecolorpermissions", "list", "setplayerchatfocus", "channel", "renameglobalchannel", "setusestaffchannel"));

            completions.removeIf(option -> !sender.hasPermission("starchat.command.admin." + option));

            arg = args[0].toLowerCase();
        } else if (args[0].equalsIgnoreCase("setconsolenameformat") || args[0].equalsIgnoreCase("setcnf")) {
            if (!sender.hasPermission("starchat.command.admin.setconsolenameformat")) {
                return null;
            }

            completions.add("<consolename>");
            arg = args[1].toLowerCase();
        } else if (args[0].equalsIgnoreCase("setprivatemessageformat") || args[0].equalsIgnoreCase("setpmf")) {
            if (!sender.hasPermission("starchat.command.admin.setprivatemessageformat")) {
                return null;
            }

            completions.add("<pmformat>");
            arg = args[1].toLowerCase();
        } else if (args[0].equalsIgnoreCase("setuseplaceholderapi") || args[0].equalsIgnoreCase("setupapi")) {
            if (!sender.hasPermission("starchat.command.admin.setuseplaceholderapi")) {
                return null;
            }

            completions.addAll(List.of("true", "yes", "false", "no"));
            arg = args[1].toLowerCase();
        } else if (args[0].equalsIgnoreCase("setusecolorpermissions") || args[0].equalsIgnoreCase("setucp")) {
            if (!sender.hasPermission("starchat.command.admin.setusecolorpermissions")) {
                return null;
            }

            completions.addAll(List.of("true", "yes", "false", "no"));
            arg = args[1].toLowerCase();
        } else if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("starchat.command.admin.list")) {
                return null;
            }

            completions.addAll(List.of("all", "channels", "rooms", "conversations"));

            completions.removeIf(option -> !sender.hasPermission("starchat.command.admin.list." + option));

            arg = args[1].toLowerCase();
        } else if (args[0].equalsIgnoreCase("setplayerchatfocus") || args[0].equalsIgnoreCase("setplayerfocus") || args[0].equalsIgnoreCase("setfocus")) {
            if (!(sender.hasPermission("starchat.command.admin.setplayerchatfocus"))) {
                return null;
            }

            if (args.length == 2) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
                arg = args[1].toLowerCase();
            } else if (args.length == 3) {
                for (ChatChannel chatChannel : plugin.getChannelRegistry()) {
                    completions.add(chatChannel.getName());
                }
                arg = args[2].toLowerCase();
            }
        } else if (args[0].equalsIgnoreCase("channel")) {
            if (!(sender.hasPermission("starchat.command.admin.channel"))) {
                return null;
            }

            if (args.length == 2) {
                completions.add("create");
                for (ChatChannel chatChannel : plugin.getChannelRegistry()) {
                    if (chatChannel.getPlugin().getName().equalsIgnoreCase(this.plugin.getName())) {
                        completions.add(chatChannel.getName());
                    }
                }
                arg = args[1].toLowerCase();
                noSort = true;
            } else if (args[1].equalsIgnoreCase("create")) {
                if (!(sender.hasPermission("starchat.command.admin.channel.create"))) {
                    return null;
                }

                // /starchat channel create <name>
                if (args.length == 3) {
                    completions.add("<name>");
                    arg = args[2].toLowerCase();
                }
            } else {
                ChatChannel chatChannel = plugin.getChannelRegistry().get(args[1].toLowerCase());
                if (chatChannel == null) {
                    return null;
                }
                
                if (args.length == 3) {
                    // /starchat channel <name> <option>
                    completions.addAll(List.of("delete", "set"));
                    arg = args[2].toLowerCase();
                } else if (args[2].equalsIgnoreCase("set")) {
                    // /starchat channel <name> set <property> <value>
                    
                    if (args.length == 4) {
                        for (Field field : ReflectionHelper.getClassFields(chatChannel.getClass())) {
                            if (Property.class.isAssignableFrom(field.getType())) {
                                completions.add(field.getName().toLowerCase());
                            }
                        }
                        arg = args[3].toLowerCase();
                    } else if (args.length == 5) {
                        completions.add("<value>");
                        arg = args[4].toLowerCase();
                    }
                }
            }
        }

        String finalArg = arg;
        completions.removeIf(option -> !option.toLowerCase().startsWith(finalArg));
        
        if (!noSort) {
            Collections.sort(completions);
        }

        return completions;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender.hasPermission("starchat.command.admin"))) {
            ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
            return true;
        }

        if (!(args.length > 0)) {
            ColorHandler.getInstance().coloredMessage(sender, "&cYou must provide a sub-command."); //TODO Print out help when commands done
            return true;
        }

        if (args[0].equalsIgnoreCase("save")) {
            if (!sender.hasPermission("starchat.command.admin.save")) {
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            plugin.getMainConfig().save();
            ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.savesuccess"));
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("starchat.command.admin.reload")) {
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }

            plugin.reload(false);
            ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.reloadsuccess"));
        } else if (args[0].equalsIgnoreCase("setconsolenameformat") || args[0].equalsIgnoreCase("setcnf")) {
            if (!sender.hasPermission("starchat.command.admin.setconsolenameformat")) {
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            if (!(args.length > 1)) {
                ColorHandler.getInstance().coloredMessage(sender, "&cYou must provide a new console name.");
                return true;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }

            String consoleName = sb.toString().trim();
            plugin.setConsoleNameFormat(consoleName);
            ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.setconsolename").replace("{NEWNAME}", consoleName));
        } else if (args[0].equalsIgnoreCase("setprivatemessageformat") || args[0].equalsIgnoreCase("setpmf")) {
            if (!sender.hasPermission("starchat.command.admin.setprivatemessageformat")) {
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            if (!(args.length > 1)) {
                ColorHandler.getInstance().coloredMessage(sender, "&cYou must provide a new private message format.");
                return true;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }

            String privateMessageFormat = sb.toString().trim();
            plugin.setPrivateMessageFormat(privateMessageFormat);
            sender.sendMessage(ColorHandler.getInstance().color("&aSet the new private message format to &r") + privateMessageFormat);
        } else if (args[0].equalsIgnoreCase("setuseplaceholderapi") || args[0].equalsIgnoreCase("setupapi")) {
            if (!sender.hasPermission("starchat.command.admin.setuseplaceholderapi")) {
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            if (!(args.length > 1)) {
                ColorHandler.getInstance().coloredMessage(sender, "&cYou must provide a value");
                return true;
            }

            if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("yes")) {
                if (plugin.isUsePlaceholderAPI()) {
                    if (plugin.getPapiExpansion() != null && plugin.getPapiExpansion().isRegistered()) {
                        ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.alreadyconfigandenabled"));
                        return true;
                    } else {
                        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
                        if (papi != null && papi.isEnabled()) {
                            plugin.setPapiExpansion(new PAPIExpansion(plugin));
                            plugin.getPapiExpansion().register();
                            plugin.setPlaceholderHandler(new PAPIPlaceholders());
                            ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.configbutnotenabled"));
                        } else {
                            ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.configbutnotdetected"));
                        }
                    }
                } else {
                    Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
                    if (papi != null && papi.isEnabled()) {
                        plugin.setPapiExpansion(new PAPIExpansion(plugin));
                        plugin.getPapiExpansion().register();
                        plugin.setPlaceholderHandler(new PAPIPlaceholders());
                        plugin.getMainConfig().set("use-placeholderapi", true);
                        plugin.setUsePlaceholderAPI(true);
                        ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.detectedandenabled"));
                    } else {
                        ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.notdetectednotenabled"));
                    }
                }
            } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("no")) {
                if (plugin.getPapiExpansion() == null || !plugin.getPapiExpansion().isRegistered()) {
                    ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.setuseapi.alreadydisabled"));
                    return true;
                }

                plugin.getPapiExpansion().unregister();
                plugin.setPapiExpansion(null);
                plugin.setPlaceholderHandler(new DefaultPlaceholders());
                plugin.getMainConfig().set("use-placeholderapi", false);
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.disabledsuccess"));
            } else {
                ColorHandler.getInstance().coloredMessage(sender, "&cYou must provide true, yes, false, or no.");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("setusecolorpermissions") || args[0].equalsIgnoreCase("setucp")) {
            if (!sender.hasPermission("starchat.command.admin.setusecolorpermissions")) {
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            if (!(args.length > 1)) {
                ColorHandler.getInstance().coloredMessage(sender, "&cYou must provide a value");
                return true;
            }

            if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("yes")) {
                if (plugin.isUseColorPermissions()) {
                    ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusecolorperms.alreadyenabled"));
                    return true;
                }
                plugin.setUseColorPermissions(true);
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusecolorperms.enabled"));
            } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("no")) {
                if (!plugin.isUseColorPermissions()) {
                    ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusecolorperms.alreadydisabled"));
                    return true;
                }
                plugin.setUseColorPermissions(false);
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusecolorperms.disabled"));
            } else {
                ColorHandler.getInstance().coloredMessage(sender, "&cYou must provide true, yes, false, or no.");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("starchat.command.admin.list")) {
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            if (!(args.length > 1)) {
                ColorHandler.getInstance().coloredMessage(sender, "&cUsage: /" + label + " list <all|channels|rooms|conversations>");
                return true;
            }

            if (args[1].equalsIgnoreCase("all")) {
                if (!sender.hasPermission("starchat.command.admin.list.all")) {
                    ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.list.all.header"));
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
                    ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.list.channels.header"));
                listChannels(sender);
            } else if (args[1].equalsIgnoreCase("rooms")) {
                if (!(sender.hasPermission("starchat.command.admin.list.rooms") || sender.hasPermission("starchat.command.admin.list.all"))) {
                    ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.list.rooms.header"));
                listRooms(sender);
            } else if (args[1].equalsIgnoreCase("conversations")) {
                if (!(sender.hasPermission("starchat.command.admin.list.conversations") || sender.hasPermission("starchat.command.admin.list.all"))) {
                    ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.list.conversations.header"));
                listConversations(sender);
            }
        } else if (args[0].equalsIgnoreCase("setplayerchatfocus") || args[0].equalsIgnoreCase("setplayerfocus") || args[0].equalsIgnoreCase("setfocus")) {
            if (!(sender.hasPermission("starchat.command.admin.setplayerchatfocus"))) {
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }

            if (!(args.length > 2)) {
                ColorHandler.getInstance().coloredMessage(sender, "&cUsage: /" + label + " " + args[0] + " <player> <chatspace>");
                return true;
            }

            Actor target = Actor.create(args[1]);
            if (target == null) {
                ColorHandler.getInstance().coloredMessage(sender, "&cInvalid target, are they online?");
                return true;
            }

            if (!target.isPlayer()) {
                ColorHandler.getInstance().coloredMessage(sender, "&cTarget must be a player.");
                return true;
            }

            if (!target.isOnline()) {
                ColorHandler.getInstance().coloredMessage(sender, "&cTarget must be online to set the chat focus.");
                return true;
            }

            Player targetPlayer = ((PlayerActor) target).getPlayer();

            ChatChannel chatChannel = plugin.getChannelRegistry().get(args[2]);
            if (chatChannel == null) {
                ColorHandler.getInstance().coloredMessage(sender, "&cThat is not a valid channel.");
                return true;
            }

            plugin.setPlayerFocus(targetPlayer, chatChannel);
            ColorHandler.getInstance().coloredMessage(sender, "&eYou set &b" + targetPlayer.getName() + "'s &echat focus to &d" + chatChannel.getName());
            ColorHandler.getInstance().coloredMessage(targetPlayer, "&eYour chat focus was changed to &d" + chatChannel.getName() + " &eby &b" + Actor.create(sender).getName());
        } else if (args[0].equalsIgnoreCase("channel")) {
            if (!(sender.hasPermission("starchat.command.admin.channel"))) {
                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }

            if (!(args.length > 2)) {
                ColorHandler.getInstance().coloredMessage(sender, "&cUsage: /" + label + " channel <[channelName]|create|delete> <args>");
                return true;
            }

            if (args[1].equalsIgnoreCase("create")) {
                if (!(sender.hasPermission("starchat.command.admin.channel.create"))) {
                    ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }

                String channelName = sb.toString().trim();

                File file = new File(plugin.getDataFolder() + File.separator + "channels" + File.separator + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', channelName)).toLowerCase().replace(" ", "_") + ".yml");
                ChatChannel chatChannel = new ChatChannel(plugin, channelName, file.toPath());
                plugin.getChannelRegistry().register(chatChannel.getName(), chatChannel);
                ColorHandler.getInstance().coloredMessage(sender, "&aCreated a new channel called " + channelName);
                return true;
            }

            ChatChannel chatChannel = plugin.getChannelRegistry().get(args[1]);
            if (chatChannel == null) {
                ColorHandler.getInstance().coloredMessage(sender, "&cThat is not a registered chat channel.");
                return true;
            }

            if (args[1].equalsIgnoreCase("delete")) {
                if (!(sender.hasPermission("starchat.command.admin.channel.delete"))) {
                    ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }

                if (!chatChannel.getPlugin().getName().equalsIgnoreCase(plugin.getName())) {
                    ColorHandler.getInstance().coloredMessage(sender, "&cYou can only delete chat channels owned by StarChat.");
                    return true;
                }

                chatChannel.getFile().delete();
                plugin.getChannelRegistry().unregister(chatChannel.getName());
                ColorHandler.getInstance().coloredMessage(sender, "&eYou deleted the chat channel &b" + chatChannel.getName());
            }

            if (!chatChannel.getPlugin().getName().equalsIgnoreCase(plugin.getName())) {
                ColorHandler.getInstance().coloredMessage(sender, "&cYou can only modify chat channels owned by StarChat.");
                return true;
            }

            if (!(args.length > 4)) {
                ColorHandler.getInstance().coloredMessage(sender, "&cUsage: /" + label + " " + args[0] + " " + args[1] + " <subcommand> <arguments>");
                return true;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 4; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }

            String value = sb.toString().trim();

            if (args[2].equalsIgnoreCase("set")) {
                Property<?> property;
                try {
                    Field classField = ReflectionHelper.getClassField(chatChannel.getClass(), args[3]);
                    property = (Property<?>) classField.get(chatChannel);
                } catch (IllegalAccessException e) {
                    ColorHandler.getInstance().coloredMessage(sender, "You provided an invalid key name.");
                    return true;
                }

                if (!sender.hasPermission("starchat.command.admin.channel.set." + property.getName().toLowerCase().replace(" ", "_"))) {
                    ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }

                if (property instanceof StringProperty stringProperty) {
                    stringProperty.set(value);
                } else if (property instanceof BooleanProperty booleanProperty) {
                    booleanProperty.set(StringConverters.getConverter(boolean.class).convertTo(value));
                } else {
                    ColorHandler.getInstance().coloredMessage(sender, "Unsupported Property Value Type, contact the developer to add support.");
                    return true;
                }

                ColorHandler.getInstance().coloredMessage(sender, pluginConfig.getString("messages.command.admin.channel.set.success").replace("{channel}", chatChannel.getName()).replace("{key}", property.getName()).replace("{value}", property.getValue() + ""));
            } else {
                ColorHandler.getInstance().coloredMessage(sender, "&cInvalid sub command.");
                return true;
            }

            chatChannel.saveConfig();
        } else if (args[0].equalsIgnoreCase("setusestaffchannel")) {
            if (!(args.length > 1)) {
                ColorHandler.getInstance().coloredMessage(sender, "&cYou must provide true or false.");
                return true;
            }
            
            boolean current = plugin.getMainConfig().getBoolean("use-staff-channel");
            boolean value = Boolean.parseBoolean(args[1]);
            
            if (value) {
                if (current) {
                    ColorHandler.getInstance().coloredMessage(sender, "&cThe staff channel is already enabled.");
                    return true;
                } else {
                    plugin.getMainConfig().set("use-staff-channel", true);
                    plugin.getMainConfig().save();
                    plugin.loadStaffChannel();
                    ColorHandler.getInstance().coloredMessage(sender, "&aYou enabled the staff channel.");
                }
            } else {
                if (!current) {
                    ColorHandler.getInstance().coloredMessage(sender, "&cThe staff channel is already disabled.");
                    return true;
                } else {
                    plugin.getMainConfig().set("use-staff-channel", false);
                    plugin.getMainConfig().save();
                    plugin.unloadStaffChannel();
                    ColorHandler.getInstance().coloredMessage(sender, "&aYou disbaled the staff channel.");
                }
            }
        } else if (args[0].equalsIgnoreCase("renameglobalchannel")) {
            if (!(args.length > 1)) {
                ColorHandler.getInstance().coloredMessage(sender, "&cYou must provide a new name.");
                return true;
            }
            
            String newName = ColorHandler.stripColor(args[1]).toLowerCase();
            String oldName = plugin.getMainConfig().getString("global-channel-name");
            
            if (newName.isBlank()) {
                ColorHandler.getInstance().coloredMessage(sender, "&cThe new name is blank or effectively blank.");
                return true;
            }
            
            if (oldName.equals(newName)) {
                ColorHandler.getInstance().coloredMessage(sender, "&cThe old name and the new name are the same.");
                return true;
            }
            
            plugin.getChannelRegistry().unregister(oldName);

            File oldFile = plugin.getGlobalChannel().getFile();
            File newFile = new File(oldFile.getParentFile(), newName + ".yml");

            if (newFile.exists()) {
                newFile.delete();
            }

            oldFile.renameTo(newFile);
            
            plugin.getGlobalChannel().setFile(newFile);
            plugin.getGlobalChannel().setName(newName);
            plugin.getGlobalChannel().saveConfig();
            plugin.getMainConfig().set("global-channel-name", newName);
            plugin.getMainConfig().save();
            
            plugin.getChannelRegistry().register(plugin.getGlobalChannel().getName(), plugin.getGlobalChannel());
            
            ColorHandler.getInstance().coloredMessage(sender, "&aYou renamed the global channel from &e" + oldName + " &ato &e" + newName + "&a.");
        }
        return true;
    }

    private void listChannels(CommandSender sender) {
        for (ChatChannel chatChannel : plugin.getChannelRegistry()) {
            ColorHandler.getInstance().coloredMessage(sender, " &8- &eChannel &b" + chatChannel.getName() + " &eowned by the plugin &d" + chatChannel.getPlugin().getName());
        }
    }

    private void listRooms(CommandSender sender) {
        for (ChatRoom chatRoom : plugin.getRoomRegistry()) {
            ColorHandler.getInstance().coloredMessage(sender, " &8- &eRoom &b" + chatRoom.getName() + " &eowned by the plugin &d" + chatRoom.getPlugin().getName());
        }
    }

    private void listConversations(CommandSender sender) {
        for (PrivateMessage privateMessage : plugin.getPrivateMessages()) {
            ColorHandler.getInstance().coloredMessage(sender, " &8- &eConversation between &b" + privateMessage.getActor1().getName() + " &end &b" + privateMessage.getActor2().getName());
        }
    }
}
