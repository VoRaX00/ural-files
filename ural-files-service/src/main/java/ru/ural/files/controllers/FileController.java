package ru.ural.files.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.ural.files.api.FileApi;

@RestController
@RequiredArgsConstructor
public class FileController implements FileApi {
}
