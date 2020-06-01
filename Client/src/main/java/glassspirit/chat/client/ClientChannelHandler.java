package glassspirit.chat.client;

import glassspirit.box.network.AbstractChannelHandler;
import glassspirit.box.network.DataType;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ClientChannelHandler extends AbstractChannelHandler {
    final private ChatConnection connection;

    public ClientChannelHandler(SocketChannel channel, ChatConnection connection) {
        super(channel);
        this.connection = connection;
    }

    @Override
    protected void handleDataReceiveException(IOException e) {
        System.out.println("Ошибка принятия данных с сервера (" + e.getMessage() + ")");
        connection.reconnect(true);
    }

    @Override
    protected void handleDataSendException(IOException e) {
        System.out.println("Ошибка отправки данных на сервер (" + e.getMessage() + ")");
        connection.reconnect(true);
    }

    @Override
    protected void handleData(byte[] data, DataType dataType) {
        if (dataType == DataType.DATA_STRING) {
            handleMessage(new String(data).trim());
        }
    }

    private void handleMessage(String message) {
        System.out.println("Получено сообщение : " + message);
    }

    public void sendMessage(String message) {
        sendData(message.getBytes(), DataType.DATA_STRING);
    }
}
