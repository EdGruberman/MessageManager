package edgruberman.bukkit.messagemanager;

import edgruberman.bukkit.messagemanager.channels.Channel;

public class Main extends org.bukkit.plugin.java.JavaPlugin {
    
    static ConfigurationFile configurationFile;
    static MessageManager messageManager;
    
    public void onLoad() {
        Main.configurationFile = new ConfigurationFile(this);
        Main.configurationFile.load();
        
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
    }
    
    public void onEnable() {
        new PlayerListener(this);
        new WorldListener(this);
        
        Channel.getInstance(this.getServer());
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
    }
}