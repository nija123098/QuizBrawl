package um.nija123098.quizbrawl.server;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.impl.events.UserLeaveEvent;
import sx.blah.discord.handle.obj.*;
import um.nija123098.quizbrawl.util.RequestHandler;

import java.lang.reflect.Method;

/**
 * Made by Dev on 10/9/2016
 */
public class InfoChannel {// todo add more info, probably use log listener or other system?
    private static final String FORMAT = "Welcome to the Quiz Brawl server.\n" +
            "Quiz Brawl uses text to speech to read quiz questions out loud.\n" +
            // "There are currently <mods-available> available moderators.\n" +
            // "Most of those moderators are friendly, all non-human.\n" +
            "This server has <member-count> members.\n" +
            "Of those members <member-online> are online." +
            // "Of those members <member-active> are online now";
            "";
    private int modsAvailable, memberCount, memberOnline;
    private String chan;
    private IGuild guild;
    private IMessage message;
    private IDiscordClient client;
    public InfoChannel(IDiscordClient client) {
        this.client = client;
        this.client.getDispatcher().registerListener(this);
    }
    @EventSubscriber
    public void handleE(Event event){
        Method[] methods = InfoChannel.class.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(EventSubscriber.class) && method.getName().equals("handle") && event.getClass() == method.getParameterTypes()[0]) {
                this.update();
                return;
            }
        }
    }
    @EventSubscriber
    public void handle(PresenceUpdateEvent event){
        boolean onOld = !event.getOldPresence().equals(Presences.OFFLINE);
        boolean onNew = !event.getNewPresence().equals(Presences.OFFLINE);
        if (onOld != onNew){
            if (onNew){
                ++this.memberOnline;
            }else{
                --this.memberOnline;
            }
        }
    }
    @EventSubscriber
    public void handle(UserJoinEvent event){
        ++this.memberCount;
    }
    @EventSubscriber
    public void handle(UserLeaveEvent event){
        --this.memberCount;
    }
    @EventSubscriber
    public void handle(GuildCreateEvent event){
        this.guild = event.getGuild();
        IRole mod = this.guild.getRolesByName("moderator").get(0);
        this.memberCount = (int) this.guild.getUsers().stream().filter(user -> !user.getRolesForGuild(this.guild).contains(mod)).count() - 2;// -2 for the operators
        this.memberOnline = (int) (this.guild.getUsers().stream().filter(user -> !user.getPresence().equals(Presences.OFFLINE)).filter(user1 -> !user1.getRolesForGuild(this.guild).get(0).getName().equals("moderator")).count() - 2);
        this.message = this.guild.getChannelsByName("info").get(0).getMessages().get(0);
        this.update();
    }
    /*
    @Override
    public void handle(Event event) {
        boolean update = true;
        if (event instanceof PresenceUpdateEvent){// UNFORTUNATELY INDIVIDUAL METHODS AND REFLECTION REFUSES TO WORK
            boolean onOld = !((PresenceUpdateEvent) event).getOldPresence().equals(Presences.OFFLINE);
            boolean onNew = !((PresenceUpdateEvent) event).getNewPresence().equals(Presences.OFFLINE);
            if (onOld != onNew){
                if (onNew){
                    ++this.memberOnline;
                }else{
                    --this.memberOnline;
                }
            }
        }else if (event instanceof UserJoinEvent){
            ++this.memberCount;
        }else if (event instanceof UserLeaveEvent) {
            --this.memberCount;
        }else if (event instanceof GuildCreateEvent){
            this.guild = ((GuildCreateEvent) event).getGuild();
            IRole mod = this.guild.getRolesByName("moderator").get(0);
            this.memberCount = (int) this.guild.getUsers().stream().filter(user -> !user.getRolesForGuild(this.guild).contains(mod)).count() - 2;// -2 for the operators
            this.memberOnline = (int) (this.guild.getUsers().stream().filter(user -> !user.getPresence().equals(Presences.OFFLINE)).filter(user1 -> !user1.getRolesForGuild(this.guild).get(0).getName().equals("moderator")).count() - 2);
            this.message = this.guild.getChannelsByName("info").get(0).getMessages().get(0);
        }else{
            update = false;
        }
        if (update){
            this.update();
        }
    }//*/
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
        return FORMAT.replace("<mods-available>", Integer.toString(this.modsAvailable))
                .replace("<member-count>", Integer.toString(this.memberCount))
                .replace("<member-online>", Integer.toString(this.memberOnline));
    }
}
