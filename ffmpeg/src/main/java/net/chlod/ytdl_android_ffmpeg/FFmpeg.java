package net.chlod.ytdl_android_ffmpeg;

import android.app.Application;

import androidx.annotation.Nullable;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.BuildConfig;
import com.orhanobut.logger.Logger;

import net.chlod.ytdl_android.YoutubeDLException;
import net.chlod.ytdl_android.utils.StreamGobbler;
import net.chlod.ytdl_android.utils.YoutubeDLUtils;

import net.chlod.ytdl_android_ffmpeg.utils.StreamProcessExtractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class FFmpeg {

    private static final FFmpeg INSTANCE = new FFmpeg();
    protected static final String baseName = "youtubedl-android";
    private static final String packagesRoot = "packages";
    private static final String ffmpegBin = "usr/bin/ffmpeg";

    private boolean initialized = false;
    private File binDir;
    private File ffmpegPath;
    private String ENV_LD_LIBRARY_PATH;

    private FFmpeg(){
    }

    public static FFmpeg getInstance(){
        return INSTANCE;
    }

    synchronized public void init(Application application) throws FFmpegException {
        if (initialized) return;

        initLogger();

        File baseDir = new File(application.getFilesDir(), baseName);
        if(!baseDir.exists()) baseDir.mkdir();

        File packagesDir = new File(baseDir, packagesRoot);
        binDir = new File(packagesDir, "usr/bin");
        ffmpegPath = new File(packagesDir, ffmpegBin);

        ENV_LD_LIBRARY_PATH = packagesDir.getAbsolutePath() + "/usr/lib";

        initFFmpeg(application, packagesDir);

        initialized = true;
    }

    private void initFFmpeg(Application application, File packagesDir) throws FFmpegException {
        if (!ffmpegPath.exists()) {
            if (!packagesDir.exists()) {
                packagesDir.mkdirs();
            }
            try {
                YoutubeDLUtils.unzip(application.getResources().openRawResource(R.raw.ffmpeg_arm), packagesDir);
            } catch (IOException e) {
                // delete for recovery later
                YoutubeDLUtils.delete(ffmpegPath);
                throw new FFmpegException("failed to initialize", e);
            }
            markExecutable(binDir);
        }
    }

    private void markExecutable(File binDir) {
        File[] directoryListing = binDir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if(!child.isDirectory()) child.setExecutable(true);
            }
        }
    }

    private void initLogger() {
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, @Nullable String tag) {
                return BuildConfig.DEBUG;
            }
        });
    }

    public FFmpegResponse execute(String[] arguments, FFmpegProgressCallback callback) throws InterruptedException, FFmpegException {
        if (!initialized) throw new FFmpegException("FFmpeg not yet initialized.");

        FFmpegResponse ffmpegResponse;
        Process process;
        int exitCode;
        StringBuffer outBuffer = new StringBuffer(); //stdout
        StringBuffer errBuffer = new StringBuffer(); //stderr
        long startTime = System.currentTimeMillis();

        List<String> command = new ArrayList<>();
        command.add(ffmpegPath.getAbsolutePath());
        command.addAll(Arrays.asList(arguments));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Map<String, String> env = processBuilder.environment();
        env.put("LD_LIBRARY_PATH", ENV_LD_LIBRARY_PATH);
        env.put("PATH",  System.getenv("PATH") + ":" + binDir.getAbsolutePath());

        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new FFmpegException(e);
        }

        InputStream outStream = process.getInputStream();
        InputStream errStream = process.getErrorStream();

        StreamGobbler stdOutProcessor = new StreamGobbler(outBuffer, outStream);
        StreamProcessExtractor stdErrProcessor = new StreamProcessExtractor(
                Pattern.compile("frame=\\s*(\\d+)\\s*fps=\\s*\\d+\\s*q=\\s*(-?[\\d.]+)\\s*size=\\s*(\\d+)kB\\s*time=\\s*(\\d{2}):(\\d{2}):(\\d{2}).(\\d{2})\\s*bitrate=\\s*([\\d.]+)kbits/s\\s*speed=\\s*([\\d.]+)x\\s*$"), errBuffer, errStream, callback);

        try {
            stdOutProcessor.join();
            stdErrProcessor.join();
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            process.destroy();
            throw e;
        }

        String out = outBuffer.toString();
        String err = errBuffer.toString();

        if (exitCode > 0) {
            throw new FFmpegException(err);
        }

        long elapsedTime = System.currentTimeMillis() - startTime;

        ffmpegResponse = new FFmpegResponse(command, exitCode, elapsedTime, out, err);

        return ffmpegResponse;
    }

    public FFmpegResponse execute(String[] arguments) throws InterruptedException, FFmpegException {
        return execute(arguments, null);
    }
}
