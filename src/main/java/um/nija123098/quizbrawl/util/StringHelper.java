package um.nija123098.quizbrawl.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Made by Dev on 10/22/2016
 */
public class StringHelper {
    public static String getList(List<String> list){
        switch (list.size()){
            case 0:
                return "";
            case 1:
                return list.get(0);
            case 2:
                return list.get(0) + " and " + list.get(1);
            default:
                String s = "";
                for (int i = 0; i < list.size() - 1; i++) {
                    s += list.get(i) + ", ";
                }
                s += "and " + list.get(list.size() - 1);
                return s;
        }
    }
    public static String getList(String...list){
        List<String> strings = new ArrayList<String>(list.length);
        Collections.addAll(strings, list);
        return getList(strings);
    }
}
