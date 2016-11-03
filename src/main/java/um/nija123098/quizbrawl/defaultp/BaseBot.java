package um.nija123098.quizbrawl.defaultp;

import um.nija123098.quizbrawl.util.StringHelper;
import um.nija123098.quizbrawlkit.bot.Achievement;
import um.nija123098.quizbrawlkit.bot.Bot;
import um.nija123098.quizbrawlkit.bot.BotLink;
import um.nija123098.quizbrawlkit.bot.Client;
import um.nija123098.quizbrawlkit.question.Difficulty;
import um.nija123098.quizbrawlkit.question.Question;
import um.nija123098.quizbrawlkit.question.Result;
import um.nija123098.quizbrawlkit.question.Topic;
import um.nija123098.quizbrawlkit.question.Type;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Made by nija123098 on 10/20/2016
 */
public class BaseBot implements Bot, Runnable {
    private static final Map<Question, File> fileMap;
    static {
        fileMap = new HashMap<Question, File>();
    }
    private List<Question> exclusions;
    private volatile long qLength;
    private volatile Client attempter;
    private volatile boolean qMode;
    private Question question;
    private List<Client> clients;
    private List<Client> wrong;
    private int pending;
    private BotLink link;
    private final EnumSet<Difficulty> difficulties;
    private final EnumSet<Type> types;
    private final EnumSet<Topic> topics;
    public BaseBot() {
        this.difficulties = EnumSet.allOf(Difficulty.class);
        this.types = EnumSet.allOf(Type.class);
        this.topics = EnumSet.allOf(Topic.class);
        this.exclusions = new ArrayList<Question>(3);
    }
    @Override
    public void init(BotLink botLink) {
        this.link = botLink;
    }
    @Override
    public void handle(String s, Client client) {
        s = s.toLowerCase();
        if (s.startsWith("leave")){
            client.kick();
        }else{
            if (this.qMode){
                this.answer(s, client);
            }else if (s.startsWith("next") || s.trim().equals("n")){
                this.next();
            }else if (s.startsWith("request ")){
                String[] strings = s.replace("<@", ":::::::").replace(">", ":::::::").split(":::::::");
                for (int i = 1; i < strings.length; i++) {
                    try {Long.parseLong(strings[i]);
                        this.link.getClient(strings[i]).msg("<@" + client.getId() + "> requests your presence");
                    }catch(Exception ignored){}
                }
            }else if (s.startsWith("add ")){
                String[] strings = s.substring(4).toUpperCase().split(" ");
                for (String st : strings) {
                    try{this.difficulties.add(Difficulty.valueOf(st));
                    }catch(Exception i){
                        try{this.types.add(Type.valueOf(st));
                        }catch(Exception ig){
                            try{this.topics.add(Topic.valueOf(st));
                            }catch(Exception ignored){}
                        }
                    }
                }
                this.updateStatus();
            }else if (s.startsWith("remove ")){
                String[] strings = s.substring(4).toUpperCase().split(" ");
                for (String st : strings) {
                    try{this.difficulties.remove(Difficulty.valueOf(st));
                    }catch(Exception i){
                        try{this.types.remove(Type.valueOf(st));
                        }catch(Exception ig){
                            try{this.topics.remove(Topic.valueOf(st));
                            }catch(Exception ignored){}
                        }
                    }
                }
                this.updateStatus();
            }else if (s.startsWith("exclusive ")){
                String[] strings = s.substring(4).toUpperCase().split(" ");
                EnumSet<Difficulty> difficulties = EnumSet.noneOf(Difficulty.class);
                EnumSet<Type> types = EnumSet.noneOf(Type.class);
                EnumSet<Topic> topics = EnumSet.noneOf(Topic.class);
                for (String st : strings) {
                    try{difficulties.add(Difficulty.valueOf(st));
                    }catch(Exception i){
                        try{types.add(Type.valueOf(st));
                        }catch(Exception ig){
                            try{topics.add(Topic.valueOf(st));
                            }catch(Exception ignored){}
                        }
                    }
                }
                if (difficulties.size() > 0){
                    this.difficulties.clear();
                }
                this.difficulties.addAll(difficulties);
                if (types.size() > 0){
                    this.types.clear();
                }
                this.types.addAll(types);
                if (topics.size() > 0){
                    this.topics.clear();
                }
                this.topics.addAll(topics);
                this.updateStatus();
            }else if (s.startsWith("help")){
                this.link.messageRoom("Commands:\n" +
                        "  next - plays the next question\n" +
                        "  leave - leaves the room\n" +
                        "  add <topic/type/difficultly>... - adds that question qualifier from the possbile questions\n" +
                        "  remove <topic/type/difficultly>... - removes that question qualifier from the possible questions\n" +
                        "  exclusive <topic/type/difficultly>... - reqires all questions to have all the listed traits\n" +
                        "  request @player... invites the player to the room");
            }
        }
    }
    @Override
    public void requestJoin(Client client) {
        this.clients.add(client);
        client.add();
        this.link.messageRoom("<@" + client.getId() + "> joined the room!");
        if (!this.qMode){
            client.enableTyping(true);
        }
    }
    @Override
    public void onNewRoom(String name) {
        if (this.clients != null){
            for (int i = 0; i < this.clients.size(); i++) {
                this.clients.get(i).kick();
            }
        }
        this.clients = new ArrayList<Client>(2);
        this.qMode = false;
        this.wrong = new ArrayList<Client>(1);
        this.difficulties.addAll(EnumSet.allOf(Difficulty.class));
        this.types.addAll(EnumSet.allOf(Type.class));
        this.topics.addAll(EnumSet.allOf(Topic.class));
        this.updateStatus();
        this.link.setStatus("with " + name);
    }
    @Override
    public void onLeaveRoom() {
        this.link.setStatus(null);
    }
    @Override
    public void onAchievementEarn(Achievement achievement, Client client) {
        this.link.messageRoom(client.getName() + " earned " + achievement.name());
    }
    @Override
    public void onClientLeave(Client client) {
        client.msg("Bye. Bye.");
        this.clients.remove(client);
        this.link.messageRoom("<@" + client.getId() + "> left the room");
    }
    @Override
    public void onClientTyping(Client client) {
        if (this.qMode){
            this.buzz(client);
        }
    }
    @Override
    public boolean botOptimal(String name) {
        return true;
    }
    @Override
    public String makerID(){
        return "191677220027236352";
    }
    public void updateStatus(){
        String s = "Playing ";
        if (this.types.size() == Type.values().length){
            s += "all";
        }else{
            List<String> typ = new ArrayList<String>(Type.values().length);
            typ.addAll(this.types.stream().map(Type::name).collect(Collectors.toList()));
            s += StringHelper.getList(typ);
        }
        s += " types on ";
        if (this.difficulties.size() == Difficulty.values().length){
            s += "all";
        }else{
            List<String> dif = new ArrayList<String>(Difficulty.values().length);
            dif.addAll(this.difficulties.stream().map(Difficulty::name).collect(Collectors.toList()));
            s += StringHelper.getList(dif);
        }
        s += " difficulties with ";
        if (this.topics.size() == Topic.values().length){
            s += "all";
        }else{
            List<String> top = new ArrayList<String>(Topic.values().length);
            top.addAll(this.topics.stream().map(Topic::name).collect(Collectors.toList()));
            s += StringHelper.getList(top);
        }
        s += " topics";
        this.link.setRoomInfo(s);
    }
    private void answer(String attempt, Client attempter){
        this.buzz(attempter);
        Result result = this.question.result(attempt);
        this.attempter.attempt(result, this.question);
        switch (result){
            case BONUS:
                this.link.messageRoom("BONUS!");
            case CORRECT:
                if (result == Result.CORRECT){
                    this.link.messageRoom("Correct!");
                }
                this.reset();
                break;
            case PROMPT:
                this.link.messageRoom("Prompt");
                this.buzz(this.attempter);
                break;
            case INCORRECT:
                this.link.messageRoom("incorrect");
                if (this.clients.size() == this.wrong.size() + this.pending + 1){
                    this.timeUp();
                }else{
                    this.attempter.enableTyping(false);
                    this.wrong.add(this.attempter);
                    this.unbuzz();
                }
                this.attempter = null;
        }
    }
    private void buzz(Client client){
        this.link.pauseVoice(true);
        this.attempter = client;
        for (int i = 0; i < this.clients.size(); i++) {
            if (!(this.clients.get(i) == client || this.wrong.contains(this.clients.get(i)))){
                this.clients.get(i).enableTyping(false);
            }
        }
    }
    private void unbuzz(){
        this.link.pauseVoice(false);
        for (int i = 0; i < this.clients.size(); i++) {
            if (!this.wrong.contains(this.clients.get(i))){
                this.clients.get(i).enableTyping(true);
            }
        }
    }
    private void buzzUp(){
        this.answer("INCORRECT ANSWER", this.attempter);
    }
    private void next(){
        Question question = this.link.getQuestion(this.difficulties, this.topics, this.types, this.exclusions);
        if (question.answer().equals("unfound")){
            this.exclusions.remove(0);
            this.next();
        }else{
            if (this.exclusions.size() > 3){
                this.exclusions.remove(0);
            }
            this.exclusions.add(question);
        }
        this.qMode = true;
        this.link.clearVoice();
        this.link.pauseVoice(true);
        this.question = question;
        File f = fileMap.get(this.question);
        if (f == null) {
            f = this.link.getTempFile("mp3");
            fileMap.put(this.question, f);
            this.qLength = Util.getSynth(f, this.question.question()) + 3000;
        }else{
            this.qLength = Util.getFileTime(f);
        }
        final File finalF = f;
        Regard.less(() -> this.link.playVoice(finalF));
        new Thread(this).start();
        this.link.pauseVoice(false);
    }
    private void timeUp(){
        this.reset();
        this.link.messageRoom("The correct answer was \"" + this.question.answer() + '\"');
    }
    private void reset(){
        this.qMode = false;
        for (int i = 0; i < this.clients.size(); i++) {
            this.clients.get(i).enableTyping(true);
        }
        this.wrong = new ArrayList<Client>(1);
        this.pending = 0;
    }
    @Override
    public void run(){
        long last = System.currentTimeMillis();
        long qPassed = 0;
        long bPassed = 0;
        while (true){
            long delta = System.currentTimeMillis() - last;
            last = System.currentTimeMillis();
            if (delta >= 1){
                if (this.attempter != null){
                    bPassed += delta;
                    if (bPassed >= 3000){
                        this.buzzUp();
                    }
                }else{
                    bPassed = 0;
                    qPassed += delta;
                    if (qPassed >= this.qLength){
                        this.timeUp();
                    }
                }
            }
            if (!this.qMode){
                break;
            }
        }
    }
    @FunctionalInterface
    private interface Regard{
        static void less(Regard regard){
            try {
                regard.less();
            }catch(Throwable e){
                e.printStackTrace();// if it were actually regardless this wouldn't be here
            }
        }
        void less() throws Throwable;
    }
}
