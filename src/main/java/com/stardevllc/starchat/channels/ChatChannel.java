package com.stardevllc.starchat.channels;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starcore.color.ColorUtils;
import com.stardevllc.starcore.utils.Config;
import com.stardevllc.starlib.observable.property.writable.BooleanProperty;
import com.stardevllc.starlib.observable.property.writable.LongProperty;
import com.stardevllc.starlib.observable.property.writable.StringProperty;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
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

        this.id = new LongProperty(this, "id");
        this.name = new StringProperty(this, "name", name);

        createDefaults();

        this.name.addListener((observableValue, oldValue, newValue) -> config.set("name", newValue));
        this.viewPermission = new StringProperty(this, "viewPermission", this.config.getString("permissions.view"));
        this.viewPermission.addListener((observableValue, oldValue, newValue) -> config.set("permissions.view", newValue));
        this.sendPermission = new StringProperty(this, "sendPermission", this.config.getString("permissions.send"));
        this.sendPermission.addListener((observableValue, oldValue, newValue) -> config.set("permissions.send", newValue));
        this.senderFormat = new StringProperty(this, "senderFormat", this.config.getString("formats.sender"));
        this.senderFormat.addListener((observableValue, oldValue, newValue) -> config.set("formats.sender", newValue));
        this.systemFormat = new StringProperty(this, "systemFormat", this.config.getString("formats.system"));
        this.systemFormat.addListener((observableValue, oldValue, newValue) -> config.set("formats.system", newValue));
        this.useColorPermissions = new BooleanProperty(this, "useColorPermissions", config.getBoolean("settings.usecolorpermissions"));
        this.useColorPermissions.addListener((observableValue, oldValue, newValue) -> config.set("settings.usecolorpermissions", newValue));
    }

    protected void createDefaults() {
        config.addDefault("name", this.name.get(), "The name of the channel.", "It is recommended to not change this name in the file and instead use commands.");
        config.addDefault("formats.sender", "", "The format used when a Player or the Console sends a chat message in this channel.");
        config.addDefault("formats.system", "", "The format used when a message is sent to this channel without a sender.");
        config.addDefault("permissions.view", "", "The permission that is required to have in order for a player to receive messages from this channel.");
        config.addDefault("permissions.send", "", "The permission that is required to have in order for a player to send messages in this channel.");
        config.addDefault("settings.usecolorpermissions", false, "Whether or not to use fine-controlled color permissions from StarCore.");
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
            message = ColorUtils.color(context.getMessage());
        } else {
            if (!canSendMessages(context.getSender())) {
                return;
            }

            CommandSender sender = context.getSender();

            if (context.getChatEvent() != null && context.getChatEvent().isCancelled()) {
                if (!sender.hasPermission("starchat.channel.bypass.cancelledevent")) {
                    return;
                }
            }

            message = context.getMessage();

            if (this.useColorPermissions.get()) {
                message = ColorUtils.color(context.getSender(), message);
            } else {
                message = ColorUtils.color(message);
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
            format = ColorUtils.color(systemFormat.get().replace("{message}", message));
        } else {
            if (context.getSender() instanceof ConsoleCommandSender) {
                format = ColorUtils.color(senderFormat.get().replace("{displayname}", displayName)).replace("{message}", message);
            } else {
                Player player = (Player) context.getSender();
                format = ColorUtils.color(StarChat.getInstance().getPlaceholderHandler().setPlaceholders(player, senderFormat.get().replace("{displayname}", displayName))).replace("{message}", message);
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
}