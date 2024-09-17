package com.example.filehandling.controller;



import com.example.filehandling.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@Controller
public class FileController {

    @Autowired
    private S3Service s3Service;

    // Home page to upload and list files
    @GetMapping("/")
    public String homePage(Model model) {
        List<String> files = s3Service.listFiles();
        model.addAttribute("files", files);
        return "index";
    }

    // Upload file
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(file.getBytes());
        s3Service.uploadFile(file.getOriginalFilename(), inputStream);
        return "redirect:/";
    }

    // Download file
    @GetMapping("/download/{fileName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileName) {
        byte[] fileData = s3Service.downloadFile(fileName);
        InputStream inputStream = new ByteArrayInputStream(fileData);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    // Delete file
    @GetMapping("/delete/{fileName}")
    public String deleteFile(@PathVariable String fileName) {
        s3Service.deleteFile(fileName);
        return "redirect:/";
    }
}
