package com.stardevllc.starchat.rooms;

import java.util.Objects;
import java.util.UUID;

public class RoomBan {
    private String actor;
    private UUID target;
    private String reason;
    private long timestamp;

    public RoomBan(String actor, UUID target, String reason, long timestamp) {
        this.actor = actor;
        this.target = target;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public String getActor() {
        return actor;
    }

    public UUID getTarget() {
        return target;
    }

    public String getReason() {
        return reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        RoomBan roomBan = (RoomBan) object;

        if (!Objects.equals(actor, roomBan.actor)) return false;
        return Objects.equals(target, roomBan.target);
    }

    @Override
    public int hashCode() {
        int result = actor != null ? actor.hashCode() : 0;
        result = 31 * result + (target != null ? target.hashCode() : 0);
        return result;
    }
}