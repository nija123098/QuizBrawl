package um.nija123098.quizbrawlkit.bot;

import um.nija123098.quizbrawlkit.question.Difficulty;
import um.nija123098.quizbrawlkit.question.Question;
import um.nija123098.quizbrawlkit.question.Topic;
import um.nija123098.quizbrawlkit.question.Type;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

/**
 * Made by Dev on 10/10/2016
 */
public interface BotLink {
    void setStatus(String status);
    void pauseVoice(boolean pause);
    void clearVoice();
    void playVoice(File file) throws IOException, UnsupportedAudioFileException;
    void playVoice(AudioInputStream stream) throws IOException;
    Question getQuestion(EnumSet<Difficulty> difficulties, EnumSet<Topic> topics, EnumSet<Type> types);
    Question getQuestion(EnumSet<Difficulty> difficulties, EnumSet<Topic> topics, EnumSet<Type> types, List<Question> exclusions);
    void abandonRoom();
    File getTempFile(String extension);
    Message messageRoom(String msg);
    Client getClient(String id);
    void setRoomInfo(String s);
}
