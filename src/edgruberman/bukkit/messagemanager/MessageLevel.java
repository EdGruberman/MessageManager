package edgruberman.bukkit.messagemanager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Standardization for coloring of common messages.
 */
@SuppressWarnings("serial")
public class MessageLevel extends Level {
    
    static Map<String, MessageLevel> known = new HashMap<String, MessageLevel>();

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
     * (Defaults: send = WHITE; broadcast = WHITE; Level.INFO = 800)
     */
    public static final MessageLevel INFO = new MessageLevel(Level.INFO);
    
    /**
     * Directly related to player's actions.
     * (Defaults: send = GREEN; broadcast = DARK_GREEN; Level = 775)
     */
    public static final MessageLevel STATUS = new MessageLevel("STATUS", 775);
    
    /**
     * External to player's actions. 
     * (Defaults: send = GRAY; broadcast = GRAY; Level = 750)
     */
    public static final MessageLevel EVENT = new MessageLevel("EVENT", 750);
    
    /**
     * Permissions related. 
     * (Defaults: send = BLUE; broadcast = DARK_BLUE; Level = 725)
     */
    public static final MessageLevel RIGHTS = new MessageLevel("RIGHTS", 725);
    
    /**
     * Current settings.
     * (Defaults: send = AQUA; broadcast = DARK_AQUA; Level = CONFIG/700)
     */
    public static final MessageLevel CONFIG = new MessageLevel(Level.CONFIG);
    
    /**
     * Debug messages. 
     * (Defaults: send = BLACK; broadcast = BLACK; Level = FINE/500)
     */
    public static final MessageLevel FINE = new MessageLevel(Level.FINE);
    
    /**
     * Detailed debug messages. 
     * (Defaults: send = BLACK; broadcast = BLACK; Level = FINER/400)
     */
    public static final MessageLevel FINER = new MessageLevel(Level.FINER);
    
    /**
     * More detail than you can shake a stick at debug messages. 
     * (Defaults: send = BLACK; broadcast = BLACK; Level = FINEST/300)
     */
    public static final MessageLevel FINEST = new MessageLevel(Level.FINEST);
    
    /**
     * Setting minimum level to this value will turn all messages off.
     * Do not send messages with this level.
     */
    public static final MessageLevel OFF = new MessageLevel(Level.OFF);
    
    /**
     * Setting minimum level to this value will turn all messages on.
     * Do not send messages with this level.
     */
    public static final MessageLevel ALL = new MessageLevel(Level.ALL);
    
    private MessageLevel(final Level level) {
        this(level.getName(), level.intValue());
    }
    
    protected MessageLevel(final String name, final int value) {
        super(name, value);
        MessageLevel.known.put(this.getName(), this);
    }
    
    /**
     * Find existing MessageLevel by name.
     * 
     * @param name name of existing message level
     * @return existing MessageLevel; null otherwise
     */
    public static MessageLevel parse(final String name) {
        return MessageLevel.known.get(name);
    }
}