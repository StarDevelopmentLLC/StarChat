package com.stardevllc.starchat.rooms;

import java.util.Objects;
import java.util.UUID;

public class RoomInvite {
    private String chatRoom;
    private UUID invitee;
    private Object inviter;
    private long timestamp;
    private RoomRole role;

    public RoomInvite(String chatRoom, UUID invitee, Object inviter, long timestamp) {
        this(chatRoom, invitee, inviter, timestamp, RoomRole.MEMBER);
    }

    public RoomInvite(String chatRoom, UUID invitee, Object inviter, long timestamp, RoomRole role) {
        this.chatRoom = chatRoom;
        this.invitee = invitee;
        this.inviter = inviter;
        this.timestamp = timestamp;
        this.role = role;
    }

    public String getChatRoom() {
        return chatRoom;
    }

    public UUID getInvitee() {
        return invitee;
    }

    public Object getInviter() {
        return inviter;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public RoomRole getRole() {
        return role;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        RoomInvite that = (RoomInvite) object;

        if (!Objects.equals(chatRoom, that.chatRoom)) return false;
        if (!Objects.equals(invitee, that.invitee)) return false;
        return Objects.equals(inviter, that.inviter);
    }

    @Override
    public int hashCode() {
        int result = chatRoom != null ? chatRoom.hashCode() : 0;
        result = 31 * result + (invitee != null ? invitee.hashCode() : 0);
        result = 31 * result + (inviter != null ? inviter.hashCode() : 0);
        return result;
    }
}