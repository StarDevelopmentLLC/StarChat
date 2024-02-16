package com.stardevllc.starchat.rooms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.stardevllc.starchat.rooms.RoomPermission.*;

public class RoomRole {
    
    public static final RoomRole MEMBER = new RoomRole("member", null, 1);
    public static final RoomRole TRUSTED = new RoomRole("trusted", MEMBER, 10);
    public static final RoomRole MOD = new RoomRole("mod", TRUSTED, 20, REMOVE_MEMBER);
    public static final RoomRole ADMIN = new RoomRole("admin", MOD, 30, CHANGE_PRIVACY, BAN, UNBAN, ADD_MEMBER);
    public static final RoomRole OWNER = new RoomRole("owner", ADMIN, 100);
    
    private String name;
    private RoomRole parent;
    private int weight;
    private Set<RoomPermission> permissions = new HashSet<>();
    
    public RoomRole(String name, RoomRole parent, int weight, RoomPermission... permissions) {
        this.name = name;
        this.parent = parent;
        this.permissions.addAll(List.of(permissions));
        this.weight = weight;
    }
    
    public boolean hasPermission(RoomPermission permission) {
        return permissions.contains(permission) || (parent != null && parent.hasPermission(permission));
    }

    public String getName() {
        return name;
    }

    public RoomRole getParent() {
        return parent;
    }

    public int getWeight() {
        return weight;
    }
}
