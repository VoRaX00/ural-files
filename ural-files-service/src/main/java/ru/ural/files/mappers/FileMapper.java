package ru.ural.files.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.ural.files.dto.FileDto;
import ru.ural.files.entities.File;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileMapper {

    FileDto toDto(File file);

    List<FileDto> toDto(List<File> file);

}
