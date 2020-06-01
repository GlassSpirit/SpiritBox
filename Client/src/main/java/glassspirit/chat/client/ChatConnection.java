package glassspirit.chat.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class ChatConnection {
    private final SpiritChatClient application;
    private ClientChannelHandler handler;
    private boolean connected;
    private String hostName;
    private int port;

    public ChatConnection(SpiritChatClient application, String hostName, int port) {
        this.application = application;
        this.hostName = hostName;
        this.port = port;
        reconnect(false);
    }

    public boolean isConnected() {
        return handler != null && connected;
    }

    public ClientChannelHandler getHandler() {
        return handler;
    }

    public void reconnect(boolean error) {
        if (handler != null) {
            connected = false;
            handler.close();
            handler = null;
        }
        if (error) {
            System.out.println("Пытаемся возобновить соединение через 5 секунд...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            handler = new ClientChannelHandler(SocketChannel.open(new InetSocketAddress(hostName, port)), this);
            connected = true;
        } catch (IOException e) {
            System.out.println("Ошибка инициализации соединения");
            reconnect(true);
        }
    }
}
