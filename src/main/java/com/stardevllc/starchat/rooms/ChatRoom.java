package com.stardevllc.starchat.rooms;

import com.stardevllc.starchat.ChatSpace;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starmclib.actor.Actor;
import com.stardevllc.starmclib.actor.ServerActor;
import com.stardevllc.starmclib.color.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

@SuppressWarnings("EqualsBetweenInconvertibleTypes")
public class ChatRoom extends ChatSpace {
    protected Actor owner;
    protected Map<UUID, RoomMember> members = new HashMap<>();
    protected Set<RoomInvite> invites = new HashSet<>();
    protected Privacy privacy = Privacy.PRIVATE;
    protected Set<RoomBan> bans = new HashSet<>();

    public ChatRoom(String name, Actor owner) {
        super(name);
        this.owner = owner;
    }

    public ChatRoom(String name, Actor owner, String senderFormat, String systemFormat) {
        super(name, senderFormat, systemFormat);
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

        formattedMessage = formattedMessage.replace("{message}", message);
        formattedMessage = ColorUtils.color(formattedMessage);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isMember(player.getUniqueId())) {
                player.sendMessage(formattedMessage);
            }
        }
    }

    public boolean isBanned(UUID uuid) {
        for (RoomBan ban : this.bans) {
            if (ban.getTarget().equals(uuid)) {
                return false;
            }
        }
        return false;
    }

    public boolean isMember(UUID uuid) {
        if (owner.isPlayer()) {
            if (owner.equals(uuid)) {
                return true;
            }
        }
        return members.get(uuid) != null;
    }

    public boolean isInvited(UUID uuid) {
        return getInvite(uuid) != null;
    }

    public RoomInvite getInvite(UUID invited) {
        for (RoomInvite invite : this.invites) {
            if (invite.getInvitee().equals(invited)) {
                return invite;
            }
        }

        return null;
    }

    public void changeOwner(Actor newOwner) {
        this.owner = newOwner;
    }

    public void changePrivacy(Privacy newPrivacy) {
        this.privacy = newPrivacy;
    }

    public RoomBan banMember(Object actor, UUID target, String reason) {
        String actorValue;
        if (actor instanceof ConsoleCommandSender) {
            actorValue = "console";
        } else if (actor instanceof Player player) {
            actorValue = "player:" + player.getUniqueId();
        } else if (actor instanceof JavaPlugin plugin) {
            actorValue = "plugin:" + plugin.getName();
        } else if (actor instanceof UUID uuid) {
            if (uuid.equals(ServerActor.serverUUID)) {
                actorValue = "console";
            } else {
                actorValue = "player:" + uuid;
            }
        } else {
            return null;
        }

        RoomBan roomBan = new RoomBan(actorValue, target, reason, System.currentTimeMillis());
        this.bans.add(roomBan);
        this.members.remove(target);
        this.invites.removeIf(invite -> invite.getInvitee().equals(target) || invite.getInviter().equals(target));
        return roomBan;
    }

    public boolean unbanMember(UUID target) {
        return this.bans.removeIf(ban -> ban.getTarget().equals(target));
    }
    
    private int getActorWeight(Object actor) {
        if (this.owner.equals(actor)) {
            return RoomRole.OWNER.getWeight();
        } else {
            RoomMember actorMember = null;
            if (actor instanceof Player player) {
                actorMember = this.members.get(player.getUniqueId());
            } else if (actor instanceof UUID uuid) {
                actorMember = this.members.get(uuid);
            }

            if (actorMember != null) {
                return actorMember.getRole().getWeight();
            } else {
                return RoomRole.OWNER.getWeight() - 1;
            }
        }
    }

    public boolean addMember(Object actor, RoomMember member) {
        int actorWeight = getActorWeight(actor);
        if (actorWeight <= member.getRole().getWeight()) {
            return false;
        }
        
        this.members.put(member.getUniqueId(), member);
        return true;
    }

    public boolean removeMember(Object actor, RoomMember member) {
        int actorWeight = getActorWeight(actor);
        if (actorWeight <= member.getRole().getWeight()) {
            return false;
        }
        
        this.members.remove(member.getUniqueId());
        return true;
    }

    public RoomInvite inviteMember(Object actor, UUID invited, RoomRole role) {
        int actorWeight = getActorWeight(actor);
        if (role != null) {
            if (actorWeight <= role.getWeight()) {
                return null;
            }
        }
        
        RoomInvite roomInvite = new RoomInvite(this.getName(), invited, actor, System.currentTimeMillis(), role);
        this.invites.add(roomInvite);
        return roomInvite;
    }

    public boolean cancelInvite(Object actor, UUID invited) {
        return this.invites.removeIf(invite -> invite.getInvitee().equals(invited));
    }

    public RoomInvite acceptInvite(UUID invited) {
        Iterator<RoomInvite> inviteIterator = this.invites.iterator();
        while (inviteIterator.hasNext()) {
            RoomInvite invite = inviteIterator.next();
            if (invite.getInvitee().equals(invited)) {
                RoomMember roomMember = new RoomMember(invited, invite.getRole());
                this.members.put(invited, roomMember);
                inviteIterator.remove();
                return invite;
            }
        }
        
        return null;
    }

    public RoomInvite rejectInvite(UUID invited) {
        Iterator<RoomInvite> inviteIterator = this.invites.iterator();
        while (inviteIterator.hasNext()) {
            RoomInvite invite = inviteIterator.next();
            if (invite.getInvitee().equals(invited)) {
                inviteIterator.remove();
                return invite;
            }
        }

        return null;
    }
}