package net.chlod.ytdl_android;

public interface DownloadProgressCallback {
    void onProgressUpdate(float progress, long etaInSeconds);
}
