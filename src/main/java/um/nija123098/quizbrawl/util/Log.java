package um.nija123098.quizbrawl.util;

import sx.blah.discord.Discord4J;

/**
 * Made by Dev on 10/10/2016
 */
public enum  Log {
    TRACE(){
        @Override
        public void log(String log){
            Discord4J.LOGGER.trace(log);
        }
    }, DEBUG(){
        @Override
        public void log(String log){
            Discord4J.LOGGER.debug(log);
        }
    }, INFO(){
        @Override
        public void log(String log){
            Discord4J.LOGGER.info(log);
        }
    }, WARN(){
        @Override
        public void log(String log){
            Discord4J.LOGGER.warn(log);
        }
    }, ERROR(){
        @Override
        public void log(String log){
            Discord4J.LOGGER.error(log);
        }
    }, NONE(){
        @Override
        public void log(String log){}
    },;
    Log(){}
    public abstract void log(String log);
    @Deprecated
    public static void log(int level, String s){
        Log.values()[level].log(s);
    }
}
