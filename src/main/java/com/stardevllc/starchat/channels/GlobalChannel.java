package com.stardevllc.starchat.channels;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class GlobalChannel extends ChatChannel {
    public GlobalChannel(JavaPlugin plugin, String name, File configFile) {
        super(plugin, name, configFile.toPath());
        this.senderFormat.set("{displayname}&8: &r{message}");
        this.systemFormat.set("&r{message}");
        this.config.save();
    }
}