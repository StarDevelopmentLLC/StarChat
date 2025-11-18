package com.stardevllc.starchat.commands;

import com.stardevllc.config.file.FileConfig;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.placeholder.*;
import com.stardevllc.starchat.pm.PrivateMessage;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starcore.api.colors.ColorHandler;
import com.stardevllc.starlib.converter.string.StringConverters;
import com.stardevllc.starlib.injector.Inject;
import com.stardevllc.starlib.observable.Property;
import com.stardevllc.starlib.observable.ReadOnlyProperty;
import com.stardevllc.starlib.observable.property.readwrite.*;
import com.stardevllc.starlib.reflection.ReflectionHelper;
import com.stardevllc.starmclib.actors.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

public class StarChatAdminCmd implements TabExecutor {
    
    @Inject
    private StarChat plugin;
    
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("starchat.command.admin")) {
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
        } else if (Stream.of("setplayerchatfocus", "setplayerfocus", "setfocus").anyMatch(s -> args[0].equalsIgnoreCase(s))) {
            if (!sender.hasPermission("starchat.command.admin.setplayerchatfocus")) {
                return null;
            }

            if (args.length == 2) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
                arg = args[1].toLowerCase();
            } else if (args.length == 3) {
                for (ChatChannel chatChannel : plugin.getChannelRegistry().values()) {
                    completions.add(chatChannel.getName());
                }
                arg = args[2].toLowerCase();
            }
        } else if (args[0].equalsIgnoreCase("channel")) {
            if (!sender.hasPermission("starchat.command.admin.channel")) {
                return null;
            }

            if (args.length == 2) {
                completions.add("create");
                for (ChatChannel chatChannel : plugin.getChannelRegistry().values()) {
                    if (chatChannel.getPlugin().getName().equalsIgnoreCase(this.plugin.getName())) {
                        completions.add(chatChannel.getName());
                    }
                }
                arg = args[1].toLowerCase();
                noSort = true;
            } else if (args[1].equalsIgnoreCase("create")) {
                if (!sender.hasPermission("starchat.command.admin.channel.create")) {
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
                    completions.addAll(List.of("delete", "set", "mute", "unmute"));
                    arg = args[2].toLowerCase();
                } else if (args[2].equalsIgnoreCase("set")) {
                    // /starchat channel <name> set <property> <value>
                    
                    if (args.length == 4) {
                        for (Field field : ReflectionHelper.getClassFields(chatChannel.getClass()).values()) {
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
        FileConfig pluginConfig = plugin.getMainConfig();
        if (!sender.hasPermission("starchat.command.admin")) {
            StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
            return true;
        }

        if (!(args.length > 0)) {
            StarColors.coloredMessage(sender, "&cYou must provide a sub-command."); //TODO Print out help when commands done
            return true;
        }

        if (args[0].equalsIgnoreCase("save")) {
            if (!sender.hasPermission("starchat.command.admin.save")) {
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            plugin.saveMainConfig();
            StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.savesuccess"));
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("starchat.command.admin.reload")) {
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }

            plugin.reload(false);
            StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.reloadsuccess"));
        } else if (args[0].equalsIgnoreCase("setconsolenameformat") || args[0].equalsIgnoreCase("setcnf")) {
            if (!sender.hasPermission("starchat.command.admin.setconsolenameformat")) {
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            if (!(args.length > 1)) {
                StarColors.coloredMessage(sender, "&cYou must provide a new console name.");
                return true;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }

            String consoleName = sb.toString().trim();
            plugin.setConsoleNameFormat(consoleName);
            StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setconsolename").replace("{NEWNAME}", consoleName));
        } else if (args[0].equalsIgnoreCase("setprivatemessageformat") || args[0].equalsIgnoreCase("setpmf")) {
            if (!sender.hasPermission("starchat.command.admin.setprivatemessageformat")) {
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            if (!(args.length > 1)) {
                StarColors.coloredMessage(sender, "&cYou must provide a new private message format.");
                return true;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                sb.append(args[i]).append(" ");
            }

            String privateMessageFormat = sb.toString().trim();
            plugin.setPrivateMessageFormat(privateMessageFormat);
            sender.sendMessage(StarColors.color("&aSet the new private message format to &r") + privateMessageFormat);
        } else if (args[0].equalsIgnoreCase("setuseplaceholderapi") || args[0].equalsIgnoreCase("setupapi")) {
            if (!sender.hasPermission("starchat.command.admin.setuseplaceholderapi")) {
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            if (!(args.length > 1)) {
                StarColors.coloredMessage(sender, "&cYou must provide a value");
                return true;
            }

            if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("yes")) {
                if (plugin.isUsePlaceholderAPI()) {
                    if (plugin.getPapiExpansion() != null && plugin.getPapiExpansion().isRegistered()) {
                        StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.alreadyconfigandenabled"));
                        return true;
                    } else {
                        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
                        if (papi != null && papi.isEnabled()) {
                            plugin.setPapiExpansion(new PAPIExpansion(plugin));
                            plugin.getPapiExpansion().register();
                            plugin.setPlaceholderHandler(new PAPIPlaceholders());
                            StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.configbutnotenabled"));
                        } else {
                            StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.configbutnotdetected"));
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
                        StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.detectedandenabled"));
                    } else {
                        StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.notdetectednotenabled"));
                    }
                }
            } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("no")) {
                if (plugin.getPapiExpansion() == null || !plugin.getPapiExpansion().isRegistered()) {
                    StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setuseapi.alreadydisabled"));
                    return true;
                }

                plugin.getPapiExpansion().unregister();
                plugin.setPapiExpansion(null);
                plugin.setPlaceholderHandler(new DefaultPlaceholders());
                plugin.getMainConfig().set("use-placeholderapi", false);
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusepapi.disabledsuccess"));
            } else {
                StarColors.coloredMessage(sender, "&cYou must provide true, yes, false, or no.");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("setusecolorpermissions") || args[0].equalsIgnoreCase("setucp")) {
            if (!sender.hasPermission("starchat.command.admin.setusecolorpermissions")) {
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            if (!(args.length > 1)) {
                StarColors.coloredMessage(sender, "&cYou must provide a value");
                return true;
            }

            if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("yes")) {
                if (plugin.isUseColorPermissions()) {
                    StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusecolorperms.alreadyenabled"));
                    return true;
                }
                plugin.setUseColorPermissions(true);
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusecolorperms.enabled"));
            } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("no")) {
                if (!plugin.isUseColorPermissions()) {
                    StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusecolorperms.alreadydisabled"));
                    return true;
                }
                plugin.setUseColorPermissions(false);
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.setusecolorperms.disabled"));
            } else {
                StarColors.coloredMessage(sender, "&cYou must provide true, yes, false, or no.");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("starchat.command.admin.list")) {
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            if (!(args.length > 1)) {
                StarColors.coloredMessage(sender, "&cUsage: /" + label + " list <all|channels|rooms|conversations>");
                return true;
            }

            if (args[1].equalsIgnoreCase("all")) {
                if (!sender.hasPermission("starchat.command.admin.list.all")) {
                    StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.list.all.header"));
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
                    StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.list.channels.header"));
                listChannels(sender);
            } else if (args[1].equalsIgnoreCase("rooms")) {
                if (!(sender.hasPermission("starchat.command.admin.list.rooms") || sender.hasPermission("starchat.command.admin.list.all"))) {
                    StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.list.rooms.header"));
                listRooms(sender);
            } else if (args[1].equalsIgnoreCase("conversations")) {
                if (!(sender.hasPermission("starchat.command.admin.list.conversations") || sender.hasPermission("starchat.command.admin.list.all"))) {
                    StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.list.conversations.header"));
                listConversations(sender);
            }
        } else if (Stream.of("setplayerchatfocus", "setplayerfocus", "setfocus").anyMatch(string -> args[0].equalsIgnoreCase(string))) {
            if (!sender.hasPermission("starchat.command.admin.setplayerchatfocus")) {
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }

            if (!(args.length > 2)) {
                StarColors.coloredMessage(sender, "&cUsage: /" + label + " " + args[0] + " <player> <chatspace>");
                return true;
            }

            Actor target = Actors.create(args[1]);
            if (target == null) {
                StarColors.coloredMessage(sender, "&cInvalid target, are they online?");
                return true;
            }

            if (!target.isPlayer()) {
                StarColors.coloredMessage(sender, "&cTarget must be a player.");
                return true;
            }

            if (!target.isOnline()) {
                StarColors.coloredMessage(sender, "&cTarget must be online to set the chat focus.");
                return true;
            }

            Player targetPlayer = ((PlayerActor) target).getPlayer();

            ChatChannel chatChannel = plugin.getChannelRegistry().get(args[2]);
            if (chatChannel == null) {
                StarColors.coloredMessage(sender, "&cThat is not a valid channel.");
                return true;
            }

            plugin.setPlayerFocus(targetPlayer, chatChannel);
            StarColors.coloredMessage(sender, "&eYou set &b" + targetPlayer.getName() + "'s &echat focus to &d" + chatChannel.getName());
            StarColors.coloredMessage(targetPlayer, "&eYour chat focus was changed to &d" + chatChannel.getName() + " &eby &b" + Actors.create(sender).getName());
        } else if (args[0].equalsIgnoreCase("channel")) {
            if (!sender.hasPermission("starchat.command.admin.channel")) {
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }

            if (!(args.length > 2)) {
                StarColors.coloredMessage(sender, "&cUsage: /" + label + " channel <[channelName]|create|delete> <args>");
                return true;
            }

            if (args[1].equalsIgnoreCase("create")) {
                if (!sender.hasPermission("starchat.command.admin.channel.create")) {
                    StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
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
                StarColors.coloredMessage(sender, "&aCreated a new channel called " + channelName);
                return true;
            }

            ChatChannel chatChannel = plugin.getChannelRegistry().get(args[1]);
            if (chatChannel == null) {
                StarColors.coloredMessage(sender, "&cThat is not a registered chat channel.");
                return true;
            }
            
            if (args[2].equalsIgnoreCase("delete")) {
                if (!sender.hasPermission("starchat.command.admin.channel.delete")) {
                    StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }

                if (!chatChannel.getPlugin().getName().equalsIgnoreCase(plugin.getName())) {
                    StarColors.coloredMessage(sender, "&cYou can only delete chat channels owned by StarChat.");
                    return true;
                }

                chatChannel.getConfig().delete();
                plugin.getChannelRegistry().unregister(chatChannel.getName());
                StarColors.coloredMessage(sender, "&eYou deleted the chat channel &b" + chatChannel.getName());
            }

            if (!chatChannel.getPlugin().getName().equalsIgnoreCase(plugin.getName())) {
                StarColors.coloredMessage(sender, "&cYou can only modify chat channels owned by StarChat.");
                return true;
            }

            if (args[2].equalsIgnoreCase("set")) {
                if (!(args.length > 4)) {
                    StarColors.coloredMessage(sender, "&cUsage: /" + label + " " + args[0] + " " + args[1] + " <subcommand> <arguments>");
                    return true;
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 4; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }

                String value = sb.toString().trim();
                
                ReadOnlyProperty<?> property;
                try {
                    Field classField = null;

                    for (Field field : ReflectionHelper.getClassFields(chatChannel.getClass()).values()) {
                        if (field.getName().equalsIgnoreCase(args[3])) {
                            classField = field;
                            break;
                        }
                    }
                    
                    classField.setAccessible(true);
                    property = (ReadOnlyProperty<?>) classField.get(chatChannel);
                } catch (IllegalAccessException | NullPointerException e) {
                    StarColors.coloredMessage(sender, "&cYou provided an invalid setting name.");
                    return true;
                }

                if (!sender.hasPermission("starchat.command.admin.channel.set." + property.getName().toLowerCase().replace(" ", "_"))) {
                    StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }

                switch (property) {
                    case ReadWriteStringProperty stringProperty -> stringProperty.set(value);
                    case ReadWriteBooleanProperty booleanProperty ->
                            booleanProperty.set(StringConverters.getConverter(boolean.class).convertTo(value));
                    case ReadWriteObjectProperty<?> objectProperty -> {
                        if (objectProperty.getName().equalsIgnoreCase("mutedBy")) {
                            ((ReadWriteObjectProperty<Actor>) objectProperty).set(Actors.create(value));
                        }
                    }
                    default -> {
                        StarColors.coloredMessage(sender, "&cUnsupported Property Value Type, contact the developer to add support.");
                        return true;
                    }
                }

                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.admin.channel.set.success").replace("{channel}", chatChannel.getName()).replace("{key}", property.getName()).replace("{value}", property.getValue() + ""));
            } else if (args[2].equalsIgnoreCase("mute")) {
                if (!sender.hasPermission("starchat.command.admin.channel.mute")) {
                    StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                
                if (chatChannel.isMuted()) {
                    StarColors.coloredMessage(sender, "&cThat channel is already muted.");
                    return true;
                }
                
                Actor actor = Actors.create(sender);
                
                StringBuilder rb = new StringBuilder();
                for (int i = 3; i < args.length; i++) {
                    rb.append(args[i]).append(" ");
                }
                
                String reason = rb.toString().trim();
                chatChannel.mute(actor, reason);
                String muteMsg = chatChannel.getMuteFormat();
                muteMsg = muteMsg.replace("{channelName}", chatChannel.getName());
                muteMsg = muteMsg.replace("{actor}", actor.getName());
                chatChannel.sendMessage(new ChatContext(muteMsg));
            } else if (args[2].equalsIgnoreCase("unmute")) {
                if (!sender.hasPermission("starchat.command.admin.channel.unmute")) {
                    StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                    return true;
                }
                
                if (!chatChannel.isMuted()) {
                    StarColors.coloredMessage(sender, "&cThat channel is not muted.");
                    return true;
                }

                Actor actor = Actors.create(sender);
                chatChannel.unmute(actor);
                String unmuteMsg = chatChannel.getUnmuteFormat();
                unmuteMsg = unmuteMsg.replace("{channelName}", chatChannel.getName());
                unmuteMsg = unmuteMsg.replace("{actor}", actor.getName());
                chatChannel.sendMessage(new ChatContext(unmuteMsg));
            } else {
                StarColors.coloredMessage(sender, "&cInvalid sub command.");
                return true;
            }

            chatChannel.saveConfig();
        } else if (args[0].equalsIgnoreCase("setusestaffchannel")) {
            if (!sender.hasPermission("starchat.command.admin.setusestaffchannel")) {
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            
            if (!(args.length > 1)) {
                StarColors.coloredMessage(sender, "&cYou must provide true or false.");
                return true;
            }
            
            boolean current = plugin.getMainConfig().getBoolean("use-staff-channel");
            boolean value = Boolean.parseBoolean(args[1]);
            
            if (value) {
                if (current) {
                    StarColors.coloredMessage(sender, "&cThe staff channel is already enabled.");
                    return true;
                } else {
                    plugin.getMainConfig().set("use-staff-channel", true);
                    plugin.saveMainConfig();
                    plugin.loadStaffChannel();
                    StarColors.coloredMessage(sender, "&aYou enabled the staff channel.");
                }
            } else {
                if (!current) {
                    StarColors.coloredMessage(sender, "&cThe staff channel is already disabled.");
                    return true;
                } else {
                    plugin.getMainConfig().set("use-staff-channel", false);
                    plugin.saveMainConfig();
                    plugin.unloadStaffChannel();
                    StarColors.coloredMessage(sender, "&aYou disbaled the staff channel.");
                }
            }
        } else if (args[0].equalsIgnoreCase("renameglobalchannel")) {
            if (!sender.hasPermission("starchat.command.admin.renameglobalchannel")) {
                StarColors.coloredMessage(sender, pluginConfig.getString("messages.command.nopermission"));
                return true;
            }
            
            if (!(args.length > 1)) {
                StarColors.coloredMessage(sender, "&cYou must provide a new name.");
                return true;
            }
            
            String newName = ColorHandler.stripColor(args[1]).toLowerCase();
            String oldName = plugin.getMainConfig().getString("global-channel-name");
            
            if (newName.isBlank()) {
                StarColors.coloredMessage(sender, "&cThe new name is blank or effectively blank.");
                return true;
            }
            
            if (oldName.equals(newName)) {
                StarColors.coloredMessage(sender, "&cThe old name and the new name are the same.");
                return true;
            }
            
            plugin.getChannelRegistry().unregister(oldName);
            plugin.getGlobalChannel().getConfig().renameFile(newName);
            plugin.getGlobalChannel().setName(newName);
            plugin.getGlobalChannel().saveConfig();
            plugin.getMainConfig().set("global-channel-name", newName);
            plugin.saveMainConfig();
            
            plugin.getChannelRegistry().register(plugin.getGlobalChannel().getName(), plugin.getGlobalChannel());
            
            StarColors.coloredMessage(sender, "&aYou renamed the global channel from &e" + oldName + " &ato &e" + newName + "&a.");
        }
        return true;
    }

    private void listChannels(CommandSender sender) {
        for (ChatChannel chatChannel : plugin.getChannelRegistry().values()) {
            StarColors.coloredMessage(sender, " &8- &eChannel &b" + chatChannel.getName() + " &eowned by the plugin &d" + chatChannel.getPlugin().getName());
        }
    }

    private void listRooms(CommandSender sender) {
        for (ChatRoom chatRoom : plugin.getRoomRegistry().values()) {
            StarColors.coloredMessage(sender, " &8- &eRoom &b" + chatRoom.getName() + " &eowned by the plugin &d" + chatRoom.getPlugin().getName());
        }
    }

    private void listConversations(CommandSender sender) {
        for (PrivateMessage privateMessage : plugin.getPrivateMessages()) {
            StarColors.coloredMessage(sender, " &8- &eConversation between &b" + privateMessage.getActor1().getName() + " &end &b" + privateMessage.getActor2().getName());
        }
    }
}