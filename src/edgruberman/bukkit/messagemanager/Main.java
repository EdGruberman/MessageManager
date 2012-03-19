package edgruberman.bukkit.messagemanager;

import java.util.TimeZone;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.channels.Dispatcher;
import edgruberman.bukkit.messagemanager.channels.Timestamp;

public final class Main extends JavaPlugin {

    public static MessageManager messageManager;

    static ConfigurationFile recipients;
    static ConfigurationFile configurationFile;

    final private static String MINIMUM_CONFIGURATION_VERSION = "5.1.0a0";
    private final boolean firstEnable = true;

    @Override
    public void onLoad() {
        Main.plugin = this;

        Main.configurationFile = new ConfigurationFile(this);
        Main.configurationFile.setMinVersion(Main.MINIMUM_CONFIGURATION_VERSION);
        Main.configurationFile.load();
        this.setLoggingLevel();
        Main.recipients = new ConfigurationFile(this, "recipients.yml", null, null, 60);

        Main.messageManager = new MessageManager(this);
    }

    private void setLoggingLevel() {
        final String name = Main.configurationFile.getConfig().getString("logLevel", "INFO");
        Level level = MessageLevel.parse(name);
        if (level == null) level = Level.INFO;
        this.getLogger().setLevel(level);
    }

    @Override
    public void onEnable() {
        this.loadConfiguration();
        new Dispatcher(this);

        new edgruberman.bukkit.messagemanager.commands.MessageManager(this);
        new edgruberman.bukkit.messagemanager.commands.Timestamp(this);
        new edgruberman.bukkit.messagemanager.commands.TimeZone(this);
    }

    @Override
    public void onDisable() {
        if (Main.recipients.isSaveQueued()) Main.recipients.save();
    }

    public void loadConfiguration() {
        if (!this.firstEnable) Main.configurationFile.load();
        Main.recipients.load();
    }

    public static boolean useTimestampFor(final String player) {
        return Main.recipients.getConfig().getBoolean(player + ".useTimestamp", Settings.DEFAULT_MESSAGE_USE_TIMESTAMP);
    }

    public static Timestamp timestampFor(final String player) {
        final Timestamp timestamp = new Timestamp(Main.messageManager.getSettings().timestamp.getPattern()
                , Main.messageManager.getSettings().timestamp.getFormat()
                , Main.messageManager.getSettings().timestamp.getTimeZone()
        );

        final ConfigurationSection recipient = Main.recipients.getConfig().getConfigurationSection(player);
        if (recipient == null) return timestamp;

        timestamp.setFormat(recipient.getString("timestamp.format", timestamp.getFormat()));
        timestamp.setPattern(recipient.getString("timestamp.pattern", timestamp.getPattern()));
        timestamp.setTimeZone(TimeZone.getTimeZone(recipient.getString("timestamp.timezone", timestamp.getTimeZone().getID())));

        return timestamp;
    }

    public static void saveRecipient(final String player, final Timestamp timestamp, final Boolean useTimestamp) {
        if (timestamp == null) {
            Main.recipients.getConfig().set(player + ".timestamp", null);
        } else {
            Main.recipients.getConfig().set(player + ".timestamp.pattern", timestamp.getPattern());
            Main.recipients.getConfig().set(player + ".timestamp.format", timestamp.getFormat());
            Main.recipients.getConfig().set(player + ".timestamp.timezone", timestamp.getTimeZone().getID());
        }

        Main.recipients.getConfig().set(player + ".useTimestamp", useTimestamp);

        Main.recipients.save(false);
    }

    private static Plugin plugin;
    public static void log(final Level level, final String message) {
        Main.plugin.getLogger().log(level, message);
    }

}
