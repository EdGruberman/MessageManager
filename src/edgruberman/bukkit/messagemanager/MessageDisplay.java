package edgruberman.bukkit.messagemanager;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

/**
 * Text color and formatting.
 */
public class MessageDisplay {

    private static final String MARKER = "&";
    private static final String CLOSURE = "_";
    private static final String RESET = "*";
    private static final String VALUES = "[0-9A-FK-ORa-fk-or]";
    private static final Pattern CODE = Pattern.compile(MessageDisplay.MARKER + "(" + MessageDisplay.MARKER + "|" + MessageDisplay.CLOSURE + "|\\" + MessageDisplay.RESET + "|" + MessageDisplay.VALUES + ")");
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

        final Stack<ChatColor> state = new Stack<ChatColor>();
        final StringBuffer converted = new StringBuffer();

        state.addAll(base);
        for (final ChatColor code : base) converted.append(code.toString());

        final Matcher m = MessageDisplay.CODE.matcher(message);
        while (m.find()) {
            // Replace escaped marker with single marker
            if (m.group(1).equals(MessageDisplay.MARKER)) {
                m.appendReplacement(converted, String.valueOf(MessageDisplay.MARKER));
                continue;
            }

            // Replace closure code with previous state
            if (m.group(1).equals(MessageDisplay.CLOSURE)) {
                m.appendReplacement(converted, "");
                if (state.size() == 0) continue;

                final ChatColor removed = state.pop();

                // Apply reset if format was removed
                if (removed.isFormat()) converted.append(ChatColor.RESET.toString());

                // Apply last color
                for (int i = state.size() - 1; i >= 0; i--) {
                    final ChatColor code = state.get(i);
                    if (code.isColor()) {
                        converted.append(code.toString());
                        break;
                    }
                }

                // Re-apply all previous formats if format was removed (need to be after color to take effect)
                if (removed.isFormat())
                    for (final ChatColor code : state)
                        if (code.isFormat()) converted.append(code.toString());

                continue;
            }

            // Replace reset code with reset and base display
            if (m.group(1).equals(MessageDisplay.RESET)) {
                m.appendReplacement(converted, ChatColor.RESET.toString());
                if (base.size() != 0) for (final ChatColor code : base) converted.append(code.toString());
                continue;
            }

            // Replace message code with Minecraft code
            final ChatColor code = ChatColor.getByChar(m.group(1).toLowerCase());
            state.push(code);
            m.appendReplacement(converted, state.peek().toString());
        }
        m.appendTail(converted);

        // TODO Remove codes that won't change display due to another code overriding it before more text

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
