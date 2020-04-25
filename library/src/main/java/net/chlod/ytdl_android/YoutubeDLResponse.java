package net.chlod.ytdl_android;

import java.util.List;

public class YoutubeDLResponse {

    private final List<String> command;
    private final int exitCode;
    private final String out;
    private final String err;
    private final long elapsedTime;

    public YoutubeDLResponse(List<String> command, int exitCode, long elapsedTime, String out, String err) {
        this.command = command;
        this.elapsedTime = elapsedTime;
        this.exitCode = exitCode;
        this.out = out;
        this.err = err;
    }

    public List<String> getCommand() {
        return command;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOut() {
        return out;
    }

    public String getErr() {
        return err;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }
}
