package ru.ural.files.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvatarRequest {

    @Schema(description = "Срез по x")
    private Integer cropX;

    @Schema(description = "Срез по y")
    private Integer cropY;

    @Schema(description = "Размер среза")
    private Integer cropSize;

}
