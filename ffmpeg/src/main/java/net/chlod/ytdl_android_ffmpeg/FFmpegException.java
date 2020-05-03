package net.chlod.ytdl_android_ffmpeg;

public class FFmpegException extends Exception {

    public FFmpegException(String message) {
        super(message);
    }

    public FFmpegException(String message, Throwable e) {
        super(message, e);
    }

    public FFmpegException(Throwable e) {
        super(e);
    }

}
