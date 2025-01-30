package com.stardevllc.starchat.handler;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.hooks.VaultHook;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VaultDisplayNameHandler implements DisplayNameHandler {

    @Override
    public String getPrefix(CommandSender sender) {
        VaultHook vaultHook = StarChat.getInstance().getVaultHook();
        if (vaultHook == null) {
            return "";
        }
        
        if (vaultHook.getChat() == null) {
            return "";
        }
        
        if (sender instanceof Player player) {
            return vaultHook.getChat().getPlayerPrefix(player);
        }
        
        return "";
    }

    @Override
    public String getSuffix(CommandSender sender) {
        VaultHook vaultHook = StarChat.getInstance().getVaultHook();
        if (vaultHook == null) {
            return "";
        }

        if (vaultHook.getChat() == null) {
            return "";
        }

        if (sender instanceof Player player) {
            return vaultHook.getChat().getPlayerSuffix(player);
        }

        return "";
    }
}
