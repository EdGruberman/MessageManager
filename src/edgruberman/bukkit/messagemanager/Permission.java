package edgruberman.bukkit.messagemanager;

public enum Permission {
      MESSAGEMANAGER("messagemanager")
    , MESSAGEMANAGER_RELOAD("messagemanager.reload")
    
    , TIMESTAMP("timestamp")
    
    , TIMESTAMP_GET("timestamp.get")
    , TIMESTAMP_GET_PLAYER("timestamp.get.player.%1$s")
    
    , TIMESTAMP_PATTERN_GET("timestamp.pattern.get")
    , TIMESTAMP_PATTERN_GET_PLAYER("timestamp.pattern.get.player.%1$s")
    , TIMESTAMP_PATTERN_SET("timestamp.pattern.set")
    , TIMESTAMP_PATTERN_SET_PLAYER("timestamp.pattern.set.player.%1$s")
    
    , TIMESTAMP_FORMAT_GET("timestamp.format.get")
    , TIMESTAMP_FORMAT_GET_PLAYER("timestamp.format.get.player.%1$s")
    , TIMESTAMP_FORMAT_SET("timestamp.format.set")
    , TIMESTAMP_FORMAT_SET_PLAYER("timestamp.format.set.player.%1$s")
    
    , TIMESTAMP_TIMEZONE_GET("timestamp.timezone.get")
    , TIMESTAMP_TIMEZONE_GET_PLAYER("timestamp.timezone.get.player.%1$s")
    , TIMESTAMP_TIMEZONE_SET("timestamp.timezone.set")
    , TIMESTAMP_TIMEZONE_SET_PLAYER("timestamp.timezone.set.player.%1$s")
    
    , TIMESTAMP_COLOR_GET("timestamp.color.get")
    , TIMESTAMP_COLOR_GET_PLAYER("timestamp.color.get.player.%1$s")
    , TIMESTAMP_COLOR_SET("timestamp.color.set")
    , TIMESTAMP_COLOR_SET_PLAYER("timestamp.color.set.player.%1$s")
    
    , TIMESTAMP_RESET("timestamp.reset")
    , TIMESTAMP_RESET_PLAYER("timestamp.reset.player.%1$s")
    
    , TIMESTAMP_TOGGLE("timestamp.toggle")
    , TIMESTAMP_TOGGLE_PLAYER("timestamp.toggle.player.%1$s")
    
    ;
    
    static final String PLUGIN_NAME = "messagemanager";
    
    private final String format;
    
    private Permission(String format) {
        this.format = Permission.PLUGIN_NAME + "." + format;
    }
    
    @Override
    public String toString() {
        return this.toString((Object) null);
    }
    
    public String toString(Object... args) {
        return String.format(this.format, args);
    }
}