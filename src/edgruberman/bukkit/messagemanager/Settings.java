package edgruberman.bukkit.messagemanager;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.channels.Channel;
import edgruberman.bukkit.messagemanager.channels.Timestamp;

/**
 * MessageManager configuration settings. This code defines defaults that can
 * be overridden by settings defined in MessageManager's config.yml which can also
 * be overridden by settings defined in the owning plugin's MessageManager.yml.
 */
public final class Settings {

    public static final MessageLevel DEFAULT_MESSAGE_LEVEL = MessageLevel.INFO;
    public static final boolean DEFAULT_MESSAGE_USE_TIMESTAMP = true;

    public static final Map<Channel.Type, MessageLevel> DEFAULT_LEVEL = new HashMap<Channel.Type, MessageLevel>();
    public static final Map<Channel.Type, String> DEFAULT_FORMAT = new HashMap<Channel.Type, String>();
    public static final Map<Channel.Type, String> DEFAULT_LOG = new HashMap<Channel.Type, String>();

    public static final String DEFAULT_TIMESTAMP_PATTERN = "HH:mm:ss"; // SimpleDateFormat
    public static final String DEFAULT_TIMESTAMP_FORMAT = "&8%2$s &_%1$s"; // 1 = Formatted Message, 2 = Formatted Timestamp
    public static final TimeZone DEFAULT_TIMESTAMP_TIMEZONE = TimeZone.getDefault();

    public static final Map<MessageLevel, Map<Channel.Type, ChatColor>> DEFAULT_COLOR = new HashMap<MessageLevel, Map<Channel.Type, ChatColor>>();

    static {
        Settings.DEFAULT_LEVEL.put(Channel.Type.PLAYER, MessageLevel.ALL);
        Settings.DEFAULT_LEVEL.put(Channel.Type.SERVER, MessageLevel.ALL);
        Settings.DEFAULT_LEVEL.put(Channel.Type.WORLD,  MessageLevel.ALL);
        Settings.DEFAULT_LEVEL.put(Channel.Type.CUSTOM, MessageLevel.ALL);
        Settings.DEFAULT_LEVEL.put(Channel.Type.LOG,    MessageLevel.ALL);

        Settings.DEFAULT_FORMAT.put(Channel.Type.PLAYER, "~ %1$s"); // 1 = Message, 2 = Player Name
        Settings.DEFAULT_FORMAT.put(Channel.Type.SERVER, "%1$s"); // 1 = Message, 2 = Server Name
        Settings.DEFAULT_FORMAT.put(Channel.Type.WORLD,  "[%2$s] %1$s"); // 1 = Message, 2 = World Name
        Settings.DEFAULT_FORMAT.put(Channel.Type.CUSTOM, "[%2$s] %1$s"); // 1 = Message, 2 = Channel Name
        Settings.DEFAULT_FORMAT.put(Channel.Type.LOG,    "[%2$s] %1$s"); // 1 = Message, 2 = Plugin Name

        Settings.DEFAULT_LOG.put(Channel.Type.PLAYER, "[%2$s] %1$s"); // 1 = Message, 2 = Player Name
        Settings.DEFAULT_LOG.put(Channel.Type.SERVER, "[SERVER] %1$s"); // 1 = Message, 2 = Server Name
        Settings.DEFAULT_LOG.put(Channel.Type.WORLD,  "%1$s"); // 1 = Message, 2 = World Name
        Settings.DEFAULT_LOG.put(Channel.Type.CUSTOM, "%1$s"); // 1 = Message, 2 = Channel Name
        Settings.DEFAULT_LOG.put(Channel.Type.LOG,    "%1$s"); // 1 = Message, 2 = Plugin Name

        Settings.loadDefaultColor(MessageLevel.SEVERE,  ChatColor.RED,          ChatColor.DARK_RED, ChatColor.DARK_RED, ChatColor.DARK_RED, ChatColor.DARK_RED);
        Settings.loadDefaultColor(MessageLevel.WARNING, ChatColor.YELLOW,       ChatColor.GOLD, ChatColor.GOLD, ChatColor.GOLD, ChatColor.GOLD);
        Settings.loadDefaultColor(MessageLevel.NOTICE,  ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE, ChatColor.DARK_PURPLE, ChatColor.DARK_PURPLE, ChatColor.DARK_PURPLE);
        Settings.loadDefaultColor(MessageLevel.INFO,    ChatColor.WHITE,        ChatColor.WHITE, ChatColor.WHITE, ChatColor.WHITE, ChatColor.WHITE);
        Settings.loadDefaultColor(MessageLevel.STATUS,  ChatColor.GREEN,        ChatColor.DARK_GREEN, ChatColor.DARK_GREEN, ChatColor.DARK_GREEN, ChatColor.DARK_GREEN);
        Settings.loadDefaultColor(MessageLevel.EVENT,   ChatColor.GRAY,         ChatColor.DARK_GRAY, ChatColor.DARK_GRAY, ChatColor.DARK_GRAY, ChatColor.DARK_GRAY);
        Settings.loadDefaultColor(MessageLevel.CONFIG,  ChatColor.AQUA,         ChatColor.DARK_AQUA, ChatColor.DARK_AQUA, ChatColor.DARK_AQUA, ChatColor.DARK_AQUA);
        Settings.loadDefaultColor(MessageLevel.RIGHTS,  ChatColor.BLUE,         ChatColor.DARK_BLUE, ChatColor.DARK_BLUE, ChatColor.DARK_BLUE, ChatColor.DARK_BLUE);
        Settings.loadDefaultColor(MessageLevel.FINE,    ChatColor.BLACK,        ChatColor.BLACK, ChatColor.BLACK, ChatColor.BLACK, ChatColor.BLACK);
        Settings.loadDefaultColor(MessageLevel.FINER,   ChatColor.BLACK,        ChatColor.BLACK, ChatColor.BLACK, ChatColor.BLACK, ChatColor.BLACK);
        Settings.loadDefaultColor(MessageLevel.FINEST,  ChatColor.BLACK,        ChatColor.BLACK, ChatColor.BLACK, ChatColor.BLACK, ChatColor.BLACK);
    }

    private static void loadDefaultColor(MessageLevel level, ChatColor player, ChatColor server, ChatColor world, ChatColor custom, ChatColor log) {
        Settings.DEFAULT_COLOR.put(level, new HashMap<Channel.Type, ChatColor>());
        Settings.DEFAULT_COLOR.get(level).put(Channel.Type.PLAYER, player);
        Settings.DEFAULT_COLOR.get(level).put(Channel.Type.SERVER, server);
        Settings.DEFAULT_COLOR.get(level).put(Channel.Type.WORLD, world);
        Settings.DEFAULT_COLOR.get(level).put(Channel.Type.CUSTOM, custom);
        Settings.DEFAULT_COLOR.get(level).put(Channel.Type.LOG, log);
    }

    private static Settings parent = new Settings();

    public Map<Channel.Type, MessageLevel> level = new HashMap<Channel.Type, MessageLevel>();
    public Map<Channel.Type, String> format = new HashMap<Channel.Type, String>();
    public Map<Channel.Type, String> log = new HashMap<Channel.Type, String>();
    public Timestamp timestamp = new Timestamp(Settings.DEFAULT_TIMESTAMP_PATTERN, Settings.DEFAULT_TIMESTAMP_FORMAT, Settings.DEFAULT_TIMESTAMP_TIMEZONE);
    public Map<MessageLevel, Map<Channel.Type, ChatColor>> color = new HashMap<MessageLevel, Map<Channel.Type, ChatColor>>();

    private FileConfiguration config;
    private boolean isLoaded = false;

    private Settings() {}

    Settings(final Plugin owner) {
        if (!Settings.parent.isLoaded)
            Settings.parent.load(Bukkit.getServer().getPluginManager().getPlugin("MessageManager").getConfig());

        ConfigurationFile file = new ConfigurationFile(owner, "MessageManager.yml");
        file.load();
        this.load(file.getConfig());
    }

    private void load(final FileConfiguration configuration) {
        this.config = configuration;

        for (Channel.Type type : Channel.Type.values()) {
            this.level.put(type, this.parseMessageLevel(
                    "send." + type.name() + ".level"
                  , Settings.parent.level.get(type)
                  , Settings.DEFAULT_LEVEL.get(type)
            ));

            this.format.put(type, this.parseString(
                      "send." + type.name() + ".format"
                    , Settings.parent.format.get(type)
                    , Settings.DEFAULT_FORMAT.get(type)
            ));

            this.log.put(type, this.parseString(
                    "send." + type.name() + ".log"
                  , Settings.parent.log.get(type)
                  , Settings.DEFAULT_FORMAT.get(type)
          ));
        }

        this.timestamp.setPattern(this.parseString("timestamp.pattern", Settings.parent.timestamp.getPattern(), Settings.DEFAULT_TIMESTAMP_PATTERN));
        this.timestamp.setFormat(this.parseString("timestamp.format", Settings.parent.timestamp.getFormat(), Settings.DEFAULT_TIMESTAMP_FORMAT));
        this.timestamp.setTimeZone(this.parseTimeZone("timestamp.timezone", Settings.parent.timestamp.getTimeZone(), Settings.DEFAULT_TIMESTAMP_TIMEZONE));

        for (MessageLevel level : MessageLevel.known.values()) {
            this.color.put(level, new HashMap<Channel.Type, ChatColor>());
            for (Channel.Type type : Channel.Type.values())
                this.color.get(level).put(type, this.loadColor(level, type));
       }

        this.isLoaded = true;
    }

    private ChatColor loadColor(MessageLevel level, Channel.Type type) {
        ChatColor parentDefault = null;
        if (Settings.parent.color.containsKey(level))
            parentDefault = Settings.parent.color.get(level).get(type);

        ChatColor codeDefault = (Settings.DEFAULT_COLOR.get(level) != null ? Settings.DEFAULT_COLOR.get(level).get(type) : null);
        return this.parseChatColor(
                "color." + level.getName() + "." + type.name()
                , parentDefault
                , codeDefault
        );
    }

    private MessageLevel parseMessageLevel(final String path, final MessageLevel parentDefault, final MessageLevel codeDefault) {
        String name = this.config.getString(path);
        if (name == null) return (parentDefault != null ? parentDefault : codeDefault);

        return MessageLevel.parse(name);
    }

    private ChatColor parseChatColor(final String path, final ChatColor parentDefault, final ChatColor codeDefault) {
        String name = this.config.getString(path);
        if (name == null) return (parentDefault != null ? parentDefault : codeDefault);

        return ChatColor.valueOf(name);
    }

    private String parseString(final String path, final String parentDefault, final String codeDefault) {
        String name = this.config.getString(path);
        if (name == null) return (parentDefault != null ? parentDefault : codeDefault);

        return name;
    }

    private TimeZone parseTimeZone(final String path, final TimeZone parentDefault, final TimeZone codeDefault) {
        String name = this.config.getString(path);
        if (name == null) return (parentDefault != null ? parentDefault : codeDefault);

        return TimeZone.getTimeZone(name);
    }
}