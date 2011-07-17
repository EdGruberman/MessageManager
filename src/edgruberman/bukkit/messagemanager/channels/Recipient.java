package edgruberman.bukkit.messagemanager.channels;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;


public final class Recipient {
    
    private static Map<String, Recipient> instances = new HashMap<String, Recipient>(); 
    
    private Player player;
    private Timestamp timestamp = null;
        
    private Recipient(final Player player) {
        if (Recipient.exists(player))
            throw new IllegalArgumentException("Instance already exists for " + player.getName());
        
        this.player = player;
        
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
        this.send(message, null);
    }
    
    public void send(final String message, final Boolean isTimestampRequested) {
        String msg = message;
        boolean isTimestamped = (isTimestampRequested != null ? isTimestampRequested : true); 
        
        if (isTimestamped && this.timestamp != null)
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
}