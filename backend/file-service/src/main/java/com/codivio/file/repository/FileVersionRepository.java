package com.codivio.file.repository;

import com.codivio.file.entity.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileVersionRepository extends JpaRepository<FileVersion, Long> {

    List<FileVersion> findByFileIdOrderByVersionDesc(String fileId);

    Optional<FileVersion> findByFileIdAndVersion(String fileId, Integer version);
}
