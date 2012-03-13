package edgruberman.bukkit.messagemanager.commands;

import edgruberman.bukkit.messagemanager.commands.util.Action;
import edgruberman.bukkit.messagemanager.commands.util.Context;
import edgruberman.bukkit.messagemanager.commands.util.Handler;

class TimestampFormat extends Action {

    TimestampFormat(final Handler handler) {
        super(handler, "format");
        new TimestampFormatGet(this);
        new TimestampFormatSet(this);
    }

    @Override
    public boolean perform(final Context context) {
        // Example: /<command> format([ get][ <Player>]| set[ <Player>] <Format>)
        return this.children.get(0).perform(context);
    }

}
