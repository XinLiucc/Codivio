package com.codivio.file.service.impl;

import com.codivio.file.client.ProjectServiceClient;
import com.codivio.file.dto.FileResponseDTO;
import com.codivio.file.dto.FileUploadRequestDTO;
import com.codivio.file.entity.File;
import com.codivio.file.repository.FileRepository;
import com.codivio.file.service.FileService;
import com.codivio.file.util.FileTypeUtil;
import com.codivio.file.util.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文件服务实现类
 */
@Service
@Transactional
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Autowired
    private ProjectServiceClient projectServiceClient;

    /**
     * 文件大小限制（10MB）
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Override
    public FileResponseDTO uploadFile(FileUploadRequestDTO request, Long currentUserId) {
        logger.info("开始上传文件，项目ID：{}，文件路径：{}，用户ID：{}", 
                    request.getProjectId(), request.getFilePath(), currentUserId);

        // 1. 验证用户权限
        if (!hasProjectWritePermission(request.getProjectId(), currentUserId)) {
            throw new RuntimeException("没有权限在该项目中上传文件");
        }

        // 2. 从文件路径提取文件名
        String fileName = extractFileName(request.getFilePath());
        
        // 3. 验证文件类型
        if (!FileTypeUtil.isAllowedFileType(fileName)) {
            throw new RuntimeException("不支持的文件类型：" + fileName);
        }

        // 4. 验证文件内容大小
        if (request.getContent() != null) {
            long contentSize = request.getContent().getBytes().length;
            if (FileTypeUtil.isFileSizeExceeded(contentSize, MAX_FILE_SIZE)) {
                throw new RuntimeException("文件大小超出限制：" + FileTypeUtil.formatFileSize(contentSize));
            }
        }

        // 5. 检查项目中是否已存在同名文件
        Optional<File> existingFile = fileRepository.findByProjectIdAndOriginalNameAndStatus(
                request.getProjectId(), fileName, 1);
        
        if (existingFile.isPresent()) {
            // 如果文件已存在，更新内容
            return updateExistingFile(existingFile.get(), request.getContent(), currentUserId);
        } else {
            // 创建新文件
            return createNewFile(request, fileName, currentUserId);
        }
    }

    @Override
    public FileResponseDTO uploadMultipartFile(MultipartFile file, Long projectId, String filePath, Long currentUserId) {
        logger.info("开始上传MultipartFile文件，项目ID：{}，文件名：{}，用户ID：{}", 
                    projectId, file.getOriginalFilename(), currentUserId);

        try {
            // 转换为FileUploadRequestDTO
            FileUploadRequestDTO request = new FileUploadRequestDTO();
            request.setProjectId(projectId);
            request.setFilePath(filePath);
            request.setContent(new String(file.getBytes()));
            
            return uploadFile(request, currentUserId);
        } catch (IOException e) {
            logger.error("读取MultipartFile内容失败", e);
            throw new RuntimeException("读取文件内容失败");
        }
    }

    @Override
    public FileResponseDTO getFileById(String fileId, Long currentUserId) {
        logger.info("获取文件信息，文件ID：{}，用户ID：{}", fileId, currentUserId);

        // 1. 查找文件
        Optional<File> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty() || fileOpt.get().getStatus() == 0) {
            throw new RuntimeException("文件不存在或已删除");
        }

        File file = fileOpt.get();

        // 2. 验证用户权限
        if (!hasProjectReadPermission(file.getProjectId(), currentUserId)) {
            throw new RuntimeException("没有权限访问该文件");
        }

        // 3. 转换为响应DTO
        return convertToFileResponseDTO(file);
    }

    @Override
    public List<FileResponseDTO> getFilesByProjectId(Long projectId, Long currentUserId) {
        logger.info("获取项目文件列表，项目ID：{}，用户ID：{}", projectId, currentUserId);

        // 1. 验证用户权限
        if (!hasProjectReadPermission(projectId, currentUserId)) {
            throw new RuntimeException("没有权限访问该项目文件");
        }

        // 2. 查询文件列表
        List<File> files = fileRepository.findByProjectIdAndStatus(projectId, 1);

        // 3. 转换为响应DTO
        return files.stream()
                   .map(this::convertToFileResponseDTO)
                   .collect(Collectors.toList());
    }

    @Override
    public FileResponseDTO updateFileContent(String fileId, String content, Long currentUserId) {
        logger.info("更新文件内容，文件ID：{}，用户ID：{}", fileId, currentUserId);

        // 1. 查找文件
        Optional<File> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty() || fileOpt.get().getStatus() == 0) {
            throw new RuntimeException("文件不存在或已删除");
        }

        File file = fileOpt.get();

        // 2. 验证用户权限
        if (!hasProjectWritePermission(file.getProjectId(), currentUserId)) {
            throw new RuntimeException("没有权限编辑该文件");
        }

        // 3. 验证文件大小
        if (content != null) {
            long contentSize = content.getBytes().length;
            if (FileTypeUtil.isFileSizeExceeded(contentSize, MAX_FILE_SIZE)) {
                throw new RuntimeException("文件大小超出限制：" + FileTypeUtil.formatFileSize(contentSize));
            }
        }

        // 4. 更新文件内容
        file.setContent(content);
        file.setFileSize((long) (content != null ? content.getBytes().length : 0));
        file.setLastEditorId(currentUserId);
        file.setUpdatedAt(LocalDateTime.now());
        file.setVersion(file.getVersion() + 1);

        // 5. 保存更新
        File updatedFile = fileRepository.save(file);
        logger.info("文件内容更新成功，文件ID：{}，新版本：{}", fileId, updatedFile.getVersion());

        return convertToFileResponseDTO(updatedFile);
    }

    @Override
    public boolean deleteFile(String fileId, Long currentUserId) {
        logger.info("删除文件，文件ID：{}，用户ID：{}", fileId, currentUserId);

        // 1. 查找文件
        Optional<File> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty() || fileOpt.get().getStatus() == 0) {
            throw new RuntimeException("文件不存在或已删除");
        }

        File file = fileOpt.get();

        // 2. 验证用户权限
        if (!hasProjectDeletePermission(file.getProjectId(), currentUserId)) {
            throw new RuntimeException("没有权限删除该文件");
        }

        // 3. 逻辑删除文件
        file.setStatus(0);
        file.setLastEditorId(currentUserId);
        file.setUpdatedAt(LocalDateTime.now());

        fileRepository.save(file);
        logger.info("文件删除成功，文件ID：{}", fileId);

        return true;
    }

    @Override
    public boolean hasFilePermission(String fileId, Long currentUserId, String requiredPermission) {
        Optional<File> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty() || fileOpt.get().getStatus() == 0) {
            return false;
        }

        File file = fileOpt.get();
        
        return switch (requiredPermission.toLowerCase()) {
            case "read" -> hasProjectReadPermission(file.getProjectId(), currentUserId);
            case "write" -> hasProjectWritePermission(file.getProjectId(), currentUserId);
            case "delete" -> hasProjectDeletePermission(file.getProjectId(), currentUserId);
            default -> false;
        };
    }

    @Override
    public Long countProjectFiles(Long projectId) {
        return fileRepository.countByProjectId(projectId);
    }

    @Override
    public Long getProjectTotalFileSize(Long projectId) {
        Long totalSize = fileRepository.sumFileSizeByProjectId(projectId);
        return totalSize != null ? totalSize : 0L;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 创建新文件
     */
    private FileResponseDTO createNewFile(FileUploadRequestDTO request, String fileName, Long currentUserId) {
        // 生成文件ID
        String fileId = snowflakeIdGenerator.nextIdString();
        
        // 创建文件实体
        File file = new File();
        file.setId(fileId);
        file.setOriginalName(fileName);
        file.setFileName(fileId + FileTypeUtil.getFileExtension(fileName)); // 存储文件名使用ID+扩展名
        file.setFilePath("/files/" + file.getFileName()); // 简化版本：直接存储在files目录
        file.setFileSize((long) (request.getContent() != null ? request.getContent().getBytes().length : 0));
        file.setMimeType(FileTypeUtil.getMimeType(fileName));
        file.setFileExtension(FileTypeUtil.getFileExtension(fileName));
        file.setProjectId(request.getProjectId());
        file.setLastEditorId(currentUserId);
        file.setContent(request.getContent());
        file.setVersion(1);
        file.setStatus(1);

        // 保存文件
        File savedFile = fileRepository.save(file);
        logger.info("新文件创建成功，文件ID：{}，文件名：{}", savedFile.getId(), savedFile.getOriginalName());

        return convertToFileResponseDTO(savedFile);
    }

    /**
     * 更新现有文件
     */
    private FileResponseDTO updateExistingFile(File existingFile, String newContent, Long currentUserId) {
        existingFile.setContent(newContent);
        existingFile.setFileSize((long) (newContent != null ? newContent.getBytes().length : 0));
        existingFile.setLastEditorId(currentUserId);
        existingFile.setUpdatedAt(LocalDateTime.now());
        existingFile.setVersion(existingFile.getVersion() + 1);

        File updatedFile = fileRepository.save(existingFile);
        logger.info("现有文件更新成功，文件ID：{}，新版本：{}", updatedFile.getId(), updatedFile.getVersion());

        return convertToFileResponseDTO(updatedFile);
    }

    /**
     * 从文件路径提取文件名
     */
    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new RuntimeException("文件路径不能为空");
        }
        
        // 去除路径，只保留文件名
        int lastSlash = filePath.lastIndexOf('/');
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    /**
     * 转换为文件响应DTO
     */
    private FileResponseDTO convertToFileResponseDTO(File file) {
        FileResponseDTO response = new FileResponseDTO();
        response.setId(file.getId());
        response.setOriginalName(file.getOriginalName());
        response.setFileSize(file.getFileSize());
        response.setFileSizeFormatted(FileTypeUtil.formatFileSize(file.getFileSize()));
        response.setMimeType(file.getMimeType());
        response.setFileExtension(file.getFileExtension());
        response.setFileTypeDescription(FileTypeUtil.getFileTypeDescription(file.getOriginalName()));
        response.setProjectId(file.getProjectId());
        response.setContent(file.getContent());
        response.setVersion(file.getVersion());
        response.setLastEditorId(file.getLastEditorId());
        response.setCreatedAt(file.getCreatedAt());
        response.setUpdatedAt(file.getUpdatedAt());
        response.setDownloadUrl("/api/v1/files/" + file.getId() + "/download");
        return response;
    }

    // ========== 权限验证方法 ==========

    /**
     * 验证用户是否有项目读权限
     */
    private boolean hasProjectReadPermission(Long projectId, Long userId) {
        try {
            // 简化版本：暂时返回true，后续实现OpenFeign调用
            // ProjectServiceClient.ProjectMemberRoleResponse response = 
            //     projectServiceClient.getUserRoleInProject(projectId, userId, "Bearer " + jwt);
            // return response.getExists();
            return true; // MVP阶段暂时跳过权限验证
        } catch (Exception e) {
            logger.error("验证项目读权限失败，项目ID：{}，用户ID：{}", projectId, userId, e);
            return false;
        }
    }

    /**
     * 验证用户是否有项目写权限
     */
    private boolean hasProjectWritePermission(Long projectId, Long userId) {
        try {
            // 简化版本：暂时返回true，后续实现OpenFeign调用
            return true; // MVP阶段暂时跳过权限验证
        } catch (Exception e) {
            logger.error("验证项目写权限失败，项目ID：{}，用户ID：{}", projectId, userId, e);
            return false;
        }
    }

    /**
     * 验证用户是否有文件删除权限
     */
    private boolean hasProjectDeletePermission(Long projectId, Long userId) {
        try {
            // 简化版本：暂时返回true，后续实现角色权限检查
            return true; // MVP阶段暂时跳过权限验证
        } catch (Exception e) {
            logger.error("验证项目删除权限失败，项目ID：{}，用户ID：{}", projectId, userId, e);
            return false;
        }
    }
}