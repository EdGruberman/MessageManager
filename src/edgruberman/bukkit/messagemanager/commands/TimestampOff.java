package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.OfflinePlayer;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.channels.Recipient;
import edgruberman.bukkit.messagemanager.commands.util.Action;
import edgruberman.bukkit.messagemanager.commands.util.Context;
import edgruberman.bukkit.messagemanager.commands.util.Handler;
import edgruberman.bukkit.messagemanager.commands.util.Parser;

final class TimestampOff extends Action {

    TimestampOff(final Handler handler) {
        super(handler, "off");
    }

    @Override
    public boolean perform(final Context context) {
        // Example: /<command> off[ <Player>]
        OfflinePlayer target = Parser.parsePlayer(context, 1);
        if (target == null && context.sender instanceof OfflinePlayer) target = (OfflinePlayer) context.sender;
        if (target == null) {
            Main.messageManager.respond(context.sender, "Unable to determine player", MessageLevel.SEVERE, false);
            return false;
        }

        String targetName = target.getName();
        if (target.getPlayer() != null) targetName = target.getPlayer().getName();

        if (!Timestamp.isAllowed(context.sender, this.permission, targetName)) {
            Main.messageManager.respond(context.sender, "You are not allowed to use the " + this.getNamePath() + " action for " + target.getName(), MessageLevel.RIGHTS, false);
            return true;
        }

        // Reset recipient file configuration
        Main.saveRecipient(targetName, Main.timestampFor(targetName), false);

        // Update recipient if online
        if (target.getPlayer() != null)
            Recipient.getInstance(target.getPlayer()).setUseTimestamp(false);

        // Respond with verification
        Main.messageManager.respond(context.sender, targetName + "'s Timestamp has been toggled off", MessageLevel.STATUS, false);

        return true;
    }

}
