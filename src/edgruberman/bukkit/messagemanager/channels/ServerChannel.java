package edgruberman.bukkit.messagemanager.channels;

import org.bukkit.Server;
import org.bukkit.entity.Player;


public final class ServerChannel extends Channel {
    
    private Server server;
    
    ServerChannel(final Server server) {
        super(Channel.Type.SERVER, server.getName());
        this.server = server;
        this.resetMembers();
    }
    
    @Override
    public void resetMembers() {
        super.resetMembers();
        for (Player player : this.server.getOnlinePlayers())
            super.addMember(player);
    }
    
    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (other == null) return false;
        
        if (!(other instanceof ServerChannel)) return false;
        ServerChannel that = (ServerChannel) other;
        if (!that.canEqual(this)) return false;
        if (!super.equals(that)) return false;
        
        return true;
    }
    
    @Override
    public boolean canEqual(Object other) {
        return (other instanceof ServerChannel);
    }
}