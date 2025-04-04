package com.stardevllc.starchat.channels;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class StaffChannel extends ChatChannel {
    public StaffChannel(JavaPlugin plugin, File configFile) {
        super(plugin, "staff", configFile.toPath());
        this.senderFormat.set("&2&l[&a&lSTAFF&2&l] {displayname}&8: &f{message}");
        this.systemFormat.set("&2&l[&a&lSTAFF&2&l] &f{message}");
        this.viewPermission.set("starchat.channels.staff.view");
        this.sendPermission.set("starchat.channels.staff.send");
        try {
            this.config.save(this.file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}