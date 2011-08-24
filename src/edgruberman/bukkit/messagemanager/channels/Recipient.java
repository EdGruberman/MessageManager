package edgruberman.bukkit.messagemanager.channels;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import edgruberman.bukkit.messagemanager.Main;

public final class Recipient {
    
    private static Map<String, Recipient> instances = new HashMap<String, Recipient>();
    
    private Player player;
    private boolean useTimestamp;
    private Timestamp timestamp;
    
    private Recipient(final Player player) {
        if (Recipient.exists(player))
            throw new IllegalArgumentException("Instance already exists for " + player.getName());
        
        this.player = player;
        this.useTimestamp = Main.useTimestampFor(player.getName());
        this.timestamp = Main.timestampFor(player.getName());
        
        Recipient.instances.put(player.getName(), this);
    }
    
    public static Recipient getInstance(final Player player) {
        if (!Recipient.instances.containsKey(player.getName()))
            new Recipient(player);
        
        return Recipient.instances.get(player.getName());
    }
    
    public static void disposeInstance(Recipient recipient) {
        Recipient.instances.remove(recipient.player.getName());
    }
    
    public static boolean exists(Player player) {
        return Recipient.instances.containsValue(player.getName());
    }
    
    public void send(final String message) {
        this.send(message, this.useTimestamp);
    }
    
    public void send(final String message, final boolean isTimestampRequested) {
        String msg = message;
        
        if (isTimestampRequested && this.useTimestamp)
            msg = this.timestamp.format(message);
        
        this.player.sendMessage(msg);
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public void setPlayer(final Player player) {
        this.player = player;
    }
    
    public Timestamp getTimestamp() {
        return this.timestamp;
    }
    
    public void setTimestamp(final Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean getUseTimestamp() {
        return this.useTimestamp;
    }
    
    public void setUseTimestamp(final boolean useTimestamp) {
        this.useTimestamp = useTimestamp;
    }
}