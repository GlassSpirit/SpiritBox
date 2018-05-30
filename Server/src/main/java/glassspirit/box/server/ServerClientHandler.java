package glassspirit.box.server;

import glassspirit.box.logging.SpiritLogger;
import glassspirit.box.model.User;
import glassspirit.box.network.AbstractChannelHandler;
import glassspirit.box.network.DataType;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

public class ServerClientHandler extends AbstractChannelHandler {

    public static ServerCommandHandler commandHandler = new ServerCommandHandler();

    private SocketAddress address;
    private Logger logger;
    private boolean removed;

    private User user;

    private long lastHeartbeat = System.currentTimeMillis();

    public ServerClientHandler(SocketChannel channel) {
        super(channel);
        logger = SpiritLogger.getLogger();
        try {
            this.address = channel.getRemoteAddress();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public SocketAddress getAddress() {
        return address;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public boolean isRemoved() {
        return removed;
    }

    @Override
    protected void handleDataReceiveException(IOException e) {
        logger.warn("Ошибка получения данных от клиента " + address + " (" + e.getMessage() + ")");
        remove();
    }

    @Override
    protected void handleDataSendException(IOException e) {
        logger.warn("Ошибка отправки данных клиенту " + address + " (" + e.getMessage() + ")");
        remove();
    }

    @Override
    protected void handleData(byte[] data, DataType dataType) {
        switch (dataType) {
            case DATA_HEARTBEAT:
                handleHeartbeat();
                break;
            case DATA_TEST_BYTES:
                logger.info("Принято " + data.length + " тестовых байтов");
                break;
            case DATA_STRING:
                handleString(new String(data).trim());
                break;
        }
    }

    private void handleString(String string) {
        String prefix = "[" + getAddress() + "]: ";
        if (user != null && user.isAuthorized())
            prefix = user.getUsername() + " [" + getAddress() + "]: ";
        logger.info(prefix + string);

        if (string.startsWith("/"))
            handleCommand(string);
        else commandHandler.processCommand(this, "/msg " + string);
    }

    private void handleCommand(String command) {
        commandHandler.processCommand(this, command);
    }

    private void handleHeartbeat() {
        lastHeartbeat = System.currentTimeMillis();
    }

    public void sendString(String string) {
        if (commandHandler.preProcess(this, string))
            sendData(string.getBytes(), DataType.DATA_STRING);
    }

    public void sendHeartbeat() {
        sendData(new byte[0], DataType.DATA_HEARTBEAT);
    }

    public void sendFromServer(String string) {
        String message = "[СЕРВЕР]: " + string;
        sendData(message.getBytes(), DataType.DATA_STRING);
    }

    public void remove() {
        receiveBuffer = null;
        dataBuffer = null;
        logger.info("Удаление клиента " + address);
        close();
        removed = true;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
