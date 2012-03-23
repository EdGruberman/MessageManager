package edgruberman.bukkit.messagemanager.commands;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.commands.util.Action;
import edgruberman.bukkit.messagemanager.commands.util.Context;
import edgruberman.bukkit.messagemanager.commands.util.Handler;

final class MessageManagerReload extends Action {

    MessageManagerReload(final Handler handler) {
        super(handler, "reload");
    }

    @Override
    public boolean perform(final Context context) {
        ((Main) context.handler.command.getPlugin()).reload();
        Main.messageManager.send(context.sender, "Configuration reloaded", MessageLevel.STATUS, false);
        return true;
    }

}
