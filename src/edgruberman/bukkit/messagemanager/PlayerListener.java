package edgruberman.bukkit.messagemanager;

import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {
    
    private Main plugin;
    
    public PlayerListener(Main plugin) {
        this.plugin = plugin;
    }
    
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult().equals(Result.ALLOWED)) return;
        
        MessageLevel level = this.plugin.getMessageLevel("playerLogin.other");
        String message = this.plugin.getConfiguration().getString("playerLogon.other.reason");
        switch (event.getResult()) {
            case KICK_BANNED:
                level = this.plugin.getMessageLevel("playerLogin.banned");
                message = String.format(this.plugin.getMessageFormat("playerLogin")
                        , this.plugin.getConfiguration().getString("playerLogon.banned.reason"));
            case KICK_FULL:
                level = this.plugin.getMessageLevel("playerLogin.full");
                message = String.format(this.plugin.getMessageFormat("playerLogin")
                        , this.plugin.getConfiguration().getString("playerLogon.full.reason"));
            case KICK_WHITELIST:
                level = this.plugin.getMessageLevel("playerLogin.whitelist");
                message = String.format(this.plugin.getMessageFormat("playerLogin")
                        , this.plugin.getConfiguration().getString("playerLogon.whitelist.reason"));
        }
        
        event.setKickMessage(level + message);
        Main.messageManager.log(level, message);
     }
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        MessageLevel level = this.plugin.getMessageLevel("broadcast.player.join");
        String message = this.plugin.formatBroadcast(level
                , String.format(this.plugin.getMessageFormat("broadcast.player.join")
                        , event.getPlayer().getDisplayName()
                )
        );
        
        event.setJoinMessage(message);
        Main.messageManager.log(level, message);
    }
    
    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) return;
        
        event.setFormat(this.plugin.formatChat(event.getPlayer(), event.getMessage()).replace("%", "%%"));
    }
    
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        MessageLevel level = this.plugin.getMessageLevel("broadcast.player.quit");
        String message = this.plugin.formatBroadcast(level
                , String.format(this.plugin.getMessageFormat("broadcast.player.quit")
                        , event.getPlayer().getDisplayName()
                )
        );
        
        event.setQuitMessage(message);
        Main.messageManager.log(level, message);
    }
    
    @Override
    public void onPlayerKick(PlayerKickEvent event) {
        if (event.isCancelled()) return;
        
        MessageLevel level = this.plugin.getMessageLevel("broadcast.player.kick");
        String message = this.plugin.formatBroadcast(
                level
                , String.format(this.plugin.getMessageFormat("broadcast.player.kick")
                        , event.getPlayer().getDisplayName()
                )
        );
        
        event.setReason(level.getSendColor() + event.getReason());
        event.setLeaveMessage(message);
        Main.messageManager.log(level, message);
    }
}