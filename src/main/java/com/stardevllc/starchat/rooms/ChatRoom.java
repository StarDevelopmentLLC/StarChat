package com.stardevllc.starchat.rooms;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.api.SpaceChatEvent;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.handler.DisplayNameHandler;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starlib.observable.property.readwrite.*;
import com.stardevllc.starmclib.actors.Actor;
import com.stardevllc.starmclib.actors.Actors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

@SuppressWarnings("EqualsBetweenInconvertibleTypes")
public class ChatRoom implements ChatSpace {
    protected long id;
    protected JavaPlugin plugin;
    
    protected final ReadWriteStringProperty name;
    protected final ReadWriteBooleanProperty useColorPermissions;
    protected final ReadWriteStringProperty senderFormat;
    protected final ReadWriteStringProperty systemFormat;
    
    protected final ReadWriteBooleanProperty muted;
    protected final ReadWriteObjectProperty<Actor> mutedBy;
    protected final ReadWriteStringProperty muteReason;
    protected final ReadWriteStringProperty muteFormat;
    protected final ReadWriteStringProperty unmuteFormat;
    protected final ReadWriteStringProperty muteErrorFormat;
    
    protected DisplayNameHandler displayNameHandler;
    
    protected Actor owner;
    protected Map<UUID, RoomMember> members = new HashMap<>();
    
    public ChatRoom(JavaPlugin plugin, Actor owner, String name) {
        this.plugin = plugin;
        this.owner = owner;
        this.name = new ReadWriteStringProperty(this, "name", name);
        this.useColorPermissions = new ReadWriteBooleanProperty(this, "useColorPermissions", false);
        this.senderFormat = new ReadWriteStringProperty(this, "senderFormat", "");
        this.systemFormat = new ReadWriteStringProperty(this, "systemFormat", "");
        this.muted = new ReadWriteBooleanProperty(this, "muted", false);
        this.mutedBy = new ReadWriteObjectProperty<>(this, "mutedby", Actor.class);
        this.muteReason = new ReadWriteStringProperty(this, "muteReason", "");
        this.muteFormat = new ReadWriteStringProperty(this, "muteFormat", "");
        this.unmuteFormat = new ReadWriteStringProperty(this, "unmuteFormat", "");
        this.muteErrorFormat = new ReadWriteStringProperty(this, "muteErrorFormat", "");
    }
    
    public ChatRoom(JavaPlugin plugin, String name) {
        this(plugin, Actors.of(plugin), name);
    }
    
    @Override
    public void sendMessage(ChatContext context) {
        for (UUID uuid : this.members.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if (canViewMessages(player)) {
                    context.getRecipients().add(uuid);
                }
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
                plugin.getLogger().info("The sender " + context.getSender().getName() + " cannot chat in " + getName());
                return;
            }
            
            CommandSender sender = context.getSender();
            
            if (isMuted()) {
                if (!sender.hasPermission("starchat.room.bypass.mute")) {
                    if (sender instanceof Player player) {
                        RoomMember roomMember = this.members.get(player.getUniqueId());
                        if (!roomMember.hasPermission(DefaultPermissions.BYPASS_MUTE)) {
                            String msg = this.muteErrorFormat.get().replace("{roomName}", this.name.get()).replace("{actor}", this.mutedBy.get().getName());
                            StarColors.coloredMessage(player, msg);
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
                prefix = handler.getPrefix(player);
                playerName = handler.getName(player);
                suffix = handler.getSuffix(player);
            }
        }
        
        String format;
        if (context.getSender() == null) {
            format = StarColors.color(systemFormat.get().replace("{message}", message));
        } else {
            if (context.getSender() instanceof Player player) {
                format = StarColors.color(StarChat.getInstance().getPlaceholderHandler().setPlaceholders(player, senderFormat.get().replace("{displayname}", displayName).replace("{prefix}", prefix).replace("{name}", playerName).replace("{suffix}", suffix))).replace("{message}", message);
            } else {
                format = StarColors.color(senderFormat.get().replace("{displayname}", displayName)).replace("{message}", message);
            }
        }
        
        context.setFinalMessage(format);
        
        for (UUID uuid : context.getRecipients()) {
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
    
    @Override
    public boolean supportsCooldowns() {
        return false;
    }
    
    @Override
    public boolean isMuted() {
        return this.muted.get();
    }
    
    @Override
    public void mute(Actor actor, String reason) {
        this.muted.set(true);
        this.mutedBy.set(actor);
        this.muteReason.set(reason);
    }
    
    @Override
    public void unmute(Actor actor) {
        this.muted.set(false);
        this.mutedBy.set(null);
        this.muteReason.set(null);
    }
    
    @Override
    public Set<Actor> getMembers() {
        Set<Actor> members = new HashSet<>();
        for (UUID uuid : this.members.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                members.add(Actors.of(player));
            }
        }
        
        return members;
    }
    
    @Override
    public void sendToConsole(String message) {
        if (message != null && !message.isBlank()) {
            Bukkit.getServer().getLogger().info("[room: " + getName() + "] " + StarColors.stripColor(message));
        }
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