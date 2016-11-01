package um.nija123098.quizbrawl.defaultp;

import um.nija123098.quizbrawlkit.bot.Client;
import um.nija123098.quizbrawlkit.question.*;

/**
 * Made by Dev on 10/28/2016
 */
public class RawParser implements Parser {
    private static final String SPLITTER = ":";
    private String splitter;
    private Client client;
    private QuestionProcessor questionProcessor;
    private RawQuestion rawQuestion;
    public RawParser() {
        this.splitter = ":";
    }
    @Override
    public void init(Client client, QuestionProcessor processor) {
        this.client = client;
        this.questionProcessor = processor;
    }
    @Override
    public void handle(String s) {
        if (s.startsWith("splitter ")){
            this.splitter = s.trim().substring(8);
            this.client.msg("Splitter set to \"" + this.splitter + "\"");
        }else if (s.startsWith("question ")){
            String raw = s.substring(9).replace(this.splitter, SPLITTER) + ":" + this.client.getId();
            try{
                if (this.questionProcessor.giveQuestion(this.parse(raw), this, raw)){
                    System.out.println("MADE IT");
                }else{
                    System.out.println("REJECTED");
                }
            }catch(Exception e){
                this.client.msg(e.getMessage());
                e.printStackTrace();
            }
        }else if (s.startsWith("help")){
            this.client.msg("Parser Commands\n" +
                    "  splitter <splitter> - sets the question parsing splitter, default \":\"\n" +
                    "  question <type>:<difficulty>:<topic>:<question>:<answer> - attempts adding a question");
        }
    }
    @Override
    public String format(Question q) {
        String s = q.type().name() + SPLITTER;
        s += q.difficulty().name() + SPLITTER;
        s += q.topic().name() + SPLITTER;
        s += q.question() + SPLITTER;
        s += q.answer() + SPLITTER;
        s += q.authorID();
        return s;
    }
    @Override
    public Question parse(String s) {
        try{return new RawQuestion(s, this);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public String id(){
        return "raw";
    }
    private class RawQuestion implements Question {
        private Type type;
        private Difficulty difficulty;
        private Topic topic;
        private String question, answer, id;
        private Parser parser;
        private RawQuestion(String s, Parser parser) throws Exception {
            String[] strings = s.split(SPLITTER);
            this.type = Type.valueOf(strings[0].toUpperCase());
            this.difficulty = Difficulty.valueOf(strings[1].toUpperCase());
            this.topic = Topic.valueOf(strings[2].toUpperCase());
            this.question = strings[3];
            this.answer = strings[4];
            this.id = strings[5];
            this.parser = parser;
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
            return response.trim().toLowerCase().contains(this.answer.trim().toLowerCase()) ? Result.CORRECT : Result.INCORRECT;
        }
        @Override
        public String answer() {
            return this.answer;
        }
        @Override
        public String authorID() {
            return this.id;
        }
        @Override
        public String parserID() {
            return this.parser.id();
        }
    }
}
