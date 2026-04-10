package com.personalweb.ai.controller;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/upload")
public class FileUploadController {

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping
    public Mono<Map<String, String>> uploadFile(@RequestPart("file") FilePart file) {
        Map<String, String> result = new HashMap<>();
        
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.filename();
        String ext = originalFilename != null && originalFilename.indexOf('.') > -1
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".png";
        
        String fileName = UUID.randomUUID().toString() + ext;
        File destFile = new File(UPLOAD_DIR, fileName);

        return file.transferTo(destFile)
                .then(Mono.fromCallable(() -> {
                    result.put("url", "/api/files/" + fileName);
                    return result;
                }))
                .onErrorResume(e -> {
                    result.put("error", e.getMessage());
                    return Mono.just(result);
                });
    }
}