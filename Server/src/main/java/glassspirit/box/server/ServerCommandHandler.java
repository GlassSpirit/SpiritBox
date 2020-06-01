package glassspirit.box.server;

import glassspirit.box.command.Command;
import glassspirit.box.command.Commands;
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
        if (command.getType() == Commands.UNKNOWN) {
            handler.sendFromServer("Неизвестная команда " + string + "!");
        } else if (command.getType() == Commands.LOGIN) {
            handleLogin(handler, command);
        } else if (command.getType() == Commands.LOGOUT) {
            handler.sendString(new Command(Commands.LOGOUT, "").toString());
            handler.setUser(null);
        } else if (command.getType() == Commands.REGISTER) {
            handleRegister(handler, command);
        } else if (command.getType() == Commands.CHANGE_NICKNAME) {
        } else if (command.getType() == Commands.CHANGE_PASSWORD) {
        } else if (command.getType() == Commands.MESSAGE) {
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
        String username = (String) args.get(0);
        String password = (String) args.get(1);
        User user = SpiritBoxServer.authService.getUser(username);
        if (SpiritBoxServer.authService.auth(user, password)) {
            handler.setUser(user);
            handler.sendString(new Command(Commands.AUTH_OK, "true").toString());
            handler.sendFromServer("Авторизация успешна. Здравствуй, " + user.getUsername() + "!");
        } else {
            handler.sendString(new Command(Commands.AUTH_OK, "false").toString());
            handler.sendFromServer("Ошибка авторизации!");
        }
    }

    private void handleRegister(ServerClientHandler handler, Command command) {
        if (handler.getUser() != null && handler.getUser().isAuthorized()) {
            handler.sendFromServer("Вы уже авторизованы!");
            return;
        }

        Command.CommandContext args = command.getContext();
        String username = (String) args.get(0);
        String password = (String) args.get(1);
        String nickname = (String) args.get(2);

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
        String message = (String) command.getContext().get(0);
        for (ServerClientHandler client : SpiritBoxServer.getClients()) {
            if (client.getUser() != null && client.getUser().isAuthorized())
                client.sendString(handler.getUser().getUsername() + ": " + message);
        }
    }

}
