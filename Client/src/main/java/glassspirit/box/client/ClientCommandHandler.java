package glassspirit.box.client;

import glassspirit.box.client.ui.RootsController;
import glassspirit.box.command.Command;
import glassspirit.box.model.User;

import static glassspirit.box.command.Command.CommandType.addCommandType;

/**
 * Обработчик команд, поступающих от сервера
 */
public class ClientCommandHandler {

    public static final Command.CommandType AUTH_OK, CLEAR;

    static {
        AUTH_OK = addCommandType("authok", Command.CommandElement.bool("value"));
        CLEAR = addCommandType("clear");
    }

    /**
     * Обработка полученной с сервера команды
     */
    public void processCommand(String string) {
        BoxConnection connection = SpiritBoxClient.getConnection();
        Command command = new Command(string);

        if (command.getType() == AUTH_OK) {
            if ((boolean) command.getContext().get("value")) {
                if (connection.getUser() != null) {
                    connection.getUser().setAuthorized(true);
                    RootsController.moveToChat();
                } else RootsController.addTextToOutput("Не указан пользователь");
            }
        } else if (command.getType() == CLEAR) {
            RootsController.clearOutput();
        } else if (command.getType() == Command.LOGOUT) {
            SpiritBoxClient.getConnection().setUser(null);
            RootsController.moveToAuth();
        }
    }

    /**
     * Обработка строки перед тем, как отправить ее на сервер.
     *
     * @param string Исходная строка
     * @return Нужно ли отправлять строку на сервер
     */
    public boolean preProcess(String string) {
        Command command = new Command(string);

        if (command.getType() == CLEAR) {
            RootsController.clearOutput();
            return false;
        } else if (command.getType() == Command.LOGIN) {
            String username = (String) command.getContext().get("username");
            String password = (String) command.getContext().get("password");
            String nickname = (String) command.getContext().get("nickname");
            SpiritBoxClient.getConnection().setUser(new User(username, password, nickname));
            return true;
        }

        return true;
    }
}
