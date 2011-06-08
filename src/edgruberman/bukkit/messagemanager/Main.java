package edgruberman.bukkit.messagemanager;

import org.bukkit.ChatColor;

public class Main extends org.bukkit.plugin.java.JavaPlugin {
    
    protected static MessageManager messageManager = null;
    
    protected static Main that = null;
    
    public Main() {
        Main.that = this;
    }
    
    public void onLoad() {
        Configuration.load(this);
    }
    
    public void onEnable() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
    }
    
    protected String getTimestamp() {
        return Main.parseChatColor(this.getConfiguration().getString("timestamp.color"))
            + new java.text.SimpleDateFormat(this.getMessageFormat("timestamp"))
                .format(new java.util.GregorianCalendar().getTime());
    }
    
    protected MessageLevel getMessageLevel(String path) {
        return MessageLevel.parse(this.getConfiguration().getString(path + ".level"));
    }
    
    protected String getMessageFormat(String path) {
        return this.getConfiguration().getString(path + ".format");
    }
    
    protected static ChatColor getMessageColor(String level, String type) {
        return Main.parseChatColor(Main.that.getConfiguration().getString("colors." + level + "." + type));
    }
    
    //TODO: parse start and end color tags  
    protected static String colorize(String message) {
        String colorized = message;
        
        ChatColor color = null;
        if (colorized.matches("^%[^%]+%.+")) {
            String[] split = colorized.split("%", 3);
            
            MessageLevel level = MessageLevel.parse(split[1]);
            if (level != null) {
                color = level.getBroadcastColor();
            } else {
                color = Main.parseChatColor(split[1]);
            }
            
            if (color != null)
                colorized = color + split[2];
        }
        
        return colorized;
    }
    
    protected static ChatColor parseChatColor(String name) {
        if (name == null) return null;
        
             if (name.toUpperCase().equals("BLACK"))        return ChatColor.BLACK;
        else if (name.toUpperCase().equals("DARK_BLUE"))    return ChatColor.DARK_BLUE;
        else if (name.toUpperCase().equals("DARK_BLUE"))    return ChatColor.DARK_BLUE;
        else if (name.toUpperCase().equals("DARK_GREEN"))   return ChatColor.DARK_GREEN;
        else if (name.toUpperCase().equals("DARK_AQUA"))    return ChatColor.DARK_AQUA;
        else if (name.toUpperCase().equals("DARK_RED"))     return ChatColor.DARK_RED;
        else if (name.toUpperCase().equals("DARK_PURPLE"))  return ChatColor.DARK_PURPLE;
        else if (name.toUpperCase().equals("GOLD"))         return ChatColor.GOLD;
        else if (name.toUpperCase().equals("GRAY"))         return ChatColor.GRAY;
        else if (name.toUpperCase().equals("DARK_GRAY"))    return ChatColor.DARK_GRAY;
        else if (name.toUpperCase().equals("BLUE"))         return ChatColor.BLUE;
        else if (name.toUpperCase().equals("GREEN"))        return ChatColor.GREEN;
        else if (name.toUpperCase().equals("AQUA"))         return ChatColor.AQUA;
        else if (name.toUpperCase().equals("RED"))          return ChatColor.RED;
        else if (name.toUpperCase().equals("LIGHT_PURPLE")) return ChatColor.LIGHT_PURPLE;
        else if (name.toUpperCase().equals("YELLOW"))       return ChatColor.YELLOW;
        else if (name.toUpperCase().equals("WHITE"))        return ChatColor.WHITE;
        
        return null;
    }
}