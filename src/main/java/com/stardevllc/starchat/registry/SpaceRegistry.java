package com.stardevllc.starchat.registry;

import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starlib.registry.AbstractRegistry;
import com.stardevllc.starlib.registry.RegistryKey;

import java.util.HashMap;
import java.util.Set;

public class SpaceRegistry extends AbstractRegistry<ChatSpace> {
    public SpaceRegistry() {
        super(ChatSpace.class, RegistryKey.of("spaces"), "Spaces", new HashMap<>(), null, false, null, Set.of());
    }
}