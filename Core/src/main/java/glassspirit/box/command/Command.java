package glassspirit.box.command;

import glassspirit.box.logging.SpiritLogger;
import org.apache.commons.text.StringTokenizer;
import org.apache.commons.text.matcher.StringMatcherFactory;

import java.util.ArrayList;
import java.util.List;

import static glassspirit.box.command.Command.CommandType.addCommandType;

public class Command {

    public static final CommandType UNKNOWN = CommandType.addCommandType("");
    public static final CommandType LOGIN, LOGOUT, REGISTER, CHANGE_PASSWORD, CHANGE_NICKNAME, MESSAGE;


    static {
        LOGIN = addCommandType("login", Command.CommandElement.string("username"), Command.CommandElement.string("password"));
        LOGOUT = addCommandType("logout");
        REGISTER = addCommandType("register", Command.CommandElement.string("username"), Command.CommandElement.string("password"), Command.CommandElement.string("nickname").optional());
        CHANGE_PASSWORD = addCommandType("changepass", Command.CommandElement.string("password"));
        CHANGE_NICKNAME = addCommandType("changenick", Command.CommandElement.string("nickname"));
        MESSAGE = addCommandType("msg", Command.CommandElement.remainigStrings("message"));
    }

    public static class CommandType {
        static ArrayList<CommandType> registeredCommands = new ArrayList<>();

        private String name;
        private CommandElement[] commandElements;

        private CommandType(String name, CommandElement... elements) {
            this.name = name;
            this.commandElements = elements;
        }

        public static CommandType addCommandType(String name, CommandElement... elements) {
            for (CommandType type : registeredCommands) {
                if (type.name.equals(name)) {
                    SpiritLogger.getLogger().error("Попытка зарегистрировать существующую команду!");
                    return UNKNOWN;
                }
            }
            CommandType newType = new CommandType(name, elements);
            registeredCommands.add(newType);
            return newType;
        }
    }

    private CommandType command = Command.UNKNOWN;
    private CommandContext context = new CommandContext("");

    public Command(String string) {
        String commandName = string.substring(string.indexOf('/') + 1).toLowerCase();
        if (commandName.indexOf(' ') != -1)
            commandName = commandName.substring(0, commandName.indexOf(' ')).toLowerCase();
        for (CommandType type : CommandType.registeredCommands) {
            if (type.name.equals(commandName)) {
                command = type;
                context = new CommandContext(string.substring(string.indexOf(' ') + 1));
            }
        }
    }

    public Command(CommandType type, String args) {
        this.command = type;
        this.context = new CommandContext(args);
    }

    public CommandType getType() {
        return command;
    }

    public CommandContext getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "/" + command.name + " " + context.content;
    }

    public class CommandContext {
        private final String content;
        private List<CommandElement> keys = new ArrayList<>();
        private List<Object> values = new ArrayList<>();

        CommandContext(String string) {
            content = string;
            StringTokenizer tokenizer = new StringTokenizer(content, StringMatcherFactory.INSTANCE.splitMatcher(), StringMatcherFactory.INSTANCE.singleQuoteMatcher());
            for (CommandElement element : command.commandElements) {
                if (tokenizer.hasNext()) {
                    try {
                        keys.add(element);
                        values.add(element.parse(tokenizer.next(), content));
                    } catch (Exception e) {
                        if (!element.optional) {
                            break;
                        }
                    }
                }
            }
        }

        public Object get(String key) {
            try {
                for (int i = 0; i < keys.size(); i++)
                    if (keys.get(i).key.equals(key))
                        return values.get(i);
            } catch (Exception e) {
                return null;
            }
            return null;
        }

        public Object get(int order) {
            try {
                return values.get(order);
            } catch (Exception e) {
                return null;
            }
        }

        public void set(String key, Object value) {
            try {
                for (int i = 0; i < keys.size(); i++)
                    if (keys.get(i).key.equals(key))
                        values.set(i, value);
            } catch (Exception ignored) {
            }
        }

        public void set(int order, Object value) throws IndexOutOfBoundsException {
            try {
                values.set(order, value);
            } catch (Exception ignored) {
            }
        }

        public List<Object> getAll() {
            return values;
        }
    }

    public static abstract class CommandElement<T> {
        private final String key;
        private final Class<T> valueClassType;
        private boolean optional = false;

        private CommandElement(String key, Class<T> clazz) {
            this.key = key;
            this.valueClassType = clazz;
        }

        abstract T parse(String arg, String string);

        public CommandElement<T> optional() {
            this.optional = true;
            return this;
        }

        public static CommandElement bool(String key) {
            return new CommandElement<Boolean>(key, Boolean.TYPE) {
                @Override
                Boolean parse(String arg, String string) {
                    return Boolean.parseBoolean(arg);
                }
            };
        }

        public static CommandElement integer(String key) {
            return new CommandElement<Integer>(key, Integer.TYPE) {
                @Override
                Integer parse(String arg, String string) {
                    return Integer.parseInt(arg);
                }
            };
        }

        public static CommandElement string(String key) {
            return new CommandElement<String>(key, String.class) {
                @Override
                String parse(String arg, String string) {
                    return arg;
                }
            };
        }

        public static CommandElement remainigStrings(String key) {
            return new CommandElement<String>(key, String.class) {
                @Override
                String parse(String arg, String string) {
                    return string.substring(string.indexOf(arg));
                }
            };
        }
    }
}
