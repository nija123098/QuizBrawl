package um.nija123098.quizbrawl.util;

/**
 * Made by Dev on 10/21/2016
 */
public class EnumSetHelper {
    /*public static void toggle(EnumSet set, Enum en){
        if (set.contains(en)){
            set.remove(en);
        }else{
            set.add(en);
        }
    }
    public static <E extends Enum<E>> Enum<E> get(String s, Class<E> c){
        System.out.println(c.getName());
        //return ((Class<Enum>) c).getEnumConstants()[0].valueOf(c, s);
        return Enum.valueOf(c, s);
    }
    public static void toggle(EnumSet set, String num){
        toggle(set, get(num, ((Class<Enum>) set.getClass().getMethods()[0].getParameterTypes()[0])));
    }
    /*
    List<Method> m = new ArrayList<Method>();
        Collections.addAll(m, set.getClass().getSuperclass().getSuperclass().getSuperclass().getMethods());
        System.out.println(set.getClass().getName());
        for (int i = 0; i < m.size(); i++) {
            System.out.println(i + ". " + m.get(i).getName());
        }
        m.stream().filter(method -> method.getName().equals("typecheck")).limit(1).forEach(method1 -> {
            toggle(set, get(num, ((Class<Enum>) method1.getParameterTypes()[0])));
        });
    public static void main(String[] args) {
        System.out.println(Enum.valueOf(Red.class, "ORANGE").name());
        System.out.println(Red.class.getSuperclass().getName());
        EnumSet<Red> enumSet = EnumSet.noneOf(Red.class);
        System.out.println(enumSet.size());
        toggle(enumSet, "ORANGE");
        System.out.println(enumSet.size());
    }
    //Is there a way to get a Class object from a set (specificly an enum set) without getting
    private enum Red{
        BLUE, GREEN, ORANGE,;
    }
    private class A{
        public void get(){

        }
    }
    private class B extends A{
        public void set(){

        }
    }*/
}
