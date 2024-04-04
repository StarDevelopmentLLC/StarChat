package com.stardevllc.starchat.rooms;

import com.stardevllc.starchat.ChatSpace;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starcore.utils.actor.Actor;
import com.stardevllc.starcore.utils.color.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("EqualsBetweenInconvertibleTypes")
public class ChatRoom extends ChatSpace {
    protected Actor owner;
    protected Map<UUID, RoomMember> members = new HashMap<>();

    public ChatRoom(JavaPlugin plugin, String name, Actor owner) {
        super(plugin, name);
        this.owner = owner;
    }

    public ChatRoom(JavaPlugin plugin, String name, Actor owner, String senderFormat, String systemFormat) {
        super(plugin, name, senderFormat, systemFormat);
        this.owner = owner;
    }

    @Override
    public void sendMessage(CommandSender sender, String message) {
        if (message == null) {
            return;
        }
        String formattedMessage = "";

        if (sender == null) {
            formattedMessage = systemFormat;
        } else if (sender instanceof ConsoleCommandSender) {
            formattedMessage = senderFormat.replace("{displayname}", StarChat.getConsoleNameFormat());
        } else if (sender instanceof Player player) {
            if (!isMember(player.getUniqueId())) {
                player.sendMessage(ColorUtils.color("&cYou are not a member of that room."));
                return;
            }

            RoomMember member = this.members.get(player.getUniqueId());
            if (!member.hasPermission(DefaultPermissions.SEND_MESSAGES) && !isOwner(player.getUniqueId())) {
                ColorUtils.coloredMessage(player, "&cYou do not have permission to send messages in that room.");
                return;
            }

            formattedMessage = senderFormat.replace("{displayname}", formatPlayerDisplayName(player));
        }

        if (StarChat.isUsePlaceholderAPI() && sender instanceof Player player) {
            formattedMessage = StarChat.getPlayerPlaceholders().setPlaceholders(player, formattedMessage);
        }

        formattedMessage = ColorUtils.color(formattedMessage);
        if (StarChat.isUseColorPermissions()) {
            formattedMessage = formattedMessage.replace("{message}", ColorUtils.color(sender, message));
        } else {
            formattedMessage = formattedMessage.replace("{message}", ColorUtils.color(message));
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isMember(player.getUniqueId())) {
                RoomMember member = this.members.get(player.getUniqueId());
                if (member.hasPermission(DefaultPermissions.VIEW_MESSAGES) || isOwner(player.getUniqueId())) {
                    player.sendMessage(formattedMessage);
                }
            }
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