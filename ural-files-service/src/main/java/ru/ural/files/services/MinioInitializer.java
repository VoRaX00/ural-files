package ru.ural.files.services;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.ural.exceptions.InternalServerException;
import ru.ural.files.properties.MinioProperty;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioInitializer implements ApplicationRunner {

    private static final String INVALID_INITIALIZATION_MINIO_BUCKET = "Invalid initialization MinIO bucket";

    private final MinioClient minioClient;

    private final MinioProperty minioProperty;

    @Override
    public void run(ApplicationArguments args) {
        try {
            BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder()
                    .bucket(minioProperty.getBucket())
                    .build();

            boolean exists = minioClient.bucketExists(bucketExistsArgs);

            if (exists) {
                log.info("Bucket: {} exists", minioProperty.getBucket());
                return;
            }

            MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                    .bucket(minioProperty.getBucket())
                    .build();

            minioClient.makeBucket(makeBucketArgs);
            log.info("Bucket: {} initialize", minioProperty.getBucket());
        } catch (Exception e) {
            throw new InternalServerException(INVALID_INITIALIZATION_MINIO_BUCKET, e);
        }
    }

}
