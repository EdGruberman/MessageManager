package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.OfflinePlayer;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.channels.Recipient;
import edgruberman.bukkit.messagemanager.commands.util.Action;
import edgruberman.bukkit.messagemanager.commands.util.Context;
import edgruberman.bukkit.messagemanager.commands.util.Parser;

final class TimestampFormatSet extends Action {

    TimestampFormatSet(final Action parent) {
        super(parent, "set");
    }

    @Override
    public boolean perform(final Context context) {
        // Example: /<command> format set[ <Player>] <Format>
        if (context.arguments.size() < 3) return false;

        OfflinePlayer target = Parser.parsePlayer(context, 2);
        if (target == null && context.sender instanceof OfflinePlayer) target = (OfflinePlayer) context.sender;
        if (target == null) {
            Main.messageManager.respond(context.sender, "Unable to determine player", MessageLevel.SEVERE, false);
            return false;
        }

        // Parse target player name
        String targetName = target.getName();
        if (target.getPlayer() != null) targetName = target.getPlayer().getName();

        // Verify requester has permission for player
        if (!Timestamp.isAllowed(context.sender, this.permission, targetName)) {
            Main.messageManager.respond(context.sender, "You are not allowed to use the " + this.getNamePath() + " action of the " + context.label + " command for " + target.getName(), MessageLevel.RIGHTS, false);
            return true;
        }


        final String format = (context.arguments.size() >= 4 ? context.arguments.get(3) : context.arguments.get(2));
        if (format == null) {
            Main.messageManager.respond(context.sender, "Unable to determine format", MessageLevel.SEVERE, false);
            return false;
        }

        edgruberman.bukkit.messagemanager.channels.Timestamp timestamp;
        boolean useTimestamp;
        if (target.getPlayer() != null) {
            // Use recipient if online
            timestamp = Recipient.getInstance(target.getPlayer()).getTimestamp();
            useTimestamp = Recipient.getInstance(target.getPlayer()).getUseTimestamp();
        } else {
            // Otherwise use configuration file
            timestamp = Main.timestampFor(targetName);
            useTimestamp = Main.useTimestampFor(targetName);
        }

        if (!format.contains("%1$s")) {
            Main.messageManager.respond(context.sender, "Timestamp format must at least contain message identifier: %1$s", MessageLevel.SEVERE, false);
            return true;
        }

        // Update timestamp and save to file
        timestamp.setFormat(format);
        Main.saveRecipient(targetName, timestamp, useTimestamp);

        // Respond with verification
        timestamp = Main.timestampFor(targetName);
        Main.messageManager.respond(context.sender, targetName + "'s Timestamp " + TimestampFormatGet.message(timestamp), MessageLevel.STATUS, false);

        return true;
    }

}
