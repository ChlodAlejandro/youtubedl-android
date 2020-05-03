package net.chlod.ytdl_android_ffmpeg;

import net.chlod.ytdl_android_ffmpeg.objects.FFmpegProgressUpdate;

public interface FFmpegProgressCallback {

    public void onProgressUpdate(FFmpegProgressUpdate update);

}
