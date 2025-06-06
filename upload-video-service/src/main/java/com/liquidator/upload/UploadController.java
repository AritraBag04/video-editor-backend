package com.liquidator.upload;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UploadController {
    @PostMapping("/upload")
    public ResponseEntity<String> uploadVideos(@RequestParam("files") List<MultipartFile> files) throws IOException {
        if (files.isEmpty()) {
            return ResponseEntity.badRequest().body("No files uploaded");
        }

        StringBuilder result = new StringBuilder();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                result.append("Uploaded: ")
                        .append(file.getOriginalFilename())
                        .append(" (")
                        .append(file.getSize())
                        .append(" bytes)\n");

                // Optional: Save the file to disk
                 file.transferTo(new File("/home/aritra/Desktop/" + file.getOriginalFilename()));
            }
        }

        return ResponseEntity.ok(result.toString());
    }
}
