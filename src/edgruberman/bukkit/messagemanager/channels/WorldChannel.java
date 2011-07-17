package edgruberman.bukkit.messagemanager.channels;

import org.bukkit.World;
import org.bukkit.entity.Player;


public final class WorldChannel extends Channel {
    
    public final World world;
    
    WorldChannel(final World world) {
        super(Channel.Type.WORLD, world.getName());
        this.world = world;
        this.resetMembers();
    }
        
    @Override
    public void resetMembers() {
        super.resetMembers();
        for (Player player : this.world.getPlayers())
            super.addMember(player);
    }
    
    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (other == null) return false;
        
        if (!(other instanceof WorldChannel)) return false;
        WorldChannel that = (WorldChannel) other;
        if (!that.canEqual(this)) return false;
        if (!super.equals(that)) return false;
        
        return true;
    }
    
    @Override
    public boolean canEqual(Object other) {
        return (other instanceof WorldChannel);
    }
}