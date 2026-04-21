package ru.ural.files;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UralFilesApplication {

    public static void main(String[] args) {
        SpringApplication.run(UralFilesApplication.class, args);
    }

}
