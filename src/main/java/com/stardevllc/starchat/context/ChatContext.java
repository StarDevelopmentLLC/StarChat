package com.stardevllc.starchat.context;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatContext {
    public enum Source {
        CHAT_EVENT, DIRECT_SENDER, DIRECT_NO_SENDER
    }

    private final Source source;
    private CommandSender sender;
    private String message;
    private AsyncPlayerChatEvent chatEvent;
    
    public ChatContext(String message) {
        this.source = Source.DIRECT_NO_SENDER;
        this.message = message;
    }
    
    public ChatContext(CommandSender sender, String message) {
        this.source = Source.DIRECT_SENDER;
        this.sender = sender;
        this.message = message;
    }

    public ChatContext(AsyncPlayerChatEvent chatEvent) {
        this.source = Source.CHAT_EVENT;
        this.chatEvent = chatEvent;
    }

    public Source getSource() {
        return source;
    }

    public CommandSender getSender() {
        if (chatEvent != null) {
            return chatEvent.getPlayer();
        }
        return sender;
    }

    public String getMessage() {
        if (chatEvent != null) {
            return chatEvent.getMessage();
        }
        return message;
    }

    public AsyncPlayerChatEvent getChatEvent() {
        return chatEvent;
    }
    
    public Player getSenderAsPlayer() {
        if (getSender() instanceof Player player) {
            return player;
        }
        
        return null;
    }
}