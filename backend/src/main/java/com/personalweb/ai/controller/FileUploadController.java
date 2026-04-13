package com.personalweb.ai.controller;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import com.personalweb.ai.service.FileValidationService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/upload")
public class FileUploadController {

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private final FileValidationService validationService;

    public FileUploadController(FileValidationService validationService) {
        this.validationService = validationService;
    }

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
                
        // Only allow safe characters in extension
        ext = ext.replaceAll("[^a-zA-Z0-9.]", "").toLowerCase();
        if (!ext.startsWith(".")) ext = "." + ext;
        
        String fileName = UUID.randomUUID().toString() + ext;
        File destFile = new File(UPLOAD_DIR, fileName);

        // Here we just limit extensions and rename to UUID
        // The DataBuffer consumption in FileValidationService might break transferTo, 
        // so we can either write to disk first and validate, or rely on extension check and Spring max file size
        // For simplicity and to not break transferTo, we check extension from validationService without consuming stream
        return validationService.validateFilePart(file)
                .then(file.transferTo(destFile))
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