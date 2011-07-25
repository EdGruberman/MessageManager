package edgruberman.bukkit.messagemanager;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.channels.Channel;
import edgruberman.bukkit.messagemanager.channels.PlayerChannel;
import edgruberman.bukkit.messagemanager.channels.Recipient;
import edgruberman.bukkit.messagemanager.channels.ServerChannel;
import edgruberman.bukkit.messagemanager.channels.WorldChannel;

final class PlayerListener extends org.bukkit.event.player.PlayerListener {
    
    public PlayerListener(final Plugin plugin) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            this.resetRecipient(player);
            this.resetPlayerChannel(player);
        }
        
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Monitor, plugin);
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TELEPORT, this, Event.Priority.Monitor, plugin);
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.Monitor, plugin);
    }
    
    private void resetRecipient(Player player) {
        Recipient recipient = Recipient.getInstance(player);
        recipient.setTimestamp(Main.messageManager.settings.timestamp);
    }
    
    private void resetPlayerChannel(Player player) {
        PlayerChannel channel = PlayerChannel.getInstance(player);
        channel.setPlayer(player);
        channel.resetMembers();
    }
    
    @Override
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.resetRecipient(event.getPlayer());
        this.resetPlayerChannel(event.getPlayer());
        ServerChannel.getInstance(event.getPlayer().getServer()).addMember(event.getPlayer());
        WorldChannel.getInstance(event.getPlayer().getWorld()).addMember(event.getPlayer());
    }
    
    @Override
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        
        if (event.getFrom().getWorld().equals(event.getTo().getWorld())) return;
        
        WorldChannel.getInstance(event.getFrom().getWorld()).removeMember(event.getPlayer());
        WorldChannel.getInstance(event.getTo().getWorld()).addMember(event.getPlayer());
    }
    
    @Override
    public void onPlayerQuit(final PlayerQuitEvent event) {
        Channel.disconnect(event.getPlayer());
        PlayerChannel.disposeInstance(event.getPlayer());
    }
}