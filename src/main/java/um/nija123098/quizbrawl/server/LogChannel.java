package um.nija123098.quizbrawl.server;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.handle.impl.events.DiscordReconnectedEvent;
import sx.blah.discord.handle.impl.events.GuildCreateEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.MessageBuilder;
import um.nija123098.quizbrawl.util.RequestHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Made by nija123098 on 11/4/2016
 */
public class LogChannel {
    private boolean connected;
    private String id;
    private IGuild guild;
    private IDiscordClient discordClient;
    private Map<String, MessageBuilder.Styles> messages;
    public LogChannel(IDiscordClient discordClient){
        this.discordClient = discordClient;
        this.discordClient.getDispatcher().registerListener(this);
        this.messages = new HashMap<String, MessageBuilder.Styles>();
    }
    @EventSubscriber
    public void handle(GuildCreateEvent event){
        this.guild = event.getGuild();
        this.id = this.guild.getChannelsByName("log").get(0).getID();
        this.connected = true;
        RequestHandler.request(() -> this.guild.getChannelByID(this.id).getMessages().deleteAfter(0));
    }
    @EventSubscriber
    public void handle(DiscordDisconnectedEvent event){
        this.connected = true;
    }
    @EventSubscriber
    public void handle(DiscordReconnectedEvent event){
        this.connected = false;
        this.messages.forEach((s, styles) -> this.send(s.substring(1), styles));
        this.messages = new HashMap<String, MessageBuilder.Styles>();
    }
    public void trace(String s){
        this.msg(s, null);
    }
    public void debug(String s) {
        this.msg(s, MessageBuilder.Styles.ITALICS);
    }
    public void info(String s){
        this.msg(s, MessageBuilder.Styles.UNDERLINE);
    }
    public void warn(String s){
        this.msg(s, MessageBuilder.Styles.BOLD_ITALICS);
    }
    public void error(String s){
        this.msg(s, MessageBuilder.Styles.UNDERLINE_BOLD_ITALICS);
    }
    private void msg(String s, MessageBuilder.Styles styles){
        if (this.connected){
            this.send(s, styles);
        }else{
            this.messages.put(this.messages.size() + s, styles);
        }
    }
    private void send(String s, MessageBuilder.Styles styles){
        MessageBuilder builder = new MessageBuilder(this.discordClient).withChannel(this.guild.getChannelByID(this.id));
        if (styles == null){
            builder.withContent(s);
        }else{
            builder.withContent(s, styles);
        }
        try{RequestHandler.request(builder::send);
        }catch(Throwable throwable){
            throwable.printStackTrace();
        }
    }
}
