package um.nija123098.quizbrawl.quizprovider;

import um.nija123098.quizbrawl.ArchServer;
import um.nija123098.quizbrawl.server.ServerClient;
import um.nija123098.quizbrawl.util.Log;
import um.nija123098.quizbrawlkit.question.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Made by nija123098 on 10/28/2016
 */
public class PendingQuestionProcessor implements QuestionProcessor {
    private final ArchServer arch;
    private final List<PendingQuestion> pendingQuestions;
    private final List<String> strings;
    private final QuizProvider quizProvider;
    private final List<Parser> parsers;
    public PendingQuestionProcessor(List<String> strings, List<String> pendingQuestions, List<Parser> parsers, QuizProvider quizProvider, ArchServer archServer){
        this.arch = archServer;
        this.parsers = parsers;
        this.parsers.forEach(parser -> parser.init(null, this));
        this.pendingQuestions = new ArrayList<PendingQuestion>(strings.size());
        this.quizProvider = quizProvider;
        this.strings = strings;
        this.strings.forEach(s -> this.parsers.stream().filter(parser -> parser.id().equals(s.split(":")[0])).forEach(parser -> this.quizProvider.addQuestion(parser.parse(s.substring(s.split(":")[0].length() + 1)))));
        pendingQuestions.forEach(s -> {
            PendingQuestion pq = this.parse(s);
            if (pq != null){
                this.pendingQuestions.add(pq);
            }
        });
        Log.info("Loaded " + this.strings.size() + " normal and " + this.pendingQuestions.size() + " pending questions");
    }
    @Override
    public boolean giveQuestion(Question question, Parser parser, String raw) {
        question = parser.parse(parser.format(question));// ought to be redundant
        if (question.type() != null && question.difficulty() != null && question.topic() != null && question.question() != null && question.answer() != null){
            try{Long.parseLong(question.authorID());
            }catch(Exception ignored){return false;}
            this.pendingQuestions.add(new PenQuest(question, raw, parser.id(), this));
            return true;
        }
        return false;
    }
    public Parser getParser(String id){
        for (Parser parser : this.parsers) {
            if (parser.id().trim().toLowerCase().equals(id.trim().toLowerCase())) {
                try{return parser.getClass().newInstance();
                }catch(InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    public PendingQuestion getQuestion(ServerClient client, List<PendingQuestion> exclude, EnumSet<Difficulty> difficulties, EnumSet<Type> types, EnumSet<Topic> topics, List<String> parserIDs){
        final PendingQuestion[] pq = new PendingQuestion[1];
        this.pendingQuestions.stream().filter(pendingQuestion -> !pendingQuestion.hasReviewed(client.id())).filter(pendingQuestion2 -> !exclude.contains(pendingQuestion2)).filter(pendingQuestion3 -> difficulties.contains(pendingQuestion3.getQuestion().difficulty())).filter(pendingQuestion4 -> types.contains(pendingQuestion4.getQuestion().type())).filter(pendingQuestion5 -> topics.contains(pendingQuestion5.getQuestion().topic())).filter(pendingQuestion6 -> parserIDs.contains(pendingQuestion6.getParserID())).limit(1).forEach(pendingQuestion1 -> pq[0] = pendingQuestion1);
        return pq[0];
    }
    private PendingQuestion parse(String s){
        String[] strings = s.split(":");
        for (Parser parser : this.parsers) {
            if (parser.id().equals(strings[0])) {//strings[2]
                String st = "";// s.substring(strings[0].length() + strings[1].length() - 1)
                for (int i = 2; i < strings.length; i++) {// make shorter ^
                    st += strings[i] + (i == strings.length - 1 ? "" : ":");
                }
                return new PenQuest(parser.parse(st), strings[1].split(";"), st, parser.id(), this);
            }
        }
        return null;
    }
    private String format(PendingQuestion pendingQuestion){
        String s = pendingQuestion.getQuestion().parserID() + ":";
        for (int i = 0; i < pendingQuestion.getReviewed().size(); i++) {
            s += pendingQuestion.getReviewed().get(i) + (i == pendingQuestion.getReviewed().size() - 1 ? "" : ";");
        }
        s += ":";
        // int l = s.length();
        for (int i = 0; i < this.parsers.size(); i++) {
            if (pendingQuestion.getQuestion().parserID().equals(this.parsers.get(i).id())){
                s += this.parsers.get(i).format(pendingQuestion.getQuestion());
                break;
            }
        }
        return s;
    }
    private void done(PenQuest penQuest) {
        this.addQuestion(penQuest.getParserID() + ":" + penQuest.getRaw());
        this.pendingQuestions.remove(penQuest);
    }// redo these two and part of init
    private void addQuestion(String rawPlus){
        for (Parser parser : this.parsers) {
            if (parser.id().equals(rawPlus.split(":")[0])) {
                this.strings.add(rawPlus);
                this.quizProvider.addQuestion(parser.parse(rawPlus.substring(rawPlus.split(":")[0].length() + 1)));
                return;
            }
        }
    }
    public void save(){
        List<String> strings = new ArrayList<String>(this.pendingQuestions.size());
        this.pendingQuestions.forEach(pendingQuestion -> strings.add(this.format(pendingQuestion)));
        this.arch.savePending(strings);
        this.arch.saveQuestions(this.strings);
    }
    public void close(){
        this.save();
    }
    private class PenQuest extends PendingQuestion{
        private PendingQuestionProcessor questionProcessor;
        public PenQuest(Question question, String raw, String parser, PendingQuestionProcessor questionProcessor) {
            super(question, raw, parser);
            this.questionProcessor = questionProcessor;
        }
        public PenQuest(Question question, String[] ids, String raw, String parser, PendingQuestionProcessor questionProcessor) {
            super(question, ids[0].equals("") ? new String[0] : ids, raw, parser);
            this.questionProcessor = questionProcessor;
        }
        @Override
        public void onComplete() {
            this.questionProcessor.done(this);
        }
    }
}
