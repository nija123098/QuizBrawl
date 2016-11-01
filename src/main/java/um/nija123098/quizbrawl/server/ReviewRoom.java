package um.nija123098.quizbrawl.server;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Permissions;
import um.nija123098.quizbrawl.bothandler.MessageImpl;
import um.nija123098.quizbrawl.quizprovider.PendingQuestion;
import um.nija123098.quizbrawl.quizprovider.PendingQuestionProcessor;
import um.nija123098.quizbrawl.util.PermisionsHelper;
import um.nija123098.quizbrawl.util.RequestHandler;
import um.nija123098.quizbrawlkit.bot.Message;
import um.nija123098.quizbrawlkit.question.Difficulty;
import um.nija123098.quizbrawlkit.question.Topic;
import um.nija123098.quizbrawlkit.question.Type;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Made by Dev on 10/30/2016
 */
public class ReviewRoom implements IListener<MessageReceivedEvent> {
    private PendingQuestion pendingQuestion;
    private List<PendingQuestion> skipped;
    private PendingQuestionProcessor questionProcessor;
    private ServerClient client;
    private String chan;
    private IGuild guild;
    private EnumSet<Difficulty> difficulties;
    private EnumSet<Type> types;
    private EnumSet<Topic> topics;
    private List<String> parserIDs;
    public ReviewRoom(IDiscordClient discordClient, IGuild guild, ServerClient client, Server server) {
        discordClient.getDispatcher().registerListener(this);
        this.client = client;
        this.guild = guild;
        this.questionProcessor = server.getQuestionProcessor();
        RequestHandler.request(() -> {
            this.chan = this.guild.createChannel("reviewchannel").getID();
            new PermisionsHelper(this.client.user(), this.channel()).addAllow(Permissions.SEND_MESSAGES, Permissions.READ_MESSAGE_HISTORY, Permissions.READ_MESSAGES).enact();
            this.msg("Review ready");
        });
        this.difficulties = EnumSet.allOf(Difficulty.class);
        this.types = EnumSet.allOf(Type.class);
        this.topics = EnumSet.allOf(Topic.class);
        this.parserIDs = new ArrayList<String>();
        this.parserIDs.add("raw");
        this.skipped = new ArrayList<PendingQuestion>();
    }
    @Override
    public void handle(MessageReceivedEvent event) {
        if (event.getMessage().getChannel().getID().equals(this.chan)){
            String s = event.getMessage().getContent().toLowerCase();
            if (s.startsWith("help")){
                this.msg("Commands:\n" +
                        "  leave - leaves the current reviewchannel\n" +
                        "  next - goes to the next pending question\n" +
                        "  approve - approves the current question");
                        /*
                        "  add <attribute/parser> - adds qualifiers to filter\n" +
                        "  remove <attribute/parser> - removes qualifiers from filter"
                         */
            }else if (s.startsWith("leave")){
                this.client.leaveReviewer();
            }else if (s.startsWith("next")){
                this.pendingQuestion = this.questionProcessor.getQuestion(this.client, this.skipped, this.difficulties, this.types, this.topics, this.parserIDs);
                if (this.pendingQuestion == null){
                    this.msg("There are no current questions matching the filter");
                    return;
                }
                this.msg("Question: \"" + this.pendingQuestion.getQuestion().question() + "\"\n" +
                        "Parser: \"" + this.pendingQuestion.getParserID() + "\"\n" +
                        "Raw: \"" + this.pendingQuestion.getRaw() + "\"\n" +// TODO ADD RAW TO QUESTION
                        "Answer: \"" + this.pendingQuestion.getQuestion().answer() + "\"\n" +
                        "Attributes: " + this.pendingQuestion.getQuestion().type() + " " + this.pendingQuestion.getQuestion().difficulty() + " " + this.pendingQuestion.getQuestion().topic());
            }else if (s.startsWith("approve")){
                this.pendingQuestion.setReviewed(this.client.id());
            }
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
    public void close() {
        RequestHandler.request(() -> this.channel().delete());
    }
}
