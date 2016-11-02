package um.nija123098.quizbrawl;

import org.apache.commons.io.FileUtils;
import um.nija123098.quizbrawl.defaultp.BaseBot;
import um.nija123098.quizbrawl.defaultp.RawParser;
import um.nija123098.quizbrawl.quizprovider.Brawler;
import um.nija123098.quizbrawl.server.Server;
import um.nija123098.quizbrawl.util.FileHelper;
import um.nija123098.quizbrawl.util.Log;
import um.nija123098.quizbrawlkit.bot.Bot;
import um.nija123098.quizbrawlkit.question.Parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Made by Dev on 10/13/2016
 */
public class ArchServer {
    private Server server;
    public ArchServer(String path) throws Exception{
        List<String> tokens;
        try{
            tokens = Files.readAllLines(Paths.get(FileHelper.getJarPath(), "Tokens.txt"));
        }catch(NoSuchFileException e){
            Files.createFile(Paths.get(FileHelper.getJarPath(), "Tokens.txt"));
            System.out.println("Please place tokens in \"Tokens.txt\"");
            return;
        }/*
        List<QuestionPack> packs = new ArrayList<QuestionPack>();
        File[] pFiles = new File(path + "\\questionpacks").listFiles();
        if (pFiles != null){
            for (int i = 0; i < pFiles.length; i++) {
                if (pFiles[i].getName().endsWith(".jar")){
                    int count = packs.size();
                    packs.addAll(FileHelper.grabInstances(QuestionPack.class, pFiles[i].getPath()));
                    Log.INFO.log("Loaded " + (packs.size() - count) + " QuestionPacks from " + pFiles[i].getName());
                }
            }
        }
        Log.INFO.log("Loaded " + packs.size() + " QuestionPacks total");
        if (packs.size() == 0){
            packs.add(new DefaultQuestionPack());
        }*/
        List<Bot> bots = new CopyOnWriteArrayList<Bot>();
        File[] bFiles = new File(path + "\\bots").listFiles();
        if (bFiles != null){
            for (int i = 0; i < bFiles.length; i++) {
                if (bFiles[i].getName().endsWith(".jar")){
                    int count = bots.size();
                    // bots.addAll(FileHelper.grabInstances(Bot.class, bFiles[i].getPath()));
                    Log.info("Loaded " + (bots.size() - count) + " Bots from "  + bFiles[i].getName());
                }
            }
        }
        Log.info("Loaded " + bots.size() + " Bots total");
        while (bots.size() != tokens.size() - 1){
            bots.add(new BaseBot());
        }
        List<Parser> parsers = new ArrayList<Parser>();
        File[] pFiles = new File(path + "\\parsers").listFiles();
        if (bFiles != null){
            for (int i = 0; i < pFiles.length; i++) {
                if (pFiles[i].getName().endsWith(".jar")){
                    int count = parsers.size();
                    bots.addAll(FileHelper.grabInstances(Bot.class, bFiles[i].getPath()));
                    Log.info("Loaded " + (parsers.size() - count) + " Parsers from "  + pFiles[i].getName());
                }
            }
        }
        Log.info("Loaded " + parsers.size() + " Parsers total");
        parsers.add(new RawParser());
        FileHelper.assureExistance(FileHelper.getJarPath() + "\\userdata");
        /*File[] brawlerFiles = new File(FileHelper.getJarPath()).listFiles((dir, name) -> {
            return name.equals("userdata");
        })[0].listFiles();*/
        File[] brawlerFiles = new File(Paths.get(FileHelper.getJarPath(), "userdata").toString()).listFiles();
        List<Brawler> brawlers = new ArrayList<Brawler>(brawlerFiles.length);
        for (File file : brawlerFiles) {
            brawlers.add(Brawler.parse(Files.readAllLines(file.toPath())));
        }
        FileHelper.assureExistance(FileHelper.getJarPath() + "\\processingquestions");
        File[] pQFiles = new File(Paths.get(FileHelper.getJarPath(), "processingquestions").toString()).listFiles();
        List<String> pendingQuestions = new ArrayList<String>(pQFiles.length);
        for (File file : pQFiles) {
            pendingQuestions.addAll(Files.readAllLines(file.toPath()));
        }
        FileHelper.assureExistance(FileHelper.getJarPath() + "\\questions");
        File[] qFiles = new File(Paths.get(FileHelper.getJarPath(), "questions").toString()).listFiles();
        List<String> questions = new ArrayList<String>(qFiles.length);
        for (File file : qFiles) {
            questions.addAll(Files.readAllLines(file.toPath()));
        }
        this.server = new Server(tokens, brawlers, bots, parsers, questions, pendingQuestions, this);
    }
    public ArchServer() throws Exception{
        this(FileHelper.getJarPath());
    }
    public void saveBrawlers(List<Brawler> brawlers){
        try {
            FileUtils.cleanDirectory(new File(FileHelper.getJarPath() + "\\userdata"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < brawlers.size(); i++) {
            try {
                Files.write(Paths.get(new File(FileHelper.getJarPath() + "\\userdata\\brawler" + i).toString()), Brawler.format(brawlers.get(i)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void saveQuestions(List<String> questions){
        try {
            FileUtils.cleanDirectory(new File(FileHelper.getJarPath() + "\\questions"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Files.write(Paths.get(new File(FileHelper.getJarPath() + "\\questions\\questions").toString()), questions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void savePending(List<String> pendingQuestions){
        try {FileUtils.cleanDirectory(new File(FileHelper.getJarPath() + "\\processingquestions"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {Files.write(Paths.get(new File(FileHelper.getJarPath() + "\\processingquestions\\processingquestions").toString()), pendingQuestions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
