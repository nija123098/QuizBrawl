package um.nija123098.quizbrawlkit.question;

/**
 * Made by Dev on 10/9/2016
 */
public interface Question {
    Type type();
    Difficulty difficulty();
    Topic topic();
    String question();
    Result result(String response);
    String answer();
    String authorID();
    String parserID();
}
