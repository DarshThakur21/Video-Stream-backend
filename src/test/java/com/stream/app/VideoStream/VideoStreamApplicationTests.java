package com.stream.app.VideoStream;

import com.stream.app.VideoStream.Services.VideoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class VideoStreamApplicationTests {


	@Autowired
	VideoService videoService;
	@Test
	void contextLoads() {
		videoService.processVideo("d835b665-0a3f-498d-a1a9-44828f18149f");
	}

}
