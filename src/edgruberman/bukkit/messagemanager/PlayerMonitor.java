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
        for (Player player : plugin.getServer().getOnlinePlayers())
            this.reset(player);
        
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Monitor, plugin);
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TELEPORT, this, Event.Priority.Monitor, plugin);
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.Monitor, plugin);
    }
    
    /**
     * Reset PlayerChannel to ensure it is ready for player only messages.
     * 
     * @param player player to reset PlayerChannel for
     */
    private void reset(final Player player) {
        this.last.put(player, player.getLocation());
        PlayerChannel channel = PlayerChannel.getInstance(player);
        channel.setPlayer(player);
        channel.resetMembers();
    }
    
    @Override
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.reset(event.getPlayer());
        ServerChannel.getInstance(event.getPlayer().getServer()).addMember(event.getPlayer());
        WorldChannel.getInstance(event.getPlayer().getWorld()).addMember(event.getPlayer());
    }
    
    @Override
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        
        Location last = this.last.get(event.getPlayer());
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