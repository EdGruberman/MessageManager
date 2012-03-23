package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.OfflinePlayer;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.channels.Recipient;
import edgruberman.bukkit.messagemanager.commands.util.Action;
import edgruberman.bukkit.messagemanager.commands.util.Context;
import edgruberman.bukkit.messagemanager.commands.util.Parser;

final class TimestampPatternSet extends Action {

    TimestampPatternSet(final Action parent) {
        super(parent, "set");
    }

    @Override
    public boolean perform(final Context context) {
        // Example: /<command> pattern set[ <Player>] <Pattern>
        if (context.arguments.size() < 3) return false;

        OfflinePlayer target = Parser.parsePlayer(context, 2);
        if (target == null && context.sender instanceof OfflinePlayer) target = (OfflinePlayer) context.sender;
        if (target == null) {
            Main.messageManager.send(context.sender, "Unable to determine player", MessageLevel.SEVERE, false);
            return true;
        }

        String targetName = target.getName();
        if (target.getPlayer() != null) targetName = target.getPlayer().getName();

        if (!Timestamp.isAllowed(context.sender, this.permission, targetName)) {
            Main.messageManager.send(context.sender, "You are not allowed to use the " + this.getNamePath() + " action of the " + context.label + " command for " + target.getName(), MessageLevel.RIGHTS, false);
            return true;
        }

        final String pattern = (context.arguments.size() >= 4 ? context.arguments.get(3) : context.arguments.get(2));
        if (pattern == null) {
            Main.messageManager.send(context.sender, "Unable to determine pattern", MessageLevel.SEVERE, false);
            return false;
        }

        final Recipient recipient = Timestamp.getRecipient(target);
        try {
            recipient.getTimestamp().applyPattern(pattern);
        } catch (final IllegalArgumentException e) {
            Main.messageManager.send(context.sender, "Unable to update timestamp pattern for " + targetName + " with: " + pattern, MessageLevel.STATUS, false);
            return true;
        }
        recipient.save();

        // Respond with verification
        Main.messageManager.send(context.sender, "Timestamp pattern for " + targetName + ": " + TimestampPatternGet.message(recipient.load()), MessageLevel.STATUS, false);
        return true;
    }

}
