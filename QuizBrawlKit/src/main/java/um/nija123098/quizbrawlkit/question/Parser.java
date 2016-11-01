package um.nija123098.quizbrawlkit.question;

import um.nija123098.quizbrawlkit.bot.Client;

/**
 * Made by Dev on 10/28/2016
 */
public interface Parser {
    void init(Client client, QuestionProcessor processor);
    void handle(String s);
    String format(Question q);
    Question parse(String s);
    default String id(){
        return this.getClass().getName();
    }
}
