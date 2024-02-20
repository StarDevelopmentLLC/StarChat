package com.stardevllc.starchat;

import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starchat.channels.GlobalChannel;
import com.stardevllc.starchat.channels.StaffChannel;
import com.stardevllc.starchat.pm.PrivateMessage;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starcore.Config;
import com.stardevllc.starlib.registry.StringRegistry;
import com.stardevllc.starmclib.actor.Actor;
import com.stardevllc.starmclib.actor.PlayerActor;
import com.stardevllc.starmclib.color.ColorUtils;
import net.milkbowl.vault.chat.Chat;
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
    public static String consoleNameFormat; //How the console name appears
    public static String privateMessageFromat; //The format used for private messages
    public static Chat vaultChat; //Vault chat hook
    private Config mainConfig;
    private ChatChannel globalChannel, staffChannel; //Default channels
    private Map<UUID, String> playerChatSelection = new HashMap<>(); //Current player focus

    private StringRegistry<ChatChannel> channelRegistry = new StringRegistry<>(); //All channels
    private StringRegistry<ChatRoom> roomRegistry = new StringRegistry<>(); //All rooms
    private Set<PrivateMessage> privateMessages = new HashSet<>();
    private Map<Actor, PrivateMessage> lastMessage = new HashMap<>();

    @Override
    public void onEnable() {
        mainConfig = new Config(new File(getDataFolder(), "config.yml"));

        if (!setupChat()) {
            getLogger().severe("Could not setup Vault Chat.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        mainConfig.addDefault("console-name-format", "&4Console", "The name that the console appears as in chat spaces.");
        mainConfig.addDefault("private-msg-format", "&6[&c{from} &6-> [&c{to}]&8: &f{message}", "The format used for private messaging.");
        mainConfig.save();

        StarChat.consoleNameFormat = mainConfig.getString("console-name-format");

        globalChannel = new GlobalChannel(new File(getDataFolder() + File.separator + "channels" + File.separator + "defaults", "global.yml"));
        this.channelRegistry.register(globalChannel.getSimplifiedName(), globalChannel);

        staffChannel = new StaffChannel(new File(getDataFolder() + File.separator + "channels" + File.separator + "defaults", "staff.yml"));
        this.channelRegistry.register(staffChannel.getSimplifiedName(), staffChannel);

        getServer().getPluginManager().registerEvents(this, this);
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
                    PrivateMessage privateMessage = this.lastMessage.get(senderActor);
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
            }
            
            if (chatSpace == null) {
                sender.sendMessage(ColorUtils.color("&cSorry, but &e" + channelName + " is not a registered chat space."));
                return true;
            }

            if (chatSpace instanceof ChatChannel chatChannel) {
                String sendPermission = chatChannel.getSendPermission();
                if (!sendPermission.isEmpty() && !player.hasPermission(sendPermission)) {
                    sender.sendMessage(ColorUtils.color("&cYou do not have permission to send messages in " + chatSpace.getName() + "."));
                    return true;
                }
            }

            this.setPlayerFocus(player, chatSpace);
            String spaceName = chatSpace.getName();
            if (!nameOverride.isEmpty()) {
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
                privateMessage = new PrivateMessage(senderActor, targetActor, mainConfig.getString("private-msg-format"));
                this.privateMessages.add(privateMessage);
            }
            
            StringBuilder msgBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                msgBuilder.append(args[i]).append(" ");
            }
            
            privateMessage.sendMessage(sender, msgBuilder.toString().trim());
            this.lastMessage.put(senderActor, privateMessage);
            this.lastMessage.put(targetActor, privateMessage);
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
                privateMessage = this.lastMessage.get(senderActor);
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
        }
        return true;
    }

    public ChatChannel getGlobalChannel() {
        return globalChannel;
    }

    public ChatSpace getPlayerFocus(Player player) {
        String playerSelection = playerChatSelection.get(player.getUniqueId());
        ChatSpace space = this.channelRegistry.get(playerSelection);
        if (space == null) {
            space = this.roomRegistry.get(playerSelection);
        }

        return space;
    }

    public void setPlayerFocus(Player player, ChatSpace chatSpace) {
        setPlayerFocus(player, chatSpace.getSimplifiedName());
    }

    public void setPlayerFocus(Player player, String chatSpace) {
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
            boolean containsActor2 = privateMessage.getActor1().equals(actor1) || privateMessage.getActor2().equals(actor2);
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
}