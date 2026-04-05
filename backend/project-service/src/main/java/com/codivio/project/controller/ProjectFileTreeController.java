package com.codivio.project.controller;

import com.codivio.project.dto.ResultVO;
import com.codivio.project.entity.ProjectFileTree;
import com.codivio.project.exception.BaseBusinessException;
import com.codivio.project.exception.ErrorCode;
import com.codivio.project.service.ProjectFileTreeService;
import com.codivio.project.util.GatewayUserUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 项目文件树控制器
 * 
 * 处理项目文件树相关的HTTP请求，提供文件树查询、
 * 文件/目录的CRUD操作等RESTful API接口。
 * 
 * API路径设计：
 * - GET    /api/v1/projects/{projectId}/files           - 获取完整文件树
 * - GET    /api/v1/projects/{projectId}/files/children  - 获取指定路径的子节点
 * - POST   /api/v1/projects/{projectId}/files/nodes     - 创建文件/目录节点
 * - PUT    /api/v1/projects/{projectId}/files/nodes     - 重命名节点
 * - DELETE /api/v1/projects/{projectId}/files/nodes     - 删除节点
 * - PUT    /api/v1/projects/{projectId}/files/nodes/move - 移动节点
 * 
 * 权限控制：
 * - 查询操作：项目成员（OWNER/EDITOR/VIEWER）
 * - 修改操作：项目编辑者（OWNER/EDITOR）
 * 
 * 响应格式：统一使用ResultVO包装返回数据
 * 
 * @author Codivio Development Team  
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/files")
public class ProjectFileTreeController {

    // ================================ 依赖注入 ================================

    /**
     * 项目文件树服务接口
     */
    @Autowired
    private ProjectFileTreeService projectFileTreeService;

    /**
     * 网关用户工具类 - 用于获取当前登录用户信息
     */
    @Autowired
    private GatewayUserUtil gatewayUserUtil;

    // ================================ 查询相关接口 ================================

    /**
     * 获取项目的完整文件树结构
     * 
     * GET /api/v1/projects/{projectId}/files
     * 
     * 接口功能：
     * 1. 验证用户对项目的访问权限
     * 2. 查询项目所有文件树节点
     * 3. 构建层级化的树形数据结构
     * 4. 返回包含统计信息的完整文件树
     * 
     * 权限要求：用户必须是项目成员
     * 
     * 响应数据结构：
     * ```json
     * {
     *   "code": 200,
     *   "message": "success", 
     *   "data": {
     *     "tree": [...],           // 树形节点数据
     *     "totalFiles": 15,        // 文件总数
     *     "totalDirectories": 8,   // 目录总数
     *     "lastModified": "..."    // 最后修改时间
     *   }
     * }
     * ```
     * 
     * @param projectId 项目ID，通过路径参数传入
     * @return 包含完整文件树结构的响应数据
     */
    @GetMapping
    public ResultVO<Map<String, Object>> getProjectFileTree(
            @PathVariable String projectId) {
        
        // 获取当前登录用户ID
        Long userId = gatewayUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }

        // 调用服务获取文件树
        Map<String, Object> fileTree = projectFileTreeService
            .getProjectFileTree(projectId, userId);

        return ResultVO.success(fileTree, "获取文件树成功");
    }

    /**
     * 获取指定路径下的直接子节点
     * 
     * GET /api/v1/projects/{projectId}/files/children?parentPath=/src/main
     * 
     * 接口功能：
     * 1. 验证用户对项目的访问权限  
     * 2. 验证父路径的有效性
     * 3. 查询指定父路径下的直接子节点
     * 4. 按目录优先、名称升序返回
     * 
     * 使用场景：
     * - 文件树的懒加载展开
     * - 按需加载大项目的子目录
     * 
     * 权限要求：用户必须是项目成员
     * 
     * @param projectId 项目ID，通过路径参数传入
     * @param parentPath 父级路径，根目录传空字符串或null
     * @return 直接子节点列表
     */
    @GetMapping("/children")
    public ResultVO<List<ProjectFileTree>> getChildrenByParentPath(
            @PathVariable String projectId,
            @RequestParam(value = "parentPath", required = false) String parentPath) {
        
        // 获取当前登录用户ID
        Long userId = gatewayUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }

        // 处理空字符串参数 - 前端可能传递空字符串表示根目录
        if ("".equals(parentPath)) {
            parentPath = null;
        }

        // 调用服务获取子节点
        List<ProjectFileTree> children = projectFileTreeService
            .getChildrenByParentPath(projectId, parentPath, userId);

        return ResultVO.success(children, "获取子节点成功");
    }

    // ================================ 节点操作接口 ================================

    /**
     * 创建文件或目录节点
     * 
     * POST /api/v1/projects/{projectId}/files/nodes
     * 
     * 请求体数据结构：
     * ```json
     * {
     *   "filePath": "/src/main/App.java",
     *   "fileName": "App.java", 
     *   "parentPath": "/src/main",
     *   "type": "FILE"
     * }
     * ```
     * 
     * 接口功能：
     * 1. 验证用户对项目的编辑权限
     * 2. 验证路径参数的有效性
     * 3. 检查路径冲突
     * 4. 创建文件树节点记录
     * 5. 异步创建物理文件（如果是文件类型）
     * 
     * 特殊处理：
     * - 自动创建不存在的中间目录路径
     * - 文件类型会通过消息队列异步创建物理文件
     * - 目录类型立即完成创建
     * 
     * 权限要求：用户必须具有项目编辑权限（OWNER/EDITOR）
     * 
     * @param projectId 项目ID，通过路径参数传入
     * @param request 创建节点的请求数据
     * @return 创建的文件树节点信息
     */
    @PostMapping("/nodes")
    @ResponseStatus(HttpStatus.CREATED)  // 201 Created
    public ResultVO<ProjectFileTree> createFileTreeNode(
            @PathVariable String projectId,
            @RequestBody CreateFileTreeNodeRequest request) {
        
        // 获取当前登录用户ID
        Long userId = gatewayUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }

        // 参数验证
        if (request == null) {
            throw new BaseBusinessException(ErrorCode.INVALID_PARAMETER, "请求数据不能为空");
        }

        // 调用服务创建节点
        ProjectFileTree createdNode = projectFileTreeService.createFileTreeNode(
            projectId, 
            request.getFilePath(), 
            request.getFileName(), 
            request.getParentPath(), 
            request.getType(), 
            userId
        );

        return ResultVO.success(createdNode, "创建节点成功");
    }

    /**
     * 重命名文件或目录节点
     * 
     * PUT /api/v1/projects/{projectId}/files/nodes
     * 
     * 请求体数据结构：
     * ```json
     * {
     *   "filePath": "/src/main/OldName.java",
     *   "newFileName": "NewName.java"
     * }
     * ```
     * 
     * 接口功能：
     * 1. 验证用户对项目的编辑权限
     * 2. 查找要重命名的节点
     * 3. 检查新名称是否冲突
     * 4. 更新节点信息和路径
     * 5. 批量更新子节点路径（如果是目录）
     * 
     * 影响范围：
     * - 文件重命名：只影响单个节点
     * - 目录重命名：影响该目录下的所有子节点路径
     * 
     * 权限要求：用户必须具有项目编辑权限（OWNER/EDITOR）
     * 
     * @param projectId 项目ID，通过路径参数传入
     * @param request 重命名请求数据
     * @return 重命名后的文件树节点信息
     */
    @PutMapping("/nodes")
    public ResultVO<ProjectFileTree> renameFileTreeNode(
            @PathVariable String projectId,
            @RequestBody RenameFileTreeNodeRequest request) {
        
        // 获取当前登录用户ID
        Long userId = gatewayUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }

        // 参数验证
        if (request == null) {
            throw new BaseBusinessException(ErrorCode.INVALID_PARAMETER, "请求数据不能为空");
        }

        // 调用服务重命名节点
        ProjectFileTree renamedNode = projectFileTreeService.renameFileTreeNode(
            projectId, 
            request.getFilePath(), 
            request.getNewFileName(), 
            userId
        );

        return ResultVO.success(renamedNode, "重命名节点成功");
    }

    /**
     * 删除文件或目录节点
     * 
     * DELETE /api/v1/projects/{projectId}/files/nodes?filePath=/src/main/App.java
     * 
     * 接口功能：
     * 1. 验证用户对项目的编辑权限
     * 2. 查找要删除的节点
     * 3. 递归删除所有子节点（如果是目录）
     * 4. 从数据库批量删除记录
     * 5. 异步删除物理文件
     * 
     * 删除策略：
     * - 立即删除数据库记录，用户看到节点立即消失
     * - 异步删除物理文件，失败时记录日志但不回滚
     * 
     * 权限要求：用户必须具有项目编辑权限（OWNER/EDITOR）
     * 
     * @param projectId 项目ID，通过路径参数传入
     * @param filePath 要删除的文件路径
     * @return 删除成功的响应
     */
    @DeleteMapping("/nodes")
    public ResultVO<Void> deleteFileTreeNode(
            @PathVariable String projectId,
            @RequestParam("filePath") String filePath) {
        
        // 获取当前登录用户ID
        Long userId = gatewayUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }

        // 参数验证
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new BaseBusinessException(ErrorCode.INVALID_PARAMETER, "文件路径不能为空");
        }

        // 调用服务删除节点
        projectFileTreeService.deleteFileTreeNode(projectId, filePath, userId);

        return ResultVO.success(null, "删除节点成功");
    }

    /**
     * 移动文件或目录节点
     * 
     * PUT /api/v1/projects/{projectId}/files/nodes/move
     * 
     * 请求体数据结构：
     * ```json
     * {
     *   "sourcePath": "/src/main/App.java",
     *   "targetParentPath": "/src/components"
     * }
     * ```
     * 
     * 接口功能：
     * 1. 验证用户对项目的编辑权限
     * 2. 验证源路径和目标路径
     * 3. 检查目标位置是否冲突
     * 4. 防止目录移动到自己的子目录
     * 5. 更新节点路径信息
     * 6. 批量更新子节点路径（如果是目录）
     * 
     * 特殊处理：
     * - 确保移动操作的原子性
     * - 防止循环引用（目录移动到自己的子目录）
     * 
     * 权限要求：用户必须具有项目编辑权限（OWNER/EDITOR）
     * 
     * @param projectId 项目ID，通过路径参数传入
     * @param request 移动节点请求数据
     * @return 移动后的文件树节点信息
     */
    @PutMapping("/nodes/move")
    public ResultVO<ProjectFileTree> moveFileTreeNode(
            @PathVariable String projectId,
            @RequestBody MoveFileTreeNodeRequest request) {
        
        // 获取当前登录用户ID
        Long userId = gatewayUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }

        // 参数验证
        if (request == null) {
            throw new BaseBusinessException(ErrorCode.INVALID_PARAMETER, "请求数据不能为空");
        }

        // 调用服务移动节点
        ProjectFileTree movedNode = projectFileTreeService.moveFileTreeNode(
            projectId, 
            request.getSourcePath(), 
            request.getTargetParentPath(), 
            userId
        );

        return ResultVO.success(movedNode, "移动节点成功");
    }

    // ================================ 请求数据类定义 ================================

    /**
     * 创建文件树节点请求数据
     */
    public static class CreateFileTreeNodeRequest {
        private String filePath;
        private String fileName;
        private String parentPath;
        private ProjectFileTree.FileTreeType type;

        // Getters and Setters
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getParentPath() { return parentPath; }
        public void setParentPath(String parentPath) { this.parentPath = parentPath; }
        
        public ProjectFileTree.FileTreeType getType() { return type; }
        public void setType(ProjectFileTree.FileTreeType type) { this.type = type; }
    }

    /**
     * 重命名文件树节点请求数据
     */
    public static class RenameFileTreeNodeRequest {
        private String filePath;
        private String newFileName;

        // Getters and Setters
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public String getNewFileName() { return newFileName; }
        public void setNewFileName(String newFileName) { this.newFileName = newFileName; }
    }

    /**
     * 移动文件树节点请求数据
     */
    public static class MoveFileTreeNodeRequest {
        private String sourcePath;
        private String targetParentPath;

        // Getters and Setters
        public String getSourcePath() { return sourcePath; }
        public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }
        
        public String getTargetParentPath() { return targetParentPath; }
        public void setTargetParentPath(String targetParentPath) { this.targetParentPath = targetParentPath; }
    }
}