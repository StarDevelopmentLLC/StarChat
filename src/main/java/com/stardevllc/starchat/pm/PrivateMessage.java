package com.stardevllc.starchat.pm;

import com.mysql.cj.conf.StringProperty;
import com.stardevllc.starchat.context.ChatContext;
import com.stardevllc.starchat.space.ChatSpace;
import com.stardevllc.starcore.actor.Actor;
import com.stardevllc.starcore.color.ColorHandler;
import com.stardevllc.starlib.observable.property.writable.ReadWriteStringProperty;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.function.Function;

public class PrivateMessage implements ChatSpace {

    protected long id;
    protected JavaPlugin plugin;

    protected final ReadWriteStringProperty name;
    protected final ReadWriteStringProperty senderFormat;
    protected Function<Player, String> displayNameHandler;

    private Actor actor1, actor2;

    public PrivateMessage(JavaPlugin plugin, Actor actor1, Actor actor2, String format) {
        this.plugin = plugin;
        this.name = new ReadWriteStringProperty(this, "name", "pm-" + actor1.getName() + "-" + actor2.getName());
        this.senderFormat = new ReadWriteStringProperty(this, "senderFormat", format);
        this.actor1 = actor1;
        this.actor2 = actor2;
    }

    @Override
    public void sendMessage(ChatContext chatContext) {
        Actor senderActor;
        Actor targetActor;
        
        CommandSender sender = chatContext.getSender();
        String message = chatContext.getMessage();

        if (actor1.equals(sender)) {
            senderActor = actor1;
            targetActor = actor2;
        } else if (actor2.equals(sender)) {
            senderActor = actor2;
            targetActor = actor1;
        } else {
            sender.sendMessage(ColorHandler.getInstance().color("&cCould not determine if you are involved with that conversation."));
            return;
        }

        String format = this.senderFormat.get().replace("{message}", message);

        String senderMsg = format.replace("{from}", "me").replace("{to}", targetActor.getName());
        String targetMsg = format.replace("{from}", senderActor.getName()).replace("{to}", "me");
        
        senderActor.sendMessage(ColorHandler.getInstance().color(senderMsg));
        targetActor.sendMessage(ColorHandler.getInstance().color(targetMsg));
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
