package com.stardevllc.starchat.hooks;

import com.stardevllc.starchat.StarChat;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    private StarChat plugin;
    private Chat chat;

    public VaultHook(StarChat plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp != null) {
            chat = rsp.getProvider();
            return true;
        }
        return false;
    }

    public Chat getChat() {
        return chat;
    }
}