package com.example.filethreader.service;

import com.example.filethreader.entity.User;
import com.example.filethreader.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Executor taskExecutor;

    public boolean saveAll(List<User> users) {
        try {
            userRepository.saveAll(users);
        } catch(Exception e) {
            System.err.println("Exception occurred in userService = "+e);

            return false;
        }

        return true;
    }

    //if this throws exception the controller will catch it and log it
    public CompletableFuture<List<User>> getAllUsers() {
        return CompletableFuture.supplyAsync(
            // Assume this might throw a runtime exception (e.g., database issues)
            userRepository::findAll, taskExecutor);
    }

}
