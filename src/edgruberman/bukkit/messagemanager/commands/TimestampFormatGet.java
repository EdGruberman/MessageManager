package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.OfflinePlayer;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.commands.util.Action;
import edgruberman.bukkit.messagemanager.commands.util.Context;
import edgruberman.bukkit.messagemanager.commands.util.Parser;

class TimestampFormatGet extends Action {

    TimestampFormatGet(final Action parent) {
        super(parent, "get");
    }

    @Override
    public boolean matches(Context context) {
        return super.matchesBreadcrumb(context);
    }

    @Override
    public boolean perform(final Context context) {
        // Example: /<command> format[ get][ <Player>]
        int position = 2;
        if (context.arguments.size() >= 2 && !context.arguments.get(1).equalsIgnoreCase(this.name)) position = 1;
        OfflinePlayer target = Parser.parsePlayer(context, position);
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

        edgruberman.bukkit.messagemanager.channels.Timestamp timestamp = Main.timestampFor(targetName);
        Main.messageManager.respond(context.sender, targetName + "'s Timestamp " + TimestampFormatGet.message(timestamp), MessageLevel.STATUS, false);

        return true;
    }

    static String message(final edgruberman.bukkit.messagemanager.channels.Timestamp timestamp) {
        return "Format: " + timestamp.getFormat().replaceAll("&", "&&") + " (%1$s=msg*, %2$s=time)";
    }
}