package edgruberman.bukkit.messagemanager.commands.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import edgruberman.bukkit.messagemanager.Main;
import edgruberman.bukkit.messagemanager.MessageLevel;

/**
 * Individual command execution request
 */
public class Context {

    public Handler handler;
    public CommandSender sender;
    public String label;
    public List<String> arguments;
    public Action action;

    /**
     * Parse a command execution request.
     *
     * @param handler command handler
     * @param sender command sender
     * @param args passed command arguments
     */
    Context(final Handler handler, final CommandSender sender, final String label, final String[] args) {
        this.handler = handler;
        this.sender = sender;
        this.label = label;
        this.arguments = parseArguments(args);
        this.action = this.parseAction();
        Main.messageManager.log("Command Context for " + label + "; Action: " + this.action.getNamePath() + "; Arguments: " + this.arguments, MessageLevel.FINEST);
    }

    /**
     * Identify requested action.
     *
     * @return the most specific matching action or the default action if none applies
     */
    private Action parseAction() {
        Action action = this.parseAction(this.handler.actions);
        if (action != null) return action;

        // Return default action (first action registered)
        return this.handler.actions.get(0);
    }

    /**
     * Iterate any sub-actions to find most specific match.
     *
     * @param actions actions to check if they match
     * @return action that matches this context; null if no actions match
     */
    private Action parseAction(final List<Action> actions) {
        for (Action parent : actions)
            if (parent.matches(this)) {
                Action child = this.parseAction(parent.children);
                if (child != null) return child;

                return parent;
            }

        return null;
    }

    /**
     * Concatenate arguments to compensate for double quotes indicating single
     * argument, removing any delimiting double quotes.
     *
     * @return arguments
     *
     * TODO use \ for escaping double quote characters
     * TODO make this less messy
     */
    private List<String> parseArguments(String[] args) {
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