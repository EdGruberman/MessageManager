package edgruberman.bukkit.messagemanager.channels;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;

public class Channel {
    
    public static Map<Channel.Type, Map<String, Channel>> instances = new HashMap<Channel.Type, Map<String, Channel>>();
    
    public String name;
    public Type type;
    protected Set<Recipient> members = new HashSet<Recipient>();
    
    protected Channel(final Type type, final String name) {
        if (Channel.exists(type, name))
            throw new IllegalArgumentException(type.toString() + " channel [" + name + "] already exists.");
        
        this.name = name;
        this.type = type;
        
        if (!Channel.instances.containsKey(this.type))
            Channel.instances.put(this.type, new HashMap<String, Channel>());
        
        Channel.instances.get(this.type).put(this.name, this);
    }
    
    public boolean addMember(final Player member) {
        return this.addMember(Recipient.getInstance(member));
    }
    
    public boolean addMember(final Recipient member) {
        boolean added = this.members.add(member);
        if (added) Main.messageManager.log(this.toString() + " Added member " + member.getPlayer().getName(), MessageLevel.FINER);
        return added;
    }
    
    public boolean removeMember(final Player member) {
        return this.removeMember(Recipient.getInstance(member));
    }
    
    public boolean removeMember(final Recipient member) {
        boolean removed = this.members.remove(member);
        if (removed) Main.messageManager.log(this.toString() + " Removed member " + member.getPlayer().getName(), MessageLevel.FINER);
        return removed;
    }
    
    public static void disconnect(final Player player) {
        Channel.disconnect(Recipient.getInstance(player));
    }
    
    public static void disconnect(final Recipient member) {
        for (Map<String, Channel> types : Channel.instances.values())
            for (Channel channel : types.values())
                channel.removeMember(member);
        
        Recipient.disposeInstance(member);
    }
    
    public void resetMembers() {
        this.members.clear();
    }
    
    public boolean isMember(Player player) {
        return this.members.contains(Recipient.getInstance(player));
    }
    
    public void send(final String message) {
        this.send(message, Main.messageManager.useTimestampDefault);
    }
    
    public void send(final String message, final boolean isTimestamped) {
        if (!Channel.exists(this))
            throw new IllegalArgumentException("Channel reference no longer valid.");
            
        for (Recipient recipient : this.members)
            recipient.send(message, isTimestamped);
    }
    
    public static Channel getInstance(final Channel.Type type, final String name) {
        if (!Channel.exists(type, name))
            switch(type) {
            case PLAYER:
                Player player = Bukkit.getServer().getPlayer(name);
                if (!name.equalsIgnoreCase(player.getName())) player = null;
                if (player == null) return null;
                
                new PlayerChannel(player);
                break; 
                
            case SERVER:
                new ServerChannel(Bukkit.getServer());
                break;
                
            case WORLD:
                World world = Bukkit.getServer().getWorld(name);
                if (world == null) return null;
                
                new WorldChannel(world);
                break;
                
            case LOG:
                Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(name);
                if (plugin == null) return null;
                
                new LogChannel(plugin);
                break;
                
            case CUSTOM:
                new CustomChannel(name);
                break;
            }
        
        return Channel.instances.get(type).get(name);
    }
    
    public static PlayerChannel getInstance(final Player player) {
        if (!Channel.exists(player))
            new PlayerChannel(player);
        
        return (PlayerChannel) Channel.getInstance(Channel.Type.PLAYER, player.getName());
    }
    
    public static ServerChannel getInstance(final Server server) {
        if (!Channel.exists(server))
            new ServerChannel(server);
        
        return (ServerChannel) Channel.getInstance(Channel.Type.SERVER, server.getName());
    }
    
    public static WorldChannel getInstance(final World world) {
        if (!Channel.exists(world))
            new WorldChannel(world);
        
        return (WorldChannel) Channel.getInstance(Channel.Type.WORLD, world.getName());
    }
    
    public static LogChannel getInstance(final Plugin plugin) {
        if (!Channel.exists(plugin))
            new LogChannel(plugin);
        
        return (LogChannel) Channel.getInstance(Channel.Type.LOG, plugin.getDescription().getName());
    }
    
    public static CustomChannel getInstance(final String name) {
        if (!Channel.exists(name))
            new CustomChannel(name);
        
        return (CustomChannel) Channel.getInstance(Channel.Type.CUSTOM, name);
    }
    
    public static boolean exists(final Channel.Type type, final String name) {
        if (!Channel.instances.containsKey(type)) return false;
        
        return Channel.instances.get(type).containsKey(name);
    }
    
    private static boolean exists(final Channel channel) {
        return Channel.exists(channel.type, channel.name);
    }
    
    public static boolean exists(final Player player) {
        return Channel.exists(Channel.Type.PLAYER, player.getName());
    }
    
    public static boolean exists(final Server server) {
        return Channel.exists(Channel.Type.SERVER, server.getName());
    }
    
    public static boolean exists(final World world) {
        return Channel.exists(Channel.Type.WORLD, world.getName());
    }
    
    public static boolean exists(final Plugin plugin) {
        return Channel.exists(Channel.Type.LOG, plugin.getDescription().getName());
    }
    
    public static boolean exists(final String name) {
        return Channel.exists(Channel.Type.CUSTOM, name);
    }
    
    public static boolean disposeInstance(final Channel channel) {
        if (!Channel.exists(channel)) return false;
        
        Channel.instances.get(channel.type).remove(channel.name);
        return true;
    }
    
    public static boolean disposeInstance(final Channel.Type type, final String name) {
        if (!Channel.exists(type, name)) return false;
        
        Channel.disposeInstance(Channel.getInstance(type, name));
        return true;
    }
    
    public static boolean disposeInstance(final Player player) {
        return Channel.disposeInstance(Channel.Type.PLAYER, player.getName());
    }
    
    public static boolean disposeInstance(final Server server) {
        return Channel.disposeInstance(Channel.Type.SERVER, server.getName());
    }
    
    public static boolean disposeInstance(final World world) {
        return Channel.disposeInstance(Channel.Type.WORLD, world.getName());
    }
    
    public static boolean disposeInstance(final Plugin plugin) {
        return Channel.disposeInstance(Channel.Type.LOG, plugin.getDescription().getName());
    }
    
    public static boolean disposeInstance(final String name) {
        return Channel.disposeInstance(Channel.Type.CUSTOM, name);
    }
    
    @Override
    public String toString() {
        return "[" + this.type + ";" + this.name + "]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.toLowerCase().hashCode());
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (other == null) return false;
        
        if (!(other instanceof Channel)) return false;
        Channel that = (Channel) other;
        if (!that.canEqual(this)) return false;
        
        if (this.type != that.type) return false;
        if (!this.name.equalsIgnoreCase(that.name)) return false;
        
        return true;
    }
    
    public boolean canEqual(final Object other) {
        return (other instanceof Channel);
    }
    
    public enum Type {
        PLAYER, SERVER, WORLD, CUSTOM, LOG
    }
}