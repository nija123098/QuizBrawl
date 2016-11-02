package um.nija123098.quizbrawl.quizprovider;

import um.nija123098.quizbrawl.server.services.InfoLink;
import um.nija123098.quizbrawlkit.question.Difficulty;
import um.nija123098.quizbrawlkit.question.Result;
import um.nija123098.quizbrawlkit.question.Topic;
import um.nija123098.quizbrawlkit.question.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Made by nija123098 on 10/9/2016
 */
public class Brawler {
    public String id;
    private final long[][][][] stats;
    private InfoLink infoLink;
    public Brawler(String id) {
        this(id, new long[Difficulty.values().length][Topic.values().length][Type.values().length][Result.values().length]);
    }
    Brawler(String id, long[][][][] stats) {
        this.id = id;
        this.stats = stats;
    }
    public long get(Difficulty difficulty, Topic topic, Type type, Result result){
        return this.stats[difficulty.ordinal()][topic.ordinal()][type.ordinal()][result.ordinal()];
    }
    public long process(Difficulty difficulty, Topic topic, Type type, Result result){
        if (result == Result.CORRECT){
            this.infoLink.addCorrect();
        }
        return ++this.stats[difficulty.ordinal()][topic.ordinal()][type.ordinal()][result.ordinal()];
    }
    public static List<String> format(Brawler brawler){
        List<String> strings = new ArrayList<String>();
        strings.add(brawler.id);
        MBuilder builder = new MBuilder();
        for (int i = 0; i < Difficulty.values().length; i++) {
            builder.add(Difficulty.values()[i]);
            for (int j = 0; j < Topic.values().length; j++) {
                builder.add(Topic.values()[j]);
                for (int k = 0; k < Type.values().length; k++) {
                    builder.add(Type.values()[k]);
                    for (int l = 0; l < Result.values().length; l++) {
                        builder.add(Result.values()[l]);
                        builder.add(brawler.stats[i][j][k][l]);
                    }
                }
            }
        }
        strings.add(builder.get());
        return strings;
    }
    public static Brawler parse(List<String> strings) {
        Difficulty difficulty = null;
        Topic topic = null;
        Type type = null;
        Result result = null;
        long[][][][] stats = new long[Difficulty.values().length][Topic.values().length][Type.values().length][Result.values().length];
        String[] sps = strings.get(1).split(":");
        for (String s : sps) {
            try{ difficulty = Difficulty.valueOf(s);
            }catch(Exception i){
                try{ topic = Topic.valueOf(s);
                }catch(Exception ig){
                    try{ type = Type.valueOf(s);
                    }catch(Exception ign){
                        try{ result = Result.valueOf(s);
                        }catch(Exception igno){
                            stats[difficulty.ordinal()][topic.ordinal()][type.ordinal()][result.ordinal()] = Long.parseLong(s);
                        }
                    }
                }
            }
        }
        return new Brawler(strings.get(0), stats);
    }
    public void link(InfoLink infoLink) {
        this.infoLink = infoLink;
    }
    private static class MBuilder{
        String s = "";
        void add(Enum en){
            this.s += en.name() + ':';
        }
        String get(){
            return s.substring(0, s.length() - 1);
        }
        public void add(long l) {
            this.s += l + ":";
        }
    }
}
