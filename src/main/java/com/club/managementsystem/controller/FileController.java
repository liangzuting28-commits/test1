package com.club.managementsystem.controller;

import com.club.managementsystem.dto.ApiResponse;
import com.club.managementsystem.entity.FileRecord;
import com.club.managementsystem.repository.FileRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final FileRecordRepository fileRecordRepository;

    public FileController(FileRecordRepository fileRecordRepository) {
        this.fileRecordRepository = fileRecordRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "uploaderId", required = false) String uploaderId) {
        if (file.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("请选择文件"));
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(storedName);
            file.transferTo(filePath.toFile());

            FileRecord record = new FileRecord();
            record.setOriginalName(originalFilename);
            record.setStoredName(storedName);
            record.setFilePath(filePath.toString());
            record.setFileSize(file.getSize());
            record.setContentType(file.getContentType());
            record.setUploaderId(uploaderId);
            fileRecordRepository.save(record);

            return ResponseEntity.ok(ApiResponse.success("上传成功", record));
        } catch (IOException e) {
            return ResponseEntity.ok(ApiResponse.error("上传失败: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> download(@PathVariable Long fileId) {
        Optional<FileRecord> recordOpt = fileRecordRepository.findById(fileId);
        if (recordOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        FileRecord record = recordOpt.get();
        File file = new File(record.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        try {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
            String contentType = record.getContentType() != null ? record.getContentType() : "application/octet-stream";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + record.getOriginalName() + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(record.getFileSize())
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
