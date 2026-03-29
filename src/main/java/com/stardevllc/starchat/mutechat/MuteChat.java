package com.stardevllc.starchat.mutechat;

import com.stardevllc.StarColors;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.obserable.ConfigChangeListener;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starlib.collections.observable.set.ObservableHashSet;
import com.stardevllc.starlib.collections.observable.set.ObservableSet;
import com.stardevllc.starlib.values.property.*;
import com.stardevllc.actors.Actor;
import com.stardevllc.actors.Actors;

import java.util.*;

public class MuteChat {
    
    private final BooleanProperty muted;
    private final ObjectProperty<Actor> actor;
    private final StringProperty reason;
    
    private final ObservableSet<String> spacesToMute = new ObservableHashSet<>();
    
    public MuteChat(StarChat plugin) {
        List<String> spacesFromConfig = plugin.getMainConfig().getStringList("globalmute.spaces");
        this.spacesToMute.addAll(spacesFromConfig);
        
        this.muted = new BooleanProperty(this, "muted", plugin.getMainConfig().getBoolean("globalmute.enabled"));
        this.muted.addChangeListener(new ConfigChangeListener<>(plugin.getMainConfig(), "globalmute.enabled"));
        this.actor = new ObjectProperty<>(this, "actor", Actor.class);
        this.actor.set(Actors.create(plugin.getMainConfig().get("globalmute.actor")));
        this.actor.addChangeListener((v, o, n) -> {
            if (n == null) {
                plugin.getMainConfig().set("globalmute.actor", "");
            } else {
                plugin.getMainConfig().set("globalmute.actor", n.getConfigString());
            }
            
            plugin.saveMainConfig();
        });
        this.reason = new StringProperty(this, "reason", plugin.getMainConfig().getString("globalmute.reason"));
        this.reason.addChangeListener(new ConfigChangeListener<>(plugin.getMainConfig(), "globalmute.reason"));
        
        muted.addChangeListener((v, o, n) -> {
            Iterator<String> iterator = spacesToMute.iterator();
            Set<Actor> members = new HashSet<>();
            while (iterator.hasNext()) {
                String spaceName = iterator.next();
                ChatSpace chatSpace = plugin.getSpaceRegistry().get(spaceName);
                if (chatSpace == null) {
                    iterator.remove();
                } else {
                    members.addAll(chatSpace.getMembers());
                    
                    if (n) {
                        chatSpace.mute(actor.get(), reason.get());
                    } else {
                        chatSpace.unmute(null);
                    }
                }
            }
            
            String format;
            if (n) {
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
