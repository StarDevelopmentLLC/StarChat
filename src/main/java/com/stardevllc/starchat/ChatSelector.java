package com.stardevllc.starchat;

import org.bukkit.entity.Player;

public abstract class ChatSelector {
    private String type; //This is the name provided in the /chat <name> command

    public ChatSelector(String type) {
        this.type = type;
    }

    public final String getType() {
        return type;
    }

    /**
     * This method is called when the {@code type} value is the same.  
     * This has the lowest priority in terms of determining what chat space is used for the command.
     * @param player The Player that used the /chat command
     * @param args The arguments of the command. This is the full arguments and not stripped down in any way.
     * @return The selection for the space
     */
    public abstract ChatSelection getSelection(Player player, String[] args);
    
    public record ChatSelection(ChatSpace space, String nameOverride) { }
}
