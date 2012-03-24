package edgruberman.bukkit.messagemanager;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

public class MessageColor {

    private static final char COLOR_MARKER = '&';
    private static final Pattern COLOR_CODE_ALTERNATE = Pattern.compile(MessageColor.COLOR_MARKER + "(" + MessageColor.COLOR_MARKER + "|[0-9A-FK-OR]|_)", Pattern.CASE_INSENSITIVE);
    private static final Pattern COLOR_CODE_DUPLICATES = Pattern.compile("(" + ChatColor.COLOR_CHAR + "[0-9A-FK-OR])\\1", Pattern.CASE_INSENSITIVE);

    /**
     * Convert color codes to characters Minecraft will convert to color.
     * No base color defined. (Minecraft client will use white by default.)
     *
     * @param message text to convert any existing color codes for
     * @return text converted to Minecraft recognized coloring
     */
    public static String colorize(final String message) {
        return MessageColor.colorize((ChatColor) null, message);
    }

    /**
     * Convert color codes to characters Minecraft will convert to color.
     *
     * @param base starting color to use for message
     * @param message text to convert an existing color codes for
     * @return text converted to Minecraft recognized coloring
     */
    public static String colorize(final ChatColor base, final String message) {
        final Stack<String> colors = new Stack<String>();
        colors.push((base != null ? base.toString() : ""));

        final StringBuffer colorized = new StringBuffer();
        if (base != null) colorized.append(base.toString());

        final Matcher m = MessageColor.COLOR_CODE_ALTERNATE.matcher(message);
        while (m.find()) {
            // Replace escaped ampersand with single ampersand
            if (m.group(1).equals("&")) {
                m.appendReplacement(colorized, "&");
                continue;
            }

            // Replace closure marker with previous color on stack
            if (m.group(1).equals("_")) {
                if (colors.size() > 1) colors.pop();
                m.appendReplacement(colorized, colors.peek());
                continue;
            }

            // Replace hex code with color
            final ChatColor color = ChatColor.getByChar(m.group(1).toLowerCase());
            if (color == null) continue;

            colors.push(color.toString());
            m.appendReplacement(colorized, colors.peek());
        }
        m.appendTail(colorized);

        return MessageColor.COLOR_CODE_DUPLICATES.matcher(colorized).replaceAll("$1");
    }

    /**
     * Strips the given message of all MessageManager color codes.
     *
     * @param input string to strip of color
     * @return copy of the input string, without any color codes
     */
    public static String stripColorCodes(final String input) {
        if (input == null) return null;

        return MessageColor.COLOR_CODE_ALTERNATE.matcher(input).replaceAll("");
    }

}
