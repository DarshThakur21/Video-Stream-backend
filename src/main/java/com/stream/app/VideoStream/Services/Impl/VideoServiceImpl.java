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

    @Value("${file.video.hsl}")
    String HSL_DIR;

    @PostConstruct
    public void init(){
        File file=new File(DIR);
//        File filehsl=new File(HSL_DIR);
//
//
//        if(!filehsl.exists()){
//            filehsl.mkdir();
//        }
        try {
            Files.createDirectories(Paths.get(HSL_DIR));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

    @Override
    public String processVideo(String videoId, MultipartFile multipartFile) {
        Video video=this.get(videoId);
        String filePath=video.getFilePath();
//
////        path to store data
        Path vidPath=Paths.get(filePath);

//        String output360P=HSL_DIR+videoId+"/360p/";
//        String output720P=HSL_DIR+videoId+"/720p/";
//        String output1080P=HSL_DIR+videoId+"/1080p/";


        try {
//            Files.createDirectories(Paths.get(output360P));
//            Files.createDirectories(Paths.get(output720P));
//            Files.createDirectories(Paths.get(output1080P));

            //ffmpeg command
//            StringBuilder ffmpegCmd=new StringBuilder();

//            ffmpegCmd.append("ffmpeg -i")
//                    .append(vidPath.toString())
//                    .append("")
//                    .append("-map 0:v -map 0:a -s:v:0 640x360 -b:v:0 800k ")
//                    .append("-map 0:v -map 0:a -s:v:1 1280x720 -b:v:1 2800k ")
//                    .append("-map 0:v -map 0:a -s:v:2 1920x1080 -b:v:2 5000k ")
//                    .append("-var_stream_map \"v:0,a:0 v:1,a:0 v:2,a:0\" ")
//                    .append("-master_pl_name").append(HSL_DIR).append(videoId).append("/master.m3u8 ")
//                    .append("-f hls -hls_time 10 -hls_list_size 0 ")
//                    .append("-hls_segment_filename \"").append(HSL_DIR).append()


        Path outputpath=Paths.get(HSL_DIR,videoId);

        Files.createDirectories(outputpath);


//            String ffmpegCmd=String.format(
//                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\" \"%s/index.m3u8\" ",
//                    vidPath,outputpath,outputpath
//            );

//


            String ffmpegCmd = String.format(
                    "docker exec videostream-ffmpeg-1 ffmpeg -i \"/app/input/%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"/app/output/%s_segment_%%3d.ts\" \"/app/output/%s_index.m3u8\"",
                    vidPath, outputpath, outputpath
            );

            System.out.println(ffmpegCmd);
            ProcessBuilder processBuilder=new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);

            processBuilder.inheritIO();
            Process process=processBuilder.start();


            int exit=process.waitFor();
            if (exit!=0){
                throw new RuntimeException("video process failed!!"+exit);


            }
            return videoId;




        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("failed vdo processing");
        }




    }


}
