package com.stardevllc.starchat.handler;

import org.bukkit.command.CommandSender;

public interface DisplayNameHandler {
    default String getDisplayName(CommandSender sender) {
        return getPrefix(sender) + getName(sender) + getSuffix(sender);
    }

    default String getPrefix(CommandSender sender) {
        return "";
    }

    default String getName(CommandSender sender) {
        return sender.getName();
    }

    default String getSuffix(CommandSender sender) {
        return "";
    }
}
