package edgruberman.bukkit.messagemanager.channels;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


public final class WorldChannel extends Channel {

    static final Map<Plugin, ChannelConfiguration> configuration = new HashMap<Plugin, ChannelConfiguration>();

    private final World world;

    WorldChannel(final World world) {
        super(Channel.Type.WORLD, world.getName());
        this.world = world;
        this.reset();
    }

    public World getWorld() {
        return this.world;
    }

    public void reset() {
        super.clear();
        for (final Player player : this.world.getPlayers())
            super.add(player);
    }

    @Override
    public ChannelConfiguration getConfiguration(final Plugin owner) {
        return WorldChannel.configuration.get(owner);
    }

    @Override
    public ChannelConfiguration setConfiguration(final Plugin owner, final ChannelConfiguration configuration) {
        return WorldChannel.configuration.put(owner, configuration);
    }

}
