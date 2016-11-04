package um.nija123098.quizbrawl.server;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.util.MessageBuilder;
import um.nija123098.quizbrawl.util.RequestHandler;

/**
 * Made by nija123098 on 11/4/2016
 */
public class LogChannel {
    private String id;
    private Guild guild;
    private IDiscordClient discordClient;
    private LogChannel(IDiscordClient discordClient){
        this.discordClient = discordClient;
        this.discordClient.getDispatcher().registerListener(this);
    }
    public void trace(String s){
        this.msg(s, null);
    }
    public void info(String s){
        this.msg(s, MessageBuilder.Styles.UNDERLINE);
    }
    public void warn(String s){
        this.msg(s, MessageBuilder.Styles.ITALICS);
    }
    public void error(String s){
        this.msg(s, MessageBuilder.Styles.UNDERLINE_BOLD_ITALICS);
    }
    private void msg(String s, MessageBuilder.Styles styles){
        MessageBuilder builder = new MessageBuilder(this.discordClient).withChannel(this.guild.getChannelByID(this.id));
        if (styles == null){
            builder.withContent(s);
        }else{
            builder.withContent(s, styles);
        }
        RequestHandler.request(builder::send);
    }
}
