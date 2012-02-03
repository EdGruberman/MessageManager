package edgruberman.bukkit.messagemanager.commands.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 * Static utility class to consolidate/organize common code for interacting with command line parameters.
 */
public class Parser {

    /**
     * Find matching player. Exact player name required if no trailing
     * asterisk. If trailing asterisk, then first player name found that starts
     * with the query will be returned.
     *
     * @param context command execution context
     * @param position position of player name query in command arguments (0 based, not including leading command label)
     * @return player with name matching query; null if no player has ever connected that matches
     */
    public static OfflinePlayer parsePlayer(final Context context, final int position) {
        if (context.arguments.size() < (position + 1)) return null;

        String query = context.arguments.get(position).toLowerCase();
        boolean isExact = !query.endsWith("*"); // Trailing asterisk indicates to use first player name found beginning with characters before asterisk
        if (!isExact) query = query.substring(0, query.length() - 1); // Strip trailing asterisk

        for (OfflinePlayer op : Bukkit.getServer().getOfflinePlayers()) {
            String name = (op.getPlayer() != null ? op.getPlayer().getName() : op.getName()).toLowerCase();
            if (isExact) {
                if (name.equals(query)) return op;

                continue;
            }

            // Return first matching player name starting with query characters
            if (name.startsWith(query)) return op;
        }

        return null;
    }

    /**
     * Parse a list of long numbers from the command line in comma delimited form.
     *
     * @param context command execution context
     * @param position position in command arguments (0 based, not including leading command label)
     * @return list of each number that was delimited by a comma; null if argument does not exist, or no numbers specified
     */
    public static List<Long> parseLongList(final Context context, final int position) {
        if (context.arguments.size() <= position) return null;

        List<Long> values = new ArrayList<Long>();
        for (String s : context.arguments.get(position).split(","))
            try {
                values.add(Long.parseLong(s));
            } catch (Exception e) {
                continue;
            }
        if (values.size() == 0) return null;

        return values;
    }

    /**
     * Concatenate a collection with a space between each entry.
     *
     * @param col entries to concatenate
     * @return entries concatenated; empty string if no entries
     */
    public static String join(final Collection<? extends String> col) {
        return Parser.join(col, " ");
    }

    /**
     * Concatenate a collection with a delimiter.
     *
     * @param col entries to concatenate
     * @param delim placed between each entry
     * @return entries concatenated; empty string if no entries
     */
    public static String join(final Collection<? extends String> col, final String delim) {
        if (col == null || col.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (String s : col) sb.append(s + delim);
        sb.delete(sb.length() - delim.length(), sb.length());

        return sb.toString();
    }

}