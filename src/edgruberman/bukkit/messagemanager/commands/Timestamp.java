package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.MessageManager;
import edgruberman.bukkit.messagemanager.channels.Recipient;
import edgruberman.bukkit.messagemanager.commands.util.Handler;

public final class Timestamp extends Handler {

    public Timestamp(final JavaPlugin plugin) {
        super(plugin, "timestamp");
        new TimestampGet(this).setDefault();
        new TimestampReset(this);
        new TimestampOn(this);
        new TimestampOff(this);
        new TimestampPattern(this);
        new TimestampFormat(this);
        new TimestampTimeZone(this);
    }

    /**
     * Determines if sender has permission for target.
     */
    static boolean isAllowed(final CommandSender sender, final String permission, final String targetName) {
        // Always allowed for self
        if (sender.getName().equalsIgnoreCase(targetName)) return true;

        // Check if sender is allowed for all players
        if (sender.hasPermission(permission + ".player.*")) return true;

        // Check if sender is allowed for specific player
        if (sender.hasPermission(permission + ".player." + targetName)) return true;

        return false;
    }

    // TODO does Bukkit now have a method that will work for this?
    /**
     * Returns player only if it is a full and case insensitive name match.
     *
     * @param name name of player
     * @return player that matches name
     */
    static Player getExactPlayer(final String name) {
        final Player player = Bukkit.getServer().getPlayer(name);
        if (player == null) return null;

        if (!player.getName().equalsIgnoreCase(name)) return null;

        return player;
    }

    static Recipient getRecipient(final OfflinePlayer target) {
        if (target.getPlayer() != null)
            return MessageManager.getDispatcher().getRecipient(target.getPlayer());

        return new Recipient(target);
    }

}
