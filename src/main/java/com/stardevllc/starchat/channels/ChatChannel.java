package com.stardevllc.starchat.channels;

import com.stardevllc.property.BooleanProperty;
import com.stardevllc.property.LongProperty;
import com.stardevllc.property.StringProperty;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starcore.color.ColorHandler;
import com.stardevllc.starcore.config.Config;
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
import java.util.function.Function;

public class ChatChannel implements ChatSpace {
    protected transient File file; //The main file for the config.
    protected transient Config config; //Config to store information as channels are mainly config/command controlled, transient modifier allows StarData to ignore this field without having to depend on StarData directly

    protected final LongProperty id;
    protected final JavaPlugin plugin;

    protected final StringProperty name;
    protected final StringProperty viewPermission;
    protected final StringProperty sendPermission;
    protected final StringProperty senderFormat;
    protected final StringProperty systemFormat;
    protected final BooleanProperty useColorPermissions;

    protected Function<Player, String> displayNameHandler;

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

        this.config = new Config(file);

        this.id = new LongProperty(this, "id", 0);
        this.name = new StringProperty(this, "name", name);

        createDefaults();

        this.name.addListener(e -> config.set("name", e.newValue()));
        this.viewPermission = new StringProperty(this, "viewPermission", this.config.getString("permissions.view"));
        this.viewPermission.addListener(e -> config.set("permissions.view", e.newValue()));
        this.sendPermission = new StringProperty(this, "sendPermission", this.config.getString("permissions.send"));
        this.sendPermission.addListener(e -> config.set("permissions.send", e.newValue()));
        this.senderFormat = new StringProperty(this, "senderFormat", this.config.getString("formats.sender"));
        this.senderFormat.addListener(e -> config.set("formats.sender", e.newValue()));
        this.systemFormat = new StringProperty(this, "systemFormat", this.config.getString("formats.system"));
        this.systemFormat.addListener(e -> config.set("formats.system", e.newValue()));
        this.useColorPermissions = new BooleanProperty(this, "useColorPermissions", config.getBoolean("settings.usecolorpermissions"));
        this.useColorPermissions.addListener(e -> config.set("settings.usecolorpermissions", e.newValue()));
        this.cooldownLength = new LongProperty(this, "cooldownLength", new TimeParser().parseTime(config.getString("settings.cooldownlength")));
        this.cooldownLength.addListener(e -> config.set("settings.cooldownlength", e.newValue()));
    }

    protected void createDefaults() {
        config.addDefault("name", this.name.get(), "The name of the channel.", "It is recommended to not change this name in the file and instead use commands.");
        config.addDefault("formats.sender", "", "The format used when a Player or the Console sends a chat message in this channel.");
        config.addDefault("formats.system", "", "The format used when a message is sent to this channel without a sender.");
        config.addDefault("permissions.view", "", "The permission that is required to have in order for a player to receive messages from this channel.");
        config.addDefault("permissions.send", "", "The permission that is required to have in order for a player to send messages in this channel.");
        config.addDefault("settings.usecolorpermissions", false, "Whether or not to use fine-controlled color permissions from StarCore.");
        config.addDefault("settings.cooldownlength", "3s", "The amount of time in-between chat messages.", "This can be bypasses with starcore.channel.<channelname>.bypasscooldown.");
        config.save();
    }

    public Config getConfig() {
        return config;
    }

    public File getFile() {
        return file;
    }

    public void saveConfig() {
        config.save();
    }

    public String getViewPermission() {
        return viewPermission.get();
    }

    public String getSendPermission() {
        return sendPermission.get();
    }

    @Override
    public void sendMessage(ChatContext context) {
        String displayName;
        String message;

        if (context.getSender() == null) {
            displayName = "";
            message = ColorHandler.getInstance().color(context.getMessage());
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
                            ColorHandler.getInstance().coloredMessage(player, "&cYou must wait " + TIME_FORMAT.format(lastMessage + cooldownLength.get() - System.currentTimeMillis()) + " before you can chat again.");
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
                message = ColorHandler.getInstance().color(context.getSender(), message);
            } else {
                message = ColorHandler.getInstance().color(message);
            }

            if (context.getSender() instanceof ConsoleCommandSender) {
                displayName = StarChat.getInstance().getConsoleNameFormat();
            } else {
                Player player = (Player) context.getSender();
                displayName = Objects.requireNonNullElse(this.displayNameHandler, StarChat.vaultDisplayNameFunction).apply(player);
                if (displayName == null || displayName.isEmpty()) {
                    displayName = player.getName();
                }
            }
        }

        if (displayName == null || displayName.isEmpty()) {
            displayName = "";
        }

        String format;
        if (context.getSender() == null) {
            format = ColorHandler.getInstance().color(systemFormat.get().replace("{message}", message));
        } else {
            if (context.getSender() instanceof ConsoleCommandSender) {
                format = ColorHandler.getInstance().color(senderFormat.get().replace("{displayname}", displayName)).replace("{message}", message);
            } else {
                Player player = (Player) context.getSender();
                format = ColorHandler.getInstance().color(StarChat.getInstance().getPlaceholderHandler().setPlaceholders(player, senderFormat.get().replace("{displayname}", displayName))).replace("{message}", message);
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

    public Function<Player, String> getDisplayNameHandler() {
        return displayNameHandler;
    }

    public void setDisplayNameHandler(Function<Player, String> displayNameHandler) {
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
        this.config = new Config(this.file);
    }
}