package glassspirit.box.command;

import static glassspirit.box.command.Command.CommandType.addCommandType;

public class Commands {

    public static final Command.CommandType
            UNKNOWN = addCommandType(""),
            LOGIN = addCommandType("login", Command.CommandElement.string("username"), Command.CommandElement.string("password")),
            LOGOUT = addCommandType("logout"),
            REGISTER = addCommandType("register", Command.CommandElement.string("username"), Command.CommandElement.string("password"), Command.CommandElement.string("nickname").optional()),
            CHANGE_PASSWORD = addCommandType("changepass", Command.CommandElement.string("password")),
            CHANGE_NICKNAME = addCommandType("changenick", Command.CommandElement.string("nickname")),
            MESSAGE = addCommandType("msg", Command.CommandElement.remainigStrings("message")),
            AUTH_OK = addCommandType("authok", Command.CommandElement.bool("value")),
            CLEAR = addCommandType("clear"),
            SET_FILES = addCommandType("setfiles", Command.CommandElement.remainigStrings("files")),
            GET_FILES = addCommandType("getfiles", Command.CommandElement.remainigStrings("directory")),
            DOWNLOAD_FILE = addCommandType("getfile", Command.CommandElement.remainigStrings("filename")),
            DELETE_FILE = addCommandType("deletefile", Command.CommandElement.remainigStrings("filename"));
}
