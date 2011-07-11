package edgruberman.bukkit.messagemanager;

import org.bukkit.ChatColor;

public class Main extends org.bukkit.plugin.java.JavaPlugin {
    
    private static ConfigurationFile configurationFile;
    private static MessageManager messageManager;
    
    public void onLoad() {
        Main.configurationFile = new ConfigurationFile(this);
        Main.getConfigurationFile().load();
        
        Main.messageManager = new MessageManager(this);
        Main.getMessageManager().log("Version " + this.getDescription().getVersion());
    }
    
    public void onEnable() {
        Main.getMessageManager().log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.getMessageManager().log("Plugin Disabled");
    }
    
    static ConfigurationFile getConfigurationFile() {
        return Main.configurationFile;
    }
    
    static MessageManager getMessageManager() {
        return Main.messageManager;
    }
    
    static String getTimestamp() {
        return ChatColor.valueOf(Main.getConfigurationFile().getConfiguration().getString("timestamp.color"))
            + new java.text.SimpleDateFormat(Main.getMessageFormat("timestamp"))
                .format(new java.util.GregorianCalendar().getTime());
    }
    
    static MessageLevel getMessageLevel(final String path) {
        return MessageLevel.parse(Main.getConfigurationFile().getConfiguration().getString(path + ".level"));
    }
    
    static String getMessageFormat(final String path) {
        return Main.getConfigurationFile().getConfiguration().getString(path + ".format");
    }
    
    static ChatColor getMessageColor(final String level, final String type) {
        String color = Main.getConfigurationFile().getConfiguration().getString("colors." + level + "." + type);
        if (color == null) return null;
        return ChatColor.valueOf(color);
    }
    
    //TODO: parse start and end color tags  
    static String colorize(final String message) {
        String colorized = message;
        
        ChatColor color = null;
        if (colorized.matches("^%[^%]+%.+")) {
            String[] split = colorized.split("%", 3);
            
            MessageLevel level = MessageLevel.parse(split[1]);
            if (level != null) {
                color = level.getBroadcastColor();
            } else if (split[1] != null) {
                color = ChatColor.valueOf(split[1]);
            }
            
            if (color != null)
                colorized = color + split[2];
        }
        
        return colorized;
    }
}