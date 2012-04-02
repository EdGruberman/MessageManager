package edgruberman.bukkit.messagemanager.channels;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.Main;

/**
 * Channel and Recipient Manager
 */
public final class Dispatcher implements Listener {

    private final Plugin plugin;
    private final Map<Channel.Type, Map<String, Channel>> channels = new HashMap<Channel.Type, Map<String, Channel>>();
    private final Map<CommandSender, Recipient> recipients = new HashMap<CommandSender, Recipient>();

    public Dispatcher(final Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void init() {
        // Ensure recipients and channels are configured for existing players
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            this.createRecipient(player);
            this.register(new PlayerChannel(player));
        }

        // Ensure server channel exists
        this.register(new ServerChannel(this.plugin.getServer()));

        // Ensure channels are configured for existing worlds
        for (final World world : this.plugin.getServer().getWorlds())
            this.register(new WorldChannel(world));
    }

    public boolean register(final Channel channel) {
        if (this.exists(channel.type, channel.name)) return false;

        if (!this.channels.containsKey(channel.type))
            this.channels.put(channel.type, new HashMap<String, Channel>());

        this.channels.get(channel.type).put(channel.name, channel);
        Main.logger.log(Level.FINER, "Registered channel: " + channel.toString());
        return true;
    }

    public boolean unregister(final Channel channel) {
        if (!this.channels.containsKey(channel.type))
            throw new IllegalArgumentException("Channel not registered: " + channel.toString());

        if (this.channels.get(channel.type).remove(channel.name) == null)
            return false;

        if (this.channels.get(channel.type).size() == 0)
            this.channels.remove(channel.type);

        Main.logger.log(Level.FINER, "Unregistered channel: " + channel.toString());
        return true;
    }

    public boolean exists(final Channel.Type type, final String name) {
        if (!this.channels.containsKey(type)) return false;

        return this.channels.get(type).containsKey(name);
    }

    public Set<Channel> getChannels() {
        final Set<Channel> channels = new HashSet<Channel>();
        for (final Map<String, Channel> map : this.channels.values())
            channels.addAll(map.values());

        return channels;
    }

    public ChannelConfiguration getChannelConfigurationDefaults(final Channel.Type type) {
        ChannelConfiguration defaults = this.getChannelConfiguration(type, this.plugin);
        if (defaults == null) defaults = new ChannelConfiguration();
        return defaults;
    }

    public ChannelConfiguration getChannelConfiguration(final Channel.Type type, final Plugin owner) {
        return Channel.getChannelConfiguration(type, owner);
    }

    // TODO iterate enum
    public void removeChannelConfiguration(final Plugin owner) {
        ServerChannel.configuration.remove(owner);
        PlayerChannel.configuration.remove(owner);
        WorldChannel.configuration.remove(owner);
        CustomChannel.configuration.remove(owner);
    }

    public Channel getChannel(final Channel.Type type, final String name) {
        if (!this.channels.containsKey(type)) return null;

        return this.channels.get(type).get(name);
    }

    public CustomChannel getChannel(final String custom) {
        return (CustomChannel) this.getChannel(Channel.Type.CUSTOM, custom);
    }

    public PlayerChannel getChannel(final Player player) {
        return (PlayerChannel) this.getChannel(Channel.Type.PLAYER, player.getName());
    }

    public ServerChannel getChannel(final Server server) {
        return (ServerChannel) this.getChannel(Channel.Type.SERVER, server.getName());
    }

    public WorldChannel getChannel(final World world) {
        return (WorldChannel) this.getChannel(Channel.Type.WORLD, world.getName());
    }

    public Set<Recipient> getRecipients() {
        return new HashSet<Recipient>(this.recipients.values());
    }

    public Recipient getRecipient(final CommandSender target) {
        return this.recipients.get(target);
    }

    public Recipient getRecipient(final String name) {
        final OfflinePlayer target = this.plugin.getServer().getOfflinePlayer(name);

        if (target.getPlayer() != null)
            return this.getRecipient(target.getPlayer());

        return new Recipient(target);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldLoad(final WorldLoadEvent event) {
        this.register(new WorldChannel(event.getWorld()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(final WorldUnloadEvent event) {
        this.unregister(this.getChannel(event.getWorld()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.createRecipient(event.getPlayer());
        this.register(new PlayerChannel(event.getPlayer()));
        this.getChannel(event.getPlayer().getServer()).add(event.getPlayer());
        this.getChannel(event.getPlayer().getWorld()).add(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        this.getChannel(event.getFrom()).remove(event.getPlayer());
        this.getChannel(event.getPlayer().getWorld()).add(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.getRecipient(event.getPlayer()).removeMemberships();
        this.unregister(this.getChannel(event.getPlayer()));
        this.recipients.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerCommand(final ServerCommandEvent event) {
        if (this.getRecipient(event.getSender()) == null) {
            this.createRecipient(event.getSender());
            this.register(new PlayerChannel(event.getSender()));
        }
        // TODO determine when to unregister a console based channel and remove the recipient
    }

    private Recipient createRecipient(final CommandSender target) {
        final Recipient existing = this.recipients.get(target);
        if (existing != null) return existing;

        final Recipient recipient = new Recipient(target);
        this.recipients.put(recipient.getTarget(), recipient);
        return recipient;
    }

}