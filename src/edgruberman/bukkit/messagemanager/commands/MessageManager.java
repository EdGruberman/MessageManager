package edgruberman.bukkit.messagemanager.commands;

import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.commands.util.Handler;

public final class MessageManager extends Handler {

    public MessageManager(final JavaPlugin plugin) {
        super(plugin, "messagemanager");
        new MessageManagerReload(this);
    }

}
