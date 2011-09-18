package edgruberman.bukkit.messagemanager.channels;

import org.bukkit.entity.Player;


public final class PlayerChannel extends Channel {
    
    private Player player;
    
    PlayerChannel(final Player player) {
        super(Channel.Type.PLAYER, player.getName());
        this.player = player;
        this.resetMembers();
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public void setPlayer(final Player player) {
        this.player = player;
    }
    
    @Override
    public void resetMembers() {
        super.resetMembers();
        super.addMember(this.player);
    }
    
    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (other == null) return false;
        
        if (!(other instanceof PlayerChannel)) return false;
        PlayerChannel that = (PlayerChannel) other;
        if (!that.canEqual(this)) return false;
        if (!super.equals(that)) return false;
        
        return true;
    }
    
    @Override
    public boolean canEqual(final Object other) {
        return (other instanceof PlayerChannel);
    }
}