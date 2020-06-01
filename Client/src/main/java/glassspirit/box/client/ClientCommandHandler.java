package glassspirit.box.client;

import glassspirit.box.client.ui.RootsController;
import glassspirit.box.command.Command;
import glassspirit.box.command.Commands;
import glassspirit.box.model.User;

/**
 * Обработчик команд, поступающих от сервера
 */
public class ClientCommandHandler {

    /**
     * Обработка полученной с сервера команды
     */
    public void processCommand(String string) {
        BoxConnection connection = SpiritBoxClient.getConnection();
        Command command = new Command(string);

        if (command.getType() == Commands.AUTH_OK) {
            if ((boolean) command.getContext().get(0)) {
                if (connection.getUser() != null) {
                    connection.getUser().setAuthorized(true);
                    RootsController.moveToRoot("chat");
                } else RootsController.addTextToOutput("Не указан пользователь");
            }
        } else if (command.getType() == Commands.CLEAR) {
            RootsController.clearOutput();
        } else if (command.getType() == Commands.LOGOUT) {
            SpiritBoxClient.getConnection().setUser(null);
            RootsController.moveToRoot("auth");
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

        if (command.getType() == Commands.CLEAR) {
            RootsController.clearOutput();
            return false;
        } else if (command.getType() == Commands.LOGIN) {
            String username = (String) command.getContext().get(0);
            String password = (String) command.getContext().get(1);
            String nickname = (String) command.getContext().get(2);
            SpiritBoxClient.getConnection().setUser(new User(username, password, nickname));
            return true;
        }

        return true;
    }
}
