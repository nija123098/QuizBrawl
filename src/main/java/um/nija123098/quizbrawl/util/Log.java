package um.nija123098.quizbrawl.util;

import sx.blah.discord.Discord4J;

/**
 * Made by Dev on 10/10/2016
 */
public class Log {
    public static void trace(String s){
        Discord4J.LOGGER.trace(s);
    }
    public static void debug(String s){
        Discord4J.LOGGER.debug(s);
    }
    public static void info(String s){
        Discord4J.LOGGER.info(s);
    }
    public static void warn(String s){
        Discord4J.LOGGER.warn(s);
    }
    public static void error(String s){
        Discord4J.LOGGER.error(s);
    }
}
