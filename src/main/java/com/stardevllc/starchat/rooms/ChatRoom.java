package com.stardevllc.starchat.rooms;

import com.stardevllc.starchat.ChatSpace;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starmclib.actor.Actor;
import com.stardevllc.starmclib.color.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("EqualsBetweenInconvertibleTypes")
public class ChatRoom extends ChatSpace {
    protected Actor owner;
    protected Set<UUID> members = new HashSet<>();

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

            formattedMessage = senderFormat.replace("{displayname}", formatPlayerDisplayName(player));
        }

        formattedMessage = ColorUtils.color(formattedMessage);
        if (StarChat.isUseColorPermissions()) {
            formattedMessage = formattedMessage.replace("{message}", ColorUtils.color(sender, message));
        } else {
            formattedMessage = formattedMessage.replace("{message}", ColorUtils.color(message));
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isMember(player.getUniqueId())) {
                player.sendMessage(formattedMessage);
            }
        }
    }

    public boolean isMember(UUID uuid) {
        if (owner.isPlayer()) {
            if (owner.equals(uuid)) {
                return true;
            }
        }
        return members.contains(uuid);
    }

    public void changeOwner(Actor newOwner) {
        this.owner = newOwner;
    }

    public boolean addMember(UUID member) {
        this.members.add(member);
        return true;
    }

    public boolean removeMember(UUID member) {
        this.members.remove(member);
        return true;
    }
}