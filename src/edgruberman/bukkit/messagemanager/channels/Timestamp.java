package edgruberman.bukkit.messagemanager.channels;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import edgruberman.bukkit.messagemanager.MessageManager;

public final class Timestamp {
    
    private String pattern;
    private String format;
    private TimeZone timezone;
    
    private SimpleDateFormat sdf = new SimpleDateFormat();
    private GregorianCalendar calendar = new GregorianCalendar();
    private String formatColorized;
    
    public Timestamp(final String pattern, final String format, final TimeZone timezone) {
        this.setFormat(format);
        this.setPattern(pattern);
        this.setTimeZone(timezone);
    }
    
    public String current() {
        return this.sdf.format(this.calendar.getTime());
    }
    
    /**
     * Format message to include timestamp for final display to player.
     * 
     * @param message text that includes color codes already (if no color
     * codes included, timestamp colors could color message)
     * @return formatted message including color and timestamp
     */
    public String format(final String message) {
        return String.format(this.formatColorized, message, this.current());
    }
    
    public String getPattern() {
        return this.pattern;
    }
    
    public void setPattern(final String pattern) {
        this.sdf.applyPattern(pattern);
        this.pattern = pattern;
    }
    
    public String getFormat() {
        return this.format;
    }
    
    public void setFormat(final String format) {
        this.formatColorized = MessageManager.colorize(format);
        this.format = format;
    }
    
    public TimeZone getTimeZone() {
        return this.timezone;
    }
    
    public void setTimeZone(final TimeZone timezone) {
        this.sdf.setTimeZone(timezone);
        this.timezone = timezone;
    }
}