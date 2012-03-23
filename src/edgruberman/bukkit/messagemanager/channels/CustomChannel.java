package edgruberman.bukkit.messagemanager.channels;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.Plugin;


public class CustomChannel extends Channel {

    static final Map<Plugin, ChannelConfiguration> configuration = new HashMap<Plugin, ChannelConfiguration>();

    public CustomChannel(final String name) {
        super(Channel.Type.CUSTOM, name);
    }


    @Override
    public ChannelConfiguration getConfiguration(final Plugin owner) {
        return CustomChannel.configuration.get(owner);
    }

    @Override
    public ChannelConfiguration setConfiguration(final Plugin owner, final ChannelConfiguration configuration) {
        return CustomChannel.configuration.put(owner, configuration);
    }

}
