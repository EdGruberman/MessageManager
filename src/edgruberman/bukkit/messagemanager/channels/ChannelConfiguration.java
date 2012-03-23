package edgruberman.bukkit.messagemanager.channels;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.ConfigurationFile;
import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public class ChannelConfiguration {

    public static final MessageLevel DEFAULT_LEVEL_CHANNEL = MessageLevel.ALL;
    public static final MessageLevel DEFAULT_LEVEL_LOG = MessageLevel.ALL;
    public static final String DEFAULT_FORMAT = "%1$s"; // 1 = Message, 2 = Channel Name
    public static final ChatColor DEFAULT_COLOR = ChatColor.WHITE;

    public MessageLevel levelChannel = ChannelConfiguration.DEFAULT_LEVEL_CHANNEL;
    public MessageLevel levelLog = ChannelConfiguration.DEFAULT_LEVEL_LOG;
    public String format = ChannelConfiguration.DEFAULT_FORMAT;
    private final Map<MessageLevel, ChatColor> colors = new HashMap<MessageLevel, ChatColor>();

    ChannelConfiguration() {}

    private ChannelConfiguration(final ChannelConfiguration other) {
        this.levelChannel = other.levelChannel;
        this.levelLog = other.levelLog;
        this.format = other.format;
        this.colors.putAll(other.colors);
    }

    public ChatColor getColor(final MessageLevel level) {
        if (!this.colors.containsKey(level)) return ChannelConfiguration.DEFAULT_COLOR;

        return this.colors.get(level);
    }

    public static void load(final Plugin owner, final ConfigurationFile local) {
        for (final Channel.Type type : Channel.Type.values()) {
            ConfigurationSection section = local.getConfig().getConfigurationSection(type.name());
            if (section == null) section = local.getConfig().createSection(type.name());
            final ChannelConfiguration configuration = new ChannelConfiguration(MessageManager.getDispatcher().getChannelConfigurationDefaults(type));

            if (section.isSet("level.channel")) {
                final MessageLevel levelChannel = MessageLevel.parse(section.getString("level.channel"));
                if (levelChannel == null) {
                    Main.logger.log(Level.WARNING, "Unable to determine MessageLevel from " + local.getFile().getPath() + "; " + type.name() + ".level.channel: " + section.getString("level.channel"));
                } else {
                    Main.logger.log(Level.FINE, "Override specified in " + local.getFile().getPath() + "; " + type.name() + ".level.channel: " + levelChannel.getName());
                    configuration.levelChannel = levelChannel;
                }
            }

            if (section.isSet("level.log")) {
                final MessageLevel levelLog = MessageLevel.parse(section.getString("level.log"));
                if (levelLog == null) {
                    Main.logger.log(Level.WARNING, "Unable to determine MessageLevel from " + local.getFile().getPath() + "; " + type.name() + ".level.log: " + section.getString("level.log"));
                } else {
                    Main.logger.log(Level.FINE, "Override specified in " + local.getFile().getPath() + "; " + type.name() + ".level.log: " + levelLog.getName());
                    configuration.levelLog = levelLog;
                }
            }

            if (section.isSet("format")) {
                final String format = section.getString("format");
                if (format == null) {
                    Main.logger.log(Level.WARNING, "Unable to apply null format from " + local.getFile().getPath() + "; " + type.name() + ".format");
                } else {
                    Main.logger.log(Level.FINE, "Override specified in " + local.getFile().getPath() + "; " + type.name() + ".format: " + format.replaceAll("&", "&&"));
                    configuration.format = format;
                }
            }

            if (section.isSet("color")) {
                final ConfigurationSection colors = section.getConfigurationSection("color");
                for (final String levelName : colors.getKeys(false)) {
                    final MessageLevel colorLevel = MessageLevel.parse(levelName);
                    if (colorLevel == null) {
                        Main.logger.log(Level.WARNING, "Unable to determine MessageLevel from " + local.getFile().getPath() + "; " + type.name() + ".color: " + levelName);
                        continue;
                    }

                    final ChatColor color;
                    try {
                        color = ChatColor.valueOf(colors.getString(levelName));
                    } catch (final Exception e) {
                        Main.logger.log(Level.WARNING, "Unable to determine ChatColor from " + local.getFile().getPath() + "; " + type.name() + ".color." + levelName + ": " + colors.getString(levelName));
                        continue;
                    }

                    Main.logger.log(Level.FINE, "Override specified in " + local.getFile().getPath() + "; " + type.name() + ".color." + colorLevel + ": " + color.name());
                    configuration.colors.put(colorLevel, color);
                }
            }

            Channel.setChannelConfiguration(type, owner, configuration);
        }
    }

}
