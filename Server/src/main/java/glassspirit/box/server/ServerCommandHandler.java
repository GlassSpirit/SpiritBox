package glassspirit.box.server;

import glassspirit.box.command.Command;
import glassspirit.box.model.User;

/**
 * Обработчик команд, поступающих от клиента
 */
public class ServerCommandHandler {

    /**
     * Обработка полученной от клиента команды
     *
     * @param handler клиент
     * @param string  команда в виде строки
     */
    public void processCommand(ServerClientHandler handler, String string) {
        Command command = new Command(string);
        if (command.getType() == Command.UNKNOWN) {
            handler.sendFromServer("Неизвестная команда " + string + "!");
        } else if (command.getType() == Command.LOGIN) {
            handleLogin(handler, command);
        } else if (command.getType() == Command.LOGOUT) {
            handler.sendString(new Command(Command.LOGOUT, "").toString());
            handler.setUser(null);
        } else if (command.getType() == Command.REGISTER) {
            handleRegister(handler, command);
        } else if (command.getType() == Command.CHANGE_NICKNAME) {
        } else if (command.getType() == Command.CHANGE_PASSWORD) {
        } else if (command.getType() == Command.MESSAGE) {
            handleMessage(handler, command);
        }
    }

    /**
     * Обработка строки перед тем, как отправить ее клиенту
     *
     * @return Нужно ли отправлять строку клиенту
     */
    public boolean preProcess(ServerClientHandler handler, String string) {
        return true;
    }

    private void handleLogin(ServerClientHandler handler, Command command) {
        if (handler.getUser() != null && handler.getUser().isAuthorized()) {
            handler.sendFromServer("Вы уже авторизованы!");
            return;
        }

        Command.CommandContext args = command.getContext();
        String username = (String) args.get("username");
        String password = (String) args.get("password");
        User user = SpiritBoxServer.authService.getUser(username);
        if (SpiritBoxServer.authService.auth(user, password)) {
            handler.setUser(user);
            handler.sendString("/authok true");
            handler.sendFromServer("Авторизация успешна. Здравствуй, " + user.getUsername() + "!");
        } else {
            handler.sendString("/authok false");
            handler.sendFromServer("Ошибка авторизации!");
        }
    }

    private void handleRegister(ServerClientHandler handler, Command command) {
        if (handler.getUser() != null && handler.getUser().isAuthorized()) {
            handler.sendFromServer("Вы уже авторизованы!");
            return;
        }

        Command.CommandContext args = command.getContext();
        String username = (String) args.get("username");
        String password = (String) args.get("password");
        String nickname = (String) args.get("nickname");

        if (SpiritBoxServer.authService.register(username, password, nickname)) {
            handler.sendFromServer("Регистрация успешна!");
            handleLogin(handler, command);
        } else {
            handler.sendFromServer("Ошибка регистрации!");
        }
    }

    private void handleMessage(ServerClientHandler handler, Command command) {
        if (handler.getUser() == null || !handler.getUser().isAuthorized()) {
            handler.sendFromServer("Вы не авторизованы!");
            return;
        }
        String message = (String) command.getContext().get("message");
        for (ServerClientHandler client : SpiritBoxServer.getClients()) {
            if (client.getUser() != null && client.getUser().isAuthorized())
                client.sendString(handler.getUser().getUsername() + ": " + message);
        }
    }

}
