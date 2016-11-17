package um.nija123098.quizbrawl.quizprovider;

import um.nija123098.quizbrawlkit.question.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Made by Dev on 10/28/2016
 */
public abstract class PendingQuestion {
    private final String raw;
    private final Question question;
    private final List<String> reviewIDs;
    private final String parser;
    public PendingQuestion(Question question, String raw, String parser) {
        this.raw = raw;
        this.question = question;
        this.parser = parser;
        this.reviewIDs = new ArrayList<String>(3);
    }
    public PendingQuestion(Question question, String[] ids, String raw, String parser){
        this(question, raw, parser);
        Collections.addAll(this.reviewIDs, ids);
    }
    public String getRaw(){
        return this.raw;
    }
    public Question getQuestion(){
        return this.question;
    }
    public boolean hasReviewed(String id){
        return this.reviewIDs.contains(id);
    }
    public boolean setReviewed(String id){
        this.reviewIDs.add(id);
        if (this.reviewIDs.size() > 2){
            this.onComplete();
            return true;
        }
        return false;
    }
    public List<String> getReviewed() {
        return this.reviewIDs;
    }
    public String getParserID() {
        return this.parser;
    }
    public abstract void onComplete();
}
