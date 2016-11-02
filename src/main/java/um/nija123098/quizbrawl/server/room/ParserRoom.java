package um.nija123098.quizbrawl.server.room;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Permissions;
import um.nija123098.quizbrawl.bothandler.LimitedClientImpl;
import um.nija123098.quizbrawl.bothandler.MessageImpl;
import um.nija123098.quizbrawl.quizprovider.PendingQuestionProcessor;
import um.nija123098.quizbrawl.server.ServerClient;
import um.nija123098.quizbrawl.util.PermisionsHelper;
import um.nija123098.quizbrawl.util.RequestHandler;
import um.nija123098.quizbrawlkit.bot.Message;
import um.nija123098.quizbrawlkit.question.Parser;

/**
 * Made by nija123098 on 10/30/2016
 */
public class ParserRoom implements IListener<MessageReceivedEvent> {
    private Parser parser;
    private ServerClient client;
    private String chan;
    private IGuild guild;
    public ParserRoom(IDiscordClient discordClient, String parserID, IGuild guild, ServerClient client, PendingQuestionProcessor questionProcessor) {
        this.parser = questionProcessor.getParser(parserID);
        if (this.parser == null){
            client.msg("There is no parser by the name \"" + parserID + "\"");
            return;
        }
        discordClient.getDispatcher().registerListener(this);
        this.client = client;
        this.guild = guild;
        RequestHandler.request(() -> {
            this.chan = this.guild.createChannel("parserchannel").getID();
            new PermisionsHelper(this.client.user(), this.channel()).addAllow(Permissions.SEND_MESSAGES, Permissions.READ_MESSAGE_HISTORY, Permissions.READ_MESSAGES).enact();
            LimitedClientImpl c = new LimitedClientImpl(this.guild, this.client);
            c.setChannel(this.chan);
            this.parser.init(c, questionProcessor);
            this.msg("Parser ready");
        });
    }
    @Override
    public void handle(MessageReceivedEvent event) {
        if (event.getMessage().getChannel().getID().equals(this.chan)){
            String s = event.getMessage().getContent().toLowerCase();
            if (s.startsWith("help")){
                this.msg("Commands:\n" +
                        "  leave - leaves the current parserchannel");
            }else if (s.startsWith("leave")){
                this.client.leaveParser();
            }
            this.parser.handle(event.getMessage().getContent());
        }
    }
    private IChannel channel(){
        return this.guild.getChannelByID(this.chan);
    }
    private Message msg(String s){
        MessageImpl message = new MessageImpl(this.guild);
        RequestHandler.request(() -> message.bind(this.channel().sendMessage(s)));
        return message;
    }
    public boolean failed() {
        return this.parser == null;
    }
    public void close() {
        RequestHandler.request(() -> this.channel().delete());
    }
}
