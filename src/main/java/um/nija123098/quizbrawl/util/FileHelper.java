package um.nija123098.quizbrawl.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Made by Dev on 10/13/2016
 */
public class FileHelper {
    static {
        new Object();
    }
    public static String getJarPath(){
        try {
            return new File(FileHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    public static File provideTemporaryFile(String extension){
        try {
            File file = Files.createTempFile(Paths.get(getJarPath(), "temp"), null, "." + extension).toFile();
            file.deleteOnExit();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static <E> List<E> grabInstances(Class<E> eClass, String path){
        if (!new File(path).exists() || !new File(path).getName().endsWith(".jar")){
            return new ArrayList<E>(0);
        }
        List<E> instances = new ArrayList<E>(1);
        try {
            Enumeration<JarEntry> e = new JarFile(path).entries();
            URL[] urls = {new URL("jar:file:" + path + "!/")};
            URLClassLoader cl = URLClassLoader.newInstance(urls);
            while (e.hasMoreElements()){
                JarEntry je = e.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")){
                    continue;
                }
                String className = je.getName().substring(0, je.getName().length()-6).replace('/', '.');
                Class c = cl.loadClass(className);
                try {
                    java.lang.Object o = c.newInstance();
                    if (eClass.isInstance(o)){
                        instances.add(((E) o));
                    }
                }catch (Throwable ignored){}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instances;
    }
    public static void assureExistance(String path) throws IOException {
        if (!Files.exists(Paths.get(path))) {
            Files.createDirectory(Paths.get(path));
        }
    }
    private static class Object{
        public Object() {
            new File(getJarPath() + "\\temp").mkdir();
        }
        @Override
        public void finalize() throws Throwable{
            FileUtils.cleanDirectory(new File(getJarPath() + "\\temp"));
        }
    }
}
