package um.nija123098.quizbrawl.bothandler;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Permissions;
import um.nija123098.quizbrawl.server.ServerClient;
import um.nija123098.quizbrawl.util.Log;
import um.nija123098.quizbrawl.util.PermisionsHelper;
import um.nija123098.quizbrawl.util.RequestHandler;
import um.nija123098.quizbrawlkit.bot.Client;
import um.nija123098.quizbrawlkit.bot.Message;
import um.nija123098.quizbrawlkit.bot.Team;
import um.nija123098.quizbrawlkit.question.*;

/**
 * Made by nija123098 on 10/10/2016
 */
public class ClientImpl implements Client {
    private Team team;
    protected IGuild guild;
    private BotHandler handler;
    private ServerClient client;
    public ClientImpl(IGuild guild, BotHandler handler, ServerClient client) {
        this.guild = guild;
        this.handler = handler;
        this.client = client;
        this.team = Team.NONE;
    }
    @Override
    public void add() {// may want to use permisions helper
        new PermisionsHelper(this.client.user(), this.handler.getChannel()).addAllow(Permissions.READ_MESSAGES, Permissions.READ_MESSAGE_HISTORY).enact();
        new PermisionsHelper(this.client.user(), this.handler.getVoiceChannel()).addAllow(Permissions.VOICE_CONNECT).enact();
        this.handler.addToRoom(this);// this line should be redundant
        this.client.set(this.handler);
    }
    @Override
    public void kick() {
        if (this.handler != null){
            try{
                RequestHandler.request(() -> this.handler.getChannel().removePermissionsOverride(this.client.user()));
            }catch(NullPointerException ignored){
                Log.warn("Attempted kicking a user from a imaginary room");
            }
            try{
                RequestHandler.request(() -> this.handler.getVoiceChannel().removePermissionsOverride(this.client.user()));
            }catch(NullPointerException ignored){
                Log.warn("Attempted kicking a user from a imaginary voice room");
            }
            this.mute(false);
            this.deafen(false);
            this.handler.removeFromRoom(this);
            this.handler = null;
            this.client.leave();
        }
    }
    @Override
    public void mute(boolean mute) {
        RequestHandler.request(() -> this.guild.setMuteUser(this.client.user(), mute));
    }
    @Override
    public void deafen(boolean deafen) {
        RequestHandler.request(() -> this.guild.setDeafenUser(this.client.user(), deafen));
    }
    @Override
    public Message msg(String s) {
        MessageImpl message = new MessageImpl(this.guild);
        try{RequestHandler.request(() -> {
            if (this.handler == null){
                message.bind(this.client.msg(s));
            }else{
                message.bind(this.client.msg(s, this.handler.getDiscordClient()));
            }
        });
        }catch(Exception ignored){}
        return message;
    }
    @Override
    public void attempt(Result r, Question q) {
        this.client.brawler().process(q.difficulty(), q.topic(), q.type(), r);
    }
    @Override
    public void setTeam(Team team) {
        RequestHandler.request(() -> this.client.user().removeRole(this.guild.getRolesByName(this.team.name().toLowerCase()).get(0)));
        this.team = team;
        RequestHandler.request(() -> this.client.user().addRole(this.guild.getRolesByName(this.team.name().toLowerCase()).get(0)));
    }
    @Override
    public void enableTyping(boolean enable) {
        Log.debug((enable ? "allowing" : "disallowing") + " " + this.getName() + "'s tying permisions");
        PermisionsHelper helper = new PermisionsHelper(this.client.user(), this.handler.getChannel()).addAllow(Permissions.READ_MESSAGES);
        if (enable){
            helper.addAllow(Permissions.SEND_MESSAGES);
        }else{
            helper.addDeny(Permissions.SEND_MESSAGES);
        }
        helper.enact();
    }
    @Override
    public long stat(Difficulty difficulty, Topic topic, Type type, Result result){
        return this.client.brawler().get(difficulty, topic, type, result);
    }
    @Override
    public String getName(){
        return this.client.user().getName();
    }
    @Override
    public String getId() {
        return this.client.id();
    }
}
