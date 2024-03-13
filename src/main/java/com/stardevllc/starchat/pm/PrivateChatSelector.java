package com.stardevllc.starchat.pm;

import com.stardevllc.starchat.ChatSelector;
import com.stardevllc.starchat.ChatSpace;
import com.stardevllc.starchat.StarChat;
import com.stardevllc.starmclib.actor.Actor;
import com.stardevllc.starmclib.actor.PlayerActor;
import com.stardevllc.starmclib.color.ColorUtils;
import org.bukkit.entity.Player;

public class PrivateChatSelector extends ChatSelector {
    public PrivateChatSelector() {
        super("private");
    }

    @Override
    public ChatSelection getSelection(Player player, String[] args) {
        ChatSpace chatSpace;
        String nameOverride;
        Actor senderActor = new PlayerActor(player);
        if (args.length >= 2) {
            Actor targetActor = Actor.create(args[1]);
            if (targetActor == null) {
                player.sendMessage(ColorUtils.color("&cInvalid target."));
                return null;
            }


            chatSpace = StarChat.getInstance().getPrivateMessage(senderActor, targetActor);
            if (chatSpace == null) {
                player.sendMessage(ColorUtils.color("You do not have a private conversation with " + targetActor.getName()));
                return null;
            }
            nameOverride = "Private (" + targetActor.getName() + ")";
        } else {
            PrivateMessage privateMessage = StarChat.getInstance().getLastMessage(player.getUniqueId());
            chatSpace = privateMessage;
            if (chatSpace == null) {
                player.sendMessage(ColorUtils.color("&cYou do not have a last conversation to use as a focus."));
                return null;
            }

            Actor other = privateMessage.getActor1().equals(senderActor) ? privateMessage.getActor2() : privateMessage.getActor1();
            nameOverride = "Private (" + other.getName() + ")";
        }
        return new ChatSelection(chatSpace, nameOverride);
    }
}
