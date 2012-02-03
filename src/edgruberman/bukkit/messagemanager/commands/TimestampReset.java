package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.OfflinePlayer;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.channels.Recipient;
import edgruberman.bukkit.messagemanager.commands.util.Action;
import edgruberman.bukkit.messagemanager.commands.util.Context;
import edgruberman.bukkit.messagemanager.commands.util.Handler;
import edgruberman.bukkit.messagemanager.commands.util.Parser;

class TimestampReset extends Action {

    TimestampReset(final Handler handler) {
        super(handler, "reset");
    }

    @Override
    public boolean matches(Context context) {
        return super.matchesBreadcrumb(context);
    }

    @Override
    public boolean perform(final Context context) {
        // Example: /<command> reset[ <Player>]
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
        Main.saveRecipient(targetName, null, null);

        // Update recipient if online
        if (target.getPlayer() != null) {
            Recipient.getInstance(target.getPlayer()).setTimestamp(Main.timestampFor(targetName));
            Recipient.getInstance(target.getPlayer()).setUseTimestamp(Main.useTimestampFor(targetName));
        }

        // Respond with verification
        Main.messageManager.respond(context.sender, targetName + "'s Timestamp has been reset to default", MessageLevel.STATUS, false);

        return true;
    }

}