package glassspirit.box.server;

import glassspirit.box.auth.AuthService;
import glassspirit.box.logging.SpiritLogger;
import glassspirit.box.properties.SpiritProperties;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

public class SpiritBoxServer {

    private static Logger logger = SpiritLogger.getLogger();
    private ServerSocketChannel socketChannel;
    public static AuthService authService;

    private static Vector<ServerClientHandler> clients = new Vector<>();

    public SpiritBoxServer() {
        new SpiritProperties().load();

        String host = SpiritProperties.getString("socket.hostname", "localhost");
        int port = SpiritProperties.getInteger("socket.port", 8888);

        try {
            Selector selector = Selector.open();
            socketChannel = ServerSocketChannel.open();
            socketChannel.bind(new InetSocketAddress(host, port));
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_ACCEPT);
            logger.info("Запуск сервера на " + socketChannel.getLocalAddress());

            authService = new AuthService();

            startConsole();
            startHeartbeat();

            while (!Thread.interrupted()) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        try {
                            SocketChannel clientChannel = socketChannel.accept();
                            clientChannel.configureBlocking(false);
                            clients.add(new ServerClientHandler(clientChannel));
                            clientChannel.register(selector, SelectionKey.OP_READ, clients.lastElement());
                            logger.info("Входящее соединение " + clientChannel.getRemoteAddress());
                        } catch (IOException e) {
                            logger.error("Ошибка соединения с клиентом", e);
                        }
                    }
                    if (key.isReadable()) {
                        ServerClientHandler handler = (ServerClientHandler) key.attachment();
                        handler.receiveData();
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка инициализации сокета", e);
        } finally {
            try {
                socketChannel.close();
            } catch (IOException e) {
                logger.error("Ошибка закрытия сокета", e);
            }
        }
    }

    private void startConsole() {
        Thread consoleThread = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (!Thread.interrupted()) {
                try {
                    String line = reader.readLine();
                    if (!line.startsWith("/")) {
                        clients.forEach(client -> client.sendFromServer(line));
                    } else if (line.equals("/resetdb")) {
                        authService.reset();
                    }
                } catch (IOException e) {
                    logger.error("Ошибка чтения консоли", e);
                }
            }
        });
        consoleThread.setName("console");
        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    private void startHeartbeat() {
        Thread heartBeatThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                new LinkedList<>(clients).forEach(handler -> {
                    if (handler.isRemoved()) {
                        clients.remove(handler);
                        return;
                    }
                    handler.sendHeartbeat();
                    if (System.currentTimeMillis() - handler.getLastHeartbeat() > 20000) {
                        logger.warn("Клиент " + handler.getAddress().toString() + " больше не отвечает.");
                        handler.remove();
                    }
                });
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        heartBeatThread.setName("heartbeat");
        heartBeatThread.setDaemon(true);
        heartBeatThread.start();
    }

    public static Vector<ServerClientHandler> getClients() {
        return clients;
    }

    public static void main(String[] args) {
        new SpiritBoxServer();
    }

}
