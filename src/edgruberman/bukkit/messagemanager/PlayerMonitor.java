package edgruberman.bukkit.messagemanager;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.channels.Channel;
import edgruberman.bukkit.messagemanager.channels.PlayerChannel;
import edgruberman.bukkit.messagemanager.channels.ServerChannel;
import edgruberman.bukkit.messagemanager.channels.WorldChannel;

/**
 * Monitor player joins, quits, and world changes to ensure Player, Server,
 * and World channels are properly configured and ready to send messages.
 */
final class PlayerMonitor extends PlayerListener {
    
    Map<Player, Location> last = new HashMap<Player, Location>();
    
    PlayerMonitor(final Plugin plugin) {
        // Create player channels for all existing players
        for (Player player : plugin.getServer().getOnlinePlayers())
            PlayerChannel.getInstance(player);
        
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Monitor, plugin);
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TELEPORT, this, Event.Priority.Monitor, plugin);
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.Monitor, plugin);
    }
    
    @Override
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.last.put(event.getPlayer(), event.getPlayer().getLocation());
        PlayerChannel.getInstance(event.getPlayer());
        ServerChannel.getInstance(event.getPlayer().getServer()).addMember(event.getPlayer());
        WorldChannel.getInstance(event.getPlayer().getWorld()).addMember(event.getPlayer());
    }
    
    @Override
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        
        Location last = this.last.get(event.getPlayer());
        Main.messageManager.log("Player " + event.getPlayer().getName() + " last recorded at [" + (last != null ? last.getWorld().getName() : "") + "] teleporting to [" + event.getTo().getWorld().getName() + "]", MessageLevel.FINEST);
        if (last == null) {
            this.last.put(event.getPlayer(), event.getTo());
            return;
        }
        
        if (last.getWorld().equals(event.getTo().getWorld())) return;
        
        this.last.put(event.getPlayer(), event.getTo());
        WorldChannel.getInstance(last.getWorld()).removeMember(event.getPlayer());
        WorldChannel.getInstance(event.getTo().getWorld()).addMember(event.getPlayer());
    }
    
    @Override
    public void onPlayerQuit(final PlayerQuitEvent event) {
        Channel.disconnect(event.getPlayer());
        PlayerChannel.disposeInstance(event.getPlayer());
        this.last.remove(event.getPlayer());
    }
}