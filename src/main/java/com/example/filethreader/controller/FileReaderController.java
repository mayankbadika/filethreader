package com.example.filethreader.controller;

import com.example.filethreader.entity.User;
import com.example.filethreader.service.FileReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/file")
public class FileReaderController {  // Renamed from FileReader

    @Autowired
    private FileReaderService fileReaderService;

    @GetMapping
    public ResponseEntity<?> getFileAddress(@RequestParam String path) {  // Method name updated for better readability
        List<User> users;

        try {
            users = fileReaderService.readAllUsersFromFile(path);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }

        return new ResponseEntity<>(users, HttpStatus.OK);
    }
}

