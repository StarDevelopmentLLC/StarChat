package com.stardevllc.starchat.channels;

import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starcore.api.actors.Actor;
import com.stardevllc.config.file.yaml.YamlConfig;
import com.stardevllc.observable.property.BooleanProperty;
import com.stardevllc.observable.property.LongProperty;
import com.stardevllc.observable.property.ObjectProperty;
import com.stardevllc.observable.property.StringProperty;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.api.SpaceChatEvent;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.handler.DisplayNameHandler;
import com.stardevllc.starchat.obserable.ConfigChangeListener;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.time.TimeFormat;
import com.stardevllc.time.TimeParser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class ChatChannel implements ChatSpace {
    protected transient File file; //The main file for the config.
    protected transient YamlConfig config; //Config to store information as channels are mainly config/command controlled, transient modifier allows StarData to ignore this field without having to depend on StarData directly
    
    protected final LongProperty id;
    protected final JavaPlugin plugin;
    
    protected final StringProperty name;
    protected final StringProperty viewPermission;
    protected final StringProperty sendPermission;
    protected final StringProperty senderFormat;
    protected final StringProperty systemFormat;
    protected final BooleanProperty useColorPermissions;
    protected final BooleanProperty muted;
    protected final ObjectProperty<Actor> mutedBy;
    protected final StringProperty muteReason;
    protected final StringProperty muteFormat;
    protected final StringProperty unmuteFormat;
    protected final StringProperty muteErrorFormat;
    protected final StringProperty muteBypassPermission;
    
    protected DisplayNameHandler displayNameHandler;
    
    protected final LongProperty cooldownLength;
    
    protected Map<UUID, Long> lastMessage = new HashMap<>();
    
    protected static final TimeFormat TIME_FORMAT = new TimeFormat("%*#0h%%*#0m%%*#0s%");
    
    public ChatChannel(JavaPlugin plugin, String name, Path filePath) {
        this.plugin = plugin;
        this.file = new File(filePath.toFile().getAbsolutePath());
        
        if (!this.file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
            }
        }
        
        this.config = YamlConfig.loadConfiguration(this.file);
        
        this.id = new LongProperty(this, "id", 0);
        this.name = new StringProperty(this, "name", name);
        
        createDefaults();
        
        this.name.addListener(e -> config.set("name", e.newValue()));
        this.viewPermission = new StringProperty(this, "viewPermission", this.config.getString("permissions.view"));
        this.viewPermission.addListener(new ConfigChangeListener<>(file, config, "permissions.view"));
        this.sendPermission = new StringProperty(this, "sendPermission", this.config.getString("permissions.send"));
        this.sendPermission.addListener(new ConfigChangeListener<>(file, config, "permissions.send"));
        this.senderFormat = new StringProperty(this, "senderFormat", this.config.getString("formats.sender"));
        this.senderFormat.addListener(new ConfigChangeListener<>(file, config, "formats.sender"));
        this.systemFormat = new StringProperty(this, "systemFormat", this.config.getString("formats.system"));
        this.systemFormat.addListener(new ConfigChangeListener<>(file, config, "formats.system"));
        this.useColorPermissions = new BooleanProperty(this, "useColorPermissions", config.getBoolean("settings.usecolorpermissions"));
        this.useColorPermissions.addListener(new ConfigChangeListener<>(file, config, "settings.usecolorpermissions"));
        this.cooldownLength = new LongProperty(this, "cooldownLength", new TimeParser().parseTime(config.getString("settings.cooldownlength")));
        this.cooldownLength.addListener(e -> new ConfigChangeListener<>(file, config, "settings.cooldownlength"));
        this.muted = new BooleanProperty(this, "muted", this.config.getBoolean("mute.enabled"));
        this.muted.addListener(new ConfigChangeListener<>(file, config, "mute.enabled"));
        this.mutedBy = new ObjectProperty<>(Actor.class, this, "mutedby", Actor.create(this.config.getString("mute.actor")));
        this.mutedBy.addListener(changeEvent -> {
            if (changeEvent.newValue() == null) {
                config.set("mute.actor", "");
            } else {
                config.set("mute.actor", changeEvent.newValue().getConfigString());
            }
        });
        this.muteReason = new StringProperty(this, "muteReason", this.config.getString("mute.reason"));
        this.muteReason.addListener(new ConfigChangeListener<>(file, config, "mute.reason"));
        this.muteFormat = new StringProperty(this, "muteFormat", this.config.getString("mute.formats.on_mute"));
        this.muteFormat.addListener(new ConfigChangeListener<>(file, config, "mute.formats.on_mute"));
        this.unmuteFormat = new StringProperty(this, "unmuteFormat", this.config.getString("mute.formats.on_unmute"));
        this.unmuteFormat.addListener(new ConfigChangeListener<>(file, config, "mute.formats.on_unmute"));
        this.muteErrorFormat = new StringProperty(this, "muteErrorFormat", this.config.getString("mute.formats.muted_error"));
        this.muteErrorFormat.addListener(new ConfigChangeListener<>(file, config, "mute.formats.muted_error"));
        this.muteBypassPermission = new StringProperty(this, "muteBypassPermission", this.config.getString("mute.bypass_permission"));
        this.muteBypassPermission.addListener(new ConfigChangeListener<>(file, config, "mute.bypass_permission"));
    }
    
    protected void createDefaults() {
        config.addDefault("name", this.name.get(), "The name of the channel.", "It is recommended to not change this name in the file and instead use commands.");
        config.addDefault("formats.sender", "", "The format used when a Player or the Console sends a chat message in this channel.");
        config.addDefault("formats.system", "", "The format used when a message is sent to this channel without a sender.");
        config.addDefault("permissions.view", "", "The permission that is required to have in order for a player to receive messages from this channel.");
        config.addDefault("permissions.send", "", "The permission that is required to have in order for a player to send messages in this channel.");
        config.addDefault("settings.usecolorpermissions", false, "Whether or not to use fine-controlled color permissions from StarCore.");
        config.addDefault("settings.cooldownlength", "3s", "The amount of time in-between chat messages.", "This can be bypasses with starcore.channel.<channelname>.bypasscooldown.");
        config.addDefault("mute.enabled", false, "Controls if this channel is currently muted or not");
        config.addDefault("mute.actor", "", "This is the player that muted the chat");
        config.addDefault("mute.reason", "", "This is the reason supplied for muting the chat. This can be empty");
        config.addDefault("mute.formats.on_mute", "&cThe {channelName} channel has been muted by {actor}", "The format used when the channel is muted");
        config.addDefault("mute.formats.on_unmute", "&cThe {channelName} channel has been unmuted by {actor}", "The format used when the channel is unmuted");
        config.addDefault("mute.formats.muted_error", "&cYou cannot speak in {channelName} as it has been muted by {actor}", "The format sent to players that are trying to talk in the channel while it is muted");
        config.addDefault("mute.bypass_permission", "starchat." + this.name.get() + ".mute.bypass", "The permission where those with this permission can bypass the channel mute");
        
        try {
            config.save(file);
        } catch (IOException e) {
        }
    }
    
    public YamlConfig getConfig() {
        return config;
    }
    
    public File getFile() {
        return file;
    }
    
    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
        }
    }
    
    public String getViewPermission() {
        return viewPermission.get();
    }
    
    public String getSendPermission() {
        return sendPermission.get();
    }
    
    public void mute(Actor actor) {
        mute(actor, null);
    }
    
    public void mute(Actor actor, String reason) {
        this.muted.set(true);
        this.mutedBy.set(actor);
        this.muteReason.set(reason);
    }
    
    public void unmute(Actor actor) {
        this.muted.set(false);
        this.mutedBy.set(null);
        this.muteReason.set(null);
    }
    
    @Override
    public Set<Actor> getMembers() {
        Set<Actor> members = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (canViewMessages(player)) {
                members.add(Actor.of(player));
            }
        }
        
        return members;
    }
    
    public String getMuteFormat() {
        return muteFormat.get();
    }
    
    public String getUnmuteFormat() {
        return unmuteFormat.get();
    }
    
    @Override
    public void sendMessage(ChatContext context) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (canViewMessages(player)) {
                context.getRecipients().add(player.getUniqueId());
            }
        }
        
        SpaceChatEvent spaceChatEvent = new SpaceChatEvent(this, context);
        Bukkit.getPluginManager().callEvent(spaceChatEvent);
        
        if (spaceChatEvent.isCancelled()) {
            return;
        }
        
        String displayName, prefix, playerName, suffix;
        String message;
        
        if (context.getSender() == null) {
            displayName = "";
            playerName = "";
            prefix = "";
            suffix = "";
            message = StarColors.color(context.getMessage());
        } else {
            if (!canSendMessages(context.getSender())) {
                return;
            }
            
            CommandSender sender = context.getSender();
            
            if (this.isMuted()) {
                if (!sender.hasPermission(this.muteBypassPermission.get())) {
                    String msg = this.muteErrorFormat.get();
                    msg = msg.replace("{channelName}", this.name.get());
                    if (mutedBy.get() == null) {
                        msg = msg.replace("{actor}", "CONSOLE");
                    } else {
                        msg = msg.replace("{actor}", mutedBy.get().getName());
                    }
                    StarColors.coloredMessage(sender, msg);
                    return;
                }
            }
            
            if (sender instanceof Player player) {
                if (!sender.hasPermission("starchat.channel." + getName().toLowerCase() + ".bypasscooldown")) {
                    long lastMessage = this.lastMessage.getOrDefault(player.getUniqueId(), 0L);
                    if (lastMessage != 0L) {
                        if (System.currentTimeMillis() < lastMessage + cooldownLength.get()) {
                            StarColors.coloredMessage(player, "&cYou must wait " + TIME_FORMAT.format(lastMessage + cooldownLength.get() - System.currentTimeMillis()) + " before you can chat again.");
                            return;
                        }
                    }
                }
            }
            
            message = context.getMessage();
            
            if (this.useColorPermissions.get()) {
                message = StarColors.color(context.getSender(), message);
            } else {
                message = StarColors.color(message);
            }
            
            if (context.getSender() instanceof ConsoleCommandSender) {
                displayName = StarChat.getInstance().getConsoleNameFormat();
                playerName = "";
                prefix = "";
                suffix = "";
            } else {
                Player player = (Player) context.getSender();
                DisplayNameHandler handler = Objects.requireNonNullElse(this.displayNameHandler, StarChat.getDefaultDisplayNameHandler());
                displayName = handler.getDisplayName(player);
                playerName = handler.getName(player);
                prefix = handler.getPrefix(player);
                suffix = handler.getSuffix(player);
            }
        }
        
        String format;
        if (context.getSender() == null) {
            format = StarColors.color(systemFormat.get().replace("{message}", message));
        } else {
            if (context.getSender() instanceof ConsoleCommandSender) {
                format = StarColors.color(senderFormat.get().replace("{displayname}", displayName)).replace("{message}", message);
            } else {
                Player player = (Player) context.getSender();
                format = StarColors.color(StarChat.getInstance().getPlaceholderHandler().setPlaceholders(player, senderFormat.get().replace("{displayname}", displayName).replace("{prefix}", prefix).replace("{name}", playerName).replace("{suffix}", suffix))).replace("{message}", message);
            }
        }
        
        for (UUID recipient : context.getRecipients()) {
            Player player = Bukkit.getPlayer(recipient);
            if (player != null) {
                player.sendMessage(format);
            }
        }
    }
    
    @Override
    public boolean canSendMessages(CommandSender sender) {
        if (sender != null) {
            if (getSendPermission() != null && !getSendPermission().isEmpty()) {
                return sender.hasPermission(getSendPermission());
            } else {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean canViewMessages(CommandSender sender) {
        if (sender != null) {
            if (getViewPermission() != null && !getViewPermission().isEmpty()) {
                return sender.hasPermission(getViewPermission());
            } else {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String getName() {
        return name.get();
    }
    
    @Override
    public long getId() {
        return id.get();
    }
    
    @Override
    public JavaPlugin getPlugin() {
        return plugin;
    }
    
    @Override
    public boolean supportsCooldowns() {
        return true;
    }
    
    @Override
    public boolean isMuted() {
        return this.muted.getValue();
    }
    
    public DisplayNameHandler getDisplayNameHandler() {
        return displayNameHandler;
    }
    
    public void setDisplayNameHandler(DisplayNameHandler displayNameHandler) {
        this.displayNameHandler = displayNameHandler;
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public void setSenderFormat(String senderFormat) {
        this.senderFormat.set(senderFormat);
    }
    
    public void setSystemFormat(String systemFormat) {
        this.systemFormat.set(systemFormat);
    }
    
    public void setViewPermission(String viewPermission) {
        this.viewPermission.set(viewPermission);
    }
    
    public void setSendPermission(String sendPermission) {
        this.sendPermission.set(sendPermission);
    }
    
    public String getSenderFormat() {
        return senderFormat.get();
    }
    
    public String getSystemFormat() {
        return systemFormat.get();
    }
    
    public boolean isUseColorPermissions() {
        return useColorPermissions.get();
    }
    
    public void setFile(File newFile) {
        this.file = newFile;
        this.config = YamlConfig.loadConfiguration(file);
    }
}