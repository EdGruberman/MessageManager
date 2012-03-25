package edgruberman.bukkit.messagemanager.channels;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageDisplay;
import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

/**
 * Recipient Collection
 */
public abstract class Channel {

    public static ChannelConfiguration getChannelConfiguration(final Channel.Type type, final Plugin owner) {
        switch(type) {
        case SERVER: return ServerChannel.configuration.get(owner);
        case PLAYER: return PlayerChannel.configuration.get(owner);
        case WORLD: return WorldChannel.configuration.get(owner);
        case CUSTOM: return CustomChannel.configuration.get(owner);
        }

        return null;
    }

    // TODO determine how to reference a static class dynamically based on an enum value
    public static ChannelConfiguration setChannelConfiguration(final Channel.Type type, final Plugin owner, final ChannelConfiguration configuration) {
        switch(type) {
        case SERVER: return ServerChannel.configuration.put(owner, configuration);
        case PLAYER: return PlayerChannel.configuration.put(owner, configuration);
        case WORLD: return WorldChannel.configuration.put(owner, configuration);
        case CUSTOM: return CustomChannel.configuration.put(owner, configuration);
        }

        return null;
    }

    public String name;
    public Type type;

    protected Set<Recipient> members = new HashSet<Recipient>();

    Channel(final Type type, final String name) {
        this.name = name;
        this.type = type;
    }

    public abstract ChannelConfiguration getConfiguration(final Plugin owner);
    public abstract ChannelConfiguration setConfiguration(final Plugin owner, final ChannelConfiguration configuration);

    public boolean add(final CommandSender member) {
        return this.add(MessageManager.getDispatcher().getRecipient(member));
    }

    public Set<Recipient> getMembers() {
        return Collections.unmodifiableSet(this.members);
    }

    boolean add(final Recipient member) {
        if (!this.members.add(member)) return false;

        member.memberships.add(this);
        Main.logger.log(Level.FINER, "Added member to " + this.toString() + ": " + member.getTarget().getName());
        return true;
    }

    public boolean remove(final CommandSender member) {
        return this.remove(MessageManager.getDispatcher().getRecipient(member));
    }

    boolean remove(final Recipient member) {
        if (!this.members.remove(member)) return false;

        member.memberships.remove(this);
        Main.logger.log(Level.FINER, "Removed member from " + this.toString() + ": " + member.getTarget().getName());
        return true;
    }

    public void clear() {
        for (final Recipient member : this.members) member.memberships.remove(this);
        this.members.clear();
    }

    public boolean isMember(final Player player) {
        return this.members.contains(MessageManager.getDispatcher().getRecipient(player));
    }

    public boolean isSendable(final Plugin owner, final MessageLevel level) {
        return level.intValue() >= this.getConfiguration(owner).levelChannel.intValue();
    }

    public String format(final Plugin owner, String message, final MessageLevel level) {
        message = String.format(this.getConfiguration(owner).format, message, this.name);
        return MessageDisplay.translate(this.getConfiguration(owner).getColor(level), message);
    }

    public String send(final Plugin owner, String message, final MessageLevel level, final boolean applyTimestamp) {
        if (!this.isSendable(owner, level)) return null;

        message = this.format(owner, message, level);
        for (final Recipient recipient : this.members) recipient.send(message, applyTimestamp);
        return message;
    }

    @Override
    public String toString() {
        return "#" + this.type + ";" + this.name + ";" + this.members.size() + "#";
    }

    public enum Type {
        CUSTOM, PLAYER, SERVER, WORLD
    }

}
