package edgruberman.bukkit.messagemanager.channels;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


public final class ServerChannel extends Channel {

    static final Map<Plugin, ChannelConfiguration> configuration = new HashMap<Plugin, ChannelConfiguration>();

    private final Server server;

    ServerChannel(final Server server) {
        super(Channel.Type.SERVER, server.getName());
        this.server = server;
        this.reset();
    }

    public void reset() {
        super.clear();
        for (final Player player : this.server.getOnlinePlayers())
            super.add(player);
    }

    @Override
    public ChannelConfiguration getConfiguration(final Plugin owner) {
        return ServerChannel.configuration.get(owner);
    }

    @Override
    public ChannelConfiguration setConfiguration(final Plugin owner, final ChannelConfiguration configuration) {
        return ServerChannel.configuration.put(owner, configuration);
    }

}
