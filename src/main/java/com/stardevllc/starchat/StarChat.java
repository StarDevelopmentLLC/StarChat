package com.stardevllc.starchat;

import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starchat.channels.GlobalChannel;
import com.stardevllc.starchat.channels.StaffChannel;
import com.stardevllc.starchat.commands.ChatCmd;
import com.stardevllc.starchat.commands.MessageCmd;
import com.stardevllc.starchat.commands.ReplyCmd;
import com.stardevllc.starchat.commands.StarChatAdminCmd;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.hooks.VaultHook;
import com.stardevllc.starchat.placeholder.DefaultPlaceholders;
import com.stardevllc.starchat.placeholder.PAPIExpansion;
import com.stardevllc.starchat.placeholder.PAPIPlaceholders;
import com.stardevllc.starchat.placeholder.PlaceholderHandler;
import com.stardevllc.starchat.pm.PrivateChatSelector;
import com.stardevllc.starchat.pm.PrivateMessage;
import com.stardevllc.starchat.registry.ChannelRegistry;
import com.stardevllc.starchat.registry.FocusRegistry;
import com.stardevllc.starchat.registry.RoomRegistry;
import com.stardevllc.starchat.registry.SpaceRegistry;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starcore.actor.Actor;
import com.stardevllc.starcore.actor.PlayerActor;
import com.stardevllc.starcore.actor.ServerActor;
import com.stardevllc.starcore.utils.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.function.Function;

public class StarChat extends JavaPlugin implements Listener {
    private static StarChat instance;

    public static final Function<Player, String> vaultDisplayNameFunction = player -> {
        VaultHook vc = getInstance().getVaultHook();
        if (vc == null) {
            return null;
        }

        return vc.getChat().getPlayerPrefix(player) + player.getName() + vc.getChat().getPlayerSuffix(player);
    };
    
    private SpaceRegistry spaceRegistry;
    private ChannelRegistry channelRegistry;
    private RoomRegistry roomRegistry;
    private FocusRegistry playerChatSelection;
    
    private PlaceholderHandler placeholderHandler;
    private Config mainConfig;
    private ChatChannel globalChannel, staffChannel;
    private Map<String, ChatSelector> chatSelectors = new HashMap<>();
    private PAPIExpansion papiExpansion;
    private VaultHook vaultHook;
    private Set<PrivateMessage> privateMessages = new HashSet<>();
    private Map<UUID, PrivateMessage> lastMessage = new HashMap<>();
    private PrivateMessage consoleLastMessage;

    @Override
    public void onEnable() {
        instance = this;
        mainConfig = new Config(new File(getDataFolder(), "config.yml"));

        Plugin vaultPlugin = getServer().getPluginManager().getPlugin("Vault");
        if (vaultPlugin != null) {
            if (vaultPlugin.isEnabled()) {
                vaultHook = new VaultHook(this);
                if (!vaultHook.setup()) {
                    vaultHook = null;
                }
            }
        }
        
        spaceRegistry = new SpaceRegistry();
        channelRegistry = new ChannelRegistry(this);
        roomRegistry = new RoomRegistry(this);
        playerChatSelection = new FocusRegistry();
        
        generateDefaultConfigOptions();
        loadDefaultChannels();
        loadChannels();
        determinePlaceholderHandler();

        ServicesManager servicesManager = getServer().getServicesManager();
        servicesManager.register(SpaceRegistry.class, spaceRegistry, this, ServicePriority.Highest);
        servicesManager.register(ChannelRegistry.class, channelRegistry, this, ServicePriority.Highest);
        servicesManager.register(RoomRegistry.class, roomRegistry, this, ServicePriority.Highest);
        servicesManager.register(FocusRegistry.class, playerChatSelection, this, ServicePriority.Highest);

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

        Set<String> spacesToRemove = new HashSet<>();
        for (ChatSpace space : this.spaceRegistry.getObjects().values()) {
            if (space.getPlugin().getName().equalsIgnoreCase(this.getName())) {
                spacesToRemove.add(space.getName());
            }
        }

        spacesToRemove.forEach(c -> this.channelRegistry.deregister(c));
        
        generateDefaultConfigOptions();
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
        mainConfig.addDefault("messages.command.chat.setfocus", "&eYou set your chat focus to &b{SPACE}.");
        mainConfig.addDefault("messages.command.admin.savesuccess", "&aSaved config.yml successfully.");
        mainConfig.addDefault("messages.command.admin.reloadsuccess", "&aReloaded successfully.");
        mainConfig.addDefault("messages.command.admin.setconsolename", "&eYou set the new console name format to &r{NEWNAME}");
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
        mainConfig.addDefault("messages.command.admin.channel.set.success", "&eYou set &b{channel}&e's &a{key} &eto &d{value}");
        
        mainConfig.save();
    }
    
    private void determinePlaceholderHandler() {
        setUsePlaceholderAPI(getMainConfig().getBoolean("use-placeholderapi"));
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && isUsePlaceholderAPI()) {
            placeholderHandler = new PAPIPlaceholders();
            this.papiExpansion = new PAPIExpansion(this);
            this.papiExpansion.register();
        } else {
            placeholderHandler = new DefaultPlaceholders();
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
            ChatChannel chatChannel = new ChatChannel(this, name, file.toPath());
            this.channelRegistry.register(chatChannel.getName(), chatChannel);
        }
    }

    private void loadDefaultChannels() {
        globalChannel = new GlobalChannel(this, new File(getDataFolder() + File.separator + "channels", "global.yml"));
        this.channelRegistry.register(globalChannel.getName(), globalChannel);

        staffChannel = new StaffChannel(this, new File(getDataFolder() + File.separator + "channels", "staff.yml"));
        this.channelRegistry.register(staffChannel.getName(), staffChannel);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        ChatSpace chatSpace = getPlayerFocus(player);
        
        chatSpace.sendMessage(new ChatContext(e));
        e.setCancelled(true);
    }

    public SpaceRegistry getSpaceRegistry() {
        return spaceRegistry;
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

    public ChannelRegistry getChannelRegistry() {
        return channelRegistry;
    }

    public RoomRegistry getRoomRegistry() {
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
        return this.playerChatSelection.getPlayerFocus(player);
    }

    public void setPlayerFocus(Player player, ChatSpace chatSpace) {
//        ChatSpace old = this.playerChatSelection.put(player.getUniqueId(), chatSpace);
//        getLogger().info("Set " + player.getName() + "'s chat focus to " + chatSpace + ", old focus: " + old);
        this.playerChatSelection.setPlayerFocus(player.getUniqueId(), chatSpace);
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

    public String getConsoleNameFormat() {
        return mainConfig.getString("console-name-format");
    }

    public String getPrivateMessageFormat() {
        return mainConfig.getString("private-msg-format");
    }

    public boolean isUsePlaceholderAPI() {
        return mainConfig.getBoolean("use-placeholder-api");
    }

    public void setUsePlaceholderAPI(boolean usePlaceholderAPI) {
        mainConfig.set("use-placeholder-api", usePlaceholderAPI);
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public ChatChannel getStaffChannel() {
        return staffChannel;
    }

    public PlaceholderHandler getPlaceholderHandler() {
        return placeholderHandler;
    }

    public boolean isUseColorPermissions() {
        return mainConfig.getBoolean("use-color-permissions");
    }

    public void addPrivateMessage(PrivateMessage privateMessage) {
        this.privateMessages.add(privateMessage);
    }

    public PrivateMessage getConsoleLastMessage() {
        return this.consoleLastMessage;
    }

    public void setConsoleNameFormat(String consoleNameFormat) {
        mainConfig.set("console-name-format", consoleNameFormat);
    }

    public void setPrivateMessageFormat(String privateMessageFormat) {
        mainConfig.set("private-message-format", privateMessageFormat);
    }

    public void setPlaceholderHandler(PlaceholderHandler playerPlaceholders) {
        placeholderHandler = playerPlaceholders;
    }

    public void setUseColorPermissions(boolean useColorPermissions) {
        mainConfig.set("use-color-permissions", useColorPermissions);
    }

    public static StarChat getInstance() {
        return instance;
    }
}