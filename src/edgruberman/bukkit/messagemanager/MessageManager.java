package edgruberman.bukkit.messagemanager;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.channels.Channel;
import edgruberman.bukkit.messagemanager.channels.CustomChannel;
import edgruberman.bukkit.messagemanager.channels.LogChannel;
import edgruberman.bukkit.messagemanager.channels.PlayerChannel;
import edgruberman.bukkit.messagemanager.channels.Recipient;
import edgruberman.bukkit.messagemanager.channels.ServerChannel;
import edgruberman.bukkit.messagemanager.channels.Timestamp;
import edgruberman.bukkit.messagemanager.channels.WorldChannel;

/**
 * Centralized logging and player communication API.</br>
 * </br>
 * <b>Known Bug:</b> Logging output for CONFIG level to file in Minecraft
 * does not include level prefix tag despite it displaying in the console.
 */
public final class MessageManager {

    private static Map<Plugin, MessageManager> instances = new HashMap<Plugin, MessageManager>();

    private Plugin owner;
    private Settings settings;
    private LogChannel log;
    private MessageLevel levelDefault;

    /**
     * Default value for using timestamp on messages that do not explicitly indicate.
     */
    public boolean useTimestampDefault;

    /**
     * Create instance for managing plugin messages.
     * (Messages default to timestamp on and INFO level.)
     *
     * @param owner plugin to manage messages for
     */
    public MessageManager(final Plugin owner) {
        this(owner, Settings.DEFAULT_MESSAGE_USE_TIMESTAMP, Settings.DEFAULT_MESSAGE_LEVEL);
    }

    /**
     * Create instance for managing plugin messages.
     *
     * @param owner plugin to manage messages for
     * @param useTimestampDefault true to use timestamp for messages that do not explicitly indicate
     * @param levelDefault classification of messages to use when not explicitly indicated
     */
    public MessageManager(final Plugin owner, final boolean useTimestampDefault, final MessageLevel levelDefault) {
        if (MessageManager.getInstance(owner) != null)
            throw new IllegalArgumentException("Instance already exists for " + owner.getDescription().getName());

        this.owner = owner;
        this.settings = new Settings(this.owner);
        this.log = Channel.getInstance(this.owner);
        this.log.setFormat(this.settings.log.get(Channel.Type.LOG));
        this.log.setLevel(this.settings.level.get(Channel.Type.LOG));
        this.levelDefault = levelDefault;
        this.useTimestampDefault = useTimestampDefault;

        MessageManager.instances.put(owner, this);
    }

    /**
     * Return existing instance, if one exists, for plugin.
     *
     * @param owner owning plugin
     * @return existing MessageManager instance; null otherwise
     */
    public static MessageManager getInstance(final Plugin owner) {
        return MessageManager.instances.get(owner);
    }

    /**
     * Settings for various configuration options.
     *
     * @return settings for current instance
     */
    public Settings getSettings() {
        return this.settings;
    }

    public void send(final Channel.Type type, final String name, final String message) {
        this.send(type, name, message, this.levelDefault);
    }

    public void send(final Channel.Type type, final String name, final String message, final MessageLevel level) {
        this.send(type, name, message, level, this.useTimestampDefault);
    }

    public void send(final Channel.Type type, final String name, final String message, final MessageLevel level, final boolean useTimestamp) {
        this.send(type, name, message, level, useTimestamp, null);
    }

    public void send(final Channel.Type type, final String name, final String message, final MessageLevel level, final Throwable e) {
        this.send(type, name, message, level, this.useTimestampDefault, e);
    }

    public void send(final Channel.Type type, final String name, final String message, final MessageLevel level, final boolean useTimestamp, final Throwable e) {
        if (!Channel.exists(type, name))
            throw new IllegalArgumentException(type.toString() + " channel" + (name != null ? " [" + name + "]" : "") + " does not exist.");

        if (!this.isLevel(type, level)) return;

        Channel channel = Channel.getInstance(type, name);
        String formatted = MessageManager.colorize(message, this.settings.color.get(level).get(channel.type));
        for (String line : formatted.split("\n")) {
            line = this.format(channel, level, line);

            if (channel.type != Channel.Type.LOG)
                this.log(this.formatLog(channel, line), level);

            switch(channel.type) {
            case PLAYER: ((PlayerChannel) channel).send(line, useTimestamp); break;
            case SERVER: ((ServerChannel) channel).send(line, useTimestamp); break;
            case WORLD:  ((WorldChannel)  channel).send(line, useTimestamp); break;
            case CUSTOM: ((CustomChannel) channel).send(line, useTimestamp); break;
            case LOG:    ((LogChannel)    channel).send(line, useTimestamp, level, e); break;
            default: break;
            }
        }
    }

    public String format(final Channel channel, final MessageLevel level, final String message) {
        return this.settings.color.get(level).get(channel.type).toString()
            + String.format(this.settings.format.get(channel.type)
                , message
                , channel.name
        );
    }

    private String formatLog(final Channel channel, final String message) {
        return String.format(this.settings.log.get(channel.type)
                , message
                , channel.name
        );
    }

    /**
     * Messages will only be sent if equal or higher than the defined level.
     *
     * @param type type of channels this level will be applied to
     * @param level minimum level of messages to send
     */
    public void setLevel(final Channel.Type type, final MessageLevel level) {
        this.settings.level.put(type, level);

        if (type == Channel.Type.LOG)
            this.log.setLevel(this.settings.level.get(type));
    }

    /**
     * Determines if messages of the specified level or higher will be sent.
     *
     * @param level level to determine if it will be sent or not
     * @return true if current level or higher; false otherwise
     */
    public boolean isLevel(final Channel.Type type, final MessageLevel level) {
        return level.intValue() >= this.settings.level.get(type).intValue();
    }

    // Player -----------------------------------------------------------------

    /**
     * Send a private message that only a single player can see. (Use
     * configuration file defined default level and include timestamp.)
     *
     * @param player Target player to send message to.
     * @param message Text to display on player's client interface.
     */
    public void send(final Player player, final String message) {
        this.send(Channel.Type.PLAYER, player.getName(), message);
    }

    /**
     * Send a private message that only a single player can see. (Include
     * timestamp.)
     *
     * @param player Player to target message to.
     * @param level Importance level of message.
     * @param message Text to display on player's client interface.
     */
    public void send(final Player player, final String message, final MessageLevel level) {
        this.send(Channel.Type.PLAYER, player.getName(), message, level);
    }

    /**
     * Send a private message that only a single player can see.
     *
     * @param player Player to target message to.
     * @param level Importance level of message.
     * @param message Text to display on player's client interface.
     * @param useTimestamp Include timestamp in message.
     */
    public void send(final Player player, final String message, final MessageLevel level, final Boolean useTimestamp) {
        this.send(Channel.Type.PLAYER, player.getName(), message, level, useTimestamp);
    }

    // Server -----------------------------------------------------------------

    public void send(final String message) {
        this.send(this.owner.getServer(), message);
    }

    public void send(final String message, final MessageLevel level) {
        this.send(this.owner.getServer(), message, level);
    }

    public void send(final String message, final MessageLevel level, final Boolean useTimestamp) {
        this.send(this.owner.getServer(), message, level, useTimestamp);
    }

    public void send(final Server server, final String message) {
        this.send(Channel.Type.SERVER, server.getName(), message);
    }

    public void send(final Server server, final String message, final MessageLevel level) {
        this.send(Channel.Type.SERVER, server.getName(), message, level);
    }

    public void send(final Server server, final String message, final MessageLevel level, final Boolean useTimestamp) {
        this.send(Channel.Type.SERVER, server.getName(), message, level, useTimestamp);
    }

    /**
     * Broadcast a message that all players can see. (Use configuration file
     * defined default level and include timestamp.)
     *
     * @param message text to display players' client interface
     */
    public void broadcast(final String message) {
        this.send(this.owner.getServer(), message);
    }

    /**
     * Broadcast a message that all players can see. (Include timestamp.)
     *
     * @param message text to display on player's client interface
     * @param level importance level of message
     */
    public void broadcast(final String message, final MessageLevel level) {
        this.send(this.owner.getServer(), message, level);
    }

    /**
     * Broadcast a message that all players can see.
     *
     * @param message text to display on player's client interface
     * @param level importance level of message
     * @param useTimestamp include timestamp in message
     */
    public void broadcast(final String message, final MessageLevel level, final Boolean useTimestamp) {
        this.send(this.owner.getServer(), message, level, useTimestamp);
    }

    // World ------------------------------------------------------------------

    public void send(final World world, final String message) {
        this.send(Channel.Type.WORLD, world.getName(), message);
    }

    public void send(final World world, final String message, final MessageLevel level) {
        this.send(Channel.Type.WORLD, world.getName(), message, level);
    }

    public void send(final World world, final String message, final MessageLevel level, final Boolean useTimestamp) {
        this.send(Channel.Type.WORLD, world.getName(), message, level, useTimestamp);
    }

    // Custom -----------------------------------------------------------------

    public void send(final String name, final String message) {
        this.send(Channel.Type.CUSTOM, name, message);
    }

    public void send(final String name, final String message, final MessageLevel level) {
        this.send(Channel.Type.CUSTOM, name, message, level);
    }

    public void send(final String name, final String message, final MessageLevel level, final Boolean useTimestamp) {
        this.send(Channel.Type.CUSTOM, name, message, level, useTimestamp);
    }

    // Log --------------------------------------------------------------------

    /**
     * Create an entry in the server log file. (Use default level.)
     *
     * @param message text to display in log entry
     */
    public void log(final String message) {
        this.send(Channel.Type.LOG, this.owner.getDescription().getName(), message);
    }

    /**
     * Create an entry in the server log file.
     *
     * @param message text to display in log entry
     * @param level logging level of log entry
     */
    public void log(final String message, final MessageLevel level) {
        this.send(Channel.Type.LOG, this.owner.getDescription().getName(), message, level);
    }

    /**
     * Create an entry in the server log file. (Include error information.)
     *
     * @param level logging level of log entry
     * @param message text to display in log entry
     * @param e related error message to output along with log entry
     */
    public void log(final String message, final MessageLevel level, final Throwable e) {
        this.send(Channel.Type.LOG, this.owner.getDescription().getName(), message, level, e);
    }

    // Respond ----------------------------------------------------------------

    /**
     * Determine where to send message to based on sender class type.
     * If sender is a player, it will send to player, otherwise it assumes
     * it is a console command sends the response to the log.
     * (Timestamp defaulted to current MessageManager default.)
     * (MessageLevel defaulted to current MessageManager default.)
     *
     * @param target message destination
     * @param message text to send
     */
    public void respond(final CommandSender target, final String message) {
        this.respond(target, message, this.levelDefault);
    }

    /**
     * Determine where to send message to based on sender class type.
     * If sender is a player, it will send to player, otherwise it assumes
     * it is a console command sends the response to the log.
     * (Timestamp defaulted to current MessageManager default.)
     *
     * @param target message destination
     * @param message text to send
     * @param level message classification
     */
    public void respond(final CommandSender target, final String message, final MessageLevel level) {
        this.respond(target, message, level, this.useTimestampDefault);
    }

    /**
     * Determine where to send message to based on sender class type.
     * If sender is a player, it will send to player, otherwise it assumes
     * it is a console command sends the response to the log.
     *
     * @param target message destination
     * @param message text to send
     * @param level message classification
     * @param useTimestamp true to include timestamp in message if target is a player
     */
    public void respond(final CommandSender target, final String message, final MessageLevel level, final Boolean useTimestamp) {
        if (target instanceof Player) {
            this.send((Player) target, message, level, useTimestamp);
        } else {
            this.log(message, level);
        }
    }

    public ChatColor getColor(final MessageLevel level, final Channel.Type type) {
        return this.settings.color.get(level).get(type);
    }

    /**
     * Convert color codes to characters Minecraft will convert to color.
     * No base color defined. (Minecraft client will use white by default.)
     *
     * @param message text to convert any existing color codes for
     * @return text converted to Minecraft recognized coloring
     */
    public static String colorize(final String message) {
        return MessageManager.colorize(message, null);
    }

    /**
     * Convert color codes to characters Minecraft will convert to color.
     *
     * @param message text to convert an existing color codes for
     * @param base starting color to use for message
     * @return text converted to Minecraft recognized coloring
     */
    public static String colorize(final String message, final ChatColor base) {
        Stack<String> colors = new Stack<String>();
        colors.push((base != null ? base.toString() : ""));

        StringBuffer colorized = new StringBuffer();

        Pattern p = Pattern.compile("&(&|[0-9A-FKa-fk]|_)");
        Matcher m = p.matcher(message);
        while (m.find()) {
            // Replace escaped ampersand with single ampersand.
            if (m.group(1).equals("&")) {
                m.appendReplacement(colorized, "&");
                continue;
            }

            // Replace closure marker with previous color on stack.
            if (m.group(1).equals("_")) {
                if (colors.size() > 1) colors.pop();
                m.appendReplacement(colorized, colors.peek());
                continue;
            }

            // Replace hex code with color.
            ChatColor color = ChatColor.getByChar(m.group(1).toLowerCase());
            if (color == null) continue;

            colors.push(color.toString());
            m.appendReplacement(colorized, colors.peek());
        }
        m.appendTail(colorized);

        return colorized.toString();
    }

    /**
     * Strips the given message of all MessageManagercolor codes.
     *
     * @param input string to strip of color
     * @return copy of the input string, without any color codes
     */
    public static String stripColor(final String input) {
        if (input == null) return null;

        return input.replaceAll("(?i)&[0-9A-FK]", "");
    }

    public Recipient getRecipient(final Player player) {
        return Recipient.getInstance(player);
    }

    public Timestamp getTimestampFor(final String playerName) {
        return Main.timestampFor(playerName);
    }
}