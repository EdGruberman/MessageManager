package edgruberman.bukkit.messagemanager.channels;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import edgruberman.bukkit.messagemanager.MessageManager;

public final class Timestamp {
    
    private String format;
    private String formatColorized;
    private SimpleDateFormat sdf = new SimpleDateFormat();
    
    public Timestamp(final String pattern, final String format, final TimeZone timezone) {
        this.setFormat(format);
        this.setPattern(pattern);
        this.setTimeZone(timezone);
    }
    
    /**
     * Format message to include timestamp for final display to player.
     * 
     * @param message text that includes color codes already (if no color
     * codes included, timestamp colors could color message)
     * @return formatted message including color and timestamp
     */
    public String format(final String message) {
        return String.format(this.formatColorized, message, this.sdf.format(new Date()));
    }
    
    /**
     * Recognizable string used with SimpleDateFormat for date/time.
     * 
     * @return pattern used to format date/time
     */
    public String getPattern() {
        return this.sdf.toPattern();
    }
    
    /**
     * Configure SimpleDateFormat patter for date/time.
     * 
     * @param pattern the new pattern to use for date/time
     */
    public void setPattern(final String pattern) {
        this.sdf.applyPattern(pattern);
    }
    
    /**
     * The current format string used when formatting messages with this
     * timestamp. (This can contain non-converted color codes.)
     * 
     * @return the format currently used without converting color codes
     */
    public String getFormat() {
        return this.format;
    }
    
    /**
     * Configure current format string to use when formatting messages with
     * this timestamp. (If color codes are specified, they will be converted
     * for formatting messages with this timestamp.)
     * 
     * @param format
     */
    public void setFormat(final String format) {
        this.formatColorized = MessageManager.colorize(format);
        this.format = format;
    }
    
    /**
     * Current time zone used for SimpleDateFormat.
     * 
     * @return current time zone
     */
    public TimeZone getTimeZone() {
        return this.sdf.getTimeZone();
    }
    
    /**
     * Configure time zone to use with SimpleDateFormat.
     * 
     * @param timezone the new time zone to use
     */
    public void setTimeZone(final TimeZone timezone) {
        this.sdf.setTimeZone(timezone);
    }
}