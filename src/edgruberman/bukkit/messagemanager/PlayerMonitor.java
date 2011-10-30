package edgruberman.bukkit.messagemanager;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.channels.Channel;
import edgruberman.bukkit.messagemanager.channels.PlayerChannel;
import edgruberman.bukkit.messagemanager.channels.ServerChannel;
import edgruberman.bukkit.messagemanager.channels.WorldChannel;

/**
 * Ensure Player, Server, and World channels have members properly updated.
 */
final class PlayerMonitor extends PlayerListener {
    
    PlayerMonitor(final Plugin plugin) {
        for (Player player : plugin.getServer().getOnlinePlayers())
            this.addPlayer(player);
        
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Monitor, plugin);
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHANGED_WORLD, this, Event.Priority.Monitor, plugin);
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.Monitor, plugin);
    }
    
    @Override
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.addPlayer(event.getPlayer());
    }
    
    @Override
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        WorldChannel.getInstance(event.getFrom()).removeMember(event.getPlayer());
        WorldChannel.getInstance(event.getPlayer().getWorld()).addMember(event.getPlayer());
    }
    
    @Override
    public void onPlayerQuit(final PlayerQuitEvent event) {
        Channel.disconnect(event.getPlayer());
        PlayerChannel.disposeInstance(event.getPlayer());
    }
    
    private void addPlayer(final Player player) {
        PlayerChannel.getInstance(player);
        ServerChannel.getInstance(player.getServer()).addMember(player);
        WorldChannel.getInstance(player.getWorld()).addMember(player);
    }
}