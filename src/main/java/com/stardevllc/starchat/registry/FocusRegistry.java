package com.stardevllc.starchat.registry;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.space.ChatSpace;
import org.bukkit.entity.Player;

import java.util.*;

public class FocusRegistry {
    
    private final Map<UUID, ChatSpace> map = new HashMap<>();
    
    public void setPlayerFocus(UUID uuid, ChatSpace chatSpace) {
        if (chatSpace == null) {
            map.remove(uuid);
        } else {
            map.put(uuid, chatSpace);
        }
    }

    public ChatSpace getPlayerFocus(Player player) {
        return getPlayerFocus(player, null);
    }

    public ChatSpace getPlayerFocus(Player player, ChatSpace defaultSpace) {
        if (defaultSpace == null) {
            defaultSpace = StarChat.getInstance().getGlobalChannel();
        }
        
        ChatSpace focus = map.get(player.getUniqueId());
        if (focus == null) {
            focus = defaultSpace;
        }
        setPlayerFocus(player.getUniqueId(), focus);
        return focus;
    }
}