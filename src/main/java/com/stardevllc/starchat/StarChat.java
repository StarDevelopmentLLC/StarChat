package com.stardevllc.starchat;

import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starchat.channels.GlobalChannel;
import com.stardevllc.starchat.channels.StaffChannel;
import com.stardevllc.starmclib.color.ColorUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class StarChat extends JavaPlugin implements Listener {
    public static String consoleNameFormat; //How the console name appears
    public static Chat vaultChat;
    private static YamlDocument yamlConfig;
    private ChatChannel globalChannel, staffChannel;
    private Map<UUID, String> playerChatSelection = new HashMap<>();
    private Map<String, ChatSpace> chatSpaces = new HashMap<>();

    @Override
    public void onEnable() {
        try {
            yamlConfig = YamlDocument.create(new File(getDataFolder(), "config.yml"), getResource("config.yml"), GeneralSettings.builder().setUseDefaults(true).build(), LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load config file", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupChat()) {
            getLogger().severe("Could not setup Vault Chat.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        if (!yamlConfig.contains("console-name-format")) {
            yamlConfig.set("console-name-format", "&4Console");
            yamlConfig.getBlock("console-name-format").addComment("The name that the console appears as in chat spaces");
        }
        
        if (!yamlConfig.contains("ops-name-color")) {
            yamlConfig.set("ops-name-color", "&4");
            yamlConfig.getBlock("ops-name-color").addComment("The color that server operators will have in their name.");
        }
        
        try {
            yamlConfig.save();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error while saving config.yml", e);
        }
        
        StarChat.consoleNameFormat = yamlConfig.getString("console-name-format");
        
        globalChannel = new GlobalChannel(new File(getDataFolder() + File.separator + "channels" + File.separator + "defaults", "global.yml"));
        this.chatSpaces.put(globalChannel.getSimplifiedName(), globalChannel);
        
        staffChannel = new StaffChannel(new File(getDataFolder() + File.separator + "channels" + File.separator + "defaults", "staff.yml"));
        this.chatSpaces.put(staffChannel.getSimplifiedName(), staffChannel);
        
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
            ChatSpace chatSpace = this.chatSpaces.get(channelName);
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
        return chatSpaces.getOrDefault(playerChatSelection.get(player.getUniqueId()), globalChannel);
    }
    
    public void setPlayerFocus(Player player, ChatSpace chatSpace) {
        setPlayerFocus(player, chatSpace.getSimplifiedName());
    }
    
    public void setPlayerFocus(Player player, String chatSpace) {
        this.playerChatSelection.put(player.getUniqueId(), chatSpace);
    }

    public static YamlDocument getYamlConfig() {
        return yamlConfig;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        vaultChat = rsp.getProvider();
        return vaultChat != null;
    }
}