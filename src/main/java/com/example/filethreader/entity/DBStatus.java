package com.example.filethreader.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Bean;

@Getter
@Setter
@AllArgsConstructor
public class DBStatus {

    private String fileName;
    private boolean fileStatus;
}
