package com.careerdevs;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


@RestController
public class GatewayController {
    private final String url = "https://api.nasa.gov/planetary/apod?api_key=";
    private final String API_KEY = "SYbbgZhoTZxJIn1ZOdkabDbpGdYnh2PR97Sgpoww";
    private APOD apod;


    @Autowired
    private StreamingService service;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
        return args -> {
            apod = restTemplate.getForObject(url + API_KEY, APOD.class);

        };
    }

    @GetMapping("/")
    public String rootRoute(){
        return "Welcome Home";
    }

    @GetMapping("/error")
    public String error(){
        return "Incorrect Page";
    }

    @GetMapping("/apod")
    public APOD apod() {
        return apod;
    }
    @GetMapping(value = "date/{date}")
    public String dateChange(@PathVariable String date, RestTemplate restTemplate){
        apod = restTemplate.getForObject(url + API_KEY + "&date=" + date, APOD.class);
        return "Date Changed to " + date;
    }

    @GetMapping("/media")
    public Object media() throws MalformedURLException {
        switch (apod.getMedia_type()) {
            case "image":
                byte[] image = downloadUrl(new URL(apod.getHdurl()));
                return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
            case "gif":
                byte[] gif = downloadUrl(new URL(apod.getUrl()));
                return ResponseEntity.ok().contentType(MediaType.IMAGE_GIF).body(gif);
            case "video":
                return apod.getUrl();
//                byte[] video = downloadUrl(new URL(apod.getUrl()));
//                Mono<ResponseEntity<byte[]>> video2 = Mono.just(ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(video));
//                return video2;
                
            default:
                return apod.getUrl();
        }
    }

    @GetMapping(value = "video/{title}", produces = "video/mp4")
    public Mono<Resource> getVideo(@PathVariable String title) {
        return service.getVideo(title);
    }

    private byte[] downloadUrl(URL toDownload) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            byte[] chunk = new byte[4096];
            int bytesRead;
            InputStream stream = toDownload.openStream();

            while ((bytesRead = stream.read(chunk)) > 0) {
                outputStream.write(chunk, 0, bytesRead);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return outputStream.toByteArray();
    }

}
