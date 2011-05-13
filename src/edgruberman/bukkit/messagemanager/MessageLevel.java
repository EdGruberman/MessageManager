package edgruberman.bukkit.messagemanager;

import java.util.logging.Level;

import org.bukkit.ChatColor;

/**
 * Standardization for coloring of common messages.
 */
@SuppressWarnings("serial")
public class MessageLevel extends Level {

    /**
     * Errors and denied actions.
     * (Defaults: send = RED; broadcast = DARK_RED; Level = SEVERE/1000)
     */
    public static final MessageLevel SEVERE = new MessageLevel(Level.SEVERE);
    
    /**
     * Impending actions.
     * (Defaults: send = YELLOW; broadcast = GOLD; Level = WARNING/900)
     */
    public static final MessageLevel WARNING = new MessageLevel(Level.WARNING);
    
    /**
     * Instructions, requirements.
     * (Defaults: send = LIGHT_PURPLE; broadcast = DARK_PURPLE; Level = 850)
     */
    public static final MessageLevel NOTICE = new MessageLevel("NOTICE", 850);
    
    /**
     * Standard data. 
     * (send = WHITE; broadcast = WHITE; Level.INFO = 800)
     */
    public static final MessageLevel INFO = new MessageLevel(Level.INFO);
    
    /**
     * Directly related to player's actions.
     * (send = GREEN; broadcast = DARK_GREEN; Level = 775)
     */
    public static final MessageLevel STATUS = new MessageLevel("STATUS", 775);
    
    /**
     * External to player's actions. 
     * (send = GRAY; broadcast = GRAY; Level = 750)
     */
    public static final MessageLevel EVENT = new MessageLevel("EVENT", 750);
    
    /**
     * Current settings.
     * (send = AQUA; broadcast = DARK_AQUA; Level = CONFIG/700)
     */
    public static final MessageLevel CONFIG = new MessageLevel(Level.CONFIG);
    
    /**
     * Permissions related. 
     * (send = BLUE; broadcast = DARK_BLUE; Level = 600)
     */
    public static final MessageLevel RIGHTS = new MessageLevel("RIGHTS", 600);
    
    /**
     * Debug messages. 
     * (send = BLACK; broadcast = BLACK; Level = FINE/500)
     */
    public static final MessageLevel FINE = new MessageLevel(Level.FINE);
    
    /**
     * Detailed debug messages. 
     * (send = BLACK; broadcast = BLACK; Level = FINER/400)
     */
    public static final MessageLevel FINER = new MessageLevel(Level.FINER);
    
    /**
     * More detail than you can shake a stick at debug messages. 
     * (send = BLACK; broadcast = BLACK; Level = FINEST/300)
     */
    public static final MessageLevel FINEST = new MessageLevel(Level.FINEST);
    
    /**
     * Do not send messages with this level.  Only used for setting minimum levels.
     */
    public static final MessageLevel OFF = new MessageLevel(Level.OFF);
    
    /**
     * Do not send messages with this level.  Only used for setting minimum levels.
     */
    public static final MessageLevel ALL = new MessageLevel(Level.ALL);
    
    private final ChatColor send;
    private final ChatColor broadcast;
    
    private MessageLevel(Level level) {
        this(level.getName(), level.intValue());
    }
    
    private MessageLevel(String name, int value) {
        super(name, value);

        this.send = Main.getMessageColor(name, "send");
        this.broadcast = Main.getMessageColor(name, "broadcast");
    }
    
    public ChatColor getSendColor() {
        return send;
    }
    
    public ChatColor getBroadcastColor() {
        return broadcast;
    }
    
    public static MessageLevel parse(String name) {
        if (name == null) return null;
        
             if (name.toUpperCase().equals("SEVERE"))  return MessageLevel.SEVERE;
        else if (name.toUpperCase().equals("WARNING")) return MessageLevel.WARNING;
        else if (name.toUpperCase().equals("NOTICE"))  return MessageLevel.NOTICE;
        else if (name.toUpperCase().equals("INFO"))    return MessageLevel.INFO;
        else if (name.toUpperCase().equals("STATUS"))  return MessageLevel.STATUS;
        else if (name.toUpperCase().equals("EVENT"))   return MessageLevel.EVENT;
        else if (name.toUpperCase().equals("CONFIG"))  return MessageLevel.CONFIG;
        else if (name.toUpperCase().equals("RIGHTS"))  return MessageLevel.RIGHTS;
        else if (name.toUpperCase().equals("FINE"))    return MessageLevel.FINE;
        else if (name.toUpperCase().equals("FINER"))   return MessageLevel.FINER;
        else if (name.toUpperCase().equals("FINEST"))  return MessageLevel.FINEST;
        else if (name.toUpperCase().equals("OFF"))     return MessageLevel.OFF;
        else if (name.toUpperCase().equals("ALL"))     return MessageLevel.ALL;
             
        return null;
    }
}