package com.liquidator.input_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/v1/input")
public class InputController {
    @PostMapping
    public CommandResponse generateInputs(@RequestBody Input input){
        int videoTracks = input.getVideoTracks();
        int audioTracks = input.getAudioTracks();

        // Logging the received video and audio tracks
        log.info("Received request - {}", input);
        log.info("Received request - video: {}, audio: {}", videoTracks, audioTracks);

        // Handling exception
        if (videoTracks < 0 || audioTracks < 0) {
            throw new IllegalStateException("Invalid input for audio or video");
        }

        StringBuilder command = new StringBuilder();
        // creating the command. for the multiple tracks
        for(int i = 0; i < videoTracks; i++) {
            command.append("-i video").append(i).append(".mp4 ");
        }
        for(int i = 0; i < audioTracks; i++){
            command.append("-i audio").append(i).append(".mp3 ");
        }

        String res = command.toString();
        res = res.trim(); // trimming the excess white space

        // logging the generated command
        log.info("Generated input command: {}", res);
        // returning the result
        return new CommandResponse(res);
    }
}
