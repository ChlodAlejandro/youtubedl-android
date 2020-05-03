package net.chlod.ytdl_android_ffmpeg.objects;

public class FFmpegProgressUpdate {

    public final long frames;
    public final float q;
    public final long size;
    public final long elapsedMs;
    public final float bitrate;
    public final float speed;

    public FFmpegProgressUpdate(long frames, float q, long size, long elapsedMs, float bitrate, float speed) {
        this.frames = frames;
        this.q = q;
        this.size = size;
        this.elapsedMs = elapsedMs;
        this.bitrate = bitrate;
        this.speed = speed;
    }

}
