package um.nija123098.quizbrawl.server;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import um.nija123098.quizbrawl.bothandler.BotHandler;
import um.nija123098.quizbrawl.quizprovider.Brawler;
import um.nija123098.quizbrawl.server.room.ParserRoom;
import um.nija123098.quizbrawl.server.room.ReviewRoom;
import um.nija123098.quizbrawl.server.room.UserRoom;
import um.nija123098.quizbrawl.server.services.ClientPool;
import um.nija123098.quizbrawlkit.bot.Message;

/**
 * Made by Dev on 10/9/2016
 */
public class ServerClient {
    private ReviewRoom reviewRoom;
    private ParserRoom parserRoom;
    private Server server;
    private BotHandler botHandler;
    private UserRoom userRoom;
    private Brawler brawler;
    private ClientPool pool;
    private String userID;
    private IGuild guild;
    public ServerClient(IGuild guild, String userID, Brawler brawler, IDiscordClient discordClient, ClientPool pool, Server server) {
        this.guild = guild;
        this.userID = userID;
        this.brawler = brawler;
        this.server = server;
        this.userRoom = new UserRoom(this, discordClient, this.server, this.guild);
        this.pool = pool;
    }
    public IUser user(){
        return this.guild.getUserByID(this.userID);
    }
    public Brawler brawler(){
        return this.brawler;
    }
    public void set(BotHandler botHandler){
        this.leave();
        this.botHandler = botHandler;
    }
    public void set(ParserRoom parserRoom){
        if (!parserRoom.failed()){
            this.parserRoom = parserRoom;
        }
    }
    public void set(ReviewRoom reviewRoom) {
        this.leaveReviewer();
        this.reviewRoom = reviewRoom;
    }
    public void leave(){
        if (this.botHandler != null){
            this.botHandler.leave(this);
            this.botHandler = null;
        }
    }
    public void leaveParser(){
        if (this.parserRoom != null){
            this.parserRoom.close();
            this.parserRoom = null;
        }
    }
    public void leaveReviewer() {
        if (this.reviewRoom != null){
            this.reviewRoom.close();
            this.reviewRoom = null;
        }
    }
    public String id() {
        return this.userID;
    }
    public String name() {
        return this.user().getName();
    }
    public Message msg(String s) {
        return this.userRoom.msg(s);
    }
    public Message msg(String s, IDiscordClient user){
        return this.userRoom.msg(s, user);
    }
    public void logout(){
        this.leave();
        this.leaveParser();
        this.leaveReviewer();
        this.userRoom.close();
    }
}
