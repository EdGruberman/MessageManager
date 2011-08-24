package edgruberman.bukkit.messagemanager.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;

class Context {
    
    Command owner;
    CommandSender sender;
    String label;
    String line;
    Action action;
    
    /**
     * Command line split using double quotes to distinguish single arguments.
     */
    List<String> arguments;
    
    /**
     * Groupings found according to action's regular expression pattern.
     */
    List<String> matches;
    
    Context(final Command owner, final CommandSender sender, final org.bukkit.command.Command command
            , final String label, final String[] args) {
        this.owner = owner;
        this.sender = sender;
        this.label = label;
        this.line = Command.join(args);
        this.arguments = Context.parseArguments(args);
        this.action = this.parseAction();
        this.matches = this.parseMatches();
        
        Main.messageManager.log("Command Context for " + this.label + "; Arguments: " + this.arguments + "; Action: " + (this.action != null ? this.action.name : null) + "; Matches: " + this.matches, MessageLevel.FINEST);
    }
    
    private Action parseAction() {
//        // Check for specified pattern matches.
//        for (Action action : this.owner.actions.values())
//            if (action.pattern != null && this.line.matches(action.pattern))
//                return action;
        
        // Check direct action name match in second or first argument. (/<command>[ <Target>]<Action>)
        if (this.arguments.size() >= 2 && this.owner.actions.containsKey(this.arguments.get(1))) {  
            return this.owner.actions.get(this.arguments.get(1));
        } else if (this.arguments.size() >= 1 && this.owner.actions.containsKey(this.arguments.get(0))) {    
            return this.owner.actions.get(this.arguments.get(0));
        }
        
        return this.owner.defaultAction;
    }
    
    private List<String> parseMatches() {
        List<String> matches = new ArrayList<String>();
        if (this.action == null || this.action.pattern == null)
            return matches;
        
        Pattern p = Pattern.compile(this.action.pattern);
        Matcher m = p.matcher(this.line);
        if (m.find())
            for (int i = 1; i <= m.groupCount(); i++)
                matches.add(m.group(i));
        
        return matches;
    }
    
    /**
     * Concatenate arguments to compensate for double quotes indicating single
     * argument, removing any delimiting double quotes.
     *  
     * @return arguments
     * @TODO use / for escaping double quote characters
     */
    private static List<String> parseArguments(String[] args) {
        List<String> arguments = new ArrayList<String>();
        
        String previous = null;
        for (String arg : args) {
            if (previous != null) {
                if (arg.endsWith("\"")) {
                    arguments.add(Context.stripDoubleQuotes(previous + " " + arg));
                    previous = null;
                } else {
                    previous += " " + arg;
                }
                continue;
            }

            if (arg.startsWith("\"") && !arg.endsWith("\"")) {
                previous = arg;
            } else {
                arguments.add(Context.stripDoubleQuotes(arg));
            }
        }
        if (previous != null) arguments.add(Context.stripDoubleQuotes(previous));
        
        return arguments;
    }
    
    private static String stripDoubleQuotes(final String s) {
        return Context.stripDelimiters(s, "\"");
    }
    
    private static String stripDelimiters(final String s, final String delim) {
        if (!s.startsWith(delim) || !s.endsWith(delim)) return s;
        
        return s.substring(1, s.length() - 1);
    }
}