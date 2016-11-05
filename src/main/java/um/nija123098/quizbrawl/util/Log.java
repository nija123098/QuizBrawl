package um.nija123098.quizbrawl.util;

import sx.blah.discord.Discord4J;
import um.nija123098.quizbrawl.server.LogChannel;

/**
 * Made by nija123098 on 10/10/2016
 */
public class Log {
    private static LogChannel logChannel;
    public static void addLogChannel(LogChannel channel){
        logChannel = channel;
    }
    public static void trace(String s){
        Discord4J.LOGGER.trace(s);
        if (logChannel != null){
            logChannel.trace(s);
        }
    }
    public static void debug(String s){
        Discord4J.LOGGER.debug(s);
        if (logChannel != null) {
            logChannel.debug(s);
        }
    }
    public static void info(String s){
        Discord4J.LOGGER.info(s);
        if (logChannel != null) {
            logChannel.info(s);
        }
    }
    public static void warn(String s){
        Discord4J.LOGGER.warn(s);
        if (logChannel != null) {
            logChannel.warn(s);
        }
    }
    public static void error(String s){
        Discord4J.LOGGER.error(s);
        if (logChannel != null) {
            logChannel.error(s);
        }
    }
}
