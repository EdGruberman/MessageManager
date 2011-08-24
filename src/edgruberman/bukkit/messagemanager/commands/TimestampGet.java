package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.entity.Player;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.Permission;
import edgruberman.bukkit.messagemanager.channels.Channel;

class TimestampGet extends Action {
    
    TimestampGet(final Command owner) {
        // Suggested: /timestamp[ <Player>][ get]
        super(owner, "get", Permission.TIMESTAMP_GET, "^(\\S*) ?((?:get)|\\?)?$");
    }
    
    @Override
    void execute(final Context context) {
        String target = Timestamp.parsePlayer(context);
        if (target == null) {
            Main.messageManager.respond(context.sender, "Unable to determine player.", MessageLevel.SEVERE, false);
            return;
        }
        
        if ((context.sender instanceof Player) && !((Player) context.sender).getName().equals(target)) {
            if (!context.sender.hasPermission(Permission.TIMESTAMP_GET_PLAYER.toString("*"))
                    && !context.sender.hasPermission(Permission.TIMESTAMP_GET_PLAYER.toString(target))) {
                Main.messageManager.respond(context.sender, "You are not allowed to use the " + this.name + " action for " + target, MessageLevel.RIGHTS, false);
                return;
            }
        }
        
        edgruberman.bukkit.messagemanager.channels.Timestamp timestamp = Main.timestampFor(target);
        Main.messageManager.respond(context.sender, "-- Timestamp for " + target, MessageLevel.STATUS, false);
        Main.messageManager.respond(context.sender, TimestampPattern.message(timestamp), MessageLevel.STATUS, false);
        Main.messageManager.respond(context.sender, TimestampFormat.message(timestamp), MessageLevel.STATUS, false);
        Main.messageManager.respond(context.sender, TimestampTimeZone.message(timestamp), MessageLevel.STATUS, false);
        Main.messageManager.respond(context.sender, "Example: "
                + timestamp.format(Main.messageManager.settings.color.get(MessageLevel.INFO).get(Channel.Type.PLAYER).toString()
                + "Player Informational Message"), MessageLevel.STATUS, false);
    }
}