package edgruberman.bukkit.messagemanager;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Centralized logging and player communication API.</br>
 * </br>
 * Instantiate this class in the plugin's main class as a public static
 * variable and ensure it is set to an instance in the onEnable method.</br>
 * </br>
 * <b>Known Bug:</b> Logging output for CONFIG level to file in Minecraft
 * does not include level prefix tag despite it displaying in the console.
 */
public final class MessageManager {
    
    /**
     * Default format to apply to messages broadcast to a world. (1 = Message, 2 = World Name)
     */
    public static final String DEFAULT_BROADCAST_WORLD_FORMAT = "[%2$s] %1$s";
        
    private Plugin owner = null;
    private Logger logger = null;
    
    private Level logLevel = Level.ALL;
    private Level sendLevel = Level.ALL;
    private Level broadcastLevel = Level.ALL;
    
    /**
     * Associates this manager with the owning plugin.
     * 
     * @param owner Plugin that owns this manager.
     */
    public MessageManager(final Plugin owner) {
        this.owner = owner;
        this.logger = Logger.getLogger(owner.getClass().getCanonicalName());
        
        org.bukkit.util.config.Configuration cfg = owner.getConfiguration();
        if (cfg == null) return;
        
        this.setBroadcastLevel(MessageLevel.parse(cfg.getString("level.broadcast", "ALL")));
        this.setSendLevel(MessageLevel.parse(cfg.getString("level.send", "ALL")));
        this.setLogLevel(MessageLevel.parse(cfg.getString("level.log", "ALL")));
    }

    /**
     * Configures logging to only display the specified level or higher.</br>
     * 
     * @param level Minimum logging level to show.
     */
    public void setLogLevel(final MessageLevel level) {
        this.logLevel = level;
        
        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it.
        for (Handler h : this.logger.getParent().getHandlers()) {
            if (h.getLevel().intValue() > this.logLevel.intValue()) h.setLevel(this.logLevel);
        }
        
        this.logger.setLevel(this.logLevel);
    }
    
    /**
     * Determines if current logging level will display log entries of the
     * specified level or higher.
     * 
     * @param level MessageLevel to determine if it will be displayed in the log or not.
     * @return true if current logging level will display this level; false otherwise.
     */
    public boolean isLogLevel(final MessageLevel level) {
        return level.intValue() >= this.logLevel.intValue();
    }
    
    /**
     * Create an entry in the server log file. (Use configuration file
     * defined default level.)
     * 
     * @param message Text to display in log entry.
     */
    public void log(final String message) {
        this.log(Main.getMessageLevel("log"), message);
    }
    
    /**
     * Create an entry in the server log file.
     * 
     * @param level Logging level of log entry.
     * @param message Text to display in log entry.
     */
    public void log(final MessageLevel level, final String message) {
        this.log(level, message, null);
    }
    
    /**
     * Create an entry in the server log file. (Include error information.)
     * 
     * @param level Logging level of log entry.
     * @param message Text to display in log entry.
     * @param e Related error message to output along with log entry.
     */
    public void log(final MessageLevel level, final String message, final Throwable e) {
        String formatted = message;
        if (e != null) formatted = formatted.replaceAll("\n", "   ");
        for (String messageLine : formatted.split("\n")) {
            messageLine = this.formatLog(level, messageLine, this.owner);
            this.logger.log(level, messageLine, e);
        }
    }
    
    /**
     * Messages to players will only be displayed if equal to or higher than the defined level.
     * Useful for removing player messages if feedback is not needed.
     * 
     * @param level Minimum level of messages to forward to player.
     */
    public void setSendLevel(final MessageLevel level) {
        this.sendLevel = level;
    }
    
    /**
     * Determines if current send level will send players messages of the specified level or higher.
     * 
     * @param level MessageLevel to determine if it will be displayed or not.
     * @return true if current level will display this message; false otherwise.
     */
    public boolean isSendLevel(final MessageLevel level) {
        return level.intValue() >= this.sendLevel.intValue();
    }
    
    /**
     * Send a private message that only a single player can see. (Use
     * configuration file defined default level and include timestamp.)
     * 
     * @param player Target player to send message to.
     * @param message Text to display on player's client interface.
     */
    public void send(final Player player, final String message) {
        this.send(player, null, message);
    }
    
    /**
     * Send a private message that only a single player can see. (Include
     * timestamp.)
     * 
     * @param player Player to target message to.
     * @param level Importance level of message.
     * @param message Text to display on player's client interface.
     */
    public void send(final Player player, final MessageLevel level, final String message) {
        this.send(player, level, message, true);
    }
 
    /**
     * Send a private message that only a single player can see.
     * 
     * @param player Player to target message to.
     * @param level Importance level of message.
     * @param message Text to display on player's client interface.
     * @param isTimestamped Include timestamp in message.
     */
    public void send(final Player player, final MessageLevel level, final String message, final boolean isTimestamped) {
        MessageLevel lvl = level;
        
        if (lvl == null)
            lvl = Main.getMessageLevel("send");
        
        if (!this.isSendLevel(lvl)) return;
        
        String formatted = Main.colorize(message);
        
        for (String messageLine : formatted.split("\n")) {
            messageLine = this.formatSend(lvl, messageLine, isTimestamped);
            this.log(lvl, this.formatSendLog(messageLine, player));
            player.sendMessage(messageLine);
        }
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
    public void respond(final CommandSender sender, final MessageLevel level, final String message) {
        this.respond(sender, level, message, true);
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
    public void respond(final CommandSender sender, final MessageLevel level, final String message, final boolean isTimestamped) {
        if (sender instanceof Player) {
            this.send((Player) sender, level, message, isTimestamped);
        } else {
            this.log(level, message);
        }
    }
    
    /**
     * Broadcasted messages will only be displayed if equal to or higher than
     * the defined level. Useful for reducing public message clutter.
     * 
     * @param level Minimum level of messages to broadcast.
     */
    public void setBroadcastLevel(final MessageLevel level) {
        this.broadcastLevel = level;
    }
    
    /**
     * Determines if current broadcast level will broadcast messages of the
     * specified level or higher.
     * 
     * @param level MessageLevel to determine if it will be displayed or not.
     * @return true if current level will display this message; false otherwise.
     */
    public boolean isBroadcastLevel(final MessageLevel level) {
        return level.intValue() >= this.broadcastLevel.intValue();
    }
    
    /**
     * Broadcast a message that all players can see. (Use configuration file
     * defined default level and include timestamp.)
     * 
     * @param message Text to display players' client interface.
     */
    public void broadcast(final String message) {
       this.broadcast(null, message);
    }
    
    /**
     * Broadcast a message that all players can see. (Include timestamp.)
     * 
     * @param level Importance level of message.
     * @param message Text to display on player's client interface.
     */
    public void broadcast(final MessageLevel level, final String message) {
        this.broadcast(level, message, true);
    }
    
    /**
     * Broadcast a message that all players can see.
     * 
     * @param level Importance level of message.
     * @param message Text to display on player's client interface.
     * @param isTimestamped Include timestamp in message.
     */
    public void broadcast(final MessageLevel level, final String message, final boolean isTimestamped) {
        MessageLevel lvl = level;
        if (lvl == null) lvl = Main.getMessageLevel("broadcast");
        if (!this.isBroadcastLevel(lvl)) return;
        
        String formatted = Main.colorize(message);
        
        for (String messageLine : formatted.split("\n")) {
            messageLine = this.formatBroadcast(lvl, messageLine, isTimestamped);
            this.log(lvl, this.formatBroadcastLog(messageLine));
            this.owner.getServer().broadcastMessage(messageLine);
        }
    }
    
    /**
     * Broadcast a message to all players in a specific world.
     * 
     * @param world world to filter recipients to
     * @param level important level of message
     * @param message text to display on player's client interface
     * @param isTimestamped true to include timestamp in message
     */
    public void broadcast(final World world, final MessageLevel level, final String message, final boolean isTimestamped) {
        MessageLevel lvl = level;
        if (lvl == null) lvl = Main.getMessageLevel("broadcast");
        if (!this.isBroadcastLevel(lvl)) return;
        
        String formatted = Main.colorize(message);
        
        for (String messageLine : formatted.split("\n")) {
            messageLine = this.formatBroadcastWorld(lvl, messageLine, isTimestamped, world);
            this.log(lvl, this.formatBroadcastLog(messageLine));
            
            for (Player player : world.getPlayers())
                player.sendMessage(messageLine);
        }
    }
    
    public String formatBroadcast(final MessageLevel level, final String message) {
        return this.formatBroadcast(level, message, true);
    }
    
    public String formatBroadcast(final MessageLevel level, final String message, final boolean isTimestamped) {
        return String.format(Main.getMessageFormat("broadcast")
                , message
                , level.getBroadcastColor().toString()
                , (isTimestamped ? Main.getTimestamp() : "")
        );
    }
    
    public String formatBroadcastWorld(final MessageLevel level, final String message, final boolean isTimestamped, final World world) {
        String format = Main.getMessageFormat("broadcast.world");
        if (format == null) format = MessageManager.DEFAULT_BROADCAST_WORLD_FORMAT;
        String formatted = String.format(format
                , message
                , world.getName()
        );
        
        return this.formatBroadcast(level, formatted, isTimestamped);
    }
    
    public String formatBroadcastLog(final String message) {
        return String.format(Main.getConfigurationFile().getConfiguration().getString("broadcast.log"), message);
    }
    
    public String formatLog(final MessageLevel level, final String message, final Plugin plugin) {
        return String.format(Main.getMessageFormat("log"), message, plugin.getDescription().getName());
    }
    
    public String formatSend(final MessageLevel level, final String message) {
        return this.formatSend(level, message, true);
    }
    
    public String formatSend(final MessageLevel level, final String message, final boolean isTimestamped) {
        return String.format(Main.getMessageFormat("send")
                , message
                , level.getSendColor().toString()
                , (isTimestamped ? Main.getTimestamp() : "")
        );
    }
    
    public String formatSendLog(final String message, final Player player) {
        return String.format(Main.getConfigurationFile().getConfiguration().getString("send.log"), message, player.getName());
    }
}