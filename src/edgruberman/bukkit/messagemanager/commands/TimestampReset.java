package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.entity.Player;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.Permission;
import edgruberman.bukkit.messagemanager.channels.Recipient;

class TimestampReset extends Action {
    
    TimestampReset(final Command owner) {
        // Suggested: /timestamp[ <Player>] reset
        super(owner, "reset", Permission.TIMESTAMP_RESET, "^(?:(\\S*) )?reset$");
    }
    
    @Override
    void execute(final Context context) {
        String target = Timestamp.parsePlayer(context);
        if (target == null) {
            Main.messageManager.respond(context.sender, "Unable to determine player.", MessageLevel.SEVERE, false);
            return;
        }
        
        if (!(context.sender instanceof Player) || !((Player) context.sender).getName().equals(target)) {
            Permission permission = Permission.TIMESTAMP_RESET_PLAYER;
            if (!context.sender.hasPermission(permission.toString("*"))
                    && !context.sender.hasPermission(permission.toString(target))) {
                Main.messageManager.respond(context.sender, "You are not allowed to use the " + context.label + " command's " + this.name + " action for " + target, MessageLevel.RIGHTS, false);
                return;
            }
        }
        
        // Reset recipient file configuration.
        Main.saveRecipient(target, null, null);
        
        // Update recipient if online.
        Player player = Timestamp.getExactPlayer(target);
        if (player != null) {
            Recipient.getInstance(player).setTimestamp(Main.timestampFor(target));
            Recipient.getInstance(player).setUseTimestamp(Main.useTimestampFor(target));
            
            // As long as we have it, get the proper casing for the player name.
            target = player.getName();
        }
        
        // Respond with verification.
        Main.messageManager.respond(context.sender, target + "'s Timestamp has been reset to default.", MessageLevel.STATUS, false);
    }
}