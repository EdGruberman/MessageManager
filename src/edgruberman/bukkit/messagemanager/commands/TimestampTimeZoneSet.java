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
    public boolean matches(Context context) {
        return super.matchesBreadcrumb(context);
    }

    @Override
    public boolean perform(final Context context) {
        // Example: /<command> timezone set[ <Player>]<TimeZone>
        if (context.arguments.size() < 3) return false;

        OfflinePlayer target = Parser.parsePlayer(context, 2);
        if (target == null && context.sender instanceof OfflinePlayer) target = (OfflinePlayer) context.sender;
        if (target == null) {
            Main.messageManager.respond(context.sender, "Unable to determine player", MessageLevel.SEVERE, false);
            return false;
        }

        // Parse target player name
        String targetName = target.getName();
        if (target.getPlayer() != null) targetName = target.getPlayer().getName();

        // Verify requester has permission for player
        if (!Timestamp.isAllowed(context.sender, this.permission, targetName)) {
            Main.messageManager.respond(context.sender, "You are not allowed to use the " + this.getNamePath() + " action of the " + context.label + " command for " + target.getName(), MessageLevel.RIGHTS, false);
            return true;
        }

        edgruberman.bukkit.messagemanager.channels.Timestamp timestamp;
        boolean useTimestamp;
        String value = (context.arguments.size() >= 4 ? context.arguments.get(3) : context.arguments.get(2));
        if (value == null) {
            Main.messageManager.respond(context.sender, "Unable to determine timezone", MessageLevel.SEVERE, false);
            return false;
        }

        if (target.getPlayer() != null) {
            // Use recipient if online.
            timestamp = Recipient.getInstance(target.getPlayer()).getTimestamp();
            useTimestamp = Recipient.getInstance(target.getPlayer()).getUseTimestamp();
        } else {
            // Otherwise use configuration file.
            timestamp = Main.timestampFor(targetName);
            useTimestamp = Main.useTimestampFor(targetName);
        }

        // Create empty list to work with.
        List<String> available = new ArrayList<String>();

        if (TimestampTimeZoneSet.isDouble(value)) {
            // Offset filter in hours.
            int offset = (int) (Double.parseDouble(value) * 60 * 60 * 1000);
            available.clear();
            available.addAll(Arrays.asList(TimeZone.getAvailableIDs(offset)));

        } else if (value.matches("\\d{1,2}:\\d{1,2}")) {
            // Offset filter in HH:mm format.
            Matcher m = Pattern.compile("(\\d{1,2}):(\\d{1,2})").matcher(value);
            m.find();
            int offset = ((Integer.parseInt(m.group(1)) * 60) + Integer.parseInt(m.group(2))) * 60 * 1000;
            available.clear();
            available.addAll(Arrays.asList(TimeZone.getAvailableIDs(offset)));

        } else {
            // ID filter.

            // Populate initial query with all possible IDs.
            if (available.size() == 0)
                available.addAll(Arrays.asList(TimeZone.getAvailableIDs()));

            // Reduce list with supplied filter.
            Iterator<String> i = available.iterator();
            while (i.hasNext()) {
                String ID = i.next();
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
            Main.messageManager.respond(context.sender, "No TimeZones match", MessageLevel.SEVERE, false);
            return true;

        } else if (available.size() > 1) {
            for (int i = 0; (i <= (available.size() - 1)) && (i <= (TimestampTimeZoneSet.MAXIMUM_DISPLAY - 1)); i++)
                Main.messageManager.respond(context.sender
                        , available.get(i) + " (" + TimeZone.getTimeZone(available.get(i)).getDisplayName() + ")"
                        , MessageLevel.NOTICE
                        , false
                );

            if (available.size() > TimestampTimeZoneSet.MAXIMUM_DISPLAY)
                Main.messageManager.respond(context.sender
                        , "Only first " + TimestampTimeZoneSet.MAXIMUM_DISPLAY + " of " + available.size() + " TimeZones displayed"
                        , MessageLevel.WARNING, false
                );
            return true;

        }

        TimeZone timezone = TimeZone.getTimeZone(available.get(0));

        // Update timestamp and save to file
        timestamp.setTimeZone(timezone);
        Main.saveRecipient(targetName, timestamp, useTimestamp);

        // Respond with verification.
        timestamp = Main.timestampFor(targetName);
        Main.messageManager.respond(context.sender, targetName + "'s Timestamp " + TimestampTimeZoneGet.message(timestamp), MessageLevel.STATUS, false);

        return true;
    }

    private static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;

        } catch(Exception e) {
            return false;

        }
    }

}