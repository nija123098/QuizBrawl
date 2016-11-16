package um.nija123098.quizbrawl.defaultp;

import um.nija123098.quizbrawlkit.bot.Client;
import um.nija123098.quizbrawlkit.question.*;

/**
 * Made by nija123098 on 11/15/2016
 */
public class PBParser implements Parser {
    private int count;
    private Client client;
    @Override
    public void init(Client client, QuestionProcessor processor) {
        this.client = client;
    }
    @Override
    public void handle(String s) {
        this.client.msg(++count < 5 ? "This parser does not support direct question input" : "I am only here so I don't get fined");
    }
    @Override
    public String format(Question q) {
        return q.difficulty().name() + ":" + q.topic().name() + ":" + q.question() + ":" + q.answer();
    }
    @Override
    public Question parse(String s) {
        String[] st = s.split(":");
        try{return new PBQuestion(Difficulty.valueOf(st[0]), Topic.valueOf(st[1]), st[2], st[3]);
        }catch(Exception e){return null;}
    }
    @Override
    public String id() {
        return "pbparser";
    }
    private class PBQuestion implements Question {
        private Difficulty difficulty;
        private Topic topic;
        private String question, answer;
        public PBQuestion(Difficulty difficulty, Topic topic, String question, String answer) {
            this.difficulty = difficulty;
            this.topic = topic;
            this.question = question;
            this.answer = answer;
        }
        @Override
        public Type type() {
            return Type.UNKNOWN;
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
            return answer.toLowerCase().trim().contains(response.toLowerCase().trim()) ? Result.CORRECT : Result.INCORRECT;
        }
        @Override
        public String answer() {
            return this.answer;
        }
        @Override
        public String authorID() {
            return "n";
        }
        @Override
        public String parserID() {
            return "pbparser";
        }
    }
}
