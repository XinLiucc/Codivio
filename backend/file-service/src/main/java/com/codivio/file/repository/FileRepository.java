package com.codivio.file.repository;

import com.codivio.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文件数据访问层
 */
@Repository
public interface FileRepository extends JpaRepository<File, String> {

    /**
     * 根据项目ID查询文件列表
     * @param projectId 项目ID
     * @return 文件列表
     */
    List<File> findByProjectIdAndStatus(Long projectId, Integer status);

    /**
     * 根据项目ID和文件名查询文件
     * @param projectId 项目ID
     * @param originalName 原始文件名
     * @return 文件信息
     */
    Optional<File> findByProjectIdAndOriginalNameAndStatus(Long projectId, String originalName, Integer status);

    /**
     * 根据最后编辑者ID查询文件列表
     * @param lastEditorId 最后编辑者ID
     * @return 文件列表
     */
    List<File> findByLastEditorIdAndStatus(Long lastEditorId, Integer status);

    /**
     * 根据文件类型查询文件列表
     * @param projectId 项目ID
     * @param mimeType MIME类型
     * @return 文件列表
     */
    List<File> findByProjectIdAndMimeTypeAndStatus(Long projectId, String mimeType, Integer status);

    /**
     * 统计项目中的文件数量
     * @param projectId 项目ID
     * @return 文件数量
     */
    @Query("SELECT COUNT(f) FROM File f WHERE f.projectId = :projectId AND f.status = 1")
    Long countByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计项目中的文件总大小
     * @param projectId 项目ID
     * @return 文件总大小（字节）
     */
    @Query("SELECT SUM(f.fileSize) FROM File f WHERE f.projectId = :projectId AND f.status = 1")
    Long sumFileSizeByProjectId(@Param("projectId") Long projectId);
}