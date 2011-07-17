package edgruberman.bukkit.messagemanager.channels;


public class CustomChannel extends Channel {
    
    CustomChannel(final String name) {
        super(Channel.Type.CUSTOM, name);
    }
    
    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (other == null) return false;
        
        if (!(other instanceof CustomChannel)) return false;
        CustomChannel that = (CustomChannel) other;
        if (!that.canEqual(this)) return false;
        if (!super.equals(that)) return false;
        
        return true;
    }
    
    @Override
    public boolean canEqual(Object other) {
        return (other instanceof CustomChannel);
    }
}