package com.codivio.project.repository;

import com.codivio.project.entity.ProjectFileTree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 项目文件树数据访问接口
 * 基于Spring Data JPA，提供层级化文件树查询功能
 */
@Repository
public interface ProjectFileTreeRepository extends JpaRepository<ProjectFileTree, Long> {

    /**
     * 根据项目ID和父路径查询子节点
     * 结果按类型降序（目录在前）、文件名升序排列
     * 
     * @param projectId 项目ID
     * @param parentPath 父级路径（根目录传null）
     * @return 子节点列表，目录在前，文件在后
     */
    List<ProjectFileTree> findByProjectIdAndParentPathOrderByTypeDescFileNameAsc(
            Long projectId, String parentPath);

    /**
     * 根据项目ID查询整个项目的文件树
     * 按文件路径升序排列，便于构建层级结构
     * 
     * @param projectId 项目ID
     * @return 完整的文件树列表
     */
    List<ProjectFileTree> findByProjectIdOrderByFilePathAsc(Long projectId);

    /**
     * 根据项目ID和文件路径查找特定节点
     * 用于检查路径是否存在或获取特定节点
     * 
     * @param projectId 项目ID
     * @param filePath 文件路径
     * @return 文件树节点（可能为空）
     */
    Optional<ProjectFileTree> findByProjectIdAndFilePath(Long projectId, String filePath);

    /**
     * 检查指定路径是否存在
     * 用于创建节点前的路径冲突检查
     * 
     * @param projectId 项目ID
     * @param filePath 文件路径
     * @return true-存在，false-不存在
     */
    boolean existsByProjectIdAndFilePath(Long projectId, String filePath);

    /**
     * 根据项目ID和父路径检查是否存在子节点
     * 用于判断目录是否为空，决定是否可以删除
     * 
     * @param projectId 项目ID
     * @param parentPath 父级路径
     * @return true-存在子节点，false-无子节点
     */
    boolean existsByProjectIdAndParentPath(Long projectId, String parentPath);

    /**
     * 根据文件服务ID查找文件树节点
     * 用于文件服务回调时更新文件树信息
     * 
     * @param fileId 文件服务ID
     * @return 文件树节点（可能为空）
     */
    Optional<ProjectFileTree> findByFileId(String fileId);

    /**
     * 批量删除节点及其所有子节点
     * 删除指定路径及其所有子路径的节点
     * 
     * @param projectId 项目ID
     * @param filePath 要删除的路径
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM ProjectFileTree pft WHERE pft.projectId = :projectId " +
           "AND (pft.filePath = :filePath OR pft.filePath LIKE CONCAT(:filePath, '/%'))")
    int deleteByProjectIdAndFilePathStartingWith(
            @Param("projectId") Long projectId, 
            @Param("filePath") String filePath);

    /**
     * 根据父路径查询直接子节点数量
     * 用于统计目录下的文件和子目录数量
     * 
     * @param projectId 项目ID
     * @param parentPath 父级路径
     * @return 子节点数量
     */
    long countByProjectIdAndParentPath(Long projectId, String parentPath);

    /**
     * 根据项目ID查询根目录节点（父路径为null的节点）
     * 用于构建文件树的根节点列表
     * 
     * @param projectId 项目ID
     * @return 根目录节点列表
     */
    List<ProjectFileTree> findByProjectIdAndParentPathIsNullOrderByTypeDescFileNameAsc(
            Long projectId);

    /**
     * 查询指定路径下的所有子节点（包括多级子节点）
     * 用于复制目录时获取所有子节点
     * 
     * @param projectId 项目ID
     * @param parentPath 父级路径
     * @return 所有子节点列表
     */
    @Query("SELECT pft FROM ProjectFileTree pft WHERE pft.projectId = :projectId " +
           "AND pft.filePath LIKE CONCAT(:parentPath, '/%') " +
           "ORDER BY pft.filePath ASC")
    List<ProjectFileTree> findAllChildrenByProjectIdAndParentPath(
            @Param("projectId") Long projectId, 
            @Param("parentPath") String parentPath);

    /**
     * 根据项目ID和文件类型查询节点列表
     * 用于统计项目中的文件或目录数量
     * 
     * @param projectId 项目ID
     * @param type 节点类型
     * @return 指定类型的节点列表
     */
    List<ProjectFileTree> findByProjectIdAndType(Long projectId, ProjectFileTree.FileTreeType type);

    /**
     * 统计项目中的文件数量（不包括目录）
     * 
     * @param projectId 项目ID
     * @return 文件数量
     */
    long countByProjectIdAndType(Long projectId, ProjectFileTree.FileTreeType type);

    /**
     * 批量更新父路径
     * 用于重命名或移动目录时更新所有子节点的父路径
     * 
     * @param projectId 项目ID
     * @param oldParentPath 旧的父路径
     * @param newParentPath 新的父路径
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE ProjectFileTree pft SET pft.parentPath = " +
           "CASE " +
           "  WHEN pft.parentPath = :oldParentPath THEN :newParentPath " +
           "  WHEN pft.parentPath LIKE CONCAT(:oldParentPath, '/%') THEN " +
           "    CONCAT(:newParentPath, SUBSTRING(pft.parentPath, LENGTH(:oldParentPath) + 1)) " +
           "  ELSE pft.parentPath " +
           "END " +
           "WHERE pft.projectId = :projectId " +
           "AND (pft.parentPath = :oldParentPath OR pft.parentPath LIKE CONCAT(:oldParentPath, '/%'))")
    int updateParentPathByProjectIdAndOldParentPath(
            @Param("projectId") Long projectId,
            @Param("oldParentPath") String oldParentPath,
            @Param("newParentPath") String newParentPath);

    /**
     * 批量更新文件路径
     * 用于重命名或移动目录时更新所有子节点的文件路径
     * 
     * @param projectId 项目ID
     * @param oldFilePath 旧的文件路径
     * @param newFilePath 新的文件路径
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE ProjectFileTree pft SET pft.filePath = " +
           "CASE " +
           "  WHEN pft.filePath = :oldFilePath THEN :newFilePath " +
           "  WHEN pft.filePath LIKE CONCAT(:oldFilePath, '/%') THEN " +
           "    CONCAT(:newFilePath, SUBSTRING(pft.filePath, LENGTH(:oldFilePath) + 1)) " +
           "  ELSE pft.filePath " +
           "END " +
           "WHERE pft.projectId = :projectId " +
           "AND (pft.filePath = :oldFilePath OR pft.filePath LIKE CONCAT(:oldFilePath, '/%'))")
    int updateFilePathByProjectIdAndOldFilePath(
            @Param("projectId") Long projectId,
            @Param("oldFilePath") String oldFilePath,
            @Param("newFilePath") String newFilePath);

    /**
     * 查询以指定路径开头的所有节点
     * 用于重命名/移动目录时查找所有子节点
     * 
     * @param projectId 项目ID
     * @param pathPrefix 路径前缀
     * @return 匹配的节点列表
     */
    @Query("SELECT pft FROM ProjectFileTree pft WHERE pft.projectId = :projectId " +
           "AND pft.filePath LIKE CONCAT(:pathPrefix, '%') " +
           "ORDER BY pft.filePath ASC")
    List<ProjectFileTree> findByProjectIdAndFilePathStartingWith(
            @Param("projectId") Long projectId, 
            @Param("pathPrefix") String pathPrefix);

    /**
     * 根据项目ID和父路径查询子节点（不排序）
     * 用于删除操作中查找直接子节点
     * 
     * @param projectId 项目ID
     * @param parentPath 父级路径
     * @return 子节点列表
     */
    List<ProjectFileTree> findByProjectIdAndParentPath(Long projectId, String parentPath);

    /**
     * 根据项目ID删除整个项目的文件树
     * 用于删除项目时清理文件树数据
     * 
     * @param projectId 项目ID
     * @return 删除的记录数
     */
    @Modifying
    int deleteByProjectId(Long projectId);
}