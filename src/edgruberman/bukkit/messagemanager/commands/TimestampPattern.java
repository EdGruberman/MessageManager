package edgruberman.bukkit.messagemanager.commands;

import edgruberman.bukkit.messagemanager.commands.util.Action;
import edgruberman.bukkit.messagemanager.commands.util.Context;
import edgruberman.bukkit.messagemanager.commands.util.Handler;

final class TimestampPattern extends Action {

    TimestampPattern(final Handler handler) {
        super(handler, "pattern");
        new TimestampPatternGet(this);
        new TimestampPatternSet(this);
    }

    @Override
    public boolean matches(Context context) {
        return super.matchesBreadcrumb(context);
    }

    @Override
    public boolean perform(final Context context) {
        // Example: /<command> pattern([ get][ <Player>]| set[ <Player>] <Pattern>)
        return this.children.get(0).perform(context);
    }

}