package com.example.filethreader.repository;

import com.example.filethreader.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRespository extends JpaRepository<AppUser, Integer> {
    AppUser findByUsername(String username);
}
