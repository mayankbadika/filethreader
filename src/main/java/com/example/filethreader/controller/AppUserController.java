package com.example.filethreader.controller;

import com.example.filethreader.entity.AppUser;
import com.example.filethreader.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appuser")
public class AppUserController {

    @Autowired
    private AppUserService appUserService;

    @PostMapping
    public ResponseEntity<?> createNewUser(@RequestBody AppUser appUser) {
        try {
            appUserService.saveNewUser(appUser);

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
