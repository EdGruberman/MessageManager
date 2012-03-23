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
            Main.messageManager.send(context.sender, "Unable to determine player", MessageLevel.SEVERE, false);
            return false;
        }

        String targetName = target.getName();
        if (target.getPlayer() != null) targetName = target.getPlayer().getName();

        if (!Timestamp.isAllowed(context.sender, this.permission, targetName)) {
            Main.messageManager.send(context.sender, "You are not allowed to use the " + this.getNamePath() + " action for " + target.getName(), MessageLevel.RIGHTS, false);
            return true;
        }

        final Recipient recipient = Timestamp.getRecipient(target);
        recipient.setFormat("%1$s");
        recipient.save();
        Main.messageManager.send(context.sender, "Timestamp format for " + targetName + " has been set to: " + TimestampFormatGet.message(recipient.load()), MessageLevel.STATUS, false);
        return true;
    }

}
