package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.commands.util.Handler;

public final class Timestamp extends Handler {

    public Timestamp(final JavaPlugin plugin) {
        super(plugin, "timestamp");
        new TimestampGet(this);
        new TimestampReset(this);
        new TimestampOn(this);
        new TimestampOff(this);
        new TimestampPattern(this);
        new TimestampFormat(this);
        new TimestampTimeZone(this);
    }

    /**
     * Determines if sender has permission for player.
     *
     * @param sender
     * @param permission
     * @param playerName
     * @return
     */
    static boolean isAllowed(final CommandSender sender, final String permission, final String playerName) {
        // Always allowed for self
        if ((sender instanceof Player) && ((Player) sender).getName().equalsIgnoreCase(playerName)) return true;

        // Check if sender is allowed for all players
        if (sender.hasPermission(permission + ".player.*")) return true;

        // Check if sender is allowed for specific player
        if (sender.hasPermission(permission + ".player." + playerName)) return true;

        return false;
    }

    /**
     * Returns player only if it is a full and case insensitive name match.
     *
     * @param name name of player
     * @return player that matches name
     */
    static Player getExactPlayer(String name) {
        Player player = Bukkit.getServer().getPlayer(name);
        if (player == null) return null;

        if (!player.getName().equalsIgnoreCase(name)) return null;

        return player;
    }

}