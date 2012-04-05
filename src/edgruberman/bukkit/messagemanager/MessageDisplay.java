package edgruberman.bukkit.messagemanager;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

/**
 * Text color and formatting.
 */
public class MessageDisplay {

    private static final String MARKER = "&";
    private static final String BASE = "_";
    private static final String VALUES = "[0-9A-FK-ORa-fk-or]";
    private static final Pattern CODE = Pattern.compile(MessageDisplay.MARKER + "(" + MessageDisplay.MARKER + "|" + MessageDisplay.BASE + "|" + MessageDisplay.VALUES + ")");
    private static final Pattern DUPLICATES = Pattern.compile("(" + ChatColor.COLOR_CHAR + MessageDisplay.VALUES + ")\\1", Pattern.CASE_INSENSITIVE);

    /**
     * Convert message codes to Minecraft color and formatting codes.
     * No lowest color or formatting defined. (Minecraft client will use
     * plain white by default.)
     *
     * @param message text to convert any existing codes for
     * @return text converted message
     */
    public static String translate(final String message) {
        return MessageDisplay.translate(new ArrayList<ChatColor>(), message);
    }

    /**
     * Convert message codes to Minecraft color and formatting codes.
     *
     * @param base initial color/format code
     * @param message text to convert any existing codes for
     * @return text converted message
     */
    public static String translate(final ChatColor base, final String message) {
        if (base == null) throw new IllegalArgumentException("translation base can not be null");

        return MessageDisplay.translate(new ArrayList<ChatColor>(Arrays.asList(base)), message);
    }

    /**
     * Convert message codes to Minecraft color and formatting codes.
     *
     * @param base initial color and formatting to apply
     * @param message text to convert any existing codes for
     * @return converted message
     */
    public static String translate(final List<ChatColor> base, final String message) {
        if (base == null) throw new IllegalArgumentException("translation base can not be null");

        final StringBuffer converted = new StringBuffer();
        for (final ChatColor code : base) converted.append(code.toString());

        final Matcher m = MessageDisplay.CODE.matcher(message);
        while (m.find()) {
            // Replace escaped marker with single marker
            if (m.group(1).equals(MessageDisplay.MARKER)) {
                m.appendReplacement(converted, m.group(1));
                continue;
            }

            // Replace base code with reset and base
            if (m.group(1).equals(MessageDisplay.BASE)) {
                m.appendReplacement(converted, ChatColor.RESET.toString());
                for (final ChatColor code : base) converted.append(code.toString());
                continue;
            }

            // Replace message code with Minecraft code
            final ChatColor code = ChatColor.getByChar(m.group(1).toLowerCase());
            m.appendReplacement(converted, code.toString());
        }
        m.appendTail(converted);

        // Remove duplicate codes
        return MessageDisplay.DUPLICATES.matcher(converted).replaceAll("$1");
    }

    /**
     * Remove all message codes.
     *
     * @param message original text
     * @return text without codes
     */
    public static String strip(final String message) {
        if (message == null) return null;

        return MessageDisplay.CODE.matcher(message).replaceAll("");
    }

}
