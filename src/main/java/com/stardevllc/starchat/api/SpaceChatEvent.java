package com.stardevllc.starchat.api;

import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.space.ChatSpace;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpaceChatEvent extends Event implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    
    private boolean cancelled;
    
    private final ChatSpace chatSpace;
    private final ChatContext context;

    public SpaceChatEvent(ChatSpace chatSpace, ChatContext context) {
        super(true);
        this.chatSpace = chatSpace;
        this.context = context;
    }

    public ChatSpace getChatSpace() {
        return chatSpace;
    }

    public ChatContext getContext() {
        return context;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
