package um.nija123098.quizbrawl.server.services;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Presences;
import um.nija123098.quizbrawl.quizprovider.QuizProvider;
import um.nija123098.quizbrawl.server.Server;
import um.nija123098.quizbrawl.server.ServerClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Made by Dev on 10/13/2016
 */
public class ClientPool implements IListener<PresenceUpdateEvent>{
    private List<ServerClient> clients;
    private IDiscordClient client;
    private QuizProvider provider;
    private IGuild guild;
    private Server server;
    public ClientPool(IDiscordClient client, QuizProvider provider, IGuild guild, Server server) {
        this.clients = new ArrayList<ServerClient>();
        this.client = client;
        this.provider = provider;
        this.guild = guild;
        this.server = server;
        this.client.getDispatcher().registerListener(this);
    }
    public void postInit(){
        List<IUser> users = this.guild.getUsers();
        for (int i = 0; i < users.size(); i++) {
            this.add(users.get(i).getID());
        }
    }
    @Override
    public void handle(PresenceUpdateEvent event) {
        boolean o = !event.getOldPresence().equals(Presences.OFFLINE);
        boolean n = !event.getNewPresence().equals(Presences.OFFLINE);
        if (o != n){
            if (n){
                this.add(event.getUser().getID());
            }else{
                this.remove(event.getUser().getID());
            }
        }
    }
    private void add(String id){
        IUser user = this.guild.getUserByID(id);
        if (!(user.isBot() || user.getID().equals(this.guild.getOwner().getID()) || user.getPresence().equals(Presences.OFFLINE))){
            this.clients.add(new ServerClient(this.guild, id, this.provider.getBrawler(id), this.client, this, this.server));
        }
    }
    void remove(String id){
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).id().equals(id)){
                this.clients.remove(i).logout();
                return;
            }
        }
    }
    public ServerClient get(String id) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).id().equals(id)){
                return this.clients.get(i);
            }
        }
        return null;
    }
    public void close() {
        while (this.clients.size() != 0){
            this.clients.remove(0).logout();
        }
    }
}
