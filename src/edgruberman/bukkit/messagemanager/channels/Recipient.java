package edgruberman.bukkit.messagemanager.channels;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import edgruberman.bukkit.messagemanager.ConfigurationFile;
import edgruberman.bukkit.messagemanager.MessageColor;

/**
 * Timestamp Formatter
 */
public final class Recipient {

    public static final String DEFAULT_FORMAT = "&8%2$s&_ %1$s"; // 1 = Formatted Message, 2 = Formatted Timestamp
    public static final String DEFAULT_PATTERN = "HH:mm:ss"; // SimpleDateFormat
    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();

    public static String defaultFormat = Recipient.DEFAULT_FORMAT;
    public static String defaultPattern = Recipient.DEFAULT_PATTERN;
    public static TimeZone defaultTimeZone = Recipient.DEFAULT_TIME_ZONE;

    public static ConfigurationFile configurationFile;

    private CommandSender target = null;
    private String format;
    private final SimpleDateFormat timestamp = new SimpleDateFormat();

    private final String targetClassName;
    private final String targetName;

    private String formatColorized;
    final Set<Channel> memberships = new HashSet<Channel>();

    public Recipient(final CommandSender target) {
        this(target.getClass().getSimpleName(), target.getName());
        this.target = target;
    }

    public Recipient(final OfflinePlayer target) {
        this("CraftPlayer", target.getName());
    }

    Recipient(final String targetClassName, final String targetName) {
        this.targetClassName = targetClassName;
        this.targetName = targetName;
        this.setFormat(Recipient.defaultFormat);
        this.timestamp.applyPattern(Recipient.defaultPattern);
        this.timestamp.setTimeZone(TimeZone.getTimeZone(Recipient.defaultTimeZone.getID()));
        this.load();
    }

    public CommandSender getTarget() {
        return this.target;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(final String format) {
        this.formatColorized = MessageColor.colorize(format);
        this.format = format;
    }

    public SimpleDateFormat getTimestamp() {
        return this.timestamp;
    }

    void removeMemberships() {
        final Set<Channel> memberships = new HashSet<Channel>(this.memberships);
        for (final Channel channel : memberships) channel.remove(this);
    }

    public void send(final String message, final boolean applyTimestamp) {
        this.target.sendMessage((applyTimestamp ? this.format(message) : message));
    }

    /**
     * Format message to include timestamp for final display to player.
     *
     * @param message text that includes color codes already (if no color
     * codes included, timestamp colors could color message)
     * @return formatted message including color and timestamp
     */
    public String format(final String message) {
        return String.format(this.formatColorized, message, this.timestamp.format(new Date()));
    }

    public Recipient load() {
        final ConfigurationSection section = this.getConfig();
        this.setFormat(section.getString("format", Recipient.defaultFormat));
        this.timestamp.applyPattern(section.getString("pattern", Recipient.defaultPattern));
        this.timestamp.setTimeZone(TimeZone.getTimeZone(section.getString("timezone", Recipient.defaultTimeZone.getID())));
        return this;
    }

    public void save() {
        final ConfigurationSection section = this.getConfig();
        section.set("format", this.format);
        section.set("pattern", this.timestamp.toPattern());
        section.set("timezone", this.timestamp.getTimeZone().getID());
        Recipient.configurationFile.save(false);
    }

    public Recipient reset() {
        final String path = this.targetClassName + "." + this.targetName;
        Recipient.configurationFile.getConfig().createSection(path);
        return this.load();
    }

    private ConfigurationSection getConfig() {
        final String path = this.targetClassName + "." + this.targetName;
        ConfigurationSection section = Recipient.configurationFile.getConfig().getConfigurationSection(path);
        if (section == null) section = Recipient.configurationFile.getConfig().createSection(path);
        return section;
    }

}
