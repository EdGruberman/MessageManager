package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.Permission;

public final class TimeZone extends Command implements org.bukkit.command.CommandExecutor {
    
    public TimeZone(final JavaPlugin plugin) {
        super(plugin, "timezone", Permission.TIMESTAMP_TIMEZONE_GET);
        this.setExecutorOf(this);
    }
    
    @Override
    public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command
            , final String label, final String[] args) {
        Context context = super.parse(this, sender, command, label, args);
        
        if (!(context.sender instanceof Player)) return false;

        Player player = (Player) context.sender;
        
        // Equivalent: /<command> <Player> timezone (get|set <TimeZone>)
        String timezone = null;
        if (context.arguments.size() > 0) timezone = context.arguments.get(0);
        String[] equivalent = {player.getName(), "timezone", (timezone == null ? "get" : "set"), (timezone == null ? "" : timezone)};
        return context.owner.plugin.getCommand("timestamp").execute(sender, label, equivalent);
    }
}