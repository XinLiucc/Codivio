package com.codivio.file.controller;

import com.codivio.file.dto.FileResponseDTO;
import com.codivio.file.dto.FileUploadRequestDTO;
import com.codivio.file.dto.ResultVO;
import com.codivio.file.service.FileService;
import com.codivio.file.util.GatewayUserUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件控制器
 * 处理文件相关的HTTP请求
 */
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private GatewayUserUtil gatewayUserUtil;

    /**
     * 上传文件（JSON方式）
     * POST /api/v1/files/upload
     * 
     * @param requestDTO 文件上传请求数据
     * @return 上传成功的文件信息
     */
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ResultVO<FileResponseDTO> uploadFile(
            @Valid @RequestBody FileUploadRequestDTO requestDTO) {
        
        try {
            logger.info("接收文件上传请求，项目ID：{}，文件路径：{}", 
                        requestDTO.getProjectId(), requestDTO.getFilePath());

            // 获取当前用户ID
            Long currentUserId = gatewayUserUtil.getCurrentUserId();
            
            // 调用服务层
            FileResponseDTO responseDTO = fileService.uploadFile(requestDTO, currentUserId);
            
            logger.info("文件上传成功，文件ID：{}", responseDTO.getId());
            return ResultVO.success("文件上传成功", responseDTO);
            
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            return ResultVO.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件（MultipartFile方式）
     * POST /api/v1/files/upload-multipart
     * 
     * @param file 上传的文件
     * @param projectId 项目ID
     * @param filePath 文件在项目中的路径
     * @return 上传成功的文件信息
     */
    @PostMapping("/upload-multipart")
    @ResponseStatus(HttpStatus.CREATED)
    public ResultVO<FileResponseDTO> uploadMultipartFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId,
            @RequestParam("filePath") String filePath) {
        
        try {
            logger.info("接收MultipartFile上传请求，项目ID：{}，文件名：{}，文件路径：{}", 
                        projectId, file.getOriginalFilename(), filePath);

            // 获取当前用户ID
            Long currentUserId = gatewayUserUtil.getCurrentUserId();
            
            // 调用服务层
            FileResponseDTO responseDTO = fileService.uploadMultipartFile(
                file, projectId, filePath, currentUserId);
            
            logger.info("MultipartFile上传成功，文件ID：{}", responseDTO.getId());
            return ResultVO.success("文件上传成功", responseDTO);
            
        } catch (Exception e) {
            logger.error("MultipartFile上传失败", e);
            return ResultVO.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件ID获取文件信息
     * GET /api/v1/files/{fileId}
     * 
     * @param fileId 文件ID
     * @return 文件详细信息
     */
    @GetMapping("/{fileId}")
    public ResultVO<FileResponseDTO> getFileById(@PathVariable String fileId) {
        
        try {
            logger.info("获取文件信息请求，文件ID：{}", fileId);

            // 获取当前用户ID
            Long currentUserId = gatewayUserUtil.getCurrentUserId();
            
            // 调用服务层
            FileResponseDTO responseDTO = fileService.getFileById(fileId, currentUserId);
            
            return ResultVO.success("获取文件信息成功", responseDTO);
            
        } catch (Exception e) {
            logger.error("获取文件信息失败，文件ID：{}", fileId, e);
            return ResultVO.error("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据项目ID获取文件列表
     * GET /api/v1/files/project/{projectId}
     * 
     * @param projectId 项目ID
     * @return 项目中的文件列表
     */
    @GetMapping("/project/{projectId}")
    public ResultVO<List<FileResponseDTO>> getFilesByProjectId(@PathVariable Long projectId) {
        
        try {
            logger.info("获取项目文件列表请求，项目ID：{}", projectId);

            // 获取当前用户ID
            Long currentUserId = gatewayUserUtil.getCurrentUserId();
            
            // 调用服务层
            List<FileResponseDTO> responseDTOs = fileService.getFilesByProjectId(projectId, currentUserId);
            
            logger.info("获取项目文件列表成功，项目ID：{}，文件数量：{}", projectId, responseDTOs.size());
            return ResultVO.success("获取文件列表成功", responseDTOs);
            
        } catch (Exception e) {
            logger.error("获取项目文件列表失败，项目ID：{}", projectId, e);
            return ResultVO.error("获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新文件内容
     * PUT /api/v1/files/{fileId}/content
     * 
     * @param fileId 文件ID
     * @param content 新的文件内容
     * @return 更新后的文件信息
     */
    @PutMapping("/{fileId}/content")
    public ResultVO<FileResponseDTO> updateFileContent(
            @PathVariable String fileId,
            @RequestBody String content) {
        
        try {
            logger.info("更新文件内容请求，文件ID：{}", fileId);

            // 获取当前用户ID
            Long currentUserId = gatewayUserUtil.getCurrentUserId();
            
            // 调用服务层
            FileResponseDTO responseDTO = fileService.updateFileContent(fileId, content, currentUserId);
            
            logger.info("文件内容更新成功，文件ID：{}，新版本：{}", fileId, responseDTO.getVersion());
            return ResultVO.success("文件内容更新成功", responseDTO);
            
        } catch (Exception e) {
            logger.error("更新文件内容失败，文件ID：{}", fileId, e);
            return ResultVO.error("更新文件内容失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件（逻辑删除）
     * DELETE /api/v1/files/{fileId}
     * 
     * @param fileId 文件ID
     * @return 删除结果
     */
    @DeleteMapping("/{fileId}")
    public ResultVO<Boolean> deleteFile(@PathVariable String fileId) {
        
        try {
            logger.info("删除文件请求，文件ID：{}", fileId);

            // 获取当前用户ID
            Long currentUserId = gatewayUserUtil.getCurrentUserId();
            
            // 调用服务层
            boolean result = fileService.deleteFile(fileId, currentUserId);
            
            if (result) {
                logger.info("文件删除成功，文件ID：{}", fileId);
                return ResultVO.success("文件删除成功", true);
            } else {
                logger.warn("文件删除失败，文件ID：{}", fileId);
                return ResultVO.error("文件删除失败");
            }
            
        } catch (Exception e) {
            logger.error("删除文件失败，文件ID：{}", fileId, e);
            return ResultVO.error("删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件内容
     * GET /api/v1/files/{fileId}/download
     * 
     * @param fileId 文件ID
     * @return 文件内容（直接返回文本内容）
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<String> downloadFile(@PathVariable String fileId) {
        
        try {
            logger.info("下载文件请求，文件ID：{}", fileId);

            // 获取当前用户ID
            Long currentUserId = gatewayUserUtil.getCurrentUserId();
            
            // 调用服务层获取文件信息
            FileResponseDTO fileResponseDTO = fileService.getFileById(fileId, currentUserId);
            
            // 返回文件内容
            logger.info("文件下载成功，文件ID：{}，文件名：{}", fileId, fileResponseDTO.getOriginalName());
            return ResponseEntity.ok()
                    .header("Content-Type", fileResponseDTO.getMimeType())
                    .header("Content-Disposition", "inline; filename=\"" + fileResponseDTO.getOriginalName() + "\"")
                    .body(fileResponseDTO.getContent());
            
        } catch (Exception e) {
            logger.error("下载文件失败，文件ID：{}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("下载文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目文件统计信息
     * GET /api/v1/files/project/{projectId}/stats
     * 
     * @param projectId 项目ID
     * @return 文件统计信息
     */
    @GetMapping("/project/{projectId}/stats")
    public ResultVO<FileStatsDTO> getProjectFileStats(@PathVariable Long projectId) {
        
        try {
            logger.info("获取项目文件统计请求，项目ID：{}", projectId);

            // 获取文件数量和总大小
            Long fileCount = fileService.countProjectFiles(projectId);
            Long totalSize = fileService.getProjectTotalFileSize(projectId);
            
            FileStatsDTO statsDTO = new FileStatsDTO();
            statsDTO.setProjectId(projectId);
            statsDTO.setFileCount(fileCount);
            statsDTO.setTotalSize(totalSize);
            statsDTO.setTotalSizeFormatted(com.codivio.file.util.FileTypeUtil.formatFileSize(totalSize));
            
            return ResultVO.success("获取文件统计成功", statsDTO);
            
        } catch (Exception e) {
            logger.error("获取项目文件统计失败，项目ID：{}", projectId, e);
            return ResultVO.error("获取文件统计失败: " + e.getMessage());
        }
    }

    /**
     * 文件统计DTO
     */
    public static class FileStatsDTO {
        private Long projectId;
        private Long fileCount;
        private Long totalSize;
        private String totalSizeFormatted;

        // Getter and Setter methods
        public Long getProjectId() {
            return projectId;
        }

        public void setProjectId(Long projectId) {
            this.projectId = projectId;
        }

        public Long getFileCount() {
            return fileCount;
        }

        public void setFileCount(Long fileCount) {
            this.fileCount = fileCount;
        }

        public Long getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(Long totalSize) {
            this.totalSize = totalSize;
        }

        public String getTotalSizeFormatted() {
            return totalSizeFormatted;
        }

        public void setTotalSizeFormatted(String totalSizeFormatted) {
            this.totalSizeFormatted = totalSizeFormatted;
        }

        @Override
        public String toString() {
            return "FileStatsDTO{" +
                    "projectId=" + projectId +
                    ", fileCount=" + fileCount +
                    ", totalSize=" + totalSize +
                    ", totalSizeFormatted='" + totalSizeFormatted + '\'' +
                    '}';
        }
    }
}