package glassspirit.box.properties;

import glassspirit.box.logging.SpiritLogger;

import java.io.IOException;
import java.util.Properties;

public class SpiritProperties {

    private static final Properties properties = new Properties();

    public void load() {
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties getProperties() {
        return properties;
    }

    public static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static Integer getInteger(String key, Integer defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, defaultValue.toString()));
        } catch (NumberFormatException e) {
            SpiritLogger.getLogger().error("Ошибка чтения property", e);
            return defaultValue;
        }
    }

    public static Float getFloat(String key, Float defaultValue) {
        try {
            return Float.parseFloat(properties.getProperty(key, defaultValue.toString()));
        } catch (NumberFormatException e) {
            SpiritLogger.getLogger().error("Ошибка чтения property", e);
            return defaultValue;
        }
    }

}
