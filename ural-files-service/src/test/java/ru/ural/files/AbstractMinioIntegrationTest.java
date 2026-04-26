package ru.ural.files;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ural.AbstractIntegrationTest;

@Testcontainers
public abstract class AbstractMinioIntegrationTest extends AbstractIntegrationTest {

    private static final String MINIO_IMAGE = "minio/minio:RELEASE.2024-01-18T22-51-28Z";

    @Container
    static final GenericContainer<?> MINIO_CONTAINER = new GenericContainer<>(MINIO_IMAGE)
                    .withExposedPorts(9000)
                    .withEnv("MINIO_ROOT_USER", "minio")
                    .withEnv("MINIO_ROOT_PASSWORD", "minio123")
                    .withCommand("server /data")
                    .waitingFor(Wait.forLogMessage(".*API:.*9000.*", 1));

    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url", () ->
                "http://" + MINIO_CONTAINER.getHost() + ":" + MINIO_CONTAINER.getMappedPort(9000)
        );
        registry.add("minio.access-key", () -> "minio");
        registry.add("minio.secret-key", () -> "minio123");
    }
}