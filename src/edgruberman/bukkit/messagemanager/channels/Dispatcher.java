package edgruberman.bukkit.messagemanager.channels;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;

/**
 * Ensure Player, Server, and World channels have members properly updated.
 */
public final class Dispatcher implements Listener {

    public Dispatcher(final Plugin plugin) {
        // Ensure server channel exists
        ServerChannel.getInstance(plugin.getServer());

        // Ensure channels are configured for existing players
        for (Player player : plugin.getServer().getOnlinePlayers())
            PlayerChannel.getInstance(player);

        // Ensure channels are configured for existing worlds
        for (World world : plugin.getServer().getWorlds())
            WorldChannel.getInstance(world).resetMembers();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        PlayerChannel.getInstance(event.getPlayer());
        ServerChannel.getInstance(event.getPlayer().getServer()).addMember(event.getPlayer());
        WorldChannel.getInstance(event.getPlayer().getWorld()).addMember(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        WorldChannel.getInstance(event.getFrom()).removeMember(event.getPlayer());
        WorldChannel.getInstance(event.getPlayer().getWorld()).addMember(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        Channel.disconnect(event.getPlayer());
        PlayerChannel.disposeInstance(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldLoad(final WorldLoadEvent event) {
        WorldChannel.getInstance(event.getWorld()).resetMembers();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(final WorldUnloadEvent event) {
        if (event.isCancelled()) return;

        WorldChannel.disposeInstance(event.getWorld());
    }

}