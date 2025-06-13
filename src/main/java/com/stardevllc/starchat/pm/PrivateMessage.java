package com.stardevllc.starchat.pm;

import com.stardevllc.observable.property.StringProperty;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.handler.DisplayNameHandler;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starcore.api.StarColors;
import com.stardevllc.starcore.api.actors.Actor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class PrivateMessage implements ChatSpace {

    protected long id;
    protected JavaPlugin plugin;

    protected final StringProperty name;
    protected final StringProperty senderFormat;
    protected DisplayNameHandler displayNameHandler;

    private Actor actor1, actor2;

    public PrivateMessage(JavaPlugin plugin, Actor actor1, Actor actor2, String format) {
        this.plugin = plugin;
        this.name = new StringProperty(this, "name", "pm-" + actor1.getName() + "-" + actor2.getName());
        this.senderFormat = new StringProperty(this, "senderFormat", format);
        this.actor1 = actor1;
        this.actor2 = actor2;
    }
    
    @Override
    public void sendToConsole(String message) {
        Bukkit.getServer().getLogger().info("[private] " + StarColors.stripColor(message));
    }
    
    @Override
    public void sendMessage(ChatContext context) {
        Actor senderActor;
        Actor targetActor;
        
        CommandSender sender = context.getSender();
        String message = context.getMessage();

        if (actor1.equals(sender)) {
            senderActor = actor1;
            targetActor = actor2;
        } else if (actor2.equals(sender)) {
            senderActor = actor2;
            targetActor = actor1;
        } else {
            sender.sendMessage(StarColors.color("&cCould not determine if you are involved with that conversation."));
            return;
        }

        String format = this.senderFormat.get().replace("{message}", message);

        String senderMsg = format.replace("{from}", "me").replace("{to}", targetActor.getName());
        String targetMsg = format.replace("{from}", senderActor.getName()).replace("{to}", "me");
        
        context.setFinalMessage(format.replace("{from}", senderActor.getName()).replace("{to}", targetActor.getName()));
        
        senderActor.sendMessage(StarColors.color(senderMsg));
        targetActor.sendMessage(StarColors.color(targetMsg));
    }

    @Override
    public boolean canSendMessages(CommandSender sender) {
        Actor senderActor = Actor.create(sender);
        return !(senderActor.equals(actor1) || senderActor.equals(actor2));
    }

    @Override
    public boolean canViewMessages(CommandSender sender) {
        return canSendMessages(sender);
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean supportsCooldowns() {
        return false;
    }

    @Override
    public boolean isMuted() {
        return false;
    }

    @Override
    public void mute(Actor actor, String reason) {
        
    }

    @Override
    public void unmute(Actor actor) {

    }

    @Override
    public Set<Actor> getMembers() {
        return Set.of(actor1, actor2);
    }

    public Actor getActor1() {
        return actor1;
    }

    public Actor getActor2() {
        return actor2;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        PrivateMessage that = (PrivateMessage) object;

        if (!Objects.equals(actor1, that.actor1)) return false;
        return Objects.equals(actor2, that.actor2);
    }

    @Override
    public int hashCode() {
        int result = actor1 != null ? actor1.hashCode() : 0;
        result = 31 * result + (actor2 != null ? actor2.hashCode() : 0);
        return result;
    }
}
