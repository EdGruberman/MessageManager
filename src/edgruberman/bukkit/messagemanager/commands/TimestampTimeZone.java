package edgruberman.bukkit.messagemanager.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.Permission;
import edgruberman.bukkit.messagemanager.channels.Recipient;

public class TimestampTimeZone extends Action {

    private static final int MAXIMUM_DISPLAY = 10;
    
    TimestampTimeZone(final Command owner) {
        // Suggested: /timestamp[ <Player>] timezone (get|set <TimeZone>)
        super(owner, "timezone", Permission.TIMESTAMP_TIMEZONE_GET, "^(?:(\\S*) )?timezone ?((?:get)|\\?|(?:set)|=)? ?(.+)?$");
    }
    
    @Override
    void execute(final Context context) {
        String target = Timestamp.parsePlayer(context);
        if (target == null) {
            Main.messageManager.respond(context.sender, "Unable to determine player.", MessageLevel.SEVERE, false);
            return;
        }
        
        String operation = Timestamp.parseOperation(context);
        if (!(context.sender instanceof Player) || !((Player) context.sender).getName().equals(target)) {
            Permission permission = Permission.TIMESTAMP_TIMEZONE_GET_PLAYER;
            if (operation.equals("set")) permission = Permission.TIMESTAMP_TIMEZONE_SET_PLAYER;
            
            if (!context.sender.hasPermission(permission.toString("*"))
                    && !context.sender.hasPermission(permission.toString(target))) {
                Main.messageManager.respond(context.sender, "You are not allowed to use the " + context.label + " command's " + this.name + " action's " + operation + " operation for " + target, MessageLevel.RIGHTS, false);
                return;
            }
        }
        
        edgruberman.bukkit.messagemanager.channels.Timestamp timestamp;
        boolean useTimestamp;
        if (operation.equals("set")) {
            String value = (context.matches.size() >= 3 ? context.matches.get(2) : null);
            if (value == null) {
                Main.messageManager.respond(context.sender, "Value must be specified.", MessageLevel.SEVERE, false);
                return;
            }
            
            Player player = Timestamp.getExactPlayer(target);
            if (player != null) {
                // Use recipient if online.
                timestamp = Recipient.getInstance(player).getTimestamp();
                useTimestamp = Recipient.getInstance(player).getUseTimestamp();
                
                // As long as we have it, get the proper casing for the player name.
                target = player.getName();
            } else {
                // Otherwise use configuration file.
                timestamp = Main.timestampFor(target);
                useTimestamp = Main.useTimestampFor(target);
            }
            
            // Create empty list to work with.
            List<String> available = new ArrayList<String>();
            
            if (TimestampTimeZone.isDouble(value)) {
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
                Main.messageManager.respond(context.sender, "No TimeZones match.", MessageLevel.SEVERE, false);
                return;
                
            } else if (available.size() > 1) {
                for (int i = 0; (i <= (available.size() - 1)) && (i <= (TimestampTimeZone.MAXIMUM_DISPLAY - 1)); i++)
                    Main.messageManager.respond(context.sender
                            , available.get(i) + " (" + TimeZone.getTimeZone(available.get(i)).getDisplayName() + ")"
                            , MessageLevel.NOTICE
                            , false
                    );
                
                if (available.size() > TimestampTimeZone.MAXIMUM_DISPLAY)
                    Main.messageManager.respond(context.sender
                            , "Only first " + TimestampTimeZone.MAXIMUM_DISPLAY + " of "
                                + available.size() + " TimeZones displayed."
                            , MessageLevel.WARNING, false
                    );
                return;
                
            }
            
            TimeZone timezone = TimeZone.getTimeZone(available.get(0));
            
            // Update timestamp and save to file.
            timestamp.setTimeZone(timezone);
            Main.saveRecipient(target, timestamp, useTimestamp);
        }
        
        // Respond with verification.
        timestamp = Main.timestampFor(target);
        Main.messageManager.respond(context.sender, target + "'s Timestamp " + TimestampTimeZone.message(timestamp), MessageLevel.STATUS, false);
    }
    
    static String message(final edgruberman.bukkit.messagemanager.channels.Timestamp timestamp) {
        return "TimeZone: " + timestamp.getTimeZone().getID()
            + " (" + timestamp.getTimeZone().getDisplayName() + ")";
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