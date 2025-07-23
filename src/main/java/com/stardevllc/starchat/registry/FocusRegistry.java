package com.stardevllc.starchat.registry;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starlib.registry.UUIDRegistry;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FocusRegistry extends UUIDRegistry<ChatSpace> {
    
    public void setPlayerFocus(UUID uuid, ChatSpace chatSpace) {
        if (chatSpace == null) {
            this.unregister(uuid);
        } else {
            this.register(uuid, chatSpace);
        }
    }

    public ChatSpace getPlayerFocus(Player player) {
        return getPlayerFocus(player, null);
    }

    public ChatSpace getPlayerFocus(Player player, ChatSpace defaultSpace) {
        if (defaultSpace == null) {
            defaultSpace = StarChat.getInstance().getGlobalChannel();
        }
        
        ChatSpace focus = get(player.getUniqueId());
        if (focus == null) {
            focus = defaultSpace;
        }
        setPlayerFocus(player.getUniqueId(), focus);
        return focus;
    }
}