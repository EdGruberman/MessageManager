package edgruberman.bukkit.messagemanager.channels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    public static final List<ChatColor> DEFAULT_DISPLAY = new ArrayList<ChatColor>(Arrays.asList(ChatColor.WHITE));

    public MessageLevel levelChannel = ChannelConfiguration.DEFAULT_LEVEL_CHANNEL;
    public MessageLevel levelLog = ChannelConfiguration.DEFAULT_LEVEL_LOG;
    public String format = ChannelConfiguration.DEFAULT_FORMAT;
    private final Map<MessageLevel, List<ChatColor>> color = new HashMap<MessageLevel, List<ChatColor>>();

    ChannelConfiguration() {}

    private ChannelConfiguration(final ChannelConfiguration other) {
        this();
        this.levelChannel = other.levelChannel;
        this.levelLog = other.levelLog;
        this.format = other.format;
        this.color.putAll(other.color);
    }

    public List<ChatColor> getColor(final MessageLevel level) {
        if (!this.color.containsKey(level)) return ChannelConfiguration.DEFAULT_DISPLAY;

        return this.color.get(level);
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



                    if (colors.isList(levelName)) {
                        // Multiple codes
                        configuration.color.put(colorLevel, new ArrayList<ChatColor>());
                        for (final String item : colors.getStringList(levelName)) {
                            final ChatColor color;
                            try {
                                color = ChatColor.valueOf(item);
                            } catch (final Exception e) {
                                Main.logger.log(Level.WARNING, "Unable to determine ChatColor from " + local.getFile().getPath() + "; " + type.name() + ".color." + levelName + ": " + item);
                                continue;
                            }
                            configuration.color.get(colorLevel).add(color);
                        }
                    } else {
                        // Single code
                        configuration.color.put(colorLevel, new ArrayList<ChatColor>());
                        final ChatColor color;
                        try {
                            color = ChatColor.valueOf(colors.getString(levelName));
                        } catch (final Exception e) {
                            Main.logger.log(Level.WARNING, "Unable to determine ChatColor from " + local.getFile().getPath() + "; " + type.name() + ".color." + levelName + ": " + colors.getString(levelName));
                            continue;
                        }
                        configuration.color.get(colorLevel).add(color);
                    }

                    String colorNames = "";
                    for (final ChatColor c : configuration.color.get(colorLevel)) {
                        if (!colorNames.equals("")) colorNames += ", ";
                        colorNames += c.name();
                    }
                    Main.logger.log(Level.FINE, "Override specified in " + local.getFile().getPath() + "; " + type.name() + ".color." + colorLevel + ": " + colorNames);
                }
            }

            Channel.setChannelConfiguration(type, owner, configuration);
        }
    }

}
