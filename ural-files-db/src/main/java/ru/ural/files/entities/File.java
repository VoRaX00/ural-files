package ru.ural.files.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import ru.ural.entities.BaseEntity;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "files")
public class File extends BaseEntity {
}
