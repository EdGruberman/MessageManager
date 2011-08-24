package edgruberman.bukkit.messagemanager;

import java.util.TimeZone;

import org.bukkit.util.config.ConfigurationNode;

import edgruberman.bukkit.messagemanager.channels.Channel;
import edgruberman.bukkit.messagemanager.channels.Timestamp;

public class Main extends org.bukkit.plugin.java.JavaPlugin {
    
    public static MessageManager messageManager;
    
    static ConfigurationFile recipients;
    static ConfigurationFile configurationFile;
    
    public void onLoad() {
        Main.configurationFile = new ConfigurationFile(this);
        Main.configurationFile.load();
        
        Main.recipients = new ConfigurationFile(this, "recipients.yml", null, 10);
        
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
    }
    
    public void onEnable() {
        new PlayerListener(this);
        new WorldListener(this);
        
        Channel.getInstance(this.getServer());
        
        new edgruberman.bukkit.messagemanager.commands.MessageManager(this);
        new edgruberman.bukkit.messagemanager.commands.Timestamp(this);
        new edgruberman.bukkit.messagemanager.commands.TimeZone(this);
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
    }
    
    public void loadConfiguration() {
        Main.configurationFile.load();
        Main.recipients.load();
    }
    
    public static boolean useTimestampFor(final String player) {
        boolean useTimestamp = Settings.DEFAULT_MESSAGE_USE_TIMESTAMP;
        
        ConfigurationNode recipient = Main.recipients.getConfiguration().getNode(player);
        if (recipient == null) return useTimestamp;
        
        return recipient.getBoolean("useTimestamp", useTimestamp);
    }
    
    public static Timestamp timestampFor(final String player) {
        Timestamp timestamp = new Timestamp(Main.messageManager.settings.timestamp.getPattern()
                , Main.messageManager.settings.timestamp.getFormat()
                , Main.messageManager.settings.timestamp.getTimeZone()
        );
        
        ConfigurationNode recipient = Main.recipients.getConfiguration().getNode(player);
        if (recipient == null) return timestamp;
        
        timestamp.setFormat(recipient.getString("timestamp.format", timestamp.getFormat()));
        timestamp.setPattern(recipient.getString("timestamp.pattern", timestamp.getPattern()));
        timestamp.setTimeZone(TimeZone.getTimeZone(recipient.getString("timestamp.timezone", timestamp.getTimeZone().getID())));
        
        return timestamp;
    }
    
    public static void saveRecipient(final String player, final Timestamp timestamp, final Boolean useTimestamp) {
        if (timestamp == null) {
            Main.recipients.getConfiguration().setProperty(player + ".timestamp", null);
        } else {
            Main.recipients.getConfiguration().setProperty(player + ".timestamp.pattern", timestamp.getPattern());
            Main.recipients.getConfiguration().setProperty(player + ".timestamp.format", timestamp.getFormat());
            Main.recipients.getConfiguration().setProperty(player + ".timestamp.timezone", timestamp.getTimeZone().getID());
        }
        
        Main.recipients.getConfiguration().setProperty(player + ".useTimestamp", useTimestamp);
        
        Main.recipients.save();
    }
}