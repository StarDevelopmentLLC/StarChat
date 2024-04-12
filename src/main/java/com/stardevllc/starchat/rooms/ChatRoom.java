package com.stardevllc.starchat.rooms;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starcore.utils.actor.Actor;
import com.stardevllc.starcore.utils.color.ColorUtils;
import com.stardevllc.starlib.observable.property.BooleanProperty;
import com.stardevllc.starlib.observable.property.StringProperty;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings("EqualsBetweenInconvertibleTypes")
public class ChatRoom implements ChatSpace {
    protected long id;
    protected JavaPlugin plugin;

    protected final StringProperty name;
    protected final BooleanProperty useColorPermissions;
    protected final StringProperty senderFormat;
    protected final StringProperty systemFormat;
    protected Function<Player, String> displayNameHandler;
    
    protected Actor owner;
    protected Map<UUID, RoomMember> members = new HashMap<>();

    public ChatRoom(JavaPlugin plugin, Actor owner, String name) {
        this.plugin = plugin;
        this.owner = owner;
        this.name = new StringProperty(this, "name", name);
        this.useColorPermissions = new BooleanProperty(this, "useColorPermissions", false);
        this.senderFormat = new StringProperty(this, "senderFormat", "");
        this.systemFormat = new StringProperty(this, "systemFormat", "");
    }
    
    public ChatRoom(JavaPlugin plugin, String name) {
        this(plugin, Actor.of(plugin), name);
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
                if (!sender.hasPermission("starchat.room.bypass.cancelledevent")) {
                    return;
                }
            }

            message = context.getMessage();

            if (this.useColorPermissions.get()) {
                message = ColorUtils.color(context.getSender(), message);
            }

            if (context.getSender() instanceof ConsoleCommandSender) {
                displayName = StarChat.getConsoleNameFormat();
            } else if (context instanceof Player player) {
                displayName = Objects.requireNonNullElse(this.displayNameHandler, StarChat.vaultDisplayNameFunction).apply(player);
            } else {
                return;
            }
        }

        String format;
        if (context.getSender() == null) {
            format = ColorUtils.color(systemFormat.get().replace("{message}", message));
        } else {
            if (context.getSender() instanceof Player player) {
                format = ColorUtils.color(StarChat.getPlayerPlaceholders().setPlaceholders(player, senderFormat.get().replace("{displayname}", displayName))).replace("{message}", message);
            } else {
                format = ColorUtils.color(senderFormat.get().replace("{displayname}", displayName)).replace("{message}", message);
            }
        }

        for (UUID uuid : this.members.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(format);
            }
        }
    }

    @Override
    public boolean canSendMessages(CommandSender sender) {
        if (owner.equals(sender)) {
            return true;
        }
        
        if (sender instanceof Player player) {
            RoomMember member = this.members.get(player.getUniqueId());
            if (member != null) {
                return member.hasPermission(DefaultPermissions.SEND_MESSAGES);
            }
        }
        
        return false;
    }

    @Override
    public boolean canViewMessages(CommandSender sender) {
        if (owner.equals(sender)) {
            return true;
        }

        if (sender instanceof Player player) {
            RoomMember member = this.members.get(player.getUniqueId());
            if (member != null) {
                return member.hasPermission(DefaultPermissions.VIEW_MESSAGES);
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
        return id;
    }

    @Override
    public JavaPlugin getPlugin() {
        return plugin;
    }

    public boolean isOwner(UUID uuid) {
        if (owner.isPlayer()) {
            return owner.equals(uuid);
        }
        
        return false;
    }
    
    public boolean isMember(UUID uuid) {
        if (isOwner(uuid)) {
            return true;
        }
        return members.containsKey(uuid);
    }

    public void changeOwner(Actor newOwner) {
        this.owner = newOwner;
    }

    public RoomMember addMember(UUID uniqueId, RoomPermission... permissions) {
        RoomMember member = new RoomMember(uniqueId, permissions);
        this.members.put(uniqueId, member);
        return member;
    }

    public void removeMember(UUID member) {
        this.members.remove(member);
    }
}