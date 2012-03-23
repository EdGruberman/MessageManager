package edgruberman.bukkit.messagemanager.channels;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * CommandSender (Players and consoles) Collection
 */
public final class PlayerChannel extends Channel {

    static final Map<Plugin, ChannelConfiguration> configuration = new HashMap<Plugin, ChannelConfiguration>();

    private final CommandSender target;

    PlayerChannel(final CommandSender target) {
        super(Channel.Type.PLAYER, target.getName());
        this.target = target;
        this.reset();
    }

    public CommandSender getTarget() {
        return this.target;
    }

    public void reset() {
        super.clear();
        super.add(this.target);
    }

    @Override
    public ChannelConfiguration getConfiguration(final Plugin owner) {
        return PlayerChannel.configuration.get(owner);
    }

    @Override
    public ChannelConfiguration setConfiguration(final Plugin owner, final ChannelConfiguration configuration) {
        return PlayerChannel.configuration.put(owner, configuration);
    }

}
