package um.nija123098.quizbrawl.bothandler;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.audio.AudioPlayer;
import um.nija123098.quizbrawl.defaultp.BaseBot;
import um.nija123098.quizbrawl.quizprovider.QuizProvider;
import um.nija123098.quizbrawl.server.BotPool;
import um.nija123098.quizbrawl.server.ClientPool;
import um.nija123098.quizbrawl.server.Server;
import um.nija123098.quizbrawl.server.ServerClient;
import um.nija123098.quizbrawl.util.FileHelper;
import um.nija123098.quizbrawl.util.Log;
import um.nija123098.quizbrawl.util.RequestHandler;
import um.nija123098.quizbrawlkit.bot.Bot;
import um.nija123098.quizbrawlkit.bot.BotLink;
import um.nija123098.quizbrawlkit.bot.Client;
import um.nija123098.quizbrawlkit.bot.Message;
import um.nija123098.quizbrawlkit.question.Difficulty;
import um.nija123098.quizbrawlkit.question.Question;
import um.nija123098.quizbrawlkit.question.Topic;
import um.nija123098.quizbrawlkit.question.Type;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Made by Dev on 10/10/2016
 */
public class BotHandler implements BotLink {
    private String name = "unknown";
    private boolean connected;
    private List<ClientImpl> clientImpls;
    private BotPool pool;
    private String chan, voiceChan;
    private AudioPlayer player;
    private IDiscordClient discordClient;
    private QuizProvider provider;
    private String token;
    private IGuild guild;
    private Bot bot;
    private List<Bot> bots;
    private ClientPool clientPool;
    public BotHandler(String token, Server server, List<Bot> bots, BotPool pool) {
        this.token = token;
        this.pool = pool;
        this.bots = bots;
        this.provider = server.getProvider();
        this.clientImpls = new ArrayList<ClientImpl>(2);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try{attemptLog();
                    this.cancel();
                    if (discordClient.isReady()){
                        Log.INFO.log("Log in success");
                    }
                }catch(Exception e){
                    Log.INFO.log("Bot failed to log in");
                }
            }
        }, 0, 3000);
    }
    private void attemptLog(){
        RequestHandler.request(() -> {
            discordClient = new ClientBuilder().withToken(token).login();
            discordClient.getDispatcher().registerListener(this);
        });
    }
    public void bind(ClientPool clientPool){
        this.clientPool = clientPool;
    }
    @Override
    public void setStatus(String status) {
        if (this.discordClient.isReady()){
            this.discordClient.changeStatus(status == null ? Status.empty() : Status.game(status));
        }
    }
    @Override
    public void pauseVoice(boolean pause) {
        this.player.setPaused(pause);
    }
    @Override
    public void clearVoice() {
        this.player.clear();
    }
    @Override
    public void playVoice(File file) throws IOException, UnsupportedAudioFileException{
        this.player.queue(file);
    }
    @Override
    public void playVoice(AudioInputStream stream) throws IOException {
        this.player.queue(stream);
    }
    @Override
    public Question getQuestion(EnumSet<Difficulty> difficulties, EnumSet<Topic> topics, EnumSet<Type> types) {
        return this.provider.getQuestion(difficulties, topics, types);
    }
    @Override
    public void abandonRoom() {
        this.bot.onLeaveRoom();
        if (this.roomName() != null){
            RequestHandler.request(() -> {
                this.getChannel().delete();
                this.chan = null;
            });
            RequestHandler.request(() -> {
                this.getVoiceChannel().delete();
                this.voiceChan = null;
            });
        }
        this.pool.provide(this);
        this.clientImpls = new ArrayList<ClientImpl>(2);
    }
    @EventSubscriber
    public void handle(GuildCreateEvent event){
        this.connected = true;
        this.guild = event.getGuild();
        this.player = new AudioPlayer(this.guild);
        this.pool.provide(this);
        this.bots.stream().filter(b -> b.botOptimal(this.discordClient.getOurUser().getName())).limit(1).forEach(b -> {
            this.bot = b;
            this.bots.remove(b);
            this.bots = null;
        });
        this.reattemptBotBind();
        this.bot.init(this);
        this.name = this.discordClient.getOurUser().getName();// todo fix below
        Log.INFO.log("Bot type " + this.bot.getClass().getSimpleName() + " inited with " + this.name + "'" + (this.name.endsWith("s") ? "s" : "") + " profile");
    }
    @EventSubscriber
    public void handle(MessageReceivedEvent event){
        if (event.getMessage().getChannel().getID().equals(this.chan)){
            this.bot.handle(event.getMessage().getContent(), this.getClient(((MessageReceivedEvent) event).getMessage().getAuthor().getID()));
        }
    }
    @EventSubscriber
    public void handle(TypingEvent event){
        if (event.getChannel().getID().equals(this.chan)){
            this.bot.onClientTyping(this.getClient(((TypingEvent) event).getUser().getID()));
        }
    }
    @EventSubscriber
    public void handle(DiscordDisconnectedEvent event){
        this.connected = false;
        this.pool.unprovide(this);
        try{this.abandonRoom();
        }catch(Exception ignored){}
        if (!event.getReason().name().equals("LOGGED_OUT")){
            Log.ERROR.log(this.name + " using " + this.bot.getClass().getSimpleName() + " disconnected because of " + ((DiscordDisconnectedEvent) event).getReason().name());
        }else{
            Log.INFO.log(this.name + " using " + this.bot.getClass().getSimpleName() + " logged out");
        }
    }
    @EventSubscriber
    public void handle(DiscordReconnectedEvent event){
        if (!this.connected){
            this.connected = true;
            this.pool.provide(this);
        }
        Log.INFO.log(this.name + " reconnected!");
    }
    @Override
    public File getTempFile(String extension){
        return FileHelper.provideTemporaryFile(extension);
    }
    @Override
    public Message messageRoom(String msg){
        MessageImpl message = new MessageImpl(this.guild);
        try{RequestHandler.request(() -> message.bind(this.getChannel().sendMessage(msg)));
        }catch(Exception ignored){}
        return message;
    }
    @Override
    public void setRoomInfo(String s){
        try{RequestHandler.request(() -> this.getChannel().changeTopic(s));
        }catch(NullPointerException ignored){}
    }
    public void reattemptBotBind(){// method call needs to happen once all optimal bot instances are claimed by their owner
        this.bot = new BaseBot();
        this.bots = null;
        /*if (this.bot == null && this.bots != null){// if one is true both should be
            this.bot = this.bots.remove(Ref.getInt(this.bots.size()));
        }*/
    }
    IChannel getChannel(){
        return this.guild.getChannelByID(this.chan);
    }
    IVoiceChannel getVoiceChannel(){
        return this.guild.getVoiceChannelByID(this.voiceChan);
    }
    public void setRoom(String s, ServerClient client){
        RequestHandler.request(() -> this.chan = this.guild.createChannel(s).getID());
        RequestHandler.request(() -> {
            voiceChan = guild.createVoiceChannel(s).getID();
            getVoiceChannel().join();
            this.bot.onNewRoom(s);
            requestJoin(client);
        });
        Log.INFO.log(this.name + " started room " + s + " for " + client.name());
    }
    public String roomName() {
        if (this.chan != null){
            return this.getChannel().getName();
        }
        return null;
    }
    public void requestJoin(ServerClient client) {
        this.bot.requestJoin(new ClientImpl(this.guild, this, client));
    }
    public void addToRoom(ClientImpl client){
        this.clientImpls.add(client);
    }
    public void removeFromRoom(ClientImpl client) {
        this.clientImpls.remove(client);
        if (this.clientImpls.size() == 0){
            this.abandonRoom();
            Log.INFO.log("Closing " + this.name + "' room because there are no more clients");
        }
    }
    public void leave(ServerClient client) {
        try{this.getClient(client).kick();
            this.bot.onClientLeave(this.getClient(client));
        }catch(UnsupportedOperationException | NullPointerException ignored){}
    }
    public Client getClient(String id){
        for (int i = 0; i < this.clientImpls.size(); i++) {
            if (this.clientImpls.get(i).getId().equals(id)){
                return this.clientImpls.get(i);
            }
        }
        ServerClient serverClient = this.clientPool.get(id);
        if (serverClient != null){
            return new LimitedClientImpl(this.guild, this, serverClient);
        }
        return null;
    }
    private Client getClient(ServerClient client){
        return this.getClient(client.id());
    }
    public String getId(){
        return this.discordClient.getOurUser().getID();
    }
    public void close() {
        if (this.roomName() != null){
            RequestHandler.request(() -> {
                this.getChannel().delete();
                this.chan = null;
            });
            RequestHandler.request(() -> {
                this.getVoiceChannel().delete();
                this.voiceChan = null;
            });
        }
        RequestHandler.request(() -> this.discordClient.logout());
    }
    public IDiscordClient getDiscordClient() {
        return this.discordClient;
    }
}
