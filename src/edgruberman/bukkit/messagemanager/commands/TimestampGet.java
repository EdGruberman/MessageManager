package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.OfflinePlayer;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.channels.Channel;
import edgruberman.bukkit.messagemanager.commands.util.Action;
import edgruberman.bukkit.messagemanager.commands.util.Context;
import edgruberman.bukkit.messagemanager.commands.util.Handler;
import edgruberman.bukkit.messagemanager.commands.util.Parser;

final class TimestampGet extends Action {

    TimestampGet(final Handler handler) {
        super(handler, "get");
    }

    @Override
    public boolean matches(Context context) {
        return super.matchesBreadcrumb(context);
    }

    @Override
    public boolean perform(final Context context) {
        // Example: /<command>[ get][ <Player>]
        int position = 1;
        if (context.arguments.size() >= 1 && !context.arguments.get(0).equalsIgnoreCase(this.name)) position = 0;
        OfflinePlayer target = Parser.parsePlayer(context, position);
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

        edgruberman.bukkit.messagemanager.channels.Timestamp timestamp = Main.timestampFor(targetName);
        Main.messageManager.respond(context.sender, "-- Timestamp for " + targetName, MessageLevel.STATUS, false);
        Main.messageManager.respond(context.sender, TimestampPatternGet.message(timestamp), MessageLevel.STATUS, false);
        Main.messageManager.respond(context.sender, TimestampFormatGet.message(timestamp), MessageLevel.STATUS, false);
        Main.messageManager.respond(context.sender, TimestampTimeZoneGet.message(timestamp), MessageLevel.STATUS, false);
        Main.messageManager.respond(context.sender, "Example: "
                + timestamp.format(Main.messageManager.getSettings().color.get(MessageLevel.INFO).get(Channel.Type.PLAYER).toString()
                + "Player Informational Message"), MessageLevel.STATUS, false);

        return true;
    }


}