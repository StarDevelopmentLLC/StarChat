package com.stardevllc.starchat.registry;

import com.stardevllc.starchat.channels.ChatChannel;
import com.stardevllc.starlib.objects.key.Keys;
import com.stardevllc.starlib.registry.AbstractRegistry;

import java.util.HashMap;
import java.util.Set;

public class ChannelRegistry extends AbstractRegistry<ChatChannel> {
    
    public ChannelRegistry(SpaceRegistry spaceRegistry) {
        super(ChatChannel.class, Keys.of("channels"), "Channels", new HashMap<>(), spaceRegistry, false, null, Set.of());
    }
}