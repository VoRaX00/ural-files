package ru.ural.files.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import ru.ural.files.common.enums.FileType;
import ru.ural.files.dto.FileDto;
import java.util.List;

@Validated
@RequestMapping("/api/files")
@Tag(name = "Files api", description = "API для работы с файлами")
public interface FileApi {

    ResponseEntity<List<FileDto>> uploadFiles(
            @RequestPart List<MultipartFile> files,
            @RequestPart("files") List<FileType> types
    );

}
