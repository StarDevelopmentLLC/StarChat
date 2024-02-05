package com.stardevllc.starchat.channels;

import java.io.File;

public class StaffChannel extends ChatChannel {
    public StaffChannel(File configFile) {
        super("staff", configFile);
        this.senderFormat = "&2&l[&a&lSTAFF&2] {displayname}&8: &f{message}";
        this.systemFormat = "&2&l[&a&lSTAFF&2] &f{message}";
        this.playerDisplayNameFormat = "{prefix}{name}";
        this.viewPermission = "starchat.channels.staff.view";
        this.sendPermission = "starchat.channels.staff.send";
        this.affectedByPunishments = false;
        this.createDefaults();
        this.loadSettings();
    }
}