package um.nija123098.quizbrawlkit.bot;

import um.nija123098.quizbrawlkit.question.*;

/**
 * Made by Dev on 10/10/2016
 */
public interface Client {
    void add();
    void kick();
    void mute(boolean mute);
    void deafen(boolean deafen);
    Message msg(String s);
    void attempt(Result r, Question q);
    void setTeam(Team team);
    void enableTyping(boolean enable);
    long stat(Difficulty difficulty, Topic topic, Type type, Result result);
    String getName();
    String getId();
}
