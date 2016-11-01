package um.nija123098.quizbrawl.defaultp;

import um.nija123098.quizbrawlkit.question.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Made by Dev on 10/16/2016
 */
public class DefaultQuestionPack implements QuestionPack {
    @Override
    public List<Question> getQuestions() {
        List<Question> questions = new ArrayList<Question>();
        questions.add(new DefaultQuestion(Type.UNKNOWN, Difficulty.MEDIUM, Topic.NONE,
                "The answer to this question is a number.  It is also a string.  It is not the answer to everything.", "a number"));
        return questions;
    }
    private class DefaultQuestion implements Question {
        Type type;
        Difficulty difficulty;
        Topic topic;
        String question, answer;
        DefaultQuestion(Type type, Difficulty difficulty, Topic topic, String question, String answer) {
            this.type = type;
            this.difficulty = difficulty;
            this.topic = topic;
            this.question = question;
            this.answer = answer;
        }
        @Override
        public Type type() {
            return this.type;
        }
        @Override
        public Difficulty difficulty() {
            return this.difficulty;
        }
        @Override
        public Topic topic() {
            return this.topic;
        }
        @Override
        public String question() {
            return this.question;
        }
        @Override
        public Result result(String response) {
            return this.answer.equals(response.toLowerCase()) ? Result.CORRECT : Result.INCORRECT;
        }
        @Override
        public String answer() {
            return this.answer;
        }
        @Override
        public String authorID() {
            return "191677220027236352";
        }
        @Override
        public String parserID() {
            return null;
        }
    }
}
