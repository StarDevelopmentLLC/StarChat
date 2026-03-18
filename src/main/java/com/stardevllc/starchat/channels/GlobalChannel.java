package com.stardevllc.starchat.channels;

import com.stardevllc.starchat.StarChat;

import java.io.File;

public class GlobalChannel extends ChatChannel {
    public GlobalChannel(String name, File configFile) {
        super(StarChat.getInstance(), "global", configFile.toPath());
        this.senderFormat.set("{displayname}&8: &r{message}");
        this.systemFormat.set("&r{message}");
        this.config.save();
    }
}