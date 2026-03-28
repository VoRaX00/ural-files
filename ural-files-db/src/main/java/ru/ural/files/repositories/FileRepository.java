package ru.ural.files.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ural.files.entities.File;

public interface FileRepository extends JpaRepository<File, Long> {
}
