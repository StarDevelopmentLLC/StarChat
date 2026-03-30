package com.stardevllc.starchat.registry;

import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starlib.objects.key.Keys;
import com.stardevllc.starlib.registry.AbstractRegistry;

import java.util.HashMap;
import java.util.Set;

public class SpaceRegistry extends AbstractRegistry<ChatSpace> {
    public SpaceRegistry() {
        super(ChatSpace.class, Keys.of("spaces"), "Spaces", new HashMap<>(), null, false, null, Set.of());
    }
}