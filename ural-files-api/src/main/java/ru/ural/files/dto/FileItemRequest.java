package ru.ural.files.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import ru.ural.files.common.enums.FileType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileItemRequest {

    @NotEmpty
    private MultipartFile file;

    @NotEmpty
    private FileType type;

}
