package um.nija123098.quizbrawl.util;

import java.util.Random;

/**
 * Made by Dev on 10/9/2016
 */
public class Ref {
    private static final Random RANDOM;
    static {
        RANDOM = new Random();
    }
    public static int getInt(int max){
        return Math.abs(RANDOM.nextInt()) % (max + 1);
    }
}
