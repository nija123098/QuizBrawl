package um.nija123098.quizbrawlkit.bot;

/**
 * Made by Dev on 10/9/2016
 */
public interface Bot {
    void init(BotLink botLink);
    void handle(String s, Client client);
    void requestJoin(Client client);
    void onNewRoom(String name);
    void onLeaveRoom();
    void onAchievementEarn(Achievement achievement, Client client);
    void onClientLeave(Client client);
    void onClientTyping(Client client);
    boolean botOptimal(String name);
    String makerID();
}
