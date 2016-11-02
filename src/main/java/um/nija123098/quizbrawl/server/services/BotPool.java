package um.nija123098.quizbrawl.server.services;

import um.nija123098.quizbrawl.bothandler.BotHandler;
import um.nija123098.quizbrawl.server.Server;
import um.nija123098.quizbrawl.server.ServerClient;
import um.nija123098.quizbrawl.util.Log;
import um.nija123098.quizbrawlkit.bot.Bot;

import java.util.ArrayList;
import java.util.List;

/**
 * Made by Dev on 10/10/2016
 */
public class BotPool {
    private volatile List<BotHandler> all, available;
    private volatile List<BotFuture> futures;
    private InfoLink infoLink;
    public BotPool(List<String> tokens, List<Bot> bots, Server server, InfoLink infoLink){
        this.available = new ArrayList<BotHandler>(tokens.size() - 1);
        this.all = new ArrayList<BotHandler>(tokens.size() - 1);
        this.futures = new ArrayList<BotFuture>(1);
        this.infoLink = infoLink;
        for (int i = 1; i < tokens.size(); i++) {
            this.all.add(new BotHandler(tokens.get(i), server, bots, this));
        }
    }
    public BotFuture getBot(String name, String id, ServerClient client) {
        for (BotHandler handler : this.all) {
            if (name.equals(handler.roomName()) && handler.getId().equals(id)){
                handler.requestJoin(client);
                return null;
            }
        }
        BotFuture future = new BotFuture(name, client);
        if (this.available.size() > 0){
            for (BotHandler handler : this.available) {
                if (handler.getId().equals(id)) {
                    future.grant(handler);
                    return future;
                }
            }
        }
        return null;
    }
    public BotFuture getBot(String name, ServerClient client){
        for (BotHandler handler : this.all) {
            if (name.equals(handler.roomName()) || handler.getId().equals(name)){
                handler.requestJoin(client);
                return null;
            }
        }
        try{
            Long.parseLong(name);
            return null;
        }catch(Exception ignored){}
        BotFuture future = new BotFuture(name, client);
        if (this.available.size() > 0){
            future.grant(this.available.remove(0));
        }else{
            this.futures.add(future);
            Log.warn("Bot shortage by " + this.futures.size());
        }
        return future;
    }
    public void provide(BotHandler bot){
        if (this.futures.size() > 0){
            this.futures.get(0).grant(bot);
        }else{
            this.available.add(bot);
        }
        this.infoLink.setBotsAvailable(this.available.size());
    }
    public void unprovide(BotHandler botHandler) {
        for (int i = 0; i < this.available.size(); i++) {
            if (this.available.get(i) == botHandler){
                this.available.remove(i);
            }
        }
        this.infoLink.setBotsAvailable(this.available.size());
    }
    public void bind(ClientPool clientPool){
        for (BotHandler handler : this.all) {
            handler.bind(clientPool);
        }
    }
    public void close() {
        for (int i = 0; i < this.all.size(); i++) {
            this.all.get(i).close();
        }
    }
}
