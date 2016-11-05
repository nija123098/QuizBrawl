package um.nija123098.quizbrawl.server.room;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Permissions;
import um.nija123098.quizbrawl.bothandler.MessageImpl;
import um.nija123098.quizbrawl.server.Server;
import um.nija123098.quizbrawl.server.ServerClient;
import um.nija123098.quizbrawl.util.*;
import um.nija123098.quizbrawlkit.bot.Message;
import um.nija123098.quizbrawlkit.question.Difficulty;
import um.nija123098.quizbrawlkit.question.Result;
import um.nija123098.quizbrawlkit.question.Topic;
import um.nija123098.quizbrawlkit.question.Type;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Made by nija123098 on 10/9/2016
 */
public class UserRoom {// should change to command structure
    private IDiscordClient discordClient;
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
            this.guild.getChannelByID(this.id).changeTopic("This is the main user page which only you can see");
        });
        RequestHandler.request(() -> this.client.user().addRole(this.guild.getRolesByName("user").get(0)));
        this.msg("It is recommended that you mute this server including mentions if you use the Discord mobile client due to mentions being broken on it.\nTo see a list of commands just type \"help\".");
    }
    @EventSubscriber
    public void handle(MessageReceivedEvent event) {
        if (event.getMessage().getChannel().getID().equals(this.id)){
            if (event.getMessage().getAttachments().size() == 0){
                this.handle(event.getMessage().getContent());
            }else{
                Log.info("File " + event.getMessage().getAttachments().get(0).getFilename() + " was uploaded");
            }
        }
    }
    private synchronized void handle(String handle){
        handle = handle.toLowerCase();
        if (handle.contains("help")){
            this.msg("Commands:\n" +
                    "  join <room name> - joins a room with the given name\n" +
                    "  join <@moderator> - joins the current room of the mentioned moderator if the moderator is in use\n" +
                    "  join <room name> <@moderator> - joins a room with the mentioned moderator and name if are available\n" +
                    "  stats <difficulty/type/topic/result> <difficulty/type/topic/result> <difficulty/type/topic/result>... - shows stats for the given question attributes\n" +
                    "  parser <type> - opens a room used to suggest questions where type is the type of question builder, raw by default\n" +
                    "  review - opens a room used to review suggested questions, this is only available for certified reviewers\n" +
                    "  dev - uploads the QuizBrawlKit jar file for developing for the server");
        }else if (handle.startsWith("join ")){
            this.client.leave();
            if (handle.substring(5).startsWith("<@")){
                this.client.set(this.server.requestRoomEnter(handle.replaceAll("<@", ":::::::").replaceAll(">", ":::::::").split(":::::::")[1], this.client));
            }else if (handle.contains("<@")){
                if (!StringHelper.exclusiveLetters(handle.substring(5).split(" ")[0])){
                    this.msg("Only letters are allowed in room names");
                    return;
                }
                this.server.requestRoomEnter(handle.substring(5).split(" ")[0], handle.substring(5).split(" ")[1].replace("<@", "").replace(">", ""), this.client);
            }else{
                if (!StringHelper.exclusiveLetters(handle.substring(5))){
                    this.msg("Only letters are allowed in room names");
                    return;
                }
                this.client.set(this.server.requestRoomEnter(handle.split(" ")[1].toLowerCase(), this.client));
            }
        }else if (handle.startsWith("leave")){
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
        }else if (handle.startsWith("parser")){
            if (handle.length() == 6){
                this.client.set(new ParserRoom(this.discordClient, "raw", this.guild, this.client, this.server.getQuestionProcessor()));
            }else{
                this.client.set(new ParserRoom(this.discordClient, handle.substring(7), this.guild, this.client, this.server.getQuestionProcessor()));
            }
        }else if (handle.startsWith("review")){
            if (this.client.user().getRolesForGuild(this.guild).contains(this.guild.getRolesByName("reviewer").get(0))){
                this.client.set(new ReviewRoom(this.discordClient, this.guild, this.client, this.server));
            }else{
                this.msg("You are not a certified reviewer, dm " + this.guild.getOwner().mention() + " to apply");
            }
        }else if (handle.startsWith("dev")){
            boolean found = false;
            for (File file : new File(FileHelper.getJarPath()).listFiles()) {
                if (!file.isDirectory() && file.getName().endsWith(".jar") && file.getName().contains("QuizBrawlKit-")){
                    RequestHandler.request(() -> {
                        try {this.guild.getChannelByID(this.id).sendFile(file);
                        }catch(IOException e) {
                            this.msg("There seems to have been an error uploading the jar, the developer has been notified");
                            Log.error("Error uploading QuizBrawlKit jar: "  + e.getMessage());
                        }
                    });
                    found = true;
                    break;
                }
            }
            if (!found){
                this.msg("There seems to have been an error uploading the jar, the developer has been notified");
                Log.error("No QuizBrawlKit jar in path!");
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
