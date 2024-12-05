package com.stardevllc.starchat.rooms;

import com.stardevllc.actors.Actor;
import com.stardevllc.property.BooleanProperty;
import com.stardevllc.property.StringProperty;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starcore.color.ColorHandler;
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
            message = ColorHandler.getInstance().color(context.getMessage());
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
                message = ColorHandler.getInstance().color(context.getSender(), message);
            } else {
                message = ColorHandler.getInstance().color(message);
            }
            
            if (context.getSender() instanceof ConsoleCommandSender) {
                displayName = StarChat.getInstance().getConsoleNameFormat();
            } else {
                Player player = (Player) context.getSender();
                displayName = Objects.requireNonNullElse(this.displayNameHandler, StarChat.vaultDisplayNameFunction).apply(player);
            }
        }

        String format;
        if (context.getSender() == null) {
            format = ColorHandler.getInstance().color(systemFormat.get().replace("{message}", message));
        } else {
            if (context.getSender() instanceof Player player) {
                format = ColorHandler.getInstance().color(StarChat.getInstance().getPlaceholderHandler().setPlaceholders(player, senderFormat.get().replace("{displayname}", displayName))).replace("{message}", message);
            } else {
                format = ColorHandler.getInstance().color(senderFormat.get().replace("{displayname}", displayName)).replace("{message}", message);
            }
        }

        for (UUID uuid : this.members.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if (canViewMessages(player)) {
                    player.sendMessage(format);
                }
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

    @Override
    public boolean supportsCooldowns() {
        return false;
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