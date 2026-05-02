package ru.ural.files.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.ural.files.api.FileApi;
import ru.ural.files.common.enums.FileType;
import ru.ural.files.dto.AvatarRequest;
import ru.ural.files.dto.AvatarResponse;
import ru.ural.files.dto.FileDto;
import ru.ural.files.mappers.FileMapper;
import ru.ural.files.services.FileService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FileController implements FileApi {

    private final FileService fileService;

    private final FileMapper fileMapper;

    @Override
    public ResponseEntity<List<FileDto>> uploadFiles(List<MultipartFile> files, List<FileType> types) {
        var savedFiles = fileService.uploadFiles(files, types);
        return ResponseEntity.ok(fileMapper.toDto(savedFiles));
    }

    @Override
    public ResponseEntity<List<FileDto>> getFiles(List<Long> ids) {
        return ResponseEntity.ok(fileService.getFiles(ids));
    }

    @Override
    public ResponseEntity<AvatarResponse> uploadAvatar(MultipartFile file, AvatarRequest metadata) {
        return ResponseEntity.ok(fileService.uploadAvatar(file, metadata));
    }

}
