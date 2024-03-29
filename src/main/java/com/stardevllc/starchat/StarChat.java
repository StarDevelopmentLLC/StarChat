package com.stardevllc.starchat;

import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starchat.channels.GlobalChannel;
import com.stardevllc.starchat.channels.StaffChannel;
import com.stardevllc.starchat.commands.ChatCmd;
import com.stardevllc.starchat.commands.MessageCmd;
import com.stardevllc.starchat.commands.ReplyCmd;
import com.stardevllc.starchat.commands.StarChatAdminCmd;
import com.stardevllc.starchat.placeholder.DefaultPlaceholders;
import com.stardevllc.starchat.placeholder.PAPIExpansion;
import com.stardevllc.starchat.placeholder.PAPIPlaceholders;
import com.stardevllc.starchat.placeholder.PlayerPlaceholders;
import com.stardevllc.starchat.pm.PrivateChatSelector;
import com.stardevllc.starchat.pm.PrivateMessage;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starlib.registry.StringRegistry;
import com.stardevllc.starmclib.Config;
import com.stardevllc.starmclib.actor.Actor;
import com.stardevllc.starmclib.actor.PlayerActor;
import com.stardevllc.starmclib.actor.ServerActor;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class StarChat extends JavaPlugin implements Listener {
    private static String consoleNameFormat; //How the console name appears
    private static String privateMessageFormat; //The format used for private messages
    private static boolean usePlaceholderAPI;
    private static Chat vaultChat; //Vault chat hook
    private static PlayerPlaceholders playerPlaceholders;
    private static boolean useColorPermissions;
    private Config mainConfig;
    private ChatChannel globalChannel, staffChannel; //Default channels
    private Map<UUID, ChatSpace> playerChatSelection = new HashMap<>(); //Current player focus
    private Map<String, ChatSelector> chatSelectors = new HashMap<>();
    private PAPIExpansion papiExpansion;

    private static StarChat instance;

    private StringRegistry<ChatChannel> channelRegistry = new StringRegistry<>(); //All channels
    private StringRegistry<ChatRoom> roomRegistry = new StringRegistry<>(); //All rooms
    private Set<PrivateMessage> privateMessages = new HashSet<>();
    private Map<UUID, PrivateMessage> lastMessage = new HashMap<>();
    private PrivateMessage consoleLastMessage;

    @Override
    public void onEnable() {
        instance = this;
        mainConfig = new Config(new File(getDataFolder(), "config.yml"));

        if (!setupChat()) {
            getLogger().severe("Could not setup Vault Chat.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        generateDefaultConfigOptions();
        loadMainConfigValues();
        loadDefaultChannels();
        loadChannels();
        determinePlaceholderHandler();

        getServer().getPluginManager().registerEvents(this, this);

        this.addSelector(new PrivateChatSelector());

        getCommand("chat").setExecutor(new ChatCmd(this));
        getCommand("message").setExecutor(new MessageCmd(this));
        getCommand("reply").setExecutor(new ReplyCmd(this));
        getCommand("starchat").setExecutor(new StarChatAdminCmd(this));
    }

    public void reload(boolean save) {
        if (save) {
            mainConfig.save();
            for (ChatChannel channel : this.channelRegistry.getObjects().values()) {
                channel.getConfig().save();
            }
        }

        mainConfig = new Config(new File(getDataFolder(), "config.yml"));

        Set<String> channelsToRemove = new HashSet<>();
        for (ChatChannel channel : this.channelRegistry.getObjects().values()) {
            if (channel.getPlugin().getName().equalsIgnoreCase(this.getName())) {
                channelsToRemove.add(channel.getSimplifiedName());
            }
        }

        channelsToRemove.forEach(c -> this.channelRegistry.deregister(c));
        
        generateDefaultConfigOptions();
        loadMainConfigValues();
        loadDefaultChannels();
        loadChannels();
        determinePlaceholderHandler();
        this.addSelector(new PrivateChatSelector());
    }
    
    private void generateDefaultConfigOptions() {
        mainConfig.addDefault("console-name-format", "&4Console", "The name that the console appears as in chat spaces.");
        mainConfig.addDefault("private-msg-format", "&6[&c{from} &6-> &c{to}&6]&8: &f{message}", "The format used for private messaging.");
        mainConfig.addDefault("use-placeholderapi", true, "If the PlaceholderAPI plugin is supported by default.", "If PlaceholderAPI is not installed, this setting is ignored.", "Note: Other plugins that use the systems of StarChat can override this setting", "This setting only applies to the default state of StarChat, and maybe other plugins if they decide to use this setting.");
        mainConfig.addDefault("use-color-permissioins", false, "This allows you to control color usage by permissions.", "This is false by default and will just color all messages.", "Permissions for default colors follows the format: starmclib.color.spigot.<colorname>.", "Colors added by other plugins and via StarCore's color commands may or may not have permissions. Please see StarCore for how to list the colors and their information.");
        
        mainConfig.addDefault("messages.command.nopermission", "&cYou do not have permission to use that command.");
        mainConfig.addDefault("messages.command.onlyplayers", "&cOnly players can use that command.");
        mainConfig.addDefault("messages.chatspace.notexist", "&cSorry but {PROVIDED} is not a valid chat space.");
        mainConfig.addDefault("messages.channel.nosendpermission", "&cYou do not have permission to send messages in {CHANNEL}.");
        mainConfig.addDefault("messages.room.notamember", "&cYou are not a member of {ROOM}");
        mainConfig.addDefault("messages.command.chat.setfocus", "&aSet your chat focus to &b{SPACE}.");
        mainConfig.addDefault("messages.command.admin.savesuccess", "&aSaved config.yml successfully.");
        mainConfig.addDefault("messages.command.admin.reloadsuccess", "&aReloaded successfully.");
        mainConfig.addDefault("messages.command.admin.setconsolename", "&aSet the new console name format to &r{NEWNAME}");
        mainConfig.addDefault("messages.command.admin.setusepapi.alreadyconfigandenabled", "&cPlaceholderAPI is already enabled and configured, no need to set it again.");
        mainConfig.addDefault("messages.command.admin.setusepapi.configbutnotenabled", "&aStarChat was configured to use PlaceholderAPI but was not able to load hook at startup, however, PlaceholderAPI was detected on this command and hook has been enabled now.");
        mainConfig.addDefault("messages.command.admin.setusepapi.configbutnotdetected", "&cStarChat is configured to use PlaceholderAPI, however, it was not detected, so the hook cannot be registered. Please install PlaceholderAPI and restart the server.");
        mainConfig.addDefault("messages.command.admin.setusepapi.detectedandenabled", "&aPlaceholderAPI has been detected and hooked into, StarChat will now respect PlaceholderAPI placeholders.");
        mainConfig.addDefault("messages.command.admin.setusepapi.notdetectednotenabled", "&cPlaceholderAPI is not detected as a plugin, cannot enable PlaceholderAPI support for StarChat. Please install and restart the server.");
        mainConfig.addDefault("messages.command.admin.setusepapi.alreadydisabled", "&cPlaceholderAPI support is already disabled.");
        mainConfig.addDefault("messages.command.admin.setusepapi.disabledsuccess", "&Successfully disabled PlaceholderAPI hook and switched to using default placeholder replacements.");
        mainConfig.addDefault("messages.command.admin.setusecolorperms.alreadyenabled", "&cUsage of color permissions is already enabled.");
        mainConfig.addDefault("messages.command.admin.setusecolorperms.enabled", "&aYou enabled the use of color based permissions.");
        mainConfig.addDefault("messages.command.admin.setusecolorperms.alreadydisabled", "&cUsage of color permissions is already disabled.");
        mainConfig.addDefault("messages.command.admin.setusecolorperms.disabled", "&aYou disabled the use of color based permissions.");
        mainConfig.addDefault("messages.command.admin.list.all.header", "&aList of all chat spaces registered to StarChat.");
        mainConfig.addDefault("messages.command.admin.list.channels.header", "&aList of all chat channels registered to StarChat.");
        mainConfig.addDefault("messages.command.admin.list.rooms.header", "&aList of all chat rooms registered to StarChat.");
        mainConfig.addDefault("messages.command.admin.list.conversations.header", "&aList of all conversations using StarChat.");
        
        mainConfig.save();
    }
    
    private void determinePlaceholderHandler() {
        StarChat.setUsePlaceholderAPI(getMainConfig().getBoolean("use-placeholderapi"));
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && StarChat.usePlaceholderAPI) {
            StarChat.playerPlaceholders = new PAPIPlaceholders();
            this.papiExpansion = new PAPIExpansion(this);
            this.papiExpansion.register();
        } else {
            StarChat.playerPlaceholders = new DefaultPlaceholders();
        }
    }
    
    private void loadChannels() {
        File channelsDirectory = new File(getDataFolder(), "channels");
        for (File file : channelsDirectory.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            
            if (!file.getName().endsWith(".yml")) {
                continue;
            }
            
            Config config = new Config(file);
            String name = config.getString("name");
            ChatChannel chatChannel = new ChatChannel(this, name, file);
            this.channelRegistry.register(chatChannel.getSimplifiedName(), chatChannel);
        }
    }

    private void loadMainConfigValues() {
        StarChat.consoleNameFormat = mainConfig.getString("console-name-format");
        StarChat.privateMessageFormat = mainConfig.getString("private-msg-format");
        StarChat.usePlaceholderAPI = mainConfig.getBoolean("use-placeholder-api");
        StarChat.useColorPermissions = mainConfig.getBoolean("use-color-permissions");
    }

    private void loadDefaultChannels() {
        globalChannel = new GlobalChannel(this, new File(getDataFolder() + File.separator + "channels" + File.separator + "defaults", "global.yml"));
        this.channelRegistry.register(globalChannel.getSimplifiedName(), globalChannel);

        staffChannel = new StaffChannel(this, new File(getDataFolder() + File.separator + "channels" + File.separator + "defaults", "staff.yml"));
        this.channelRegistry.register(staffChannel.getSimplifiedName(), staffChannel);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String message = e.getMessage();

        ChatSpace chatSpace = getPlayerFocus(player);
        if (e.isCancelled()) {
            if (chatSpace.isAffectedByPunishments()) {
                return; //Muted message should be handled by the punishments plugin.
            }
        }
        
        e.setCancelled(true);
        chatSpace.sendMessage(player, message);
    }

    public PAPIExpansion getPapiExpansion() {
        return papiExpansion;
    }

    public void setPapiExpansion(PAPIExpansion papiExpansion) {
        this.papiExpansion = papiExpansion;
    }

    public PrivateMessage getLastMessage(UUID uuid) {
        return this.lastMessage.get(uuid);
    }

    public StringRegistry<ChatChannel> getChannelRegistry() {
        return channelRegistry;
    }

    public StringRegistry<ChatRoom> getRoomRegistry() {
        return roomRegistry;
    }

    public Map<String, ChatSelector> getChatSelectors() {
        return chatSelectors;
    }

    public void assignLastMessage(CommandSender sender, StringBuilder msgBuilder, PrivateMessage privateMessage, Actor senderActor, Actor targetActor) {
        if (senderActor instanceof PlayerActor senderPlayerActor) {
            this.lastMessage.put(senderPlayerActor.getUniqueId(), privateMessage);
        } else if (senderActor instanceof ServerActor) {
            this.consoleLastMessage = privateMessage;
        }

        if (targetActor instanceof PlayerActor targetPlayerActor) {
            this.lastMessage.put(targetPlayerActor.getUniqueId(), privateMessage);
        } else if (targetActor instanceof ServerActor) {
            this.consoleLastMessage = privateMessage;
        }
    }

    public ChatChannel getGlobalChannel() {
        return globalChannel;
    }

    public ChatSpace getPlayerFocus(Player player) {
        return this.playerChatSelection.getOrDefault(player.getUniqueId(), globalChannel);
    }

    public void setPlayerFocus(Player player, ChatSpace chatSpace) {
//        ChatSpace old = this.playerChatSelection.put(player.getUniqueId(), chatSpace);
//        getLogger().info("Set " + player.getName() + "'s chat focus to " + chatSpace + ", old focus: " + old);
        this.playerChatSelection.put(player.getUniqueId(), chatSpace);
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        vaultChat = rsp.getProvider();
        return vaultChat != null;
    }

    public Set<PrivateMessage> getPrivateMessages() {
        return privateMessages;
    }

    public PrivateMessage getPrivateMessage(Actor actor1, Actor actor2) {
        for (PrivateMessage privateMessage : this.privateMessages) {
            boolean containsActor1 = privateMessage.getActor1().equals(actor1) || privateMessage.getActor2().equals(actor1);
            boolean containsActor2 = privateMessage.getActor1().equals(actor2) || privateMessage.getActor2().equals(actor2);
            if (containsActor2 && containsActor1) {
                return privateMessage;
            }
        }

        return null;
    }

    public List<PrivateMessage> getPrivateMessages(Actor actor) {
        List<PrivateMessage> privateMessages = new ArrayList<>();
        for (PrivateMessage privateMessage : this.privateMessages) {
            if (privateMessage.getActor1().equals(actor) || privateMessage.getActor2().equals(actor)) {
                privateMessages.add(privateMessage);
            }
        }

        return privateMessages;
    }

    public Config getMainConfig() {
        return mainConfig;
    }

    public void addSelector(ChatSelector selector) {
        this.chatSelectors.put(selector.getType().toLowerCase(), selector);
    }

    public static String getConsoleNameFormat() {
        return consoleNameFormat;
    }

    public static String getPrivateMessageFormat() {
        return privateMessageFormat;
    }

    public static boolean isUsePlaceholderAPI() {
        return usePlaceholderAPI;
    }

    public static void setUsePlaceholderAPI(boolean usePlaceholderAPI) {
        StarChat.usePlaceholderAPI = usePlaceholderAPI;
    }

    public static Chat getVaultChat() {
        return vaultChat;
    }

    public ChatChannel getStaffChannel() {
        return staffChannel;
    }

    public static PlayerPlaceholders getPlayerPlaceholders() {
        return playerPlaceholders;
    }

    public static boolean isUseColorPermissions() {
        return useColorPermissions;
    }

    public void addPrivateMessage(PrivateMessage privateMessage) {
        this.privateMessages.add(privateMessage);
    }

    public PrivateMessage getConsoleLastMessage() {
        return this.consoleLastMessage;
    }

    public static void setConsoleNameFormat(String consoleNameFormat) {
        StarChat.consoleNameFormat = consoleNameFormat;
        getInstance().getMainConfig().set("console-name-format", consoleNameFormat);
    }

    public static void setPrivateMessageFormat(String privateMessageFormat) {
        StarChat.privateMessageFormat = privateMessageFormat;
        getInstance().getMainConfig().set("private-message-format", privateMessageFormat);
    }

    public static void setPlayerPlaceholders(PlayerPlaceholders playerPlaceholders) {
        StarChat.playerPlaceholders = playerPlaceholders;
    }

    public static void setUseColorPermissions(boolean useColorPermissions) {
        StarChat.useColorPermissions = useColorPermissions;
        getInstance().getMainConfig().set("use-color-permissions", useColorPermissions);
    }

    public static StarChat getInstance() {
        return instance;
    }
}