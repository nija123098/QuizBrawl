package um.nija123098.quizbrawl.server.services;

import um.nija123098.quizbrawl.bothandler.BotHandler;
import um.nija123098.quizbrawl.server.ServerClient;
import um.nija123098.quizbrawl.util.Log;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Made by Dev on 10/10/2016
 */
public class BotFuture implements Future<BotHandler> {
    private volatile BotHandler bot;
    private volatile boolean cancelled;
    private final String name;
    private final ServerClient client;
    public BotFuture(String name, ServerClient client) {
        if (name.length() < 3){
            this.name = "room " + name;
        }else if (name.length() > 99){
            this.name = name.substring(0, 99);
        }else{
            this.name = name;
        }
        this.client = client;
    }
    synchronized boolean grant(BotHandler bot){
        boolean completed;
        completed = this.isDone();
        if (!completed){
            this.bot = bot;
            this.bot.setRoom(this.name, this.client);
        }
        return completed;
    }
    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        Log.warn("Canceling " + this.client.name() + "'s bot future which was " + (this.isDone() ? "not" : "") + " done");
        if (this.isDone()){
            this.bot.abandonRoom();
        }
        this.cancelled = true;
        return this.isDone();
    }
    @Override
    public synchronized boolean isCancelled() {
        return this.cancelled;
    }
    @Override
    public synchronized boolean isDone() {
        return this.bot != null;//  || !this.cancelled
    }
    @Override
    public synchronized BotHandler get() {
        return this.bot;
    }
    @Override
    public BotHandler get(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }
}
