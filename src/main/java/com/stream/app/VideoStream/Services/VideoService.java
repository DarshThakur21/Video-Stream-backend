package com.stream.app.VideoStream.Services;

import com.stream.app.VideoStream.Model.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {

//save video
    Video save(Video video, MultipartFile file);

//    get video
    Video get(String videoId);

//    get by title
    Video getByTitle(String title);

//    get all video
    List<Video> getAllVideos();

}
