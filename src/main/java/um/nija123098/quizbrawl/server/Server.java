package um.nija123098.quizbrawl.server;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IGuild;
import um.nija123098.quizbrawl.ArchServer;
import um.nija123098.quizbrawl.quizprovider.Brawler;
import um.nija123098.quizbrawl.quizprovider.PendingQuestionProcessor;
import um.nija123098.quizbrawl.quizprovider.QuizProvider;
import um.nija123098.quizbrawl.server.services.BotFuture;
import um.nija123098.quizbrawl.server.services.BotPool;
import um.nija123098.quizbrawl.server.services.ClientPool;
import um.nija123098.quizbrawl.server.services.InfoChannel;
import um.nija123098.quizbrawl.util.Log;
import um.nija123098.quizbrawl.util.RequestHandler;
import um.nija123098.quizbrawlkit.bot.Bot;
import um.nija123098.quizbrawlkit.question.Parser;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Made by nija123098 on 10/9/2016
 */
public class Server implements IListener<Event>{
    private ArchServer arch;
    private IGuild guild;
    private PendingQuestionProcessor questionProcessor;
    private BotPool botPool;
    private volatile QuizProvider quizProvider;
    private ClientPool clientPool;
    private IDiscordClient client;
    private InfoChannel infoChannel;
    public Server(List<String> tokens, List<Brawler> brawlers, List<Bot> bots, List<Parser> parsers, List<String> questions, List<String> pendingQuestions, ArchServer arch) {
        this.arch = arch;
        RequestHandler.request(() -> {
            this.client = new ClientBuilder().withToken(tokens.get(0)).login();
            Log.addLogChannel(new LogChannel(this.client));
            this.quizProvider = new QuizProvider(brawlers);
            this.client.getDispatcher().registerListener(this);
            this.infoChannel = new InfoChannel(this.client);
            this.botPool = new BotPool(tokens, bots, this, this.infoChannel);
            this.quizProvider.link(this.infoChannel);
        });
        this.questionProcessor = new PendingQuestionProcessor(questions, pendingQuestions, parsers, this.quizProvider, this.arch);
        new Timer("Savior").schedule(new TimerTask() {
            @Override
            public void run() {
                save();
            }
        }, 3600000, 3600000);
    }
    @Override
    public void handle(Event event) {
        if (event instanceof ReadyEvent){
            this.guild = ((ReadyEvent) event).getClient().getGuilds().get(0);
            this.clientPool = new ClientPool(this.client, this.quizProvider, this.guild, this, this.infoChannel);
            this.clientPool.postInit();
            this.botPool.bind(this.clientPool);
            if (this.guild.getChannelsByName("info").size() == 0){
                RequestHandler.request(() -> this.guild.createChannel("info"));
            }
            if (this.guild.getChannelsByName("log").size() == 0){
                RequestHandler.request(() -> this.guild.createChannel("log"));
            }
        }else if (event instanceof MessageReceivedEvent){
            if (/*((MessageReceivedEvent) event).getMessage().getAuthor().equals(this.guild.getOwner()) && */((MessageReceivedEvent) event).getMessage().getContent().equals("CLOSE")){
                this.close();
            }
        }
    }
    public BotFuture requestRoomEnter(String name, String id, ServerClient client){
        return this.botPool.getBot(name, id, client);
    }
    public BotFuture requestRoomEnter(String name, ServerClient client) {
        return this.botPool.getBot(name, client);
    }
    public void save(){
        Log.info("SAVING");
        this.arch.saveBrawlers(this.quizProvider.getBrawlers());
        this.questionProcessor.save();
    }
    public QuizProvider getProvider() {
        return this.quizProvider;
    }
    public PendingQuestionProcessor getQuestionProcessor() {
        return this.questionProcessor;
    }
    public IDiscordClient getDiscordClient(){// implement
        return this.client;
    }
    public void close(){
        this.save();
        Log.warn("CLOSING");
        try{
            this.questionProcessor.close();
            this.botPool.close();
            this.clientPool.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{this.guild.getChannelsByName("userchannel").forEach(iChannel -> RequestHandler.request(() -> iChannel.delete()));
        }catch(Exception e){
            e.printStackTrace();
        }
        RequestHandler.request(() -> this.client.logout());
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(7);
            }
        }, RequestHandler.requestCount() == 0 ? 100 : 3000);
    }
}
