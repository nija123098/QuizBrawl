package um.nija123098.quizbrawlkit.question;

import java.util.ArrayList;
import java.util.List;

/**
 * Made by Dev on 10/10/2016
 */
public interface QuestionPack {
    default List<Question> getQuestions(){
        return new ArrayList<Question>(0);
    }
    default List<Question> getQuestions(String path){
        return this.getQuestions();
    }
}
