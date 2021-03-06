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
import um.nija123098.quizbrawl.server.services.BotPool;
import um.nija123098.quizbrawl.server.services.ClientPool;
import um.nija123098.quizbrawl.server.Server;
import um.nija123098.quizbrawl.server.ServerClient;
import um.nija123098.quizbrawl.util.FileHelper;
import um.nija123098.quizbrawl.util.Log;
import um.nija123098.quizbrawl.util.RequestHandler;
import um.nija123098.quizbrawl.util.StringHelper;
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
 * Made by nija123098 on 10/10/2016
 */
public class BotHandler implements BotLink {
    private volatile String name = "unknown";
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
        attemptLog();
    }
    private void attemptLog(){
        RequestHandler.request(() -> {
            this.discordClient = new ClientBuilder().withToken(this.token).login();
            this.discordClient.getDispatcher().registerListener(this);
        });
    }
    private void postInit(IGuild guild){
        if (this.guild == null){
            this.guild = guild;
            this.connected = true;
            this.player = new AudioPlayer(this.guild);
            this.name = this.discordClient.getOurUser().getName();
            this.bots.stream().filter(b -> b.botOptimal(this.name)).limit(1).forEach(b -> {
                this.bot = b;
                this.bots.remove(b);
                this.bots = null;
            });
            this.reattemptBotBind();
            this.bot.init(this);
        }
    }
    public void bind(ClientPool clientPool){
        this.clientPool = clientPool;
    }
    @EventSubscriber
    public void handle(GuildCreateEvent event){
        this.postInit(event.getGuild());
        Log.info("Bot type " + this.bot.getClass().getSimpleName() + " inited with " + StringHelper.getPossessive(this.name) + " profile");
        this.pool.provide(this);
    }
    @EventSubscriber
    public void handle(MessageReceivedEvent event){
        if (event.getMessage().getChannel().getID().equals(this.chan)){
            this.bot.handle(event.getMessage().getContent(), this.getClient(event.getMessage().getAuthor().getID()));
        }
    }
    @EventSubscriber
    public void handle(TypingEvent event){
        if (event.getChannel().getID().equals(this.chan)){
            this.bot.onClientTyping(this.getClient(event.getUser().getID()));
        }
    }
    @EventSubscriber
    public void handle(DisconnectedEvent event){
        this.connected = false;
        this.pool.unprovide(this);
        try{this.abandonRoom();
        }catch(Exception e){
            e.printStackTrace();
        }
        if (this.guild == null){
            Log.error("A bot disconnected on log in and has not been post-inited, it will be on reconnect");// TODO WORKING ON
            return;
        }
        String behaviorName = this.bot.getClass().getSimpleName();
        if (!event.getReason().name().equals("LOGGED_OUT")){
            Log.error(this.name + " using " + behaviorName + " disconnected because of " + event.getReason().name());
        }else{
            Log.info(this.name + " using " + behaviorName + " logged out");
        }
    }
    @EventSubscriber
    public void handle(ReconnectSuccessEvent event){
        if (event.getClient().getGuilds().size() == 0){
            Log.error("Mysterious reconnection with no guilds bug!");
        }
        if (!this.connected){
            if (this.guild == null){
                RequestHandler.request(() -> {
                    this.discordClient.logout();
                    RequestHandler.request(1000, () -> this.discordClient.login());
                });
                return;
            }
            this.postInit(event.getClient().getGuilds().get(0));
            this.connected = true;
            this.pool.provide(this);
        }
        Log.info(this.name + " reconnected!");
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
    public Question getQuestion(EnumSet<Difficulty> difficulties, EnumSet<Topic> topics, EnumSet<Type> types, List<Question> exclusions) {
        return this.provider.getQuestion(difficulties, topics, types, exclusions);
    }
    @Override
    public void abandonRoom() {
        if (this.roomName() != null){
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
        if (this.bot == null){
            this.bot = new BaseBot();
            this.bots = null;
        }
    }
    public void bindBot(){
    }
    IChannel getChannel(){
        return this.guild.getChannelByID(this.chan);
    }
    IVoiceChannel getVoiceChannel(){
        return this.guild.getVoiceChannelByID(this.voiceChan);
    }
    public void setRoom(String s, ServerClient client){
        this.pool.unprovide(this);
        RequestHandler.request(() -> {
            this.chan = this.guild.createChannel(s).getID();
            RequestHandler.request(() -> {
                this.voiceChan = this.guild.createVoiceChannel(s).getID();
                getVoiceChannel().join();
                this.bot.onNewRoom(s);
                requestJoin(client);
            });
        });
        Log.info(this.name + " started room " + s + " for " + client.name());
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
            Log.info("Closing " + StringHelper.getPossessive(this.name) + " room because there are no more clients");
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
