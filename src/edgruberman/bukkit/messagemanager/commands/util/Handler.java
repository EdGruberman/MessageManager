package edgruberman.bukkit.messagemanager.commands.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;

/**
 * Command execution manager
 */
public class Handler implements CommandExecutor  {

    public PluginCommand command;
    public String permission;
    public List<Action> actions = new ArrayList<Action>();

    /**
     * Create a command executor with a default plugin.command permission.
     *
     * @param plugin command owner
     * @param label command name
     */
    public Handler(final JavaPlugin plugin, final String label) {
        this.setExecutorOf(plugin, label);
        this.permission = this.command.getPlugin().getDescription().getName().toLowerCase() + "." + this.command.getLabel();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        Main.messageManager.log(sender.getName() + " issued command: " + label + " " + Arrays.toString(args), MessageLevel.FINE);
        Context context = new Context(this, sender, label, args);

        if (!context.action.isAllowed(context)) return true;

        if (context.action.perform(context)) return true;

        // Send usage information on error
        for (String line : context.action.handler.command.getUsage().replace("<command>", context.label).split("\n"))
            Main.messageManager.respond(context.sender, line, MessageLevel.NOTICE, false);

        return true; // Always tell Bukkit this is successful as usage message errors are handled internally
    }

    /**
     * Registers executor for a command.
     *
     * @param label command label to register
     */
    private void setExecutorOf(final JavaPlugin plugin, final String label) {
        this.command = plugin.getCommand(label);
        if (this.command == null) {
            Main.messageManager.log("Unable to register " + label + " command.", MessageLevel.WARNING);
            return;
        }

        this.command.setExecutor(this);
    }

}