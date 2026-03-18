package com.stardevllc.starchat.channels;

import com.stardevllc.starchat.StarChat;

import java.io.File;

public class StaffChannel extends ChatChannel {
    public StaffChannel(String name, File configFile) {
        super(StarChat.getInstance(), name, configFile.toPath());
        this.senderFormat.set("&2&l[&a&lSTAFF&2&l] {displayname}&8: &f{message}");
        this.systemFormat.set("&2&l[&a&lSTAFF&2&l] &f{message}");
        this.viewPermission.set("starchat.channels.staff.view");
        this.sendPermission.set("starchat.channels.staff.send");
        this.config.save();
    }
}