package com.stardevllc.starchat.pm;

import com.stardevllc.starchat.ChatSpace;
import com.stardevllc.starmclib.actor.Actor;
import com.stardevllc.starmclib.color.ColorUtils;
import org.bukkit.command.CommandSender;

import java.util.Objects;

@SuppressWarnings("EqualsBetweenInconvertibleTypes")
public class PrivateMessage extends ChatSpace {

    private Actor actor1, actor2;

    public PrivateMessage(Actor actor1, Actor actor2, String format) {
        super("pm-" + actor1.getName() + "-" + actor2.getName(), format, "");
        this.actor1 = actor1;
        this.actor2 = actor2;
    }

    @Override
    public void sendMessage(CommandSender sender, String message) {
        Actor senderActor;
        Actor targetActor;

        if (actor1.equals(sender)) {
            senderActor = actor1;
            targetActor = actor2;
        } else if (actor2.equals(sender)) {
            senderActor = actor2;
            targetActor = actor1;
        } else {
            sender.sendMessage(ColorUtils.color("&cCould not determine if you are involved with that conversation."));
            return;
        }

        String format = this.senderFormat.replace("{message}", message);

        String senderMsg = format.replace("{from}", "me").replace("{to}", targetActor.getName());
        String targetMsg = format.replace("{from}", senderActor.getName()).replace("{to}", "me");
        
        senderActor.sendMessage(ColorUtils.color(senderMsg));
        targetActor.sendMessage(ColorUtils.color(targetMsg));
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
