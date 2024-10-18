package com.example.filethreader.service;

import com.example.filethreader.entity.AppUser;
import com.example.filethreader.repository.AppUserRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AppUserService {

    @Autowired
    private AppUserRespository appUserRespository;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void saveNewUser(AppUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Arrays.asList(new String[]{"user"}));

        appUserRespository.save(user);
    }
}
