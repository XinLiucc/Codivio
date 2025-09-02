package com.codivio.file.service;

import com.codivio.file.dto.FileResponseDTO;
import com.codivio.file.dto.FileUploadRequestDTO;
import com.codivio.file.entity.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件
     * @param request 文件上传请求
     * @param currentUserId 当前用户ID
     * @return 文件响应信息
     */
    FileResponseDTO uploadFile(FileUploadRequestDTO request, Long currentUserId);

    /**
     * 上传多媒体文件（MultipartFile方式）
     * @param file 上传的文件
     * @param projectId 项目ID
     * @param filePath 文件路径
     * @param currentUserId 当前用户ID
     * @return 文件响应信息
     */
    FileResponseDTO uploadMultipartFile(MultipartFile file, Long projectId, String filePath, Long currentUserId);

    /**
     * 根据文件ID获取文件信息
     * @param fileId 文件ID
     * @param currentUserId 当前用户ID
     * @return 文件响应信息
     */
    FileResponseDTO getFileById(String fileId, Long currentUserId);

    /**
     * 根据项目ID获取文件列表
     * @param projectId 项目ID
     * @param currentUserId 当前用户ID
     * @return 文件列表
     */
    List<FileResponseDTO> getFilesByProjectId(Long projectId, Long currentUserId);

    /**
     * 更新文件内容
     * @param fileId 文件ID
     * @param content 新的文件内容
     * @param currentUserId 当前用户ID
     * @return 更新后的文件信息
     */
    FileResponseDTO updateFileContent(String fileId, String content, Long currentUserId);

    /**
     * 删除文件（逻辑删除）
     * @param fileId 文件ID
     * @param currentUserId 当前用户ID
     * @return 是否删除成功
     */
    boolean deleteFile(String fileId, Long currentUserId);

    /**
     * 检查用户是否有权限访问文件
     * @param fileId 文件ID
     * @param currentUserId 当前用户ID
     * @param requiredPermission 需要的权限类型
     * @return 是否有权限
     */
    boolean hasFilePermission(String fileId, Long currentUserId, String requiredPermission);

    /**
     * 统计项目文件数量
     * @param projectId 项目ID
     * @return 文件数量
     */
    Long countProjectFiles(Long projectId);

    /**
     * 统计项目文件总大小
     * @param projectId 项目ID
     * @return 文件总大小（字节）
     */
    Long getProjectTotalFileSize(Long projectId);
}