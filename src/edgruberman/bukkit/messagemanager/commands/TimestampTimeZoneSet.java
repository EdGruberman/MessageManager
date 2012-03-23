package edgruberman.bukkit.messagemanager.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.OfflinePlayer;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.channels.Recipient;
import edgruberman.bukkit.messagemanager.commands.util.Action;
import edgruberman.bukkit.messagemanager.commands.util.Context;
import edgruberman.bukkit.messagemanager.commands.util.Parser;

public class TimestampTimeZoneSet extends Action {

    private static final int MAXIMUM_DISPLAY = 10;

    TimestampTimeZoneSet(final Action parent) {
        super(parent, "set");
    }

    @Override
    public boolean perform(final Context context) {
        // Example: /<command> timezone set[ <Player>]<TimeZone>
        if (context.arguments.size() < 3) return false;

        OfflinePlayer target = Parser.parsePlayer(context, 2);
        if (target == null && context.sender instanceof OfflinePlayer) target = (OfflinePlayer) context.sender;
        if (target == null) {
            Main.messageManager.send(context.sender, "Unable to determine player", MessageLevel.SEVERE, false);
            return false;
        }

        // Parse target player name
        String targetName = target.getName();
        if (target.getPlayer() != null) targetName = target.getPlayer().getName();

        // Verify requester has permission for player
        if (!Timestamp.isAllowed(context.sender, this.permission, targetName)) {
            Main.messageManager.send(context.sender, "You are not allowed to use the " + this.getNamePath() + " action of the " + context.label + " command for " + target.getName(), MessageLevel.RIGHTS, false);
            return true;
        }

        final String value = (context.arguments.size() >= 4 ? context.arguments.get(3) : context.arguments.get(2));
        if (value == null) {
            Main.messageManager.send(context.sender, "Unable to determine timezone", MessageLevel.SEVERE, false);
            return false;
        }

        // Create empty list to work with.
        final List<String> available = new ArrayList<String>();

        if (TimestampTimeZoneSet.isDouble(value)) {
            // Offset filter in hours.
            final int offset = (int) (Double.parseDouble(value) * 60 * 60 * 1000);
            available.clear();
            available.addAll(Arrays.asList(TimeZone.getAvailableIDs(offset)));

        } else if (value.matches("\\d{1,2}:\\d{1,2}")) {
            // Offset filter in HH:mm format.
            final Matcher m = Pattern.compile("(\\d{1,2}):(\\d{1,2})").matcher(value);
            m.find();
            final int offset = ((Integer.parseInt(m.group(1)) * 60) + Integer.parseInt(m.group(2))) * 60 * 1000;
            available.clear();
            available.addAll(Arrays.asList(TimeZone.getAvailableIDs(offset)));

        } else {
            // ID filter.

            // Populate initial query with all possible IDs.
            if (available.size() == 0)
                available.addAll(Arrays.asList(TimeZone.getAvailableIDs()));

            // Reduce list with supplied filter.
            final Iterator<String> i = available.iterator();
            while (i.hasNext()) {
                final String ID = i.next();
                if (ID.equalsIgnoreCase(value)) {
                    available.clear();
                    available.add(ID);
                    break;

                } else if (!ID.toLowerCase(Locale.ENGLISH).contains(value.toLowerCase(Locale.ENGLISH))) {
                    i.remove();

                }
            }

        }

        // If TimeZone is still ambiguous, show possibilities, up to maximum count, and exit.
        if (available.size() == 0) {
            Main.messageManager.send(context.sender, "No TimeZones match", MessageLevel.SEVERE, false);
            return true;

        } else if (available.size() > 1) {
            for (int i = 0; (i <= (available.size() - 1)) && (i <= (TimestampTimeZoneSet.MAXIMUM_DISPLAY - 1)); i++)
                Main.messageManager.send(context.sender
                        , available.get(i) + " (" + TimeZone.getTimeZone(available.get(i)).getDisplayName() + ")"
                        , MessageLevel.NOTICE
                        , false
                );

            if (available.size() > TimestampTimeZoneSet.MAXIMUM_DISPLAY)
                Main.messageManager.send(context.sender
                        , "Only first " + TimestampTimeZoneSet.MAXIMUM_DISPLAY + " of " + available.size() + " TimeZones displayed"
                        , MessageLevel.WARNING, false
                );
            return true;

        }

        final TimeZone timezone = TimeZone.getTimeZone(available.get(0));

        // Update timestamp and save to file
        final Recipient recipient = Timestamp.getRecipient(target);
        recipient.getTimestamp().setTimeZone(timezone);
        recipient.save();

        Main.messageManager.send(context.sender, "Timestamp time zone for " + targetName + ": " + TimestampTimeZoneGet.message(recipient.load()), MessageLevel.STATUS, false);
        return true;
    }

    private static boolean isDouble(final String s) {
        try {
            Double.parseDouble(s);
            return true;

        } catch(final Exception e) {
            return false;

        }
    }

}
