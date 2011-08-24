package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.entity.Player;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.Permission;
import edgruberman.bukkit.messagemanager.channels.Recipient;

class TimestampPattern extends Action {
    
    TimestampPattern(final Command owner) {
        // Suggested: /timestamp[ <Player>] pattern (get|set <Pattern>)
        super(owner, "pattern", Permission.TIMESTAMP_PATTERN_GET, "^(?:(\\S*) )?pattern ?((?:get)|\\?|(?:set)|=)? ?(.+)?$");
    }
    
    @Override
    void execute(final Context context) {
        String target = Timestamp.parsePlayer(context);
        if (target == null) {
            Main.messageManager.respond(context.sender, "Unable to determine player.", MessageLevel.SEVERE, false);
            return;
        }
        
        String operation = Timestamp.parseOperation(context);
        if (!(context.sender instanceof Player) || !((Player) context.sender).equals(target)) {
            Permission permission = Permission.TIMESTAMP_PATTERN_GET_PLAYER;
            if (operation.equals("set")) permission = Permission.TIMESTAMP_PATTERN_SET_PLAYER;
            
            if (!context.sender.hasPermission(permission.toString("*"))
                    && !context.sender.hasPermission(permission.toString(target))) {
                Main.messageManager.respond(context.sender, "You are not allowed to use the " + context.label + " command's " + this.name + " action's " + operation + " operation for " + target, MessageLevel.RIGHTS, false);
                return;
            }
        }
        
        edgruberman.bukkit.messagemanager.channels.Timestamp timestamp;
        boolean useTimestamp;
        if (operation.equals("set")) {
            String value = (context.matches.size() >= 3 ? context.matches.get(2) : null);
            if (value == null) {
                Main.messageManager.respond(context.sender, "Value must be specified.", MessageLevel.SEVERE, false);
                return;
            }
            
            Player player = Timestamp.getExactPlayer(target);
            if (player != null) {
                // Use recipient if online.
                timestamp = Recipient.getInstance(player).getTimestamp();
                useTimestamp = Recipient.getInstance(player).getUseTimestamp();
                
                // As long as we have it, get the proper casing for the player name.
                target = player.getName();
            } else {
                // Otherwise use configuration file.
                timestamp = Main.timestampFor(target);
                useTimestamp = Main.useTimestampFor(target);
            }
            
            // Update timestamp and save to file.
            try {
                timestamp.setPattern(value);
                Main.saveRecipient(target, timestamp, useTimestamp);
                
            } catch (IllegalArgumentException e) {
                Main.messageManager.respond(context.sender, "Unable to update " + target + "'s timestamp pattern with " + value, MessageLevel.STATUS, false);
                return;

            }
        }
        
        // Respond with verification.
        timestamp = Main.timestampFor(target);
        Main.messageManager.respond(context.sender, target + "'s Timestamp " + TimestampPattern.message(timestamp), MessageLevel.STATUS, false);
    }
    
    static String message(final edgruberman.bukkit.messagemanager.channels.Timestamp timestamp) {
        return "Pattern: " + timestamp.getPattern();
    }
}