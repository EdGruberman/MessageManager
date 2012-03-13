package edgruberman.bukkit.messagemanager.commands;

import edgruberman.bukkit.messagemanager.commands.util.Action;
import edgruberman.bukkit.messagemanager.commands.util.Context;
import edgruberman.bukkit.messagemanager.commands.util.Handler;

public class TimestampTimeZone extends Action {

    TimestampTimeZone(final Handler handler) {
        super(handler, "timezone");
        new TimestampTimeZoneGet(this);
        new TimestampTimeZoneSet(this);

        TimeZone.emulate = this;
    }

    @Override
    public boolean perform(final Context context) {
        // Example: /<command> timezone([ get][ <Player>]| set[ <Player>] <TimeZone>)
        return this.children.get(0).perform(context);
    }

}
