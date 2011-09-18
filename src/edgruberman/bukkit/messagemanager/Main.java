package edgruberman.bukkit.messagemanager;

import java.util.TimeZone;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.ConfigurationNode;

import edgruberman.bukkit.messagemanager.channels.ServerChannel;
import edgruberman.bukkit.messagemanager.channels.Timestamp;

public class Main extends JavaPlugin {
    
    public static MessageManager messageManager;
    
    static ConfigurationFile recipients;
    static ConfigurationFile configurationFile;
    
    @Override
    public void onLoad() {
        // Sequence of initialization is different for this plugin as compared
        // to my other plugins as it must first configure itself before it can
        // properly instantiate its own MessageManager instance.
        Main.configurationFile = new ConfigurationFile(this);
        
        Main.recipients = new ConfigurationFile(this, "recipients.yml", null, 10);
        
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
    }
    
    @Override
    public void onEnable() {
        new PlayerMonitor(this);
        new WorldMonitor(this);
        
        ServerChannel.getInstance(this.getServer());
        
        new edgruberman.bukkit.messagemanager.commands.MessageManager(this);
        new edgruberman.bukkit.messagemanager.commands.Timestamp(this);
        new edgruberman.bukkit.messagemanager.commands.TimeZone(this);
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    @Override
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
        Timestamp timestamp = new Timestamp(Main.messageManager.getSettings().timestamp.getPattern()
                , Main.messageManager.getSettings().timestamp.getFormat()
                , Main.messageManager.getSettings().timestamp.getTimeZone()
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