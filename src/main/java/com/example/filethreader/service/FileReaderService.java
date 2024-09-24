package com.example.filethreader.service;

import com.example.filethreader.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileReaderService {

    @Autowired
    private ResourceLoader resourceLoader;

    public List<User> readAllUsersFromFile(String path) throws Exception {
        List<User> users = new ArrayList<>();
        /*  Load the CSV file as a Resource
                The "file:" prefix specifies that we are accessing a file on the local file system.
                This tells the ResourceLoader to treat the provided path as a file system path,
                allowing it to locate and read the file correctly.
             */
        Resource resource = resourceLoader.getResource("file:" + path);

        // Create an InputStream to read raw byte data from the file
        // InputStream allows us to read data incrementally without loading the entire file into memory,
        // which is essential for handling large files efficiently.
        InputStream ir = resource.getInputStream();

        // BufferedReader reads the character data from the InputStream
        // It buffers input, making it more efficient to read text data line by line.
        // This way, we can easily handle large text files without performance issues.
        BufferedReader reader = new BufferedReader(new InputStreamReader(ir));

        String line;
        // Read the file line by line
        while ((line = reader.readLine()) != null) {
            String[] userData = line.split(",");
            if(userData[0].equals("id")) continue;
            User user = new User();

            user.setID(Integer.parseInt(userData[0]));
            user.setFirstName(userData[1]);
            user.setLastName(userData[2]);
            user.setEmail(userData[3]);
            user.setGender(userData[4]);
            user.setIpAddress(userData[5]);

            users.add(user);
        }

        reader.close(); // Close the reader to free resources

        return users;
    }
}
