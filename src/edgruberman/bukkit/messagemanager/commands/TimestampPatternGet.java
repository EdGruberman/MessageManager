package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.OfflinePlayer;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.channels.Recipient;
import edgruberman.bukkit.messagemanager.commands.util.Action;
import edgruberman.bukkit.messagemanager.commands.util.Context;
import edgruberman.bukkit.messagemanager.commands.util.Parser;

final class TimestampPatternGet extends Action {

    TimestampPatternGet(final Action parent) {
        super(parent, "get");
    }

    @Override
    public boolean perform(final Context context) {
        // Example: /<command> pattern[ get][ <Player>]
        int position = 2;
        if (context.arguments.size() >= 2 && !context.arguments.get(1).equalsIgnoreCase(this.name)) position = 1;
        OfflinePlayer target = Parser.parsePlayer(context, position);
        if (target == null && context.sender instanceof OfflinePlayer) target = (OfflinePlayer) context.sender;
        if (target == null) {
            Main.messageManager.send(context.sender, "Unable to determine player", MessageLevel.SEVERE, false);
            return false;
        }

        // Parse target player name
        String targetName = target.getName();
        if (target.getPlayer() != null) targetName = target.getPlayer().getName();

        // Verify requester has permission for player
        if (!Timestamp.isAllowed(context.sender, this.permission, targetName)) {
            Main.messageManager.send(context.sender, "You are not allowed to use the " + this.getNamePath() + " action of the " + context.label + " command for " + target.getName(), MessageLevel.RIGHTS, false);
            return true;
        }

        final Recipient recipient = Timestamp.getRecipient(target);
        Main.messageManager.send(context.sender, "Timestamp pattern for " + targetName + ": " + TimestampPatternGet.message(recipient), MessageLevel.STATUS, false);

        return true;
    }

    static String message(final Recipient recipient) {
        return recipient.getTimestamp().toPattern();
    }

}
