package edgruberman.bukkit.messagemanager;

import java.util.TimeZone;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.channels.Dispatcher;
import edgruberman.bukkit.messagemanager.channels.Recipient;

public final class Main extends JavaPlugin {

    public static Logger logger;
    public static MessageManager messageManager;
    public static ConfigurationFile configurationFile;

    public static final String MINIMUM_CONFIGURATION_VERSION = "6.0.0a38";
    public static final String MINIMUM_RECIPIENTS_VERSION = "6.0.0a0";
    public static final String MINIMUM_MESSAGE_MANAGER_VERSION = "6.0.0a0";

    @Override
    public void onEnable() {
        Main.logger = this.getLogger();

        Main.configurationFile = new ConfigurationFile(this);
        Main.configurationFile.setMinVersion(Main.MINIMUM_CONFIGURATION_VERSION);
        Main.configurationFile.load();
        this.setLoggingLevel();
        this.configureRecipientDefaults();

        // TODO move this into Recipient and organize load/reload/onEnable
        Recipient.configurationFile = new ConfigurationFile(this, "recipients.yml", null, null, 60);
        Recipient.configurationFile.setMinVersion(Main.MINIMUM_RECIPIENTS_VERSION);
        Recipient.configurationFile.load();

        MessageManager.dispatcher = new Dispatcher(this);
        MessageManager.dispatcher.init();
        Main.messageManager = new MessageManager(this);

        new edgruberman.bukkit.messagemanager.commands.MessageManager(this);
        new edgruberman.bukkit.messagemanager.commands.Timestamp(this);
        new edgruberman.bukkit.messagemanager.commands.TimeZone(this);
    }

    @Override
    public void onDisable() {
        if (Recipient.configurationFile.isSaveQueued()) Recipient.configurationFile.save();
        for (final MessageManager instance : MessageManager.instances.values()) instance.disable();
        MessageManager.dispatcher = null;
    }

    // Custom reload to avoid losing references for other plugins' MessageManager instances
    public void reload() {
        Main.configurationFile.load();
        this.setLoggingLevel();
        this.configureRecipientDefaults();

        Main.messageManager.enable();
        for (final MessageManager instance : MessageManager.instances.values())
            if (instance != Main.messageManager)
                instance.enable();

        Recipient.configurationFile.load();
        for (final Recipient recipient : MessageManager.dispatcher.getRecipients()) recipient.load();
    }

    private void configureRecipientDefaults() {
        final ConfigurationSection main = Main.configurationFile.getConfig();
        if (!main.isSet("timestamp")) return;

        final ConfigurationSection section = main.getConfigurationSection("timestamp");

        if (section.isSet("format")) {
            final String format = section.getString("format");
            if (format == null) {
                Main.logger.log(Level.WARNING, "Unable to apply null format from " + Main.configurationFile.getFile().getPath() + "; timestamp.format");
            } else {
                Recipient.defaultFormat = format;
            }
        }

        if (section.isSet("pattern")) {
            final String pattern = section.getString("pattern");
            if (pattern == null) {
                Main.logger.log(Level.WARNING, "Unable to apply null pattern from " + Main.configurationFile.getFile().getPath() + "; timestamp.pattern");
            } else {
                Recipient.defaultPattern = pattern;
            }
        }

        if (section.isSet("timeZone")) {
            Recipient.defaultTimeZone = TimeZone.getTimeZone(section.getString("timeZone"));
        }
    }

    private void setLoggingLevel() {
        final String name = Main.configurationFile.getConfig().getString("logLevel", "INFO");
        Level level = MessageLevel.parse(name);
        if (level == null) level = Level.INFO;

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().log(Level.CONFIG, "Logging level set to: " + this.getLogger().getLevel());
    }

}
