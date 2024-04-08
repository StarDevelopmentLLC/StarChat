package com.stardevllc.starchat.space;

import com.stardevllc.starchat.context.ChatContext;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public interface ChatSpace {
    void sendMessage(ChatContext context);
    boolean canSendMessages(CommandSender sender);
    boolean canViewMessages(CommandSender sender);
    String getName();
    long getId();
    JavaPlugin getPlugin();
}