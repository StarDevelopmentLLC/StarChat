package com.stardevllc.starchat;

import com.stardevllc.starchat.ChatSelector.ChatSelection;
import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starchat.channels.GlobalChannel;
import com.stardevllc.starchat.channels.StaffChannel;
import com.stardevllc.starchat.placeholder.DefaultPlaceholders;
import com.stardevllc.starchat.placeholder.PAPIExpansion;
import com.stardevllc.starchat.placeholder.PAPIPlaceholders;
import com.stardevllc.starchat.placeholder.PlayerPlaceholders;
import com.stardevllc.starchat.pm.PrivateMessage;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starlib.registry.StringRegistry;
import com.stardevllc.starmclib.Config;
import com.stardevllc.starmclib.actor.Actor;
import com.stardevllc.starmclib.actor.PlayerActor;
import com.stardevllc.starmclib.actor.ServerActor;
import com.stardevllc.starmclib.color.ColorUtils;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
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

    private StringRegistry<ChatChannel> channelRegistry = new StringRegistry<>(); //All channels
    private StringRegistry<ChatRoom> roomRegistry = new StringRegistry<>(); //All rooms
    private Set<PrivateMessage> privateMessages = new HashSet<>();
    private Map<UUID, PrivateMessage> lastMessage = new HashMap<>();
    private PrivateMessage consoleLastMessage;

    @Override
    public void onEnable() {
        mainConfig = new Config(new File(getDataFolder(), "config.yml"));

        if (!setupChat()) {
            getLogger().severe("Could not setup Vault Chat.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        mainConfig.addDefault("console-name-format", "&4Console", "The name that the console appears as in chat spaces.");
        mainConfig.addDefault("private-msg-format", "&6[&c{from} &6-> &c{to}&6]&8: &f{message}", "The format used for private messaging.");
        mainConfig.addDefault("use-placeholderapi", true, "If the PlaceholderAPI plugin is supported by default.", "If PlaceholderAPI is not installed, this setting is ignored.","Note: Other plugins that use the systems of StarChat can override this setting", "This setting only applies to the default state of StarChat, and maybe other plugins if they decide to use this setting.");
        mainConfig.addDefault("use-color-permissioins", false, "This allows you to control color usage by permissions.", "This is false by default and will just color all messages.", "Permissions for default colors follows the format: starmclib.color.spigot.<colorname>.", "Colors added by other plugins and via StarCore's color commands may or may not have permissions. Please see StarCore for how to list the colors and their information.");
        mainConfig.save();

        StarChat.consoleNameFormat = mainConfig.getString("console-name-format");
        StarChat.privateMessageFormat = mainConfig.getString("private-msg-format");
        StarChat.usePlaceholderAPI = mainConfig.getBoolean("use-placeholder-api");
        StarChat.useColorPermissions = mainConfig.getBoolean("use-color-permissions");

        globalChannel = new GlobalChannel(this, new File(getDataFolder() + File.separator + "channels" + File.separator + "defaults", "global.yml"));
        this.channelRegistry.register(globalChannel.getSimplifiedName(), globalChannel);

        staffChannel = new StaffChannel(this, new File(getDataFolder() + File.separator + "channels" + File.separator + "defaults", "staff.yml"));
        this.channelRegistry.register(staffChannel.getSimplifiedName(), staffChannel);

        getServer().getPluginManager().registerEvents(this, this);
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            StarChat.playerPlaceholders = new PAPIPlaceholders();
            new PAPIExpansion(this).register();
        } else {
            StarChat.playerPlaceholders = new DefaultPlaceholders();
        }
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

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("chat")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ColorUtils.color("&cOnly players can use that command."));
                return true;
            }

            if (!(args.length > 0)) {
                sender.sendMessage(ColorUtils.color("&cUsage: /" + label + " <channelName>"));
                return true;
            }
            
            Actor senderActor = new PlayerActor(player);
            String channelName = args[0].toLowerCase();
            
            ChatSpace chatSpace;
            String nameOverride = "";
            if (channelName.equalsIgnoreCase("private")) {
                if (args.length >= 2) {
                    Actor targetActor = Actor.create(args[1]);
                    if (targetActor == null) {
                        sender.sendMessage(ColorUtils.color("&cInvalid target."));
                        return true;
                    }
                    
                    chatSpace = getPrivateMessage(senderActor, targetActor);
                    if (chatSpace == null) {
                        sender.sendMessage(ColorUtils.color("You do not have a private conversation with " + targetActor.getName()));
                        return true;
                    }
                    nameOverride = "Private (" + targetActor.getName() + ")";
                } else {
                    PrivateMessage privateMessage = this.lastMessage.get(((Player) sender).getUniqueId());
                    chatSpace = privateMessage;
                    if (chatSpace == null) {
                        sender.sendMessage(ColorUtils.color("&cYou do not have a last conversation to use as a focus."));
                        return true;
                    }
                   
                    Actor other = privateMessage.getActor1().equals(senderActor) ? privateMessage.getActor2() : privateMessage.getActor1();
                    nameOverride = "Private (" + other.getName() + ")";
                }
            } else {
                chatSpace = this.channelRegistry.get(channelName);
                if (chatSpace == null) {
                    chatSpace = this.roomRegistry.get(channelName);
                }
            }
            
            if (chatSpace == null) {
                ChatSelector selector = this.chatSelectors.get(channelName);
                if (selector != null) {
                    ChatSelection selection = selector.getSelection(player, args);
                    if (selection != null) {
                        chatSpace = selection.space();
                        nameOverride = selection.nameOverride();
                    }
                }
            }
            
            if (chatSpace == null) {
                sender.sendMessage(ColorUtils.color("&cSorry, but &e" + channelName + "&c is not a registered chat space."));
                return true;
            }

            if (chatSpace instanceof ChatChannel chatChannel) {
                String sendPermission = chatChannel.getSendPermission();
                if (!sendPermission.isEmpty() && !player.hasPermission(sendPermission)) {
                    sender.sendMessage(ColorUtils.color("&cYou do not have permission to send messages in " + chatSpace.getName() + "."));
                    return true;
                }
            } else if (chatSpace instanceof ChatRoom chatRoom) {
                if (!chatRoom.isMember(player.getUniqueId())) {
                    sender.sendMessage(ColorUtils.color("&cYou are not a member of " + chatRoom.getName()));
                    return true;
                }
            }

            this.setPlayerFocus(player, chatSpace);
            String spaceName = chatSpace.getName();
            if (nameOverride != null && !nameOverride.isEmpty()) {
                spaceName = nameOverride;
            }
            sender.sendMessage(ColorUtils.color("&aSet your chat focus to &b" + spaceName + "."));
        } else if (cmd.getName().equalsIgnoreCase("message")) {
            if (!(args.length >= 2)) {
                sender.sendMessage(ColorUtils.color("&cUsage: /" + label + " <target> <message>"));
                return true;
            }
            
            Actor senderActor = Actor.create(sender);
            Actor targetActor = Actor.create(args[0]);
            
            if (targetActor == null) {
                sender.sendMessage(ColorUtils.color("&cInvalid target. They must be online, or the console."));
                return true;
            }
            
            PrivateMessage privateMessage = getPrivateMessage(senderActor, targetActor);
            if (privateMessage == null) {
                privateMessage = new PrivateMessage(this, senderActor, targetActor, mainConfig.getString("private-msg-format"));
                this.privateMessages.add(privateMessage);
            }
            
            StringBuilder msgBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                msgBuilder.append(args[i]).append(" ");
            }

            privateMessage.sendMessage(sender, msgBuilder.toString().trim());
            assignLastMessage(sender, msgBuilder, privateMessage, senderActor, targetActor);
        } else if (cmd.getName().equalsIgnoreCase("reply")) {
            if (args.length == 0) {
                sender.sendMessage(ColorUtils.color("&cUsage: /" + label + " <message>"));
                sender.sendMessage(ColorUtils.color("&cUsage: /" + label + " <target> <message>"));
                return true;
            }

            Actor senderActor = Actor.create(sender);
            Actor targetActor = Actor.create(args[0]);

            PrivateMessage privateMessage;
            
            int msgStart;
            if (targetActor != null) {
                privateMessage = getPrivateMessage(senderActor, targetActor);
                if (privateMessage == null) {
                    sender.sendMessage(ColorUtils.color("&cYou do not have a conversation open with " + targetActor.getName()));
                    return true;
                }
                msgStart = 1;
            } else {
                if (senderActor instanceof PlayerActor playerActor) {
                    privateMessage = this.lastMessage.get(playerActor.getUniqueId());
                } else {
                    privateMessage = consoleLastMessage;
                }
                
                if (privateMessage.getActor1().equals(senderActor)) {
                    targetActor = privateMessage.getActor2();
                } else {
                    targetActor = privateMessage.getActor1();
                }
                
                msgStart = 0;
            }
            
            if (privateMessage == null) {
                sender.sendMessage(ColorUtils.color("&cYou do not have a message to reply to."));
                return true;
            }

            StringBuilder msgBuilder = new StringBuilder();
            for (int i = msgStart; i < args.length; i++) {
                msgBuilder.append(args[i]).append(" ");
            }

            privateMessage.sendMessage(sender, msgBuilder.toString().trim());
            assignLastMessage(sender, msgBuilder, privateMessage, senderActor, targetActor);
        }
        return true;
    }
    
    private void assignLastMessage(CommandSender sender, StringBuilder msgBuilder, PrivateMessage privateMessage, Actor senderActor, Actor targetActor) {
        if (senderActor instanceof PlayerActor senderPlayerActor) {
            this.lastMessage.put(senderPlayerActor.getUniqueId(), privateMessage);
        } else if (senderActor instanceof ServerActor){
            this.consoleLastMessage = privateMessage;
        }
        
        if (targetActor instanceof PlayerActor targetPlayerActor) {
            this.lastMessage.put(targetPlayerActor.getUniqueId(), privateMessage);
        } else if (targetActor instanceof ServerActor){
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
}