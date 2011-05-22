package edgruberman.bukkit.messagemanager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

public class Main extends org.bukkit.plugin.java.JavaPlugin {
    
    public static MessageManager messageManager = null;
    
    public static Main that = null;
    
    public Main() {
        Main.that = this;
    }
    
    public void onLoad() {
        Configuration.load(this);
    }
    
    public void onEnable() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());

        new CommandManager(this);
        
        this.registerEvents();
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
        Main.messageManager = null;
    }
    
    private void registerEvents() {
        org.bukkit.plugin.PluginManager pluginManager = this.getServer().getPluginManager();
        PlayerListener playerListener = new PlayerListener(this);
        
        pluginManager.registerEvent(Event.Type.PLAYER_JOIN, playerListener, this.getEventPriority("broadcast.player.join"), this);
        pluginManager.registerEvent(Event.Type.PLAYER_CHAT, playerListener, this.getEventPriority("broadcast.player.chat"), this);
        pluginManager.registerEvent(Event.Type.PLAYER_QUIT, playerListener, this.getEventPriority("broadcast.player.quit"), this);
        pluginManager.registerEvent(Event.Type.PLAYER_KICK, playerListener, this.getEventPriority("broadcast.player.kick"), this);
        pluginManager.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, this.getEventPriority("playerLogin"), this);
    }
    
    private Event.Priority getEventPriority(String path) {
        return Main.parseEventPriority(this.getConfiguration().getString(path + ".priority"));
    }
    
    private static Event.Priority parseEventPriority(String name) {
            if (name.toUpperCase().equals("LOWEST"))  return Event.Priority.Lowest;
       else if (name.toUpperCase().equals("LOW"))     return Event.Priority.Low;
       else if (name.toUpperCase().equals("NORMAL"))  return Event.Priority.Normal;
       else if (name.toUpperCase().equals("HIGH"))    return Event.Priority.High;
       else if (name.toUpperCase().equals("HIGHEST")) return Event.Priority.Highest;
       else if (name.toUpperCase().equals("MONITOR")) return Event.Priority.Monitor;
       
       return null;
    }
    
    public String formatChat(Player player, String message) {
        return this.formatChat(player, message, null);
    }
    
    public String formatChat(Player player, String message, ChatColor color) {
        String type;
        String name = null;
        if (player != null) {
            type = "player";
            name = player.getDisplayName();
        } else {
            type = "server";
        }
        
        if (color != null)
            message = color + message;
        
        return this.formatBroadcast(this.getMessageLevel("broadcast." + type + ".chat")
                , String.format(this.getMessageFormat("broadcast." + type + ".chat"), message, name)
        );
    }
    
    public void broadcastSay(CommandSender sender, String message) {
        String type;
        String name = null;
        if (sender instanceof Player) {
            type = "player";
            name = ((Player) sender).getDisplayName();
        } else {
            type = "server";
        }
        
        Main.messageManager.broadcast(
                this.getMessageLevel("broadcast." + type + ".say")
                , String.format(this.getMessageFormat("broadcast." + type + ".say"), message, name)
        );
    }
    
    public void broadcastMe(CommandSender sender, String message) {
        String type;
        String name = null;
        if (sender instanceof Player) {
            type = "player";
            name = ((Player) sender).getDisplayName();
        } else {
            type = "server";
        }
        
        Main.messageManager.broadcast(
                this.getMessageLevel("broadcast." + type + ".me")
                , String.format(this.getMessageFormat("broadcast." + type + ".me"), message, name)
        );
    }
    
    public void sendTell(CommandSender sender, Player target, String message) {
        String type;
        String name = null;
        if (sender instanceof Player) {
            type = "player";
            name = ((Player) sender).getDisplayName();
        } else {
            type = "server";
        }
        
        Main.messageManager.send(
                target
                , this.getMessageLevel("send." + type + ".tell")
                , String.format(this.getMessageFormat("send." + type + ".tell"), message, name)
        );
    }
    
    public String formatBroadcast(MessageLevel level, String message) {
        return this.formatBroadcast(level, message, true);
    }
    
    public String formatBroadcast(MessageLevel level, String message, boolean isTimestamped) {
        return String.format(this.getMessageFormat("broadcast")
                , message
                , level.getBroadcastColor().toString()
                , (isTimestamped ? this.getTimestamp() : "")
        );
    }
    
    public String formatBroadcastLog(String message) {
        return String.format(this.getConfiguration().getString("broadcast.log"), message);
    }
    
    public String formatSend(MessageLevel level, String message) {
        return this.formatSend(level, message, true);
    }
    
    public String formatSend(MessageLevel level, String message, boolean isTimestamped) {
        return String.format(this.getMessageFormat("send")
                , message
                , level.getSendColor().toString()
                , (isTimestamped ? this.getTimestamp() : "")
        );
    }
    
    public String formatSendLog(String message, Player player) {
        return String.format(this.getConfiguration().getString("send.log"), message, player.getName());
    }
    
    public String formatLog(MessageLevel level, String message, Plugin plugin) {
        return String.format(this.getMessageFormat("log"), message, plugin.getDescription().getName());
    }
    
    private String getTimestamp() {
        return Main.parseChatColor(this.getConfiguration().getString("timestamp.color"))
            + new java.text.SimpleDateFormat(this.getMessageFormat("timestamp"))
                .format(new java.util.GregorianCalendar().getTime());
    }
    
    public MessageLevel getMessageLevel(String path) {
        return MessageLevel.parse(this.getConfiguration().getString(path + ".level"));
    }
    
    public String getMessageFormat(String path) {
        return this.getConfiguration().getString(path + ".format");
    }
    
    public static ChatColor getMessageColor(String level, String type) {
        return Main.parseChatColor(Main.that.getConfiguration().getString("colors." + level + "." + type));
    }
    
    //TODO: parse start and end color tags  
    public static String colorize(String message) {
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
    
    public static ChatColor parseChatColor(String name) {
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