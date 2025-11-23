package com.stardevllc.starchat.mutechat;

import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.obserable.ConfigChangeListener;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starlib.observable.collections.set.ObservableHashSet;
import com.stardevllc.starlib.observable.collections.set.ObservableSet;
import com.stardevllc.starlib.observable.property.readwrite.*;
import com.stardevllc.starmclib.actors.Actor;
import com.stardevllc.starmclib.actors.Actors;

import java.util.*;

public class MuteChat {
    
    private final ReadWriteBooleanProperty muted;
    private final ReadWriteObjectProperty<Actor> actor;
    private final ReadWriteStringProperty reason;
    
    private final ObservableSet<String> spacesToMute = new ObservableHashSet<>();
    
    public MuteChat(StarChat plugin) {
        List<String> spacesFromConfig = plugin.getMainConfig().getStringList("globalmute.spaces");
        this.spacesToMute.addAll(spacesFromConfig);
        
        this.muted = new ReadWriteBooleanProperty(this, "muted", plugin.getMainConfig().getBoolean("globalmute.enabled"));
        this.muted.addListener(new ConfigChangeListener<>(plugin.getMainConfig(), "globalmute.enabled"));
        this.actor = new ReadWriteObjectProperty<>(this, "actor", Actor.class);
        this.actor.set(Actors.create(plugin.getMainConfig().get("globalmute.actor")));
        this.actor.addListener(c -> {
            if (c.newValue() == null) {
                plugin.getMainConfig().set("globalmute.actor", "");
            } else {
                plugin.getMainConfig().set("globalmute.actor", c.newValue().getConfigString());
            }
            
            plugin.saveMainConfig();
        });
        this.reason = new ReadWriteStringProperty(this, "reason", plugin.getMainConfig().getString("globalmute.reason"));
        this.reason.addListener(new ConfigChangeListener<>(plugin.getMainConfig(), "globalmute.reason"));
        
        muted.addListener(c -> {
            Iterator<String> iterator = spacesToMute.iterator();
            Set<Actor> members = new HashSet<>();
            while (iterator.hasNext()) {
                String spaceName = iterator.next();
                ChatSpace chatSpace = plugin.getSpaceRegistry().get(spaceName);
                if (chatSpace == null) {
                    iterator.remove();
                } else {
                    members.addAll(chatSpace.getMembers());
                    
                    if (c.newValue()) {
                        chatSpace.mute(actor.get(), reason.get());
                    } else {
                        chatSpace.unmute(null);
                    }
                }
            }
            
            String format;
            if (c.newValue()) {
                format = plugin.getMainConfig().getString("globalmute.format.mute");
            } else {
                format = plugin.getMainConfig().getString("globalmute.format.unmute");
            }
            
            format = format.replace("{actor}", actor.get().getName());
            if (reason.get() != null && !reason.get().isEmpty()) {
                format = format.replace("{reason}", "for " + reason.get());
            } else {
                format = format.replace("{reason}", "");
            }
            
            format = StarColors.color(format);
            
            for (Actor member : members) {
                member.sendMessage(format);
            }
        });
        
        this.spacesToMute.addListener(c -> {
            if (c.added() != null) {
                ChatSpace chatSpace = plugin.getSpaceRegistry().get(c.added());
                if (chatSpace == null) {
                    return;
                }
                
                if (this.isMuted()) {
                    chatSpace.mute(actor.get(), reason.get());
                }
            } else if (c.removed() != null) {
                ChatSpace chatSpace = plugin.getSpaceRegistry().get(c.removed());
                if (chatSpace == null) {
                    return;
                }
                
                if (this.isMuted()) {
                    chatSpace.unmute(null);
                }
            }
        });
    }
    
    public boolean isMuted() {
        return muted.get();
    }
    
    public void mute(Actor actor, String reason) {
        this.actor.set(actor);
        this.reason.set(reason);
        this.muted.set(true);
    }
    
    public void unmute() {
        this.muted.set(false);
        this.actor.set(null);
        this.reason.set("");
    }
    
    public void addSpaceToMute(String spaceName) {
        this.spacesToMute.add(spaceName);
    }
    
    public void addSpaceToMute(ChatSpace chatSpace) {
        addSpaceToMute(chatSpace.getName());
    }
    
    public void removeSpaceToMute(String spaceName) {
        this.spacesToMute.remove(spaceName);
    }
    
    public void removeSpaceToMute(ChatSpace chatSpace) {
        removeSpaceToMute(chatSpace.getName());
    }
}
