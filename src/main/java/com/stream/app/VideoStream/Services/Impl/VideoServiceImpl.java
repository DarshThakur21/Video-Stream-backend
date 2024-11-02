package com.stream.app.VideoStream.Services.Impl;


import ch.qos.logback.core.util.StringUtil;
import com.stream.app.VideoStream.Model.Video;
import com.stream.app.VideoStream.Repository.VideoRepo;
import com.stream.app.VideoStream.Services.VideoService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class VideoServiceImpl implements VideoService {

    @Value("${files.video}")
    String DIR;

    @PostConstruct
    public void init(){
        File file=new File(DIR);

        if(!file.exists()){
            file.mkdir();
            System.out.println("folder created");
        }
        else{
            System.out.println("folder exists");
        }
    }


    public VideoServiceImpl(VideoRepo videoRepo) {
        this.videoRepo = videoRepo;
    }

    private final VideoRepo videoRepo;



    @Override
    public Video save(Video video, MultipartFile file) {

        try {


            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();

//folder path
            String cleanFileName= StringUtils.cleanPath(filename);
            String cleanFolder= StringUtils.cleanPath(DIR);

            Path paths=Paths.get(cleanFolder,cleanFileName);
            System.out.println(paths);


//copy file to the folder
          Files.copy(inputStream,paths, StandardCopyOption.REPLACE_EXISTING );


//          video metadata
            video.setContentType(contentType);
            video.setFilePath(paths.toString());


           return videoRepo.save(video);

        }catch (IOException e){
            e.printStackTrace();
            return null;
        }



    }

    @Override
    public Video get(String videoId) {
    return   videoRepo.findById(videoId).orElseThrow(()->new RuntimeException("video not found"));


    }

    @Override
    public Video getByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getAllVideos() {
       return videoRepo.findAll();
//        return List.of();
    }
}
