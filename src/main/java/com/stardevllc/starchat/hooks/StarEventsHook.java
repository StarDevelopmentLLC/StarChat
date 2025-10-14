package com.stardevllc.starchat.hooks;

import com.stardevllc.starchat.api.SpaceChatEvent;
import com.stardevllc.starevents.EventListener;
import org.bukkit.event.EventHandler;

public class StarEventsHook extends EventListener {
    @EventHandler
    public void onSpaceChat(SpaceChatEvent e) {
        EVENT_BUS.post(e);
    }
}