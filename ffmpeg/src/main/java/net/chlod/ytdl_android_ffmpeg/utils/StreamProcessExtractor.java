package net.chlod.ytdl_android_ffmpeg.utils;

import com.orhanobut.logger.Logger;

import net.chlod.ytdl_android_ffmpeg.FFmpegProgressCallback;
import net.chlod.ytdl_android_ffmpeg.objects.FFmpegProgressUpdate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamProcessExtractor extends Thread {
    private static final int GROUP_FRAMES = 1;
    private static final int GROUP_Q = 2;
    private static final int GROUP_SIZE = 3;
    private static final int GROUP_HOURS = 4;
    private static final int GROUP_MINUTES = 5;
    private static final int GROUP_SECONDS = 6;
    private static final int GROUP_MILISECONDS = 7;
    private static final int GROUP_BITRATE = 8;
    private static final int GROUP_SPEED = 9;
    private final InputStream stream;
    private final StringBuffer buffer;
    private final FFmpegProgressCallback callback;

    private final Pattern p;

    public StreamProcessExtractor(Pattern p, StringBuffer buffer, InputStream stream, FFmpegProgressCallback callback) {
        this.p = p;
        this.stream = stream;
        this.buffer = buffer;
        this.callback = callback;
        this.start();
    }

    public void run() {
        try {
            //noinspection CharsetObjectCanBeUsed
            Reader in = new InputStreamReader(stream, "UTF-8");
            StringBuilder currentLine = new StringBuilder();
            int nextChar;
            while ((nextChar = in.read()) != -1) {
                buffer.append((char) nextChar);
                if (nextChar == '\r' && callback != null) {
                    //System.out.println(currentLine.toString());
                    processOutputLine(currentLine.toString());
                    currentLine.setLength(0);
                    continue;
                }
                currentLine.append((char) nextChar);
            }
        } catch (IOException e) {
            Logger.e(e, "failed to read stream");
        }
    }

    private void processOutputLine(String line) {
        Matcher m = p.matcher(line);
        if (m.matches() && callback != null) {
            try {
                String frames = m.group(GROUP_FRAMES);
                String q = m.group(GROUP_Q);
                String lsize = m.group(GROUP_SIZE);
                String hours = m.group(GROUP_HOURS);
                String minutes = m.group(GROUP_MINUTES);
                String seconds = m.group(GROUP_SECONDS);
                String miliseconds = m.group(GROUP_MILISECONDS);
                String bitrate = m.group(GROUP_BITRATE);
                String speed = m.group(GROUP_SPEED);

                assert frames != null;
                assert q != null;
                assert lsize != null;
                assert hours != null;
                assert minutes != null;
                assert seconds != null;
                assert miliseconds != null;
                assert bitrate != null;
                assert speed != null;

                callback.onProgressUpdate(new FFmpegProgressUpdate(
                        Long.parseLong(frames),
                        Float.parseFloat(q),
                        Long.parseLong(lsize),
                        (Long.parseLong(hours) * 3600000)
                            + (Long.parseLong(minutes) * 60000)
                            + (Long.parseLong(seconds) * 1000)
                            + (Long.parseLong(miliseconds) * 10),
                        Float.parseFloat(bitrate),
                        Float.parseFloat(speed)));
            } catch (AssertionError ignored) {}
        }
    }
}
