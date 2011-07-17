package edgruberman.bukkit.messagemanager.channels;

import java.util.TimeZone;

import org.bukkit.ChatColor;

public final class Timestamp {
    
    public String pattern;
    public String format;
    public TimeZone timezone;
    public ChatColor color;
    
    public Timestamp() {
        this(null, null, null, null);
    }
    
    public Timestamp(final String pattern, final String format, final TimeZone timezone, final ChatColor color) {
        this.pattern = pattern;
        this.format = format;
        this.timezone = timezone;
        this.color = color;
    }
    
    public String current() {
        return new java.text.SimpleDateFormat(this.pattern)
                .format(new java.util.GregorianCalendar().getTime());
    }
    
    public String format(final String message) {
        return String.format(this.format, message, (this.color != null ? this.color.toString() : "") + this.current());
    }
}