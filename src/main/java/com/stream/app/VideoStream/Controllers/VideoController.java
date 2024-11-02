package com.stream.app.VideoStream.Controllers;


import com.stream.app.VideoStream.Model.Video;
import com.stream.app.VideoStream.Payload.CustomMessage;
import com.stream.app.VideoStream.Services.VideoService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.print.DocFlavor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/videos")
public class VideoController {


    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

//    uploading the video

    @PostMapping
    public ResponseEntity<?> create(@RequestParam("file") MultipartFile file,
                                                @RequestParam("title") String title,
                                                @RequestParam("description") String description
                                                ){

        Video video=new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoId(UUID.randomUUID().toString());


        Video savedVideo=videoService.save(video,file);
        if(savedVideo!=null){
            return ResponseEntity.status(HttpStatus.OK).body(video);
        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CustomMessage.builder().message("video not uploaded").
                    success(false).build());
        }


    }



    @GetMapping
    public ResponseEntity<List<Video>> getall(){
        List<Video> vds= videoService.getAllVideos();
        return  new ResponseEntity<List<Video>>(vds,HttpStatus.OK);
    }





//    streaming the video by id
    @GetMapping("/streams/{videoId}")
    public ResponseEntity<Resource> streamVideo(@PathVariable String videoId ){
                Video video=videoService.get(videoId);
                String contentType= video.getContentType();
                String filePath=video.getFilePath();

                if(contentType==null){
                    contentType="application/octet-stream";
                }
                Resource resource=new FileSystemResource(filePath);
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
    }


//    stream video in chunks by request
    @GetMapping("/streams/range/{videoId}")
    public ResponseEntity<Resource> videoChunks(@PathVariable String videoId,@RequestHeader(value = "Range",required = false) String range) throws IOException {
        Video video=videoService.get(videoId);
        Path path= Paths.get(video.getFilePath());

            Resource resource=new FileSystemResource(path);

            String contentType=video.getContentType();
        if(contentType==null){
            contentType="application/octet-stream";
        }

        long fileLength=path.toFile().length();
        if(range==null){
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
        }

        long rangeStart;
        long rangeEnd;
        String[]ranges=  range.replace("bytes=","").split("-");
        rangeStart=Long.parseLong(ranges[0]);
            if(ranges.length>1) {
                rangeEnd = Long.parseLong(ranges[1]);
            }else{
                    rangeEnd=fileLength-1;
            }


            if(rangeEnd>fileLength-1){
                rangeEnd=fileLength-1;
            }

        System.out.println("start"+rangeStart);
        System.out.println("end"+rangeEnd);
        InputStream inputStream;

            try {
                inputStream= Files.newInputStream(path);

                inputStream.skip(rangeStart);

            } catch (Exception e) {
                return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            long contentLength=rangeEnd-rangeStart+1;

            HttpHeaders httpHeaders=new HttpHeaders();
            httpHeaders.add("Content-Range","bytes "+rangeStart+"-"+rangeEnd+"/"+fileLength);
        httpHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
        httpHeaders.add("Pragma", "no-cache");
        httpHeaders.add("Expires", "0");
        httpHeaders.add("X-Content-Type-Options", "nosniff");
        httpHeaders.setContentLength(contentLength);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).headers(httpHeaders).contentType(MediaType.parseMediaType(contentType))
                .body(new InputStreamResource(inputStream));


    }
}
