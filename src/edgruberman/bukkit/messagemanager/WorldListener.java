package edgruberman.bukkit.messagemanager;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.channels.WorldChannel;

final class WorldListener extends org.bukkit.event.world.WorldListener {
    
    WorldListener(final Plugin plugin) {
        for (World world : plugin.getServer().getWorlds()) {
            WorldChannel channel = WorldChannel.getInstance(world);
            channel.resetMembers();
        }
        
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.WORLD_LOAD, this, Event.Priority.Monitor, plugin);
        pluginManager.registerEvent(Event.Type.WORLD_UNLOAD, this, Event.Priority.Monitor, plugin);
    }
    
    @Override
    public void onWorldLoad(final WorldLoadEvent event) {
        WorldChannel.getInstance(event.getWorld()).resetMembers();
    }
    
    @Override
    public void onWorldUnload(final WorldUnloadEvent event) {
        WorldChannel.disposeInstance(event.getWorld());
    }
}
