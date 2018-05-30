package glassspirit.box.network;

public enum DataType {

    /**
     * Пустой enum
     */
    NULL(0),

    /**
     * Используется для поддержания и проверки соединения с клиентом
     */
    DATA_HEARTBEAT(0),

    /**
     * Любые тестовые данные
     */
    DATA_TEST_BYTES(1024), //1 KB

    /**
     * Строковые данные (сообщения, команды)
     */
    DATA_STRING(1048576),  //1 MB

    /**
     * Данные файлов
     */
    DATA_FILE(524288000);  //500 MB

    private int maxDataSize;

    DataType(int maxDataSize) {
        this.maxDataSize = maxDataSize;
    }

    public int getMaxDataSize() {
        return maxDataSize;
    }
}
