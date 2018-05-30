package glassspirit.box.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpiritLogger {

    private static final Logger logger = LogManager.getLogger("SpiritBox");

    public static Logger getLogger() {
        return logger;
    }

}
