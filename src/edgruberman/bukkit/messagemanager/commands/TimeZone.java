package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.commands.util.Action;
import edgruberman.bukkit.messagemanager.commands.util.Context;
import edgruberman.bukkit.messagemanager.commands.util.Handler;

public final class TimeZone extends Action {

    static Action emulate;

    public TimeZone(final JavaPlugin plugin) {
        super(new Handler(plugin, "timezone"), "timezone", TimeZone.emulate.permission);
    }

    @Override
    public boolean perform(final Context context) {
        context.arguments.add(0, "timezone");

        TimeZone.emulate.handler.command.execute(
                context.sender
                , TimeZone.emulate.handler.command.getLabel()
                , context.arguments.toArray(new String[context.arguments.size()])
        );

        return true;
    }

}