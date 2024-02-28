package com.stardevllc.starchat.channels;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class GlobalChannel extends ChatChannel {
    public GlobalChannel(JavaPlugin plugin, File configFile) {
        super(plugin, "Global", configFile);
        this.senderFormat = "{displayname}&8: &f{message}";
        this.systemFormat = "&f{message}";
        this.playerDisplayNameFormat = "{prefix}{name}{suffix}";
        this.createDefaults();
        this.loadSettings();
    }
}