package com.stardevllc.starchat.channels;

import com.stardevllc.colors.StarColors;
import com.stardevllc.config.file.yaml.YamlConfig;
import com.stardevllc.observable.ChangeEvent;
import com.stardevllc.observable.ChangeListener;
import com.stardevllc.property.BooleanProperty;
import com.stardevllc.property.LongProperty;
import com.stardevllc.property.StringProperty;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.handler.DisplayNameHandler;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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

    protected DisplayNameHandler displayNameHandler;

    protected final LongProperty cooldownLength;

    protected Map<UUID, Long> lastMessage = new HashMap<>();
    
    protected static final TimeFormat TIME_FORMAT = new TimeFormat("%*#0h%%*#0m%%*#0s%");

    class ConfigChangeListener<T> implements ChangeListener<T> {
        private final String path;

        public ConfigChangeListener(String path) {
            this.path = path;
        }

        @Override
        public void changed(ChangeEvent<T> e) {
            config.set(path, e.newValue());
            try {
                config.save(file);
            } catch (IOException ex) {}
        }
    }
    
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
        this.viewPermission.addListener(new ConfigChangeListener<>("permissions.view"));
        this.sendPermission = new StringProperty(this, "sendPermission", this.config.getString("permissions.send"));
        this.sendPermission.addListener(new ConfigChangeListener<>("permissions.send"));
        this.senderFormat = new StringProperty(this, "senderFormat", this.config.getString("formats.sender"));
        this.senderFormat.addListener(new ConfigChangeListener<>("formats.sender"));
        this.systemFormat = new StringProperty(this, "systemFormat", this.config.getString("formats.system"));
        this.systemFormat.addListener(new ConfigChangeListener<>("formats.system"));
        this.useColorPermissions = new BooleanProperty(this, "useColorPermissions", config.getBoolean("settings.usecolorpermissions"));
        this.useColorPermissions.addListener(new ConfigChangeListener<>("settings.usecolorpermissions"));
        this.cooldownLength = new LongProperty(this, "cooldownLength", new TimeParser().parseTime(config.getString("settings.cooldownlength")));
        this.cooldownLength.addListener(e -> new ConfigChangeListener<>("settings.cooldownlength"));
    }

    protected void createDefaults() {
        config.addDefault("name", this.name.get(), "The name of the channel.", "It is recommended to not change this name in the file and instead use commands.");
        config.addDefault("formats.sender", "", "The format used when a Player or the Console sends a chat message in this channel.");
        config.addDefault("formats.system", "", "The format used when a message is sent to this channel without a sender.");
        config.addDefault("permissions.view", "", "The permission that is required to have in order for a player to receive messages from this channel.");
        config.addDefault("permissions.send", "", "The permission that is required to have in order for a player to send messages in this channel.");
        config.addDefault("settings.usecolorpermissions", false, "Whether or not to use fine-controlled color permissions from StarCore.");
        config.addDefault("settings.cooldownlength", "3s", "The amount of time in-between chat messages.", "This can be bypasses with starcore.channel.<channelname>.bypasscooldown.");
        try {
            config.save(file);
        } catch (IOException e) {}
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
        } catch (IOException e) {}
    }

    public String getViewPermission() {
        return viewPermission.get();
    }

    public String getSendPermission() {
        return sendPermission.get();
    }

    @Override
    public void sendMessage(ChatContext context) {
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

            if (context.getChatEvent() != null && context.getChatEvent().isCancelled()) {
                if (!sender.hasPermission("starchat.channel.bypass.cancelledevent")) {
                    return;
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

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (canViewMessages(player)) {
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