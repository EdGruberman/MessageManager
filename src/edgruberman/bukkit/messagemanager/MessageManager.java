package edgruberman.bukkit.messagemanager;

import java.util.HashMap;
import java.util.Map;

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
import edgruberman.bukkit.messagemanager.channels.ServerChannel;
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
    public Settings settings;
    private LogChannel log;
    
    /**
     * Associates this manager with the owning plugin.
     * 
     * @param owner plugin that owns this manager
     */
    public MessageManager(final Plugin owner) {
        if (MessageManager.getInstance(owner) != null)
            throw new IllegalArgumentException("Instance already exists for " + owner.getDescription().getName());
        
        this.owner = owner;
        this.settings = new Settings(this.owner);
        this.log = Channel.getInstance(this.owner);
        this.log.setFormat(this.settings.log.get(Channel.Type.LOG));
        this.log.setLevel(this.settings.level.get(Channel.Type.LOG));
        
        MessageManager.instances.put(owner, this);
    }
    
    public static MessageManager getInstance(final Plugin owner) {
        return MessageManager.instances.get(owner);
    }
    
    public void send(final Channel.Type type, final String name, final String message, final MessageLevel level, final Boolean isTimestamped, final Throwable e) {
        if (!Channel.exists(type, name))
            throw new IllegalArgumentException(type.toString() + " channel" + (name != null ? " [" + name + "]" : "") + " does not exist.");
        
        Channel channel = Channel.getInstance(type, name);
        
        MessageLevel lvl = level;
        if (lvl == null) lvl = MessageLevel.INFO;
        if (!this.isLevel(type, lvl)) return;
        
        String formatted = this.colorize(message, type);
        
        for (String line : formatted.split("\n")) {
            line = this.format(channel, lvl, line);
            
            if (channel.type != Channel.Type.LOG)
                this.log(this.formatLog(channel, line), lvl);
            
            switch(channel.type) {
            case PLAYER: ((PlayerChannel) channel).send(line, isTimestamped); break;
            case SERVER: ((ServerChannel) channel).send(line, isTimestamped); break;
            case WORLD:  ((WorldChannel)  channel).send(line, isTimestamped); break;
            case CUSTOM: ((CustomChannel) channel).send(line, isTimestamped); break;
            case LOG:    ((LogChannel)    channel).send(line, isTimestamped, lvl, e); break;
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
    
    /**
     * Send a private message that only a single player can see. (Use
     * configuration file defined default level and include timestamp.)
     * 
     * @param player Target player to send message to.
     * @param message Text to display on player's client interface.
     */
    public void send(final Player player, final String message) {
        this.send(player, message, null);
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
        this.send(player, message, level, null);
    }
 
    /**
     * Send a private message that only a single player can see.
     * 
     * @param player Player to target message to.
     * @param level Importance level of message.
     * @param message Text to display on player's client interface.
     * @param isTimestamped Include timestamp in message.
     */
    public void send(final Player player, final String message, final MessageLevel level, final Boolean isTimestamped) {
        this.send(Channel.Type.PLAYER, player.getName(), message, level, isTimestamped, null);
    }
    
    /**
     * Broadcast a message that all players can see. (Use configuration file
     * defined default level and include timestamp.)
     * 
     * @param message text to display players' client interface
     */
    public void broadcast(final String message) {
        this.broadcast(message, null);
    }
    
    public void send(final String message) {
        this.send((Server) null, message);
    }
    
    public void send(final Server server, final String message) {
        this.send(server, message, null);
    }
    
    /**
     * Broadcast a message that all players can see. (Include timestamp.)
     * 
     * @param message text to display on player's client interface
     * @param level importance level of message
     */
    public void broadcast(final String message, final MessageLevel level) {
        this.broadcast(message, level, null);
    }
    
    public void send(final Server server, final String message, final MessageLevel level) {
        this.send(server, message, level, null);
    }
    
    /**
     * Broadcast a message that all players can see.
     * 
     * @param message text to display on player's client interface
     * @param level importance level of message
     * @param isTimestamped include timestamp in message
     */
    public void broadcast(final String message, final MessageLevel level, final Boolean isTimestamped) {
        this.send((Server) null, message, level, isTimestamped);
    }
    
    public void send(final Server server, final String message, final MessageLevel level, final Boolean isTimestamped) {
        this.send(Channel.Type.SERVER, (server != null ? server.getName() : this.owner.getServer().getName()), message, level, isTimestamped, null);
    }
    
    public void send(final World world, final String message) {
        this.send(world, message, null);
    }

    public void send(final World world, final String message, final MessageLevel level) {
        this.send(world, message, level, null);
    }
    
    public void send(final World world, final String message, final MessageLevel level, final Boolean isTimestamped) {
        this.send(Channel.Type.WORLD, world.getName(), message, level, isTimestamped, null);
    }
    
    public void send(final String name, final String message) {
        this.send(name, message, null);
    }

    public void send(final String name, final String message, final MessageLevel level) {
        this.send(name, message, level, null);
    }
    
    public void send(final String name, final String message, final MessageLevel level, final Boolean isTimestamped) {
        this.send(Channel.Type.CUSTOM, name, message, level, isTimestamped, null);
    }
    
    /**
     * Create an entry in the server log file. (Use default level.)
     * 
     * @param message text to display in log entry
     */
    public void log(final String message) {
        this.log(message, null);
    }

    /**
     * Create an entry in the server log file.
     * 
     * @param message text to display in log entry
     * @param level logging level of log entry
     */
    public void log(final String message, final MessageLevel level) {
        this.log(message, level, null);
    }
    
    /**
     * Create an entry in the server log file. (Include error information.)
     * 
     * @param level logging level of log entry
     * @param message text to display in log entry
     * @param e related error message to output along with log entry
     */
    public void log(final String message, final MessageLevel level, final Throwable e) {
        this.send(Channel.Type.LOG, this.owner.getDescription().getName(), message, level, null, e);
    }
    
    public void send(final Channel.Type type, final String name, final String message) {
        this.send(type, name, message, null);
    }
    
    public void send(final Channel.Type type, final String name, final String message, final MessageLevel level) {
        this.send(type, name, message, level, null, null);
    }
    
    /**
     * Determine where to send message to based on sender class type.
     * If sender is a player, it will send to player, otherwise it assumes
     * it is a console command sends the response to the log.
     * (Include timestamp.)
     * 
     * @param sender Original command sender.
     * @param level Importance level of message.
     * @param message Text to respond to sender with.
     */
    public void respond(final CommandSender sender, final String message, final MessageLevel level) {
        this.respond(sender, message, level, null);
    }
    
    /**
     * Determine where to send message to based on sender class type.
     * If sender is a player, it will send to player, otherwise it assumes
     * it is a console command sends the response to the log.
     * 
     * @param sender Original command sender.
     * @param level Importance level of message.
     * @param message Text to respond to sender with.
     * @param isTimestamped Include timestamp in message if sender is a player.
     */
    public void respond(final CommandSender sender, final String message, final MessageLevel level, final Boolean isTimestamped) {
        if (sender instanceof Player) {
            this.send((Player) sender, message, level, isTimestamped);
        } else {
            this.log(message, level);
        }
    }
    
    //TODO: parse start and end color tags
    private String colorize(final String message, final Channel.Type type) {
        String colorized = message;
        
        ChatColor color = null;
        if (colorized.matches("^%[^%]+%.+")) {
            String[] split = colorized.split("%", 3);
            
            MessageLevel level = MessageLevel.parse(split[1]);
            if (level != null) {
                color = this.settings.color.get(level).get(type);
            } else if (split[1] != null) {
                color = ChatColor.valueOf(split[1]);
            }
            
            if (color != null)
                colorized = color + split[2];
        }
        
        return colorized;
    }
}