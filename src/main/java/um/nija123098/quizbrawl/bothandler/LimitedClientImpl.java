package um.nija123098.quizbrawl.bothandler;

import sx.blah.discord.handle.obj.IGuild;
import um.nija123098.quizbrawl.server.ServerClient;
import um.nija123098.quizbrawl.util.RequestHandler;
import um.nija123098.quizbrawlkit.bot.Message;
import um.nija123098.quizbrawlkit.bot.Team;
import um.nija123098.quizbrawlkit.question.Question;
import um.nija123098.quizbrawlkit.question.Result;

/**
 * Made by Dev on 10/21/2016
 */
public class LimitedClientImpl extends ClientImpl{
    private String chan;
    public LimitedClientImpl(IGuild guild, BotHandler handler, ServerClient client) {
        super(guild, handler, client);
    }
    public LimitedClientImpl(IGuild guild, ServerClient client){
        super(guild, null, client);
    }
    public void setChannel(String s){
        this.chan = s;
    }
    @Override
    public Message msg(String s){
        MessageImpl message = new MessageImpl(this.guild);
        RequestHandler.request(() -> message.bind(this.guild.getChannelByID(this.chan).sendMessage(s)));
        return message;
    }
    @Override
    public void add() {
        throw new UnsupportedOperationException();
    }
    @Override
    public void kick() {
        throw new UnsupportedOperationException();
    }
    @Override
    public void mute(boolean mute) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void deafen(boolean deafen) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void attempt(Result r, Question q) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void setTeam(Team team) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void enableTyping(boolean enable) {
        throw new UnsupportedOperationException();
    }
}
