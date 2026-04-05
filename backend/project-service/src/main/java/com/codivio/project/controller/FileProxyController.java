package com.codivio.project.controller;

import com.codivio.project.client.FileServiceClient;
import com.codivio.project.dto.ResultVO;
import com.codivio.project.entity.ProjectFileTree;
import com.codivio.project.repository.ProjectFileTreeRepository;
import com.codivio.project.service.impl.ProjectFileTreeServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * 文件内容代理控制器
 * 代理文件服务的文件内容操作，同时维护文件树状态
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/files")
public class FileProxyController {

    private static final Logger logger = LoggerFactory.getLogger(FileProxyController.class);

    @Autowired
    private FileServiceClient fileServiceClient;
    
    @Autowired
    private ProjectFileTreeRepository projectFileTreeRepository;
    
    @Autowired
    private ProjectFileTreeServiceImpl projectFileTreeService;

    /**
     * 获取文件内容
     * 通过文件路径获取文件内容，代理到文件服务
     * 
     * @param projectId 项目ID
     * @param filePath 文件路径
     * @param userId 用户ID
     * @return 文件内容
     */
    @GetMapping("/content")
    public ResultVO<Map<String, Object>> getFileContent(
            @PathVariable String projectId,
            @RequestParam String filePath,
            @RequestHeader("X-User-Id") Long userId) {
        
        logger.info("获取文件内容: projectId={}, filePath={}, userId={}", 
                   projectId, filePath, userId);

        try {
            // 1. 权限验证
            if (!projectFileTreeService.hasAccessPermission(projectId, userId)) {
                return ResultVO.error(403, "没有项目访问权限");
            }

            // 2. 查找文件树节点
            Optional<ProjectFileTree> nodeOpt = projectFileTreeRepository
                .findByProjectIdAndFilePath(projectId, filePath);
            
            if (nodeOpt.isEmpty()) {
                return ResultVO.error(404, "文件不存在");
            }

            ProjectFileTree node = nodeOpt.get();
            
            // 3. 检查是否是文件类型
            if (node.getType() != ProjectFileTree.FileTreeType.FILE) {
                return ResultVO.error(400, "只能获取文件内容，不能获取目录内容");
            }

            // 4. 检查是否有关联的fileId
            if (!StringUtils.hasText(node.getFileId())) {
                return ResultVO.error(404, "文件内容不存在，可能文件还未创建");
            }

            // 5. 代理到文件服务获取内容
            ResultVO<Map<String, Object>> result = fileServiceClient
                .getFileContent(node.getFileId());

            logger.info("文件内容获取成功: filePath={}, fileId={}", filePath, node.getFileId());
            return result;

        } catch (Exception e) {
            logger.error("获取文件内容失败: projectId={}, filePath={}", 
                        projectId, filePath, e);
            return ResultVO.error(500, "获取文件内容失败: " + e.getMessage());
        }
    }

    /**
     * 保存文件内容
     * 通过文件路径保存文件内容，代理到文件服务
     * 
     * @param projectId 项目ID
     * @param filePath 文件路径
     * @param content 文件内容
     * @param userId 用户ID
     * @return 保存结果
     */
    @PutMapping("/content")
    public ResultVO<Void> saveFileContent(
            @PathVariable String projectId,
            @RequestParam String filePath,
            @RequestBody Map<String, Object> content,
            @RequestHeader("X-User-Id") Long userId) {
        
        logger.info("保存文件内容: projectId={}, filePath={}, userId={}", 
                   projectId, filePath, userId);

        try {
            // 1. 权限验证 - 需要编辑权限
            if (!projectFileTreeService.hasEditPermission(projectId, userId)) {
                return ResultVO.error(403, "没有项目编辑权限");
            }

            // 2. 查找文件树节点
            Optional<ProjectFileTree> nodeOpt = projectFileTreeRepository
                .findByProjectIdAndFilePath(projectId, filePath);
            
            if (nodeOpt.isEmpty()) {
                return ResultVO.error(404, "文件不存在");
            }

            ProjectFileTree node = nodeOpt.get();
            
            // 3. 检查是否是文件类型
            if (node.getType() != ProjectFileTree.FileTreeType.FILE) {
                return ResultVO.error(400, "只能保存文件内容，不能保存目录");
            }

            // 4. 检查是否有关联的fileId
            if (!StringUtils.hasText(node.getFileId())) {
                return ResultVO.error(404, "文件内容不存在，请先创建文件");
            }

            // 5. 代理到文件服务保存内容
            ResultVO<Void> result = fileServiceClient
                .updateFileContent(node.getFileId(), content);

            // 6. 更新文件树节点的编辑信息
            if (result.getCode() == 200) {
                projectFileTreeService.updateFileEditInfo(projectId, filePath, userId);
            }

            logger.info("文件内容保存成功: filePath={}, fileId={}", filePath, node.getFileId());
            return result;

        } catch (Exception e) {
            logger.error("保存文件内容失败: projectId={}, filePath={}", 
                        projectId, filePath, e);
            return ResultVO.error(500, "保存文件内容失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在于文件服务
     * 用于文件树同步验证
     * 
     * @param projectId 项目ID
     * @param filePath 文件路径
     * @param userId 用户ID
     * @return 文件存在状态
     */
    @GetMapping("/exists")
    public ResultVO<Boolean> checkFileExists(
            @PathVariable String projectId,
            @RequestParam String filePath,
            @RequestHeader("X-User-Id") Long userId) {
        
        logger.debug("检查文件存在性: projectId={}, filePath={}", projectId, filePath);

        try {
            // 1. 权限验证
            if (!projectFileTreeService.hasAccessPermission(projectId, userId)) {
                return ResultVO.error(403, "没有项目访问权限");
            }

            // 2. 查找文件树节点
            Optional<ProjectFileTree> nodeOpt = projectFileTreeRepository
                .findByProjectIdAndFilePath(projectId, filePath);
            
            if (nodeOpt.isEmpty()) {
                return ResultVO.success(false, "文件树中不存在此文件");
            }

            ProjectFileTree node = nodeOpt.get();
            
            // 3. 如果没有fileId，说明物理文件不存在
            if (!StringUtils.hasText(node.getFileId())) {
                return ResultVO.success(false, "物理文件不存在");
            }

            // 4. 检查文件服务中是否存在
            ResultVO<Boolean> result = fileServiceClient.checkFileExists(node.getFileId());
            
            return result;

        } catch (Exception e) {
            logger.error("检查文件存在性失败: projectId={}, filePath={}", 
                        projectId, filePath, e);
            return ResultVO.error(500, "检查文件存在性失败: " + e.getMessage());
        }
    }
}