package ru.ural.files.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import ru.ural.files.common.enums.FileType;
import ru.ural.files.dto.AvatarRequest;
import ru.ural.files.dto.AvatarResponse;
import ru.ural.files.dto.FileDto;
import java.util.List;

@RequestMapping("/api/files")
@Tag(name = "Files api", description = "API для работы с файлами")
public interface FileApi {

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<List<FileDto>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("types") List<FileType> types
    );

    @GetMapping
    ResponseEntity<List<FileDto>> getFiles(@RequestParam List<Long> ids);

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<AvatarResponse> uploadAvatar(
            @RequestPart("file") MultipartFile file,
            @RequestPart("metadata") AvatarRequest metadata
    );

}
