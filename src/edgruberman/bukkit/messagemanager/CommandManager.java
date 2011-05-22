package edgruberman.bukkit.messagemanager;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor {
    private Main plugin;

    public CommandManager (Main plugin) {
        this.plugin = plugin;
        
        this.plugin.getCommand("say").setExecutor(this);
        this.plugin.getCommand("tell").setExecutor(this);
        this.plugin.getCommand("broadcast").setExecutor(this);
        this.plugin.getCommand("send").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        Main.messageManager.log(MessageLevel.FINE
                , ((sender instanceof Player) ? ((Player) sender).getName() : "CONSOLE")
                + " issued command: " + label + " " + this.join(split)
        );
        
        if (label.toLowerCase().equals("say"))
            return this.executeSay(sender, this.join(split));
        
        else if (label.toLowerCase().equals("me"))
            return this.executeMe(sender, this.join(split));
        
        else if (label.toLowerCase().equals("tell"))
            return this.executeTell(sender, this.join(split));
        
        else if (label.toLowerCase().equals("broadcast"))
            return this.executeBroadcast(sender, this.join(split));
        
        else if (label.toLowerCase().equals("send"))
            return this.executeSend(sender, this.join(split));
        
        return false;
    }
    
    private boolean executeSay(CommandSender sender, String message) {
        if (message.length() == 0) {
            Main.messageManager.respond(sender, MessageLevel.SEVERE, "Syntax Error: /say <Message>");
            return true;
        }
        
        this.plugin.broadcastSay(sender, Main.colorize(message.trim()));
        
        return true;
    }
    
    private boolean executeMe(CommandSender sender, String message) {
        if (message.length() == 0) {
            Main.messageManager.respond(sender, MessageLevel.SEVERE, "Syntax Error: /me <Message>");
            return true;
        }
        
        this.plugin.broadcastMe(sender, Main.colorize(message.trim()));
        
        return true;
    }
    
    private boolean executeTell(CommandSender sender, String text) {
        String[] split = text.split(" ", 2);
        if (split.length < 2) {
            Main.messageManager.respond(sender, MessageLevel.SEVERE, "Syntax Error: /tell <Player> <Message>");
            return true;
        }
        
        String recipient = split[0];
        Player target = this.plugin.getServer().getPlayer(recipient);
        if (target == null) {
            Main.messageManager.respond(sender, MessageLevel.SEVERE, "There's no player by the name of \"" + recipient + "\" online.");
            return true;
        }
           
        this.plugin.sendTell(sender, target, Main.colorize(split[1].trim()));
        
        return true;
    }
    
    private boolean executeBroadcast(CommandSender sender, String text) {
        if (!sender.isOp()) return false;
        
        if (text.length() == 0) {
            Main.messageManager.respond(sender, MessageLevel.SEVERE, "Syntax Error: /broadcast [(+|-)timestamp ][<Level> ]<Message>");
            return true;
        }
        
        String message = text;
        
        boolean isTimestamped = true;
        if (message.matches("^[+\\-]timestamp .+")) {
            String[] split = message.split(" ", 2);
            isTimestamped = !split[0].equals("-timestamp");
            message = split[1];
        }
        
        MessageLevel level = null;
        if (message.matches("^%[^%]+% .+")) {
            String[] split = message.split("%", 3);
            level = MessageLevel.parse(split[1]);
            message = split[2];
        }
        
        Main.messageManager.broadcast(level, Main.colorize(message.trim()), isTimestamped);
        
        return true;
    }
    
    private boolean executeSend(CommandSender sender, String text) {
        if (!sender.isOp()) return false;
        
        String[] split = text.split(" ", 2);
        if (split.length < 2) {
            Main.messageManager.respond(sender, MessageLevel.SEVERE, "Syntax Error: /send <Player> [(+|-)timestamp ][<Level> ]<Message>");
            return true;
        }
        
        String recipient = split[0];
        Player target = this.plugin.getServer().getPlayer(recipient);
        if (target == null) {
            Main.messageManager.respond(sender, MessageLevel.SEVERE, "There's no player by the name of \"" + recipient + "\" online.");
            return true;
        }
        
        String message = split[1];
        String[] splitMessage;
        
        boolean isTimestamped = true;
        if (message.matches("^[+\\-]timestamp .+")) {
            splitMessage = message.split(" ", 2);
            isTimestamped = !splitMessage[0].equals("-timestamp");
            message = splitMessage[1];
        }
        
        MessageLevel level = null;
        if (message.matches("^%[^%]+% .+")) {
            splitMessage = message.split("%", 3);
            level = MessageLevel.parse(splitMessage[1]);
            message = splitMessage[2];
        }
        
        Main.messageManager.send(target, level, Main.colorize(message.trim()), isTimestamped);
        
        return true;
    }
    
    private String join(String[] s) {
        return this.join(Arrays.asList(s), " ");
    }
    
    private String join(List<String> list, String delim) {
        if (list == null || list.isEmpty()) return "";
     
        StringBuilder sb = new StringBuilder();
        for (String s : list) sb.append(s + delim);
        sb.delete(sb.length() - delim.length(), sb.length());
        
        return sb.toString();
    }
}