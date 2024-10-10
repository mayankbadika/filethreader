package com.example.filethreader.controller;
import com.example.filethreader.entity.DBStatus;
import com.example.filethreader.entity.User;
import com.example.filethreader.service.FileReaderService;
import com.example.filethreader.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/file")
public class FileReaderController {  // Renamed from FileReader

    @Autowired
    private FileReaderService fileReaderService;

    @Autowired
    private UserService userService;

    @GetMapping
    public CompletableFuture<ResponseEntity<?>> getFileData(@RequestParam String[] path) {
        try {
            return fileReaderService.readMultipleFiles(path)
                    .thenApply(listOfUsers -> {
                        if (!listOfUsers.isEmpty()) {
                            return new ResponseEntity<>(listOfUsers, HttpStatus.OK);
                        }
                        return new ResponseEntity<>("No user data in file", HttpStatus.NO_CONTENT);
                    })
                    .exceptionally(ex -> {
                        // Handle the exception thrown by readAllUsersFromFile
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("An error occurred while reading the file: " + ex.getMessage());
                    });
        } catch (Exception e) {
            // Return a completed CompletableFuture with an error response
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("An error occurred: " + e.getMessage())
            );
        }
    }

    @GetMapping("/test")
    public CompletableFuture<ResponseEntity<?>> test (@RequestParam String[] paths) {
        // Asynchronously call the service method
        return fileReaderService.readFileAndUpdateDB(paths)
                .thenApply(dbStatuses -> {
                    // Once the future completes, construct the ResponseEntity
                    if (!dbStatuses.isEmpty()) {
                        // If there are results, return them with HTTP 200 (OK)
                        return new ResponseEntity<>(dbStatuses, HttpStatus.OK);
                    }
                    // If no results, return HTTP 204 (No Content)
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                });
    }

    @GetMapping("/getAllUsers")
    public CompletableFuture<ResponseEntity<List<User>>> getAllUsers() {
        return userService.getAllUsers()
                .thenApply(users -> new ResponseEntity<>(users, HttpStatus.OK))
                .exceptionally(e -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

      /*
    @GetMapping
    public CompletableFuture<ResponseEntity<?>> getFileAddress(@RequestParam String path) {
        try {
            return fileReaderService.readAllUsersFromFile(path)
                    .thenApply(listOfUsers -> {
                        if (!listOfUsers.isEmpty()) {
                            return new ResponseEntity<>(listOfUsers, HttpStatus.OK);
                        }
                        return new ResponseEntity<>("No user data in file", HttpStatus.NO_CONTENT);
                    })
                    .exceptionally(ex -> {
                        // Handle the exception thrown by readAllUsersFromFile
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("An error occurred while reading the file: " + ex.getMessage());
                    });
        } catch (Exception e) {
            // Return a completed CompletableFuture with an error response
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("An error occurred: " + e.getMessage())
            );
        }
    }
     */
}

