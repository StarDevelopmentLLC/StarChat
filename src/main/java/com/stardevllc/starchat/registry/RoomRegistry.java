package com.stardevllc.starchat.registry;

import com.stardevllc.starchat.rooms.ChatRoom;
import com.stardevllc.starlib.event.bus.ReflectionEventBus;
import com.stardevllc.starlib.registry.AbstractRegistry;
import com.stardevllc.starlib.registry.RegistryKey;

import java.util.HashMap;
import java.util.Set;

public class RoomRegistry extends AbstractRegistry<ChatRoom> {
    
    public RoomRegistry(SpaceRegistry spaceRegistry) {
        super(ChatRoom.class, RegistryKey.of("chatrooms"), "Chat Rooms", new HashMap<>(), spaceRegistry, false, new ReflectionEventBus(), Set.of());
    }
}
