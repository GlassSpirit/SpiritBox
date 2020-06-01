package glassspirit.box.network;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class AbstractChannelHandler {

    /**
     * Размер стандартного буфера данных
     */
    private static final int DEFAULT_DATA_SIZE = 1024;

    /**
     * Точный размер тега сериализации данных
     */
    private static final int SERIALIZER_TAG_SIZE = 11;

    protected SocketChannel channel;
    protected ByteBuffer receiveBuffer;
    protected ByteBuffer dataBuffer;
    private boolean receivingData;
    private DataType incomingDataType;
    private int incomingDataLength;
    private int incomingDataReceived;

    public AbstractChannelHandler(SocketChannel channel) {
        this.channel = channel;
        this.receiveBuffer = ByteBuffer.allocate(SERIALIZER_TAG_SIZE);
        this.dataBuffer = ByteBuffer.allocate(DEFAULT_DATA_SIZE);
    }

    public SocketChannel getChannel() {
        return channel;
    }

    /**
     * Вызывается, если в канале есть данные для чтения.
     * Изначально заполняется буффер сериализации, чтобы отсеить неправильные подключения и данные.
     * Также определяется {@link DataType} тип входящих данных и их размер.
     * После корректного заполнения буфера сериализации, начинает заполняться буфер данных до получения
     * всех байтов, указанных ранее.
     */
    public void receiveData() {
        try {
            if (!receivingData) {
                incomingDataType = DataType.NULL;
                incomingDataLength = 0;
                incomingDataReceived = 0;
                if (channel.read(receiveBuffer) > 0) {
                    receiveBuffer.flip();
                    if (receiveBuffer.getChar() != 'T') throw new IOException("Неправильный формат данных!");
                    incomingDataType = DataType.values()[receiveBuffer.get()];
                    if (receiveBuffer.getChar() != 'L') throw new IOException("Неправильный формат данных!");
                    incomingDataLength = receiveBuffer.getInt();
                    if (incomingDataLength < 0) throw new IOException("Неправильный формат данных!");
                    if (incomingDataLength > incomingDataType.getMaxDataSize()) throw new IOException("Слишком большой объем данных!");
                    if (receiveBuffer.getChar() != 'D') throw new IOException("Неправильный формат данных!");

                    if (incomingDataLength == 0) {
                        handleData(new byte[]{}, incomingDataType);
                    } else {
                        receivingData = true;
                        dataBuffer = ByteBuffer.allocate(incomingDataLength);
                    }
                } else {
                    if (dataBuffer.capacity() != DEFAULT_DATA_SIZE) {
                        dataBuffer = ByteBuffer.allocate(DEFAULT_DATA_SIZE);
                    }
                    receiveBuffer.clear();
                    dataBuffer.clear();
                }
            } else {
                if (channel.read(dataBuffer) > 0) {
                    receivePartialData();
                }
            }
        } catch (IOException e) {
            handleDataReceiveException(e);
        }
    }

    /**
     * Проверка, все ли данные получены, основываясь на текущей позиции буфера данных
     * Если данные получены, приступить к обработке.
     */
    private void receivePartialData() {
        incomingDataReceived = dataBuffer.position();
        if (incomingDataReceived >= incomingDataLength) {
            receivingData = false;
            try {
                byte[] data = new byte[incomingDataLength];
                dataBuffer.flip();
                dataBuffer.get(data, 0, incomingDataLength);
                handleData(data, incomingDataType);
            } catch (BufferOverflowException ignored) {
            }
        }
    }

    /**
     * Сериализация байтов
     *
     * @param data данные
     * @param type тип данных
     * @return сериализованные данные в {@link ByteBuffer} буфере
     */
    private ByteBuffer serializeData(byte[] data, DataType type) {
        ByteBuffer newData = ByteBuffer.allocate(data.length + SERIALIZER_TAG_SIZE);
        newData.putChar('T');
        newData.put((byte) type.ordinal());
        newData.putChar('L');
        newData.putInt(data.length);
        newData.putChar('D');
        newData.put(data);
        newData.flip();
        return newData;
    }

    /**
     * Отправка данных с предварительной сериализацией
     *
     * @param data данные
     * @param type тип данных
     */
    public void sendData(byte[] data, DataType type) {
        try {
            channel.write(serializeData(data, type));
        } catch (IOException e) {
            handleDataSendException(e);
        }
    }

    /**
     * Закрытие канала
     */
    public void close() {
        if (channel == null) return;
        try {
            channel.close();
        } catch (IOException e) {
            System.out.println("Ошибка закрытия соединения (" + e.getMessage() + ")");
        }
    }

    /**
     * Обработчик исключений
     */
    protected abstract void handleDataReceiveException(IOException e);

    /**
     * Обработчик исключений
     */
    protected abstract void handleDataSendException(IOException e);

    /**
     * Обработчик полученных данных
     *
     * @param data     данные
     * @param dataType тип данных
     */
    protected abstract void handleData(byte[] data, DataType dataType);
}
