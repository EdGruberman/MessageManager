package edgruberman.bukkit.messagemanager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.channels.Channel;
import edgruberman.bukkit.messagemanager.channels.ChannelConfiguration;
import edgruberman.bukkit.messagemanager.channels.Dispatcher;

/**
 * Centralized logging and player communication API.</br>
 * </br>
 * <b>Known Bug:</b> Logging output for CONFIG level to file in Minecraft
 * does not include level prefix tag despite it displaying in the console.
 */
public final class MessageManager {

    public static final MessageLevel DEFAULT_SEND_LEVEL = MessageLevel.INFO;
    public static final boolean DEFAULT_APPLY_TIMESTAMP = true;
    public static final String DEFAULT_LOG_FORMAT = "[%3$s] [%4$s] %2$s %1$s"; // 1 = Message, 2 = Channel Description, 3 = Originating Plugin, 4 = Message Level

    static Dispatcher dispatcher = null;
    static Map<Plugin, MessageManager> instances = new HashMap<Plugin, MessageManager>();

    public static MessageManager of(final Plugin plugin) {
        MessageManager manager = MessageManager.instances.get(plugin);
        if (manager == null) manager = new MessageManager(plugin);
        return manager;
    }

    public static Dispatcher getDispatcher() {
        return MessageManager.dispatcher;
    }

    /**
     * Default message level to send a message as.
     */
    public final MessageLevel levelDefault = MessageManager.DEFAULT_SEND_LEVEL;

    /**
     * Default timestamp applicability to send a message with.
     */
    public boolean applyTimestampDefault = MessageManager.DEFAULT_APPLY_TIMESTAMP;

    /**
     * Default log format to record a sent message as.
     */
    public String logFormat = MessageManager.DEFAULT_LOG_FORMAT;

    public final Plugin owner;

    /**
     * Create instance for managing plugin messages.
     *
     * @param owner plugin to manage messages for
     */
    public MessageManager(final Plugin owner) {
        this.owner = owner;
        this.enable();
    }

    public void enable() {
        final ConfigurationFile local = new ConfigurationFile(this.owner, "MessageManager.yml");
        local.setMinVersion(Main.MINIMUM_MESSAGE_MANAGER_VERSION);
        local.load();
        ChannelConfiguration.load(this.owner, local);

        if (Main.messageManager != null) this.logFormat = Main.messageManager.logFormat;
        if (local.getConfig().isSet("log")) {
            final String log = local.getConfig().getString("log");
            if (log == null) {
                Main.logger.log(Level.WARNING, "Unable to apply null log format from " + local.getFile().getPath() + "; log");
            } else {
                Main.logger.log(Level.FINE, "Override specified in " + local.getFile().getPath() + "; " + "log: " + log.replaceAll("&", "&&"));
                this.logFormat = log;
            }
        }

        MessageManager.instances.put(this.owner, this);
    }

    public void disable() {
        MessageManager.dispatcher.removeChannelConfiguration(this.owner);
        MessageManager.instances.remove(this);
    }

    /**
     * Determines if messages of the specified level or higher will be sent.
     *
     * @param level level to determine if it will be sent or not
     * @return true if current level or higher; false otherwise
     */
    public boolean isSendable(final Channel.Type type, final MessageLevel level) {
        return level.intValue() >= MessageManager.dispatcher.getChannelConfiguration(type, this.owner).levelChannel.intValue();
    }

    public boolean isLoggable(final Channel.Type type, final MessageLevel level) {
        return level.intValue() >= MessageManager.dispatcher.getChannelConfiguration(type, this.owner).levelLog.intValue();
    }

    // Channel -----------------------------------------------------------------

    public boolean send(final Channel channel, final String message, MessageLevel level, Boolean applyTimestamp) {
        if (!this.isSendable(channel.type, level)) return false;

        if (level == null) level = this.levelDefault;
        if (applyTimestamp == null) applyTimestamp = this.applyTimestampDefault;
        final String formatted = channel.send(this.owner, message, level, applyTimestamp);
        if (formatted == null) return false;

        if (this.isLoggable(channel.type, level)) {
            final String logMessage = String.format(this.logFormat, formatted, channel.toString(), this.owner.getDescription().getName(), level.getName());
            Main.logger.log(Level.INFO, logMessage);
        }

        return true;
    }


    // Player / Console -------------------------------------------------------

    /**
     * Send a private message that only a single player can see. (Use
     * configuration file defined default level and timestamp.)
     *
     * @param target who to send message to
     * @param message text to send
     */
    public void send(final CommandSender target, final String message) {
        this.send(target, message, (MessageLevel) null);
    }

    /**
     * Send a private message that only a single player can see. (Use
     * configuration file defined default timestamp.)
     *
     * @param target who to send message to
     * @param message text to send
     * @param level message category
     */
    public void send(final CommandSender target, final String message, final MessageLevel level) {
        this.send(target, message, level, (Boolean) null);
    }

    /**
     * Send a private message that only a single player can see.
     *
     * @param target who to send message to
     * @param message text to send
     * @param level message category
     * @param applyTimestamp true to include timestamp; false otherwise
     */
    public void send(final CommandSender target, final String message, final MessageLevel level, final Boolean applyTimestamp) {
        this.send(MessageManager.getDispatcher().getChannel(Channel.Type.PLAYER, target.getName()), message, level, applyTimestamp);
    }

    /**
     * Send a private message that only a single player can see. (Use
     * configuration file defined default level and include timestamp.)
     *
     * @param target who to send message to
     * @param message text to send
     */
    public void tell(final CommandSender target, final String message) {
        this.send(target, message);
    }

    /**
     * Send a private message that only a single player can see. (Include
     * timestamp.)
     *
     * @param target who to send message to
     * @param message text to send
     * @param level message category
     */
    public void tell(final CommandSender target, final String message, final MessageLevel level) {
        this.send(target, message, level);
    }

    /**
     * Send a private message that only a single player can see.
     *
     * @param target who to send message to
     * @param message text to send
     * @param level message category
     * @param applyTimestamp true to include timestamp; false otherwise
     */
    public void tell(final CommandSender target, final String message, final MessageLevel level, final Boolean applyTimestamp) {
        this.send(target, message, level, applyTimestamp);
    }

    // Server -----------------------------------------------------------------

    /**
     * Broadcast a message that all players can see. (Use configuration file
     * defined default level and include timestamp.)
     *
     * @param message text to send
     */
    public void send(final String message) {
        this.send(message, (MessageLevel) null);
    }

    /**
     * Broadcast a message that all players can see. (Include timestamp.)
     *
     * @param message text to send
     * @param level message category
     */
    public void send(final String message, final MessageLevel level) {
        this.send(message, level, (Boolean) null);
    }

    /**
     * Broadcast a message that all players can see.
     *
     * @param message text to send
     * @param level message category
     * @param applyTimestamp true to include timestamp; false otherwise
     */
    public void send(final String message, final MessageLevel level, final Boolean applyTimestamp) {
        this.send(MessageManager.getDispatcher().getChannel(Channel.Type.SERVER, this.owner.getServer().getName()), message, level, applyTimestamp);
    }

    /**
     * Broadcast a message that all players can see. (Use configuration file
     * defined default level and include timestamp.)
     *
     * @param message text to send
     */
    public void broadcast(final String message) {
        this.send(message);
    }

    /**
     * Broadcast a message that all players can see. (Include timestamp.)
     *
     * @param message text to send
     * @param level message category
     */
    public void broadcast(final String message, final MessageLevel level) {
        this.send(message, level);
    }

    /**
     * Broadcast a message that all players can see.
     *
     * @param message text to send
     * @param level message category
     * @param applyTimestamp true to include timestamp; false otherwise
     */
    public void broadcast(final String message, final MessageLevel level, final Boolean applyTimestamp) {
        this.send(message, level, applyTimestamp);
    }

    // World ------------------------------------------------------------------

    /**
     * Broadcast a message that players in the specified world can see. (Use
     * configuration file defined default level and include timestamp.)
     *
     * @param world where to send message to
     * @param message text to send
     */
    public void send(final World target, final String message) {
        this.send(target, message, (MessageLevel) null);
    }

    /**
     * Broadcast a message that players in the specified world can see.
     * (Include timestamp.)
     *
     * @param world where to send message to
     * @param message text to send
     * @param level message category
     */
    public void send(final World target, final String message, final MessageLevel level) {
        this.send(target, message, level, (Boolean) null);
    }

    /**
     * Broadcast a message that players in the specified world can see.
     *
     * @param world where to send message to
     * @param message text to send
     * @param level message category
     * @param useTimestamp true to include timestamp; false otherwise
     */
    public void send(final World target, final String message, final MessageLevel level, final Boolean useTimestamp) {
        this.send(MessageManager.getDispatcher().getChannel(Channel.Type.WORLD, target.getName()), message, level, useTimestamp);
    }

    // Custom -----------------------------------------------------------------

    public void send(final String name, final String message) {
        this.send(name, message, (MessageLevel) null);
    }

    public void send(final String name, final String message, final MessageLevel level) {
        this.send(name, message, level, (Boolean) null);
    }

    public void send(final String name, final String message, final MessageLevel level, final Boolean applyTimestamp) {
        this.send(MessageManager.getDispatcher().getChannel(Channel.Type.CUSTOM, name), message, level, applyTimestamp);
    }

}
