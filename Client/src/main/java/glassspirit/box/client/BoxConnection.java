package glassspirit.box.client;

import glassspirit.box.client.ui.RootsController;
import glassspirit.box.logging.SpiritLogger;
import glassspirit.box.model.User;
import glassspirit.box.network.AbstractChannelHandler;
import glassspirit.box.network.DataType;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class BoxConnection extends AbstractChannelHandler {

    public static ClientCommandHandler commandHandler = new ClientCommandHandler();

    private Logger logger = SpiritLogger.getLogger();
    private Selector selector;
    private boolean connected;
    private String hostName;
    private int port;

    private long lastHeartbeat = System.currentTimeMillis();
    private int failedConnects;

    private User user;

    public BoxConnection() {
        super(null);
        Thread clientThread = new Thread(() -> {
            try {
                selector = Selector.open();
                while (!Thread.interrupted()) {
                    if (!connected) continue;

                    selector.select();
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        if (key.isReadable()) {
                            this.receiveData();
                        }
                        iterator.remove();
                    }
                }
            } catch (IOException e) {
                logger.error("Ошибка инициализации соединения", e);
            }
        });

        clientThread.setName("client");
        clientThread.setDaemon(true);
        clientThread.start();

        Thread heartbeatThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                if (!connected) continue;

                if (System.currentTimeMillis() - lastHeartbeat > 12000) {
                    RootsController.addTextToOutput("Потеряно соединение с сервером");
                    reconnect(true);
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            }
        });
        heartbeatThread.setName("heartbeat");
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }

    public void connect(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
        reconnect(false);
    }

    public void reconnect(boolean error) {
        Thread reconnectThread = new Thread(() -> {
            RootsController.moveToAuth();
            if (channel != null) {
                this.close();
                connected = false;
                channel = null;
            }
            if (error) {
                RootsController.addTextToOutput("Ошибка инициализации соединения.\nПытаемся возобновить соединение через 5 секунд...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                channel = SocketChannel.open(new InetSocketAddress(hostName, port));
                channel.configureBlocking(false);
                channel.register(selector, SelectionKey.OP_READ);

                connected = true;
                failedConnects = 0;
                RootsController.addTextToOutput("Соединение с сервером " + channel.getRemoteAddress() + " успешно");
            } catch (IOException e) {
                logger.error("Ошибка инициализации соединения", e);
                failedConnects++;
                lastHeartbeat = System.currentTimeMillis();
                if (failedConnects < 3)
                    reconnect(true);
                else {
                    RootsController.addTextToOutput("Невозможно подключиться к серверу!");
                    this.close();
                    connected = false;
                    channel = null;
                }
            }
        });
        reconnectThread.setName("reconnect");
        reconnectThread.setDaemon(true);
        reconnectThread.start();
    }

    @Override
    protected void handleDataReceiveException(IOException e) {
        logger.warn("Ошибка принятия данных с сервера (" + e.getMessage() + ")");
        reconnect(true);
    }

    @Override
    protected void handleDataSendException(IOException e) {
        logger.warn("Ошибка отправки данных на сервер (" + e.getMessage() + ")");
        reconnect(true);
    }

    @Override
    protected void handleData(byte[] data, DataType dataType) {
        switch (dataType) {
            case DATA_HEARTBEAT:
                lastHeartbeat = System.currentTimeMillis();
                sendData(new byte[0], DataType.DATA_HEARTBEAT);
                break;
            case DATA_TEST_BYTES:
                RootsController.addTextToOutput("Принято " + data.length + " тестовых байтов");
                break;
            case DATA_STRING:
                handleString(new String(data).trim());
                break;
        }
    }

    private void handleString(String string) {
        if (string.startsWith("/")) {
            commandHandler.processCommand(string);
        } else {
            RootsController.addTextToOutput(string);
        }
    }

    public void sendString(String string) {
        if (commandHandler.preProcess(string))
            sendData(string.getBytes(), DataType.DATA_STRING);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isConnected() {
        return connected;
    }
}
