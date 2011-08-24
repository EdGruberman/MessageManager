package edgruberman.bukkit.messagemanager.commands;

import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.Permission;

public final class Timestamp extends Command implements org.bukkit.command.CommandExecutor {
    
    public Timestamp(final JavaPlugin plugin) {
        super(plugin, "timestamp", Permission.TIMESTAMP);
        this.setExecutorOf(this);
        
        this.registerAction(new TimestampGet(this), true);
        this.registerAction(new TimestampPattern(this));
        this.registerAction(new TimestampFormat(this));
        this.registerAction(new TimestampTimeZone(this));
        this.registerAction(new TimestampReset(this));
        this.registerAction(new TimestampOn(this));
        this.registerAction(new TimestampOff(this));
    }
    
    @Override
    public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command
            , final String label, final String[] args) {
        Context context = super.parse(this, sender, command, label, args);
        
        if (!this.isAllowed(context.sender)) {
            Main.messageManager.respond(context.sender, "You are not allowed to use the " + context.label + " command.", MessageLevel.RIGHTS, false);
            return true;
        }
        
        if (context.action == null) {
            Main.messageManager.respond(context.sender, "Unrecognized action for the " + context.label + " command.", MessageLevel.WARNING, false);
            return true;
        }
        
        if (!context.action.isAllowed(context.sender)) {
            Main.messageManager.respond(context.sender, "You are not allowed to use the " + context.action.name + " action of the " + context.label + " command.", MessageLevel.RIGHTS, false);
            return true;
        }
        
        context.action.execute(context);
        
        return true;
    }
    
    static String parsePlayer(final Context context) {
        String name = (context.matches.size() >= 1 ? context.matches.get(0) : null);
        if (name != null && name.equals("")) name = null;
        
        if (context.sender instanceof Player && name == null)
            name = ((Player) context.sender).getName();
        
        return name;
    }
    
    static String parseOperation(final Context context) {
        String operation = "get";
        if (context.matches.size() < 2) return operation;
        
        operation = context.matches.get(1);
        if (operation == null || operation.equals("?")) {
            operation = "get";
        } else if (operation.equals("=")) {
            operation = "set";
        } else {
            operation = operation.toLowerCase(Locale.ENGLISH);
        }
        
        return operation;
    }
    
    /**
     * Returns player only if it is a full and case insensitive name match.
     *
     * @param name name of player
     * @return player that matches name
     */
    static Player getExactPlayer(String name) {
        Player player = Bukkit.getServer().getPlayer(name);
        if (player == null) return null;
        
        if (!player.getName().equalsIgnoreCase(name)) return null;
        
        return player;
    }
}