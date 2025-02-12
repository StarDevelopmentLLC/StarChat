package com.stardevllc.starchat.space;

import com.stardevllc.actors.Actor;
import com.stardevllc.starchat.context.ChatContext;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public interface ChatSpace {
    void sendMessage(ChatContext context);
    boolean canSendMessages(CommandSender sender);
    boolean canViewMessages(CommandSender sender);
    String getName();
    long getId();
    JavaPlugin getPlugin();
    
    boolean supportsCooldowns();
    
    boolean isMuted();
    
    void mute(Actor actor, String reason);
    void unmute(Actor actor);
    
    Set<Actor> getMembers();
}