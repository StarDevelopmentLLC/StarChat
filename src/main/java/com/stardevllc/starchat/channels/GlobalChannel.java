package com.stardevllc.starchat.channels;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class GlobalChannel extends ChatChannel {
    public GlobalChannel(JavaPlugin plugin, String name, File configFile) {
        super(plugin, name, configFile.toPath());
        this.senderFormat.set("{displayname}&8: &r{message}");
        this.systemFormat.set("&r{message}");
        try {
            this.config.save(this.file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}