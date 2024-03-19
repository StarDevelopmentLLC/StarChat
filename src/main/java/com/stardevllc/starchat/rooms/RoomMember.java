package com.stardevllc.starchat.rooms;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RoomMember {
    private final UUID uniqueId;
    private Set<RoomPermission> permissions = new HashSet<>();

    public RoomMember(UUID uniqueId, RoomPermission... permissions) {
        this.uniqueId = uniqueId;
        addPermission(permissions);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }
    
    public boolean isOnline() {
        return getPlayer() != null;
    }

    public Set<RoomPermission> getPermissions() {
        return new HashSet<>(permissions);
    }
    
    public void addPermission(RoomPermission... permissions) {
        if (permissions != null) {
            this.permissions.addAll(List.of(permissions));
        }
    }
    
    public boolean hasPermission(RoomPermission permission) {
        return this.permissions.contains(permission);
    }
}