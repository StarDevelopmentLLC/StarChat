package com.stardevllc.starchat.channels;

import java.io.File;

public class GlobalChannel extends ChatChannel {
    public GlobalChannel(File configFile) {
        super("Global", configFile);
        this.senderFormat = "{displayname}&8: &f{message}";
        this.systemFormat = "&f{message}";
        this.playerDisplayNameFormat = "{prefix}{name}{suffix}";
        this.createDefaults();
        this.loadSettings();
    }
}