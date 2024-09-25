package com.example.filethreader.controller;
import com.example.filethreader.service.FileReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/file")
public class FileReaderController {  // Renamed from FileReader

    @Autowired
    private FileReaderService fileReaderService;

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

