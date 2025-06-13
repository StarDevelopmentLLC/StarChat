package com.stardevllc.starchat.context;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class ChatContext {
    public enum Source {
        CHAT_EVENT, DIRECT_SENDER, DIRECT_NO_SENDER
    }

    private final Source source;
    private CommandSender sender;
    private String message, finalMessage;
    private AsyncPlayerChatEvent chatEvent;
    private final Set<UUID> recipients = new HashSet<>();
    
    public ChatContext(String message) {
        this.source = Source.DIRECT_NO_SENDER;
        this.message = message;
    }
    
    public ChatContext(String message, Set<UUID> recipients) {
        this(message);
        this.recipients.addAll(recipients);
    }
    
    public ChatContext(CommandSender sender, String message) {
        this.source = Source.DIRECT_SENDER;
        this.sender = sender;
        this.message = message;
    }
    
    public ChatContext(CommandSender sender, String messages, Set<UUID> recipients) {
        this(sender, messages);
        this.recipients.addAll(recipients);
    }

    public ChatContext(AsyncPlayerChatEvent chatEvent) {
        this.source = Source.CHAT_EVENT;
        this.chatEvent = chatEvent;
    }
    
    public ChatContext(AsyncPlayerChatEvent chatEvent, Set<UUID> recipients) {
        this(chatEvent);
        this.recipients.addAll(recipients);
    }

    public Source getSource() {
        return source;
    }
    
    public Set<UUID> getRecipients() {
        return recipients;
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
    
    public void setMessage(String message) {
        if (chatEvent != null) {
            chatEvent.setMessage(message);
        } else {
            this.message = message;
        }
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
    
    public String getFinalMessage() {
        return finalMessage;
    }
    
    public void setFinalMessage(String finalMessage) {
        this.finalMessage = finalMessage;
    }
}