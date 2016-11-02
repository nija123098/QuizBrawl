package um.nija123098.quizbrawl.bothandler;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import um.nija123098.quizbrawl.util.RequestHandler;
import um.nija123098.quizbrawlkit.bot.Message;

/**
 * Made by nija123098 on 10/11/2016
 */
public class MessageImpl implements Message {
    private volatile String id;
    private volatile String chanId;
    private final IGuild guild;
    private volatile Message message;
    public MessageImpl(IGuild guild) {
        this.guild = guild;
    }
    public void bind(IMessage iMessage){
        this.id = iMessage.getID();
        this.chanId = iMessage.getChannel().getID();
    }
    public void bind(Message message){
        this.message = message;
    }
    private void check(){
        if (this.message != null){
            this.id = ((MessageImpl) this.message).id;
            this.chanId = ((MessageImpl) this.message).chanId;
            this.message = null;
        }
    }
    @Override
    public String getText() {
        this.check();
        return this.guild.getChannelByID(this.chanId).getMessageByID(id).getContent();
    }
    @Override
    public void edit(String content) {
        this.check();
        RequestHandler.request(() -> this.guild.getChannelByID(this.chanId).getMessageByID(id).edit(content));
    }
    @Override
    public void deleate() {
        this.check();
        RequestHandler.request(() -> this.guild.getChannelByID(this.chanId).getMessageByID(id).delete());
    }
}
