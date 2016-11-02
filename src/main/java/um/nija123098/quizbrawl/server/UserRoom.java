package um.nija123098.quizbrawl.server;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Permissions;
import um.nija123098.quizbrawl.bothandler.MessageImpl;
import um.nija123098.quizbrawl.util.PermisionsHelper;
import um.nija123098.quizbrawl.util.RequestHandler;
import um.nija123098.quizbrawl.util.StringHelper;
import um.nija123098.quizbrawlkit.bot.Message;
import um.nija123098.quizbrawlkit.question.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Made by Dev on 10/9/2016
 */
public class UserRoom implements IListener<Event> {// should change to command structure
    private IDiscordClient discordClient;
    private BotFuture future;
    private String id;// find some way to share commands between here and BotHandler
    private ServerClient client;
    private IGuild guild;
    private Server server;
    public UserRoom(ServerClient client, IDiscordClient discordClient, Server server, IGuild guild){
        this.client = client;
        this.discordClient = discordClient;
        this.server = server;
        discordClient.getDispatcher().registerListener(this);
        this.guild = guild;
        RequestHandler.request(() -> {
            this.id = this.guild.createChannel("userchannel").getID();
            RequestHandler.request(() -> new PermisionsHelper(this.client.user(), this.guild.getChannelByID(this.id))
                    .addAllow(Permissions.SEND_MESSAGES)
                    .addAllow(Permissions.READ_MESSAGES)
                    .addAllow(Permissions.READ_MESSAGE_HISTORY).enact());
        });
        RequestHandler.request(() -> this.client.user().addRole(this.guild.getRolesByName("user").get(0)));
        this.msg("It is recommended that you mute this server including mentions if you use the Discord mobile client due to mentions being broken on it.\nTo see a list of commands just type \"help\".");
    }
    @Override
    public void handle(Event event) {
        if (event instanceof MessageReceivedEvent){
            if (((MessageReceivedEvent) event).getMessage().getChannel().getID().equals(this.id)){
                this.handle(((MessageReceivedEvent) event).getMessage().getContent());
            }
        }
    }
    private void handle(String handle){
        handle = handle.toLowerCase();
        if (handle.contains("help")){
            this.msg("Commands:\n" +// TODO: 10/21/2016 make proper command structure
                    "  join <room name> - joins a room with the given name\n" +
                    "  join <@moderator> - joins the current room of the mentioned moderator if the moderator is in use\n" +
                    "  join <room name> <@moderator> - joins a room with the mentioned moderator if the name and moderator are available\n" +
                    "  stats <difficulty/type/topic/result> <difficulty/type/topic/result> <difficulty/type/topic/result>... - shows stats for the given question attributes");
        }else if (handle.startsWith("join ")){
            this.client.leave();
            if (this.future != null){
                this.future.cancel(true);
            }
            if (handle.substring(5).startsWith("<@")){
                this.future = this.server.requestRoomEnter(handle.replaceAll("<@", ":::::::").replaceAll(">", ":::::::").split(":::::::")[1], this.client);
            }else if (handle.contains("<@")){
                if (!StringHelper.exclusiveLetters(handle.substring(5).split(" ")[0])){
                    this.msg("Only letters are allowed in room names");
                    return;
                }
                this.server.requestRoomEnter(handle.substring(5).split(" ")[0], handle.substring(5).split(" ")[1].replace("<@", "").replace(">", ""), this.client);
            }else{
                this.future = this.server.requestRoomEnter(handle.split(" ")[1].toLowerCase(), this.client);
            }
        }else if (handle.startsWith("leave")){
            if (this.future != null){
                this.future.cancel(true);
            }
            this.client.leave();
        }else if (handle.startsWith("stats ")){// multiple status of the same type should use recursion
            String[] strings = handle.substring(6).toUpperCase().split(" ");
            Difficulty difficulty = null;
            Topic topic = null;
            Type type = null;
            Result result = null;
            for (String s : strings) {
                try{difficulty = Difficulty.valueOf(s);
                }catch(Exception i){
                    try{topic = Topic.valueOf(s);
                    }catch(Exception ig){
                        try{type = Type.valueOf(s);
                        }catch(Exception ign){
                            try{result = Result.valueOf(s);
                            }catch(Exception ignored){}
                        }
                    }
                }
            }
            int count = 0;
            if(difficulty != null){++count;}
            if(topic != null){++count;}
            if(type != null){++count;}
            if(result != null){++count;}
            switch (count){
                case 4:
                    this.msg("You have been " + result.name() + " on " + this.client.brawler().get(difficulty, topic, type, result) + " " + difficulty.name() + " " + topic.name() + " " + type.name() + " questions!");
                    break;
                case 3:
                    if (difficulty == null){
                        String s = "You have gotten ";
                        List<String> list = new ArrayList<String>(Difficulty.values().length);
                        for (int i = 0; i < Difficulty.values().length; i++) {
                            list.add(this.client.brawler().get(difficulty, topic, type, Result.values()[i]) + " on " + Difficulty.values()[i].name());
                        }
                        this.msg(s + " " + StringHelper.getList(list) + " " + result.name() + " " + topic.name() + " " + type.name() + " questions!");
                    }else if (topic == null){
                        String s = "You have gotten ";
                        List<String> list = new ArrayList<String>(Topic.values().length);
                        for (int i = 0; i < Topic.values().length; i++) {
                            list.add(this.client.brawler().get(difficulty, Topic.values()[i], type, result) + " on " + Difficulty.values()[i].name());
                        }
                        this.msg(s + " " + StringHelper.getList(list) + " " + result.name() + " " + difficulty.name() + " " + type.name() + " questions!");
                    }else if (type == null) {
                        String s = "You have gotten ";
                        List<String> list = new ArrayList<String>(Type.values().length);
                        for (int i = 0; i < Type.values().length; i++) {
                            list.add(this.client.brawler().get(difficulty, topic, Type.values()[i], result) + " on " + Difficulty.values()[i].name());
                        }
                        this.msg(s + " " + StringHelper.getList(list) + " " + difficulty.name() + " " + topic.name() + " " + result.name() + " questions!");
                    }else if (result == null) {
                        String s = "You have been ";
                        List<String> list = new ArrayList<String>(Result.values().length);
                        for (int i = 0; i < Result.values().length; i++) {
                            list.add(Result.values()[i].name() + " on " + this.client.brawler().get(difficulty, topic, type, Result.values()[i]));
                        }
                        this.msg(s + " " + StringHelper.getList(list) + " " + difficulty.name() + " " + topic.name() + " " + type.name() + " questions!");
                    }
                    break;
                case 2:
                    this.msg("Charts are not currently available but will be added in a future version, please specify the question three or four question attributes for now");
                    break;
                default:
                    this.msg("Not enough question attributes were specified or some were spelled wrong.  Please input three or four attributes for now.  Tables for two attributes will be in a future version.");
            }
        }else if (handle.startsWith("parser ")){
            this.client.set(new ParserRoom(this.discordClient, handle.substring(7), this.guild, this.client, this.server.getQuestionProcessor()));
        }else if (handle.startsWith("review")){
            if (this.client.user().getRolesForGuild(this.guild).contains(this.guild.getRolesByName("reviewer").get(0))){
                this.client.set(new ReviewRoom(this.discordClient, this.guild, this.client, this.server));
            }
        }
    }
    public Message msg(String content){
        MessageImpl message = new MessageImpl(this.guild);
        RequestHandler.request(() -> message.bind(this.guild.getChannelByID(this.id).sendMessage(content)));
        return message;
    }
    public Message msg(String content, IDiscordClient user){
        MessageImpl message = new MessageImpl(user.getGuilds().get(0));
        RequestHandler.request(() -> message.bind(user.getGuilds().get(0).getChannelByID(this.id).sendMessage(content)));
        return message;
    }
    public void close() {
        if (this.id != null){
            RequestHandler.request(() -> {
                this.guild.getChannelByID(this.id).delete();
                this.id = null;
            });
            RequestHandler.request(() -> this.client.user().removeRole(this.guild.getRolesByName("user").get(0)));
        }
    }
}
