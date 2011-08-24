package edgruberman.bukkit.messagemanager.commands;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.Permission;

class MessageManagerReload extends Action {
    
    MessageManagerReload(final Command owner) {
        super(owner, "reload", Permission.MESSAGEMANAGER_RELOAD);
    }
    
    @Override
    void execute(final Context context) {
        Main main = (Main) this.command.plugin;
        
        main.loadConfiguration();
        // reload all mm instances
        
        Main.messageManager.respond(context.sender, "Configuration reloaded.", MessageLevel.STATUS, false);
    }
}