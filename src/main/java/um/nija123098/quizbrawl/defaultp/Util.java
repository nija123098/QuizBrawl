package um.nija123098.quizbrawl.defaultp;

import com.darkprograms.speech.synthesiser.Synthesiser;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import sx.blah.discord.Discord4J;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.Map;

/**
 * Made by Dev on 9/12/2016
 */
public class Util {
    public static Synthesiser synthesiser = new Synthesiser("en-US");
    public static long getSynth(File to, String text){
        // System.out.println("Synthing: \"" + text + "\"");
        InputStream inputStream = null;
        try {
            inputStream = synthesiser.getMP3Data(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        try {
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                baos.write(buffer, 0, bytesRead);
            }
            FileOutputStream fos = new FileOutputStream(to);
            fos.write(baos.toByteArray());
            fos.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return getFileTime(to);
    }
    public static long getFileTime(File file){
        AudioFileFormat fileFormat;
        try {
            fileFormat = AudioSystem.getAudioFileFormat(file);
            if (fileFormat instanceof TAudioFileFormat) {
                Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
                String key = "duration";
                Long microseconds = (Long) properties.get(key);
                return microseconds / 1000;
            } else {
                throw new UnsupportedAudioFileException();
            }
        } catch (UnsupportedAudioFileException | IOException e) {
            Discord4J.LOGGER.error("Exception getting file time.", e);
            return 200;
        }
    }
}
