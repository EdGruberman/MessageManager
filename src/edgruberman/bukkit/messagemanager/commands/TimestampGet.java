package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.OfflinePlayer;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;
import edgruberman.bukkit.messagemanager.channels.Recipient;
import edgruberman.bukkit.messagemanager.commands.util.Action;
import edgruberman.bukkit.messagemanager.commands.util.Context;
import edgruberman.bukkit.messagemanager.commands.util.Handler;
import edgruberman.bukkit.messagemanager.commands.util.Parser;

final class TimestampGet extends Action {

    TimestampGet(final Handler handler) {
        super(handler, "get");
    }

    @Override
    public boolean perform(final Context context) {
        // Example: /<command>[ get][ <Player>]
        int position = 1;
        if (context.arguments.size() >= 1 && !context.arguments.get(0).equalsIgnoreCase(this.name)) position = 0;
        OfflinePlayer target = Parser.parsePlayer(context, position);
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
        String example = MessageManager.getDispatcher().getChannel(this.handler.command.getPlugin().getServer())
                .format(Main.messageManager.owner, "Server Informational Message", MessageLevel.INFO);
        example = recipient.format(example);

        Main.messageManager.send(context.sender, "-- Timestamp for " + targetName, MessageLevel.STATUS, false);
        Main.messageManager.send(context.sender, "Pattern: " + TimestampPatternGet.message(recipient), MessageLevel.STATUS, false);
        Main.messageManager.send(context.sender, "Format: " + TimestampFormatGet.message(recipient), MessageLevel.STATUS, false);
        Main.messageManager.send(context.sender, "Time Zone: " + TimestampTimeZoneGet.message(recipient), MessageLevel.STATUS, false);
        Main.messageManager.send(context.sender, "Example: " + example, MessageLevel.STATUS, false);
        return true;
    }

}
