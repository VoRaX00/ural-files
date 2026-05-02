package ru.ural.files.services;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.ural.exceptions.BadRequestException;
import ru.ural.exceptions.InternalServerException;
import ru.ural.files.common.enums.FileType;
import ru.ural.files.dto.AvatarRequest;
import ru.ural.files.dto.AvatarResponse;
import ru.ural.files.dto.FileDto;
import ru.ural.files.entities.File;
import ru.ural.files.mappers.FileMapper;
import ru.ural.files.properties.MinioProperty;
import ru.ural.files.repositories.FileRepository;
import ru.ural.models.UserPrincipals;
import ru.ural.utils.JwtUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private static final String INVALID_LOAD_INFO = "Количество файлов не соответствует типам";

    private static final String INVALID_FILE = "Не валидный файл";

    private static final String INVALID_TYPE = "Не валидный тип файла";

    private static final String ERROR_LOADING_FILE = "Ошибка загрузки файла";

    private static final String ERROR_CREATE_THUMBNAIL = "Ошибка создания уменьшенной фотографии";

    private final FileRepository fileRepository;

    private final MinioClient minioClient;

    private final MinioProperty minioProperty;

    private final FileMapper fileMapper;

    public AvatarResponse uploadAvatar(MultipartFile file, AvatarRequest metadata) {
        Authentication authentication = JwtUtils.getAuthentication();
        UserPrincipals user = JwtUtils.getUser(authentication);

        try {
            byte[] originalBytes = file.getBytes();

            var thumbnailBytes = createAvatarThumbnail(
                    file.getInputStream(),
                    metadata.getCropX(),
                    metadata.getCropY(),
                    metadata.getCropSize()
            );

            File original = buildAndUploadFile(
                    originalBytes,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    FileType.IMAGE,
                    user.getUuid()
            );

            File thumbnail = buildAndUploadFile(
                    thumbnailBytes,
                    buildThumbnailFilename(file.getOriginalFilename()),
                    "image/jpeg",
                    FileType.IMAGE,
                    user.getUuid()
            );

            List<File> saved = fileRepository.saveAll(List.of(original, thumbnail));

            return AvatarResponse.builder()
                    .photoId(saved.getFirst().getId())
                    .photoThumbnailId(saved.get(1).getId())
                    .cropX(metadata.getCropX())
                    .cropY(metadata.getCropY())
                    .cropSize(metadata.getCropSize())
                    .build();
        } catch (IOException e) {
            throw new InternalServerException(ERROR_CREATE_THUMBNAIL, e);
        }

    }

    public List<FileDto> getFiles(List<Long> ids) {
        List<File> files = fileRepository.findAllById(ids);
        return files.stream()
                .map(file -> {
                    String url = getFileUrl(file);
                    FileDto dto = fileMapper.toDto(file);
                    dto.setUrl(url);
                    return dto;
                })
                .toList();
    }

    public String getFileUrl(File file) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioProperty.getBucket())
                            .object(file.getPath())
                            .expiry(60 * 60)
                            .build()
            );
        } catch (Exception e) {
            throw new InternalServerException("Ошибка генерации ссылки", e);
        }
    }

    // TODO: оптимизировать процесс сохранения файлов в S3
    @Transactional
    public List<File> uploadFiles(List<MultipartFile> files, List<FileType> types) {
        if (files.size() != types.size()) {
            throw new BadRequestException(INVALID_LOAD_INFO);
        }

        Authentication authentication = JwtUtils.getAuthentication();
        UserPrincipals user = JwtUtils.getUser(authentication);

        var entities = new ArrayList<File>();
        IntStream.range(0, files.size()).forEach(i -> {
            var file = files.get(i);
            var fileType = Optional.of(types.get(i))
                    .orElseThrow(() -> new BadRequestException(INVALID_TYPE));

            var entity = processFile(file, fileType, user.getUuid());
            entities.add(entity);
        });

        return fileRepository.saveAll(entities);
    }

    private File processFile(MultipartFile file, FileType type, String userUuid) {
        var originalFileName = Optional.ofNullable(file.getOriginalFilename())
                .orElseThrow(() -> new BadRequestException(INVALID_FILE));

        var dotIndex = findDotIndex(originalFileName);
        var name = getFileName(originalFileName, dotIndex);
        var extension = getExtension(originalFileName, dotIndex);
        var path = uploadFile(file);

        return File.builder()
                .name(name)
                .extension(extension)
                .type(type)
                .size(file.getSize())
                .createdAt(ZonedDateTime.now())
                .userUuid(userUuid)
                .path(path)
                .build();
    }

    private int findDotIndex(String originalFilename) {
        var dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex <= 0 || dotIndex == originalFilename.length() - 1) {
            throw new BadRequestException(INVALID_FILE);
        }
        return dotIndex;
    }

    private String getFileName(String originalFilename, int dotIndex) {
        var name = originalFilename.substring(0, dotIndex);
        if (name.isBlank()) {
            throw new BadRequestException(INVALID_FILE);
        }
        return name;
    }

    private String getExtension(String originalFilename, int dotIndex) {
        var extension = originalFilename.substring(dotIndex);
        if (extension.isBlank() || extension.length() == 1) {
            throw new BadRequestException(INVALID_FILE);
        }
        return extension;
    }

    private String uploadFile(MultipartFile file) {
        var path = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .stream(inputStream, file.getSize(), -1)
                    .bucket(minioProperty.getBucket())
                    .object(path)
                    .contentType(file.getContentType())
                    .build());

            return path;
        } catch (Exception e) {
            throw new InternalServerException(ERROR_LOADING_FILE, e);
        }
    }

    private byte[] createAvatarThumbnail(
            InputStream originalImage,
            int cropX,
            int cropY,
            int cropSize
    ) throws IOException {

        BufferedImage source = ImageIO.read(originalImage);

        if (source == null) {
            throw new BadRequestException(INVALID_FILE);
        }

        validateCropBounds(source, cropX, cropY, cropSize);

        BufferedImage cropped = source.getSubimage(
                cropX,
                cropY,
                cropSize,
                cropSize
        );

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Thumbnails.of(cropped)
                .size(256, 256)
                .outputFormat("jpg")
                .outputQuality(0.85)
                .toOutputStream(output);

        return output.toByteArray();
    }

    private File buildAndUploadFile(
            byte[] bytes,
            String originalFileName,
            String contentType,
            FileType type,
            String userUuid
    ) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BadRequestException(INVALID_FILE);
        }

        var dotIndex = findDotIndex(originalFileName);
        var name = getFileName(originalFileName, dotIndex);
        var extension = getExtension(originalFileName, dotIndex);

        String path = uploadBytes(bytes, originalFileName, contentType);

        return File.builder()
                .name(name)
                .extension(extension)
                .type(type)
                .size((long) bytes.length)
                .createdAt(ZonedDateTime.now())
                .userUuid(userUuid)
                .path(path)
                .build();
    }

    private String uploadBytes(byte[] bytes, String filename, String contentType) {
        String path = UUID.randomUUID() + "_" + filename;

        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .stream(inputStream, bytes.length, -1)
                            .bucket(minioProperty.getBucket())
                            .object(path)
                            .contentType(contentType)
                            .build()
            );

            return path;
        } catch (Exception e) {
            throw new InternalServerException(ERROR_LOADING_FILE, e);
        }
    }

    private void validateCropBounds(
            BufferedImage source,
            int cropX,
            int cropY,
            int cropSize
    ) {
        if (cropX + cropSize > source.getWidth()
                || cropY + cropSize > source.getHeight()) {
            throw new BadRequestException("Область обрезки выходит за границы изображения");
        }
    }

    private String buildThumbnailFilename(String originalFilename) {
        var dotIndex = findDotIndex(originalFilename);
        var name = getFileName(originalFilename, dotIndex);

        return name + "_thumbnail.jpg";
    }

}
