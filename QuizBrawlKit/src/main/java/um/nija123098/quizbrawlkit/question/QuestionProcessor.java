package um.nija123098.quizbrawlkit.question;

/**
 * Made by Dev on 10/28/2016
 */
public interface QuestionProcessor {
    boolean giveQuestion(Question question, Parser parser, String raw);
}
