package um.nija123098.quizbrawl.quizprovider;

import um.nija123098.quizbrawl.server.services.InfoLink;
import um.nija123098.quizbrawl.util.Ref;
import um.nija123098.quizbrawlkit.question.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Made by nija123098 on 10/9/2016
 */
public class QuizProvider {
    private final List<Brawler> brawlers;
    private final List<Question> questions;
    private InfoLink infoLink;
    public QuizProvider(List<Brawler> brawlers) {
        this.brawlers = brawlers;
        this.questions = new ArrayList<Question>(20);
    }
    public Brawler getBrawler(String id){
        for (int i = 0; i < this.brawlers.size(); i++) {
            if (id.equals(this.brawlers.get(i).id)){
                return this.brawlers.get(i);
            }
        }
        return this.make(id);
    }
    private synchronized Brawler make(String id){
        Brawler brawler = new Brawler(id);
        this.brawlers.add(brawler);
        brawler.link(this.infoLink);
        return brawler;
    }
    public Question getQuestion(EnumSet<Difficulty> difficulties, EnumSet<Topic> topics, EnumSet<Type> types){
        List<Question> questions = new ArrayList<Question>(this.questions.size() / Difficulty.values().length / Topic.values().length / Type.values().length);
        questions.addAll(this.questions.stream().filter(question -> difficulties.contains(question.difficulty()) && topics.contains(question.topic()) && types.contains(question.type())).collect(Collectors.toList()));
        if (questions.size() == 0){
            return new Unfound(difficulties, topics, types);
        }
        return questions.get(Ref.getInt(questions.size() - 1));
    }
    public Question getQuestion(EnumSet<Difficulty> difficulties, EnumSet<Topic> topics, EnumSet<Type> types, List<Question> exclusions){
        List<Question> questions = new ArrayList<Question>(this.questions.size() / Difficulty.values().length / Topic.values().length / Type.values().length);
        questions.addAll(this.questions.stream().filter(question -> difficulties.contains(question.difficulty()) && topics.contains(question.topic()) && types.contains(question.type())).collect(Collectors.toList()));
        questions.removeAll(exclusions);
        if (questions.size() == 0){
            return new Unfound(difficulties, topics, types);
        }
        return questions.get(Ref.getInt(questions.size() - 1));
    }
    protected void addQuestion(Question question){
        if (question != null){
            this.questions.add(question);
        }
    }
    public List<Brawler> getBrawlers() {
        return this.brawlers;
    }
    public synchronized void link(InfoLink infoLink){
        this.infoLink = infoLink;
        for (Brawler brawler : this.brawlers) {
            brawler.link(this.infoLink);
        }
    }
    private class Unfound implements Question{
        private EnumSet<Difficulty> dif;
        private EnumSet<Topic> top;
        private EnumSet<Type> typ;
        public Unfound(EnumSet<Difficulty> dif, EnumSet<Topic> top, EnumSet<Type> typ) {
            this.dif = dif;
            this.top = top;
            this.typ = typ;
        }
        @Override
        public Type type() {
            return Type.UNKNOWN;
        }
        @Override
        public Difficulty difficulty() {
            return Difficulty.EASY;
        }
        @Override
        public Topic topic() {
            return Topic.NONE;
        }
        @Override
        public String question() {
            String s = "Apparently there are no questions that contain all of these conditions: ";
            for (Difficulty dif : this.dif) {
                s += dif.name() + " ";
            }
            for (Topic top : this.top) {
                s += top.name() + " ";
            }
            for (Type typ : this.typ) {
                s += typ.name() + " ";
            }
            return s.substring(0, s.length() - 1);
        }
        @Override
        public Result result(String response) {
            return response.toLowerCase().equals("unfound") ? Result.CORRECT : Result.INCORRECT;
        }
        @Override
        public String answer() {
            return "unfound";
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
