package ru.ural.files.properties;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
public class MinioProperty {

    @NotNull
    private String bucket;

    @NotNull
    private String url;

    @NotNull
    private String accessKey;

    @NotNull
    private String secretKey;

}
