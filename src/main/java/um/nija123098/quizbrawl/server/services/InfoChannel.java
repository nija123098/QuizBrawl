package um.nija123098.quizbrawl.server.services;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.*;
import um.nija123098.quizbrawl.util.RequestHandler;

/**
 * Made by nija123098 on 10/9/2016
 */
public class InfoChannel implements InfoLink {
    private static final String FORMAT = "Welcome to the Quiz Brawl server.\n" +
            "Quiz Brawl uses text to speech to read quiz questions out loud.\n" +
            "There are currently <mods-available> available moderators.\n" +
            "Most of those moderators are friendly, all non-human.\n" +
            "This server has <member-count> members who have gotten <correct> questions correct.\n" +
            "Of those members <member-online> are online." +
            // "Of those members <member-active> are online now";
            "";
    private int correct, modCount, memberCount, memberOnline;
    private IGuild guild;
    private IMessage message;
    private IDiscordClient client;
    public InfoChannel(IDiscordClient client) {
        this.client = client;
        this.client.getDispatcher().registerListener(this);
    }
    @EventSubscriber
    public void handle(PresenceUpdateEvent event){
        if (event.getUser().isBot() || event.getUser().equals(this.guild.getOwner())){
            return;
        }
        boolean onOld = !event.getOldPresence().equals(Presences.OFFLINE);
        boolean onNew = !event.getNewPresence().equals(Presences.OFFLINE);
        if (onOld != onNew){
            if (onNew){
                ++this.memberOnline;
            }else{
                --this.memberOnline;
            }
        }
        this.update();
    }
    @EventSubscriber
    public void handle(UserJoinEvent event){
        ++this.memberCount;
        this.update();
    }
    @EventSubscriber
    public void handle(UserLeaveEvent event){
        --this.memberCount;
        this.update();
    }
    @Override
    public void setBotsAvailable(int count) {
        this.modCount = count;
        this.update();
    }
    @Override
    public void addCorrect(){
        ++this.correct;
        this.update();
    }
    @Override
    public void setUsersOnline(int count){
        this.memberOnline = count;
        this.update();
    }
    @EventSubscriber
    public void handle(ReadyEvent event){
        this.guild = event.getClient().getGuilds().get(0);
        IRole mod = this.guild.getRolesByName("moderator").get(0);
        this.memberCount = (int) this.guild.getUsers().stream().filter(user -> !user.getRolesForGuild(this.guild).contains(mod)).count() - 2;// -2 for the operators
        this.memberOnline = (int) (this.guild.getUsers().stream().filter(user -> !user.getPresence().equals(Presences.OFFLINE)).filter(user1 -> !user1.getRolesForGuild(this.guild).get(0).getName().equals("moderator")).count() - 2);
        this.message = this.guild.getChannelsByName("info").get(0).getMessages().get(0);
        this.correct = this.getCorrect(this.message.getContent());
        this.update();
    }
    private void update(){
        try{
            if (this.message != null){
                RequestHandler.request(() -> this.message.edit(this.string()));
            }
        }catch(Exception ignored){}
        try{this.client.changeStatus(Status.game("with " + this.memberOnline + " player" + (this.memberOnline > 1 ? "s" : "")));
        }catch(Exception ignored){}
    }
    private String string(){
        return FORMAT.replace("<correct>", Integer.toString(this.correct))
                .replace("<mods-available>", Integer.toString(this.modCount))
                .replace("<member-count>", Integer.toString(this.memberCount))
                .replace("<member-online>", Integer.toString(this.memberOnline));
    }
    private int getCorrect(String s){
        String[] form = FORMAT.split(" ");
        for (int i = 0; i < form.length; i++) {
            if (form[i].equals("<correct>")){
                return Integer.parseInt(s.split(" ")[i]);
            }
        }
        return 0;
    }
}
