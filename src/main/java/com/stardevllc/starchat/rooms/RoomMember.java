package com.stardevllc.starchat.rooms;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RoomMember {
    private UUID uniqueId;
    private RoomRole role;
    private Set<RoomPermission> permissions = new HashSet<>();
    
    private String name;

    public RoomMember(UUID uniqueId, RoomRole role, RoomPermission... permissions) {
        this.uniqueId = uniqueId;
        this.role = role;
        this.permissions.addAll(List.of(permissions));
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }

    public RoomRole getRole() {
        return role;
    }
    
    public String getName() {
        if (this.name.isEmpty()) {
            Player player = Bukkit.getPlayer(uniqueId);
            if (player != null) {
                this.name = player.getName();
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uniqueId);
                if (offlinePlayer != null) {
                    this.name = offlinePlayer.getName();
                }
            }
        }
        
        return this.name;
    }
    
    public boolean hasPermission(RoomPermission permission) {
        return permissions.contains(permission) || role.hasPermission(permission);
    }
}
