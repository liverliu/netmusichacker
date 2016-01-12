package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by liverliu on 14-6-12.
 */
public class ConstantUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstantUtil.class);

    private ConstantUtil() {

    }

    private static Properties constant = new Properties();

    static {
        try {
            InputStreamReader is = new InputStreamReader(ConstantUtil.class.getResourceAsStream("/constant.properties"), "UTF-8");
            constant.load(is);
            is.close();
        } catch (IOException ex) {
            LOGGER.error("读取constant.properties出错", ex);
        }
    }
    public static String getProperty(String key) {
        return constant.getProperty(key, "");
    }

    public static int getInt(String key) {
        return Integer.parseInt(constant.getProperty(key, "0"));
    }

    public static int getInt(String key, int defaultCount) {
    	return Integer.parseInt(constant.getProperty(key, Integer.toString(defaultCount)));
    }

    public static boolean getBool(String key) {
        return Boolean.parseBoolean(constant.getProperty(key, "false"));
    }

}
