package com.example.filethreader.customActuators;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "custom")
public class CustomActuators {

    @ReadOperation
    public ResponseEntity<?> customEndpoint() {
        return new ResponseEntity("This is a custom endpoint!", HttpStatus.OK);
    }

    @WriteOperation
    public ResponseEntity<?> updateCustomEndpoint(String newValue) {
        // Handle the update logic here
        return new ResponseEntity("Custom endpoint updated with value: " + newValue, HttpStatus.OK);
    }
}
