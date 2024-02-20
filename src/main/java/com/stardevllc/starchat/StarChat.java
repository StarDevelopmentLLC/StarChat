package com.stardevllc.starchat;

import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starchat.channels.GlobalChannel;
import com.stardevllc.starchat.channels.StaffChannel;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starcore.Config;
import com.stardevllc.starcore.StarCore;
import com.stardevllc.starlib.registry.StringRegistry;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StarChat extends JavaPlugin implements Listener {
    public static String consoleNameFormat; //How the console name appears
    public static UUID consoleUniqueId; //The uuid to use for the console
    public static Chat vaultChat; //Vault chat hook
    private Config mainConfig;
    private ChatChannel globalChannel, staffChannel; //Default channels
    private Map<UUID, String> playerChatSelection = new HashMap<>(); //Current player focus

    private StringRegistry<ChatChannel> channelRegistry = new StringRegistry<>(); //All channels
    private StringRegistry<ChatRoom> roomRegistry = new StringRegistry<>(); //All rooms

    @Override
    public void onEnable() {
        mainConfig = new Config(new File(getDataFolder(), "config.yml"));

        if (!setupChat()) {
            getLogger().severe("Could not setup Vault Chat.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        mainConfig.addDefault("console-name-format", "&4Console", "The name that the console appears as in chat spaces.");
        mainConfig.save();

        StarChat.consoleNameFormat = mainConfig.getString("console-name-format");
        StarChat.consoleUniqueId = ((StarCore) Bukkit.getPluginManager().getPlugin("StarCore")).getConsoleUnqiueId(); //Replace usages with this one

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

            String channelName = args[0].toLowerCase();
            ChatSpace chatSpace = this.channelRegistry.get(channelName);
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
            sender.sendMessage(ColorUtils.color("&aSet your chat focus to &b" + chatSpace.getName()) + ".");
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
}