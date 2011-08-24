package edgruberman.bukkit.messagemanager.channels;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.Settings;


public final class LogChannel extends Channel {
    
    private Logger logger;
    private String format = "%1$s";
    
    LogChannel(final Plugin plugin) {
        super(Channel.Type.LOG, plugin.getDescription().getName());
        this.logger = Logger.getLogger(plugin.getClass().getCanonicalName());
    }
    
    @Override
    public void send(final String message, final boolean isTimestamped) {
        this.send(message, isTimestamped, Settings.DEFAULT_MESSAGE_LEVEL);
    }
    
    public void send(final String message, final boolean isTimestamped, final Level level) {
        this.send(message, isTimestamped, level, null);
    }
    
    public void send(final String message, final boolean isTimestamped, final Level level, final Throwable e) {
        this.logger.log(level, this.format(message), e);
        super.send(message, isTimestamped);
    }
    
    public void setLevel(final Level level) {
        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it.
        for (Handler h : this.logger.getParent().getHandlers()) {
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);
        }
        
        this.logger.setLevel(level);
    }
    
    public Level getLevel() {
        return this.logger.getLevel();
    }
    
    public void setFormat(final String format) {
        this.format = format;
    }
    
    public String getFormat() {
        return this.format;
    }
    
    private String format(final String message) {
        return String.format(this.format
                , message
                , this.name
        );
    }
    
    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (other == null) return false;
        
        if (!(other instanceof LogChannel)) return false;
        LogChannel that = (LogChannel) other;
        if (!that.canEqual(this)) return false;
        if (!super.equals(that)) return false;
        
        return true;
    }
    
    @Override
    public boolean canEqual(Object other) {
        return (other instanceof LogChannel);
    }
}