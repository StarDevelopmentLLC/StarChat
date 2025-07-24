package com.stardevllc.starchat.listener;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starlib.dependency.Inject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerListener implements Listener {
    
    @Inject
    private StarChat plugin;
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        ChatSpace chatSpace = plugin.getPlayerFocus(player);

        Bukkit.getScheduler().runTask(plugin, () -> {
            ChatContext context = new ChatContext(e);
            chatSpace.sendMessage(context);
            chatSpace.sendToConsole(context.getFinalMessage());
        });
        e.setCancelled(true);
    }
}
