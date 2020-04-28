package net.chlod.ytdl_android.objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.chlod.ytdl_android.YoutubeDL;
import net.chlod.ytdl_android.YoutubeDLException;
import net.chlod.ytdl_android.YoutubeDLRequest;
import net.chlod.ytdl_android.YoutubeDLResponse;
import net.chlod.ytdl_android.mapper.VideoInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoInfoCollection {

    public static VideoInfoCollection getInfo(String url) throws YoutubeDLException, InterruptedException {
        YoutubeDLRequest request = new YoutubeDLRequest(url);
        request.setOption("--dump-json");
        YoutubeDLResponse response = YoutubeDL.getInstance().execute(request);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ArrayList<VideoInfo> infoList = new ArrayList<>();
            for (String s : response.getOut().split("\n")) {
                infoList.add(objectMapper.readValue(s, VideoInfo.class));
            }
            return new VideoInfoCollection(url, infoList);
        } catch (IOException var6) {
            throw new YoutubeDLException("Unable to parse video information: " + var6.getMessage());
        }
    }

    public final String originUrl;
    public final boolean single;
    public final List<VideoInfo> infoList;

    private VideoInfoCollection(String originUrl, List<VideoInfo> infoList) {
        this.originUrl = originUrl;
        this.infoList = infoList;
        single = infoList.size() == 1;
    }

}
