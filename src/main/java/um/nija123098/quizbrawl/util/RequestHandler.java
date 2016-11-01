package um.nija123098.quizbrawl.util;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Made by Dev on 10/10/2016
 */
public class RequestHandler {
    private static volatile int count;
    public static void request(Request request){
        ++count;
        RequestBuffer.request(() -> {
            try {
                request.request();
                --count;
            } catch (DiscordException | MissingPermissionsException e) {
                if (e instanceof DiscordException && e.getMessage().contains("CloudFlair")){
                    throw new RateLimitException("CloudFlair thwarting", 100, "WHAT?", false);
                }// may remove thwarting at next version bump
                e.printStackTrace();
            }
        });
    }
    public static int requestCount() {
        return count;
    }
    @FunctionalInterface
    public interface Request{
        void request() throws DiscordException, RateLimitException, MissingPermissionsException;
    }
}