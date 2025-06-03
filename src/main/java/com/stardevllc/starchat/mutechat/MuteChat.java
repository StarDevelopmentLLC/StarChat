package com.stardevllc.starchat.mutechat;

import com.stardevllc.observable.collections.ObservableHashSet;
import com.stardevllc.observable.collections.ObservableSet;
import com.stardevllc.observable.property.BooleanProperty;
import com.stardevllc.observable.property.ObjectProperty;
import com.stardevllc.observable.property.StringProperty;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starchat.obserable.ConfigChangeListener;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starcore.api.actors.Actor;

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
        this.muted.addListener(new ConfigChangeListener<>(plugin.getMainConfigFile(), plugin.getMainConfig(), "globalmute.enabled"));
        this.actor = new ObjectProperty<>(Actor.class, this, "actor", Actor.create(plugin.getMainConfig().getString("globalmute.actor")));
        this.actor.addListener(changeEvent -> {
            if (changeEvent.newValue() == null) {
                plugin.getMainConfig().set("globalmute.actor", "");
            } else {
                plugin.getMainConfig().set("globalmute.actor", changeEvent.newValue().getConfigString());
            }
            
            plugin.saveMainConfig();
        });
        this.reason = new StringProperty(this, "reason", plugin.getMainConfig().getString("globalmute.reason"));
        this.reason.addListener(new ConfigChangeListener<>(plugin.getMainConfigFile(), plugin.getMainConfig(), "globalmute.reason"));

        muted.addListener(e -> {
            Iterator<String> iterator = spacesToMute.iterator();
            Set<Actor> members = new HashSet<>();
            while (iterator.hasNext()) {
                String spaceName = iterator.next();
                ChatSpace chatSpace = plugin.getSpaceRegistry().get(spaceName);
                if (chatSpace == null) {
                    iterator.remove();
                } else {
                    members.addAll(chatSpace.getMembers());

                    if (e.newValue()) {
                        chatSpace.mute(actor.get(), reason.get());
                    } else {
                        chatSpace.unmute(null);
                    }
                }
            }
            
            String format;
            if (e.newValue()) {
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
        
        this.spacesToMute.addListener(e -> {
            if (e.added() != null) {
                String spaceName = (String) e.added();
                ChatSpace chatSpace = plugin.getSpaceRegistry().get(spaceName);
                if (chatSpace == null) {
                    return;
                }
                
                if (this.isMuted()) {
                    chatSpace.mute(actor.get(), reason.get());
                }
            } else if (e.removed() != null) {
                String spaceName = (String) e.removed();
                ChatSpace chatSpace = plugin.getSpaceRegistry().get(spaceName);
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
