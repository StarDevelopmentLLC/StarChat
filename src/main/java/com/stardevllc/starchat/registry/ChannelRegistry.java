package com.stardevllc.starchat.registry;

import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starlib.registry.AbstractRegistry;
import com.stardevllc.starlib.registry.RegistryKey;

import java.util.HashMap;
import java.util.Set;

public class ChannelRegistry extends AbstractRegistry<ChatChannel> {
    
    public ChannelRegistry(SpaceRegistry spaceRegistry) {
        super(ChatChannel.class, RegistryKey.of("channels"), "Channels", new HashMap<>(), spaceRegistry, false, null, Set.of());
    }
}