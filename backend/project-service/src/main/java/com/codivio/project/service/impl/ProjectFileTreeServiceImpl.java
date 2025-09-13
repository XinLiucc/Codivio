package com.codivio.project.service.impl;

import com.codivio.project.entity.ProjectFileTree;
import com.codivio.project.entity.ProjectFileTree.FileTreeType;
import com.codivio.project.entity.ProjectMember;
import com.codivio.project.entity.ProjectRole;
import com.codivio.project.entity.Project;
import com.codivio.project.exception.BaseBusinessException;
import com.codivio.project.exception.ErrorCode;
import com.codivio.project.repository.ProjectFileTreeRepository;
import com.codivio.project.repository.ProjectMemberRepository;
import com.codivio.project.repository.ProjectRepository;
import com.codivio.project.service.ProjectFileTreeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目文件树服务实现类
 * 
 * 实现项目文件树的完整业务逻辑，包括文件树构建、权限验证、
 * CRUD操作、消息队列集成等核心功能。
 * 
 * 技术特点：
 * 1. 事务管理 - 读操作使用只读事务，写操作使用读写事务
 * 2. 权限集成 - 与项目成员权限系统深度集成  
 * 3. 异步处理 - 支持消息队列异步文件操作
 * 4. 数据一致性 - 确保文件树与物理文件状态同步
 * 5. 性能优化 - 内存构建树形结构，避免N+1查询
 * 
 * @author Codivio Development Team
 * @since 1.0.0
 */
@Service
public class ProjectFileTreeServiceImpl implements ProjectFileTreeService {

    // ================================ 依赖注入 ================================

    /**
     * 项目文件树数据访问接口
     */
    @Autowired
    private ProjectFileTreeRepository projectFileTreeRepository;

    /**
     * 项目成员数据访问接口 - 用于权限验证
     */
    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    /**
     * 项目数据访问接口 - 用于统计信息更新
     */
    @Autowired
    private ProjectRepository projectRepository;

    // 注：消息队列相关的依赖稍后添加
    // private FileOperationProducer fileOperationProducer;

    // ================================ 查询相关方法实现 ================================

    /**
     * 获取项目的完整层级化文件树结构
     * 
     * 实现流程：
     * 1. 参数验证 - 检查projectId和userId非空
     * 2. 权限验证 - 确保用户有项目访问权限
     * 3. 数据查询 - 一次性查询项目所有文件树节点
     * 4. 树形构建 - 内存中构建层级化数据结构
     * 5. 统计计算 - 计算文件和目录数量等统计信息
     * 
     * @param projectId 项目ID，不能为空
     * @param userId 当前用户ID，用于权限验证，不能为空
     * @return 层级化的文件树结构，包含tree、统计信息等
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getProjectFileTree(Long projectId, Long userId) {
        // 1. 参数验证
        if (projectId == null) {
            throw new IllegalArgumentException("项目ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 2. 权限验证 - 确保用户有访问权限
        if (!hasAccessPermission(projectId, userId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }

        // 3. 查询项目所有文件树节点 - 按路径排序便于构建树形结构
        List<ProjectFileTree> allNodes = projectFileTreeRepository
            .findByProjectIdOrderByFilePathAsc(projectId);

        // 4. 构建层级化树形结构
        return buildHierarchicalTree(allNodes);
    }

    /**
     * 获取指定路径下的直接子节点
     * 
     * 实现流程：
     * 1. 参数验证和权限检查
     * 2. 验证父路径存在性（如果不是根目录）
     * 3. 查询直接子节点并排序返回
     * 
     * @param projectId 项目ID，不能为空
     * @param parentPath 父级路径，根目录传null
     * @param userId 当前用户ID，用于权限验证，不能为空
     * @return 直接子节点列表，按目录优先、名称升序排列
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProjectFileTree> getChildrenByParentPath(Long projectId, String parentPath, Long userId) {
        // 1. 参数验证
        if (projectId == null) {
            throw new IllegalArgumentException("项目ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 2. 权限验证
        if (!hasAccessPermission(projectId, userId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }

        // 3. 验证父路径存在性（如果不是根目录）
        if (StringUtils.hasText(parentPath)) {
            boolean parentExists = projectFileTreeRepository
                .existsByProjectIdAndFilePath(projectId, parentPath);
            if (!parentExists) {
                throw new BaseBusinessException(ErrorCode.PARENT_PATH_NOT_FOUND, 
                    "父路径不存在: " + parentPath);
            }
        }

        // 4. 查询直接子节点 - Repository方法已自动按类型和名称排序
        return projectFileTreeRepository
            .findByProjectIdAndParentPathOrderByTypeDescFileNameAsc(projectId, parentPath);
    }

    /**
     * 根据文件服务ID查找对应的文件树节点
     * 
     * 用于文件服务回调时快速定位文件树节点
     * 
     * @param fileId 文件服务ID，不能为空
     * @return 对应的文件树节点，如果不存在返回null
     */
    @Override
    @Transactional(readOnly = true)
    public ProjectFileTree findByFileId(String fileId) {
        if (!StringUtils.hasText(fileId)) {
            throw new IllegalArgumentException("文件ID不能为空");
        }

        return projectFileTreeRepository.findByFileId(fileId).orElse(null);
    }

    // ================================ 权限验证方法实现 ================================

    /**
     * 验证用户对项目文件树的访问权限
     * 
     * 验证逻辑：
     * 1. 查询用户是否为项目成员
     * 2. 任何角色的项目成员都有访问权限
     * 
     * @param projectId 项目ID，不能为空
     * @param userId 用户ID，不能为空
     * @return true-有访问权限，false-无访问权限
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasAccessPermission(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }

        // 查询项目成员关系 - 任何角色的成员都有访问权限
        Optional<ProjectMember> memberOpt = projectMemberRepository
            .findByProjectIdAndUserId(projectId, userId);
        
        return memberOpt.isPresent();
    }

    /**
     * 验证用户对项目文件树的编辑权限
     * 
     * 验证逻辑：
     * 1. 查询用户的项目成员角色
     * 2. 只有OWNER和EDITOR角色具有编辑权限
     * 
     * @param projectId 项目ID，不能为空  
     * @param userId 用户ID，不能为空
     * @return true-有编辑权限，false-无编辑权限
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasEditPermission(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }

        // 查询项目成员角色
        Optional<ProjectMember> memberOpt = projectMemberRepository
            .findByProjectIdAndUserId(projectId, userId);
        
        if (memberOpt.isEmpty()) {
            return false;
        }

        ProjectRole role = memberOpt.get().getRole();
        
        // 只有OWNER和EDITOR有编辑权限，VIEWER只能查看
        return role == ProjectRole.OWNER || role == ProjectRole.EDITOR;
    }

    // ================================ 工具方法实现 ================================

    /**
     * 构建层级化文件树数据结构
     * 
     * 算法实现：
     * 1. 数据预处理 - 按路径排序，建立快速查找映射
     * 2. 根节点识别 - 找出所有父路径为null的根节点
     * 3. 递归构建 - 为每个节点递归构建子树
     * 4. 元数据添加 - 计算统计信息和状态数据
     * 
     * 时间复杂度：O(n)，其中n为节点总数
     * 空间复杂度：O(n)，需要额外存储映射关系
     * 
     * @param fileTreeNodes 平铺的文件树节点列表，不能为空
     * @return 层级化的树形数据结构Map
     */
    @Override
    public Map<String, Object> buildHierarchicalTree(List<ProjectFileTree> fileTreeNodes) {
        if (fileTreeNodes == null) {
            throw new IllegalArgumentException("文件树节点列表不能为空");
        }

        // 1. 统计信息计算
        long totalFiles = fileTreeNodes.stream()
            .filter(node -> node.getType() == FileTreeType.FILE)
            .count();
        
        long totalDirectories = fileTreeNodes.stream()
            .filter(node -> node.getType() == FileTreeType.DIRECTORY)
            .count();

        LocalDateTime lastModified = fileTreeNodes.stream()
            .map(ProjectFileTree::getUpdatedAt)
            .max(LocalDateTime::compareTo)
            .orElse(null);

        // 2. 构建路径到节点的映射关系 - 用于快速查找
        Map<String, ProjectFileTree> pathToNodeMap = fileTreeNodes.stream()
            .collect(Collectors.toMap(
                ProjectFileTree::getFilePath, 
                node -> node,
                (existing, replacement) -> existing  // 处理重复key，保留第一个
            ));

        // 3. 构建父路径到子节点列表的映射
        Map<String, List<ProjectFileTree>> parentToChildrenMap = fileTreeNodes.stream()
            .collect(Collectors.groupingBy(
                node -> node.getParentPath() == null ? "__ROOT__" : node.getParentPath()
            ));

        // 4. 递归构建树形结构 - 从根节点开始
        List<Map<String, Object>> treeNodes = buildTreeNodesRecursively(
            parentToChildrenMap.getOrDefault("__ROOT__", Collections.emptyList()),
            parentToChildrenMap
        );

        // 5. 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("tree", treeNodes);
        result.put("totalFiles", totalFiles);
        result.put("totalDirectories", totalDirectories);
        result.put("totalNodes", fileTreeNodes.size());
        result.put("lastModified", lastModified);

        return result;
    }

    /**
     * 递归构建树形节点结构
     * 
     * 私有辅助方法，将平铺的节点列表递归组织成树形结构
     * 
     * @param nodes 当前层级的节点列表
     * @param parentToChildrenMap 父路径到子节点的映射关系
     * @return 当前层级的树形节点列表
     */
    private List<Map<String, Object>> buildTreeNodesRecursively(
            List<ProjectFileTree> nodes,
            Map<String, List<ProjectFileTree>> parentToChildrenMap) {
        
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }

        return nodes.stream()
            .sorted((a, b) -> {
                // 排序：目录优先，然后按文件名排序
                int typeCompare = b.getType().compareTo(a.getType());  // DESC: DIRECTORY在前
                if (typeCompare != 0) {
                    return typeCompare;
                }
                return a.getFileName().compareTo(b.getFileName());  // ASC: 文件名升序
            })
            .map(node -> {
                Map<String, Object> treeNode = new HashMap<>();
                
                // 基本属性
                treeNode.put("id", node.getId());
                treeNode.put("name", node.getFileName());
                treeNode.put("type", node.getType().getValue());
                treeNode.put("path", node.getFilePath());
                treeNode.put("parentPath", node.getParentPath());
                
                // 文件特有属性
                if (node.getType() == FileTreeType.FILE) {
                    treeNode.put("fileId", node.getFileId());
                    treeNode.put("size", 0);  // 文件大小需要从文件服务获取
                }
                
                // 时间信息
                treeNode.put("lastModified", node.getLastEditedAt());
                treeNode.put("createdAt", node.getCreatedAt());
                
                // 编辑信息
                treeNode.put("lastEditorId", node.getLastEditorId());
                
                // 递归构建子节点
                List<ProjectFileTree> children = parentToChildrenMap
                    .getOrDefault(node.getFilePath(), Collections.emptyList());
                
                List<Map<String, Object>> childTreeNodes = buildTreeNodesRecursively(
                    children, parentToChildrenMap);
                
                treeNode.put("children", childTreeNodes);
                treeNode.put("hasChildren", !children.isEmpty());
                
                // UI状态属性
                treeNode.put("expanded", false);  // 默认不展开
                treeNode.put("loading", false);   // 默认不在加载中
                
                return treeNode;
            })
            .collect(Collectors.toList());
    }

    /**
     * 更新文件节点的编辑信息
     * 
     * 业务场景：文件内容被编辑保存时更新元数据
     * 
     * @param projectId 项目ID，不能为空
     * @param filePath 文件路径，不能为空
     * @param userId 编辑者用户ID，不能为空
     */
    @Override
    @Transactional
    public void updateFileEditInfo(Long projectId, String filePath, Long userId) {
        // 参数验证
        if (projectId == null) {
            throw new IllegalArgumentException("项目ID不能为空");
        }
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 查找文件节点
        Optional<ProjectFileTree> nodeOpt = projectFileTreeRepository
            .findByProjectIdAndFilePath(projectId, filePath);
        
        if (nodeOpt.isEmpty()) {
            throw new BaseBusinessException(ErrorCode.FILE_NODE_NOT_FOUND, 
                "文件节点不存在: " + filePath);
        }

        ProjectFileTree node = nodeOpt.get();
        
        // 更新编辑信息
        LocalDateTime now = LocalDateTime.now();
        node.setLastEditorId(userId);
        node.setLastEditedAt(now);
        node.setUpdatedAt(now);
        
        // 保存更新
        projectFileTreeRepository.save(node);
    }

    /**
     * 同步更新项目文件统计信息
     * 
     * 更新projects表中的统计字段：
     * - file_count: 项目中的文件数量
     * - last_activity_at: 最后活动时间
     * 
     * @param projectId 项目ID，不能为空
     */
    @Override
    @Transactional
    public void syncProjectFileStatistics(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("项目ID不能为空");
        }

        // 1. 验证项目存在
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new BaseBusinessException(ErrorCode.PROJECT_NOT_FOUND, 
                "项目不存在: " + projectId);
        }

        // 2. 统计文件数量（不包括目录）
        long fileCount = projectFileTreeRepository
            .countByProjectIdAndType(projectId, FileTreeType.FILE);

        // 3. 更新项目统计信息
        Project project = projectOpt.get();
        project.setFileCount((int) fileCount);
        project.setLastActivityAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        
        projectRepository.save(project);
    }

    /**
     * 创建缺失的父目录路径
     * 
     * 递归创建路径中所有不存在的父目录，确保文件树的完整性
     * 
     * 算法：
     * 1. 分析路径结构，从根到目标路径逐级检查
     * 2. 发现缺失的目录时立即创建
     * 3. 自动设置正确的父子关系
     * 
     * @param projectId 项目ID，不能为空
     * @param targetPath 目标路径，不能为空
     * @param userId 操作用户ID，不能为空
     */
    private void createMissingParentDirectories(Long projectId, String targetPath, Long userId) {
        if (!StringUtils.hasText(targetPath) || targetPath.equals("/")) {
            return;  // 根目录不需要创建
        }

        // 分解路径为各个层级
        String[] pathParts = targetPath.split("/");
        StringBuilder currentPath = new StringBuilder();
        String parentPath = null;

        for (String part : pathParts) {
            if (part.isEmpty()) continue;  // 跳过空字符串（开头的/会产生空字符串）

            // 构建当前路径
            currentPath.append("/").append(part);
            String currentFullPath = currentPath.toString();

            // 检查当前路径是否存在
            boolean exists = projectFileTreeRepository
                .existsByProjectIdAndFilePath(projectId, currentFullPath);

            if (!exists) {
                // 创建缺失的目录节点
                ProjectFileTree dirNode = new ProjectFileTree();
                dirNode.setProjectId(projectId);
                dirNode.setFilePath(currentFullPath);
                dirNode.setFileName(part);
                dirNode.setParentPath(parentPath);
                dirNode.setType(FileTreeType.DIRECTORY);
                dirNode.setLastEditorId(userId);
                
                LocalDateTime now = LocalDateTime.now();
                dirNode.setLastEditedAt(now);
                
                projectFileTreeRepository.save(dirNode);
            }

            // 更新父路径为当前路径
            parentPath = currentFullPath;
        }
    }

    /**
     * 在目录重命名后批量更新子节点路径
     * 
     * 当一个目录被重命名时，需要递归更新其所有子节点的路径信息，
     * 确保文件树结构的完整性。
     * 
     * @param projectId 项目ID，不能为空
     * @param oldParentPath 原父路径，不能为空
     * @param newParentPath 新父路径，不能为空
     */
    private void updateChildrenPathsAfterRename(Long projectId, String oldParentPath, String newParentPath) {
        // 查找所有以旧路径开头的子节点
        List<ProjectFileTree> childNodes = projectFileTreeRepository
            .findByProjectIdAndFilePathStartingWith(projectId, oldParentPath + "/");

        if (childNodes.isEmpty()) {
            return;  // 没有子节点，直接返回
        }

        // 批量更新子节点路径
        for (ProjectFileTree child : childNodes) {
            String oldChildPath = child.getFilePath();
            String oldChildParentPath = child.getParentPath();
            
            // 构建新的子节点路径
            String newChildPath = oldChildPath.replaceFirst(
                "^" + oldParentPath.replace("/", "\\/"), 
                newParentPath.replace("/", "\\/")
            );
            
            // 构建新的父路径
            String newChildParentPath = null;
            if (oldChildParentPath != null) {
                if (oldChildParentPath.equals(oldParentPath)) {
                    // 直接子节点
                    newChildParentPath = newParentPath;
                } else {
                    // 深层子节点
                    newChildParentPath = oldChildParentPath.replaceFirst(
                        "^" + oldParentPath.replace("/", "\\/"), 
                        newParentPath.replace("/", "\\/")
                    );
                }
            }
            
            // 更新节点路径信息
            child.setFilePath(newChildPath);
            child.setParentPath(newChildParentPath);
            child.setUpdatedAt(LocalDateTime.now());
        }

        // 批量保存更新
        projectFileTreeRepository.saveAll(childNodes);
    }

    /**
     * 递归删除目录下的所有子节点
     * 
     * 采用深度优先遍历策略，确保先删除子节点再删除父节点，
     * 避免外键约束冲突。
     * 
     * @param projectId 项目ID，不能为空
     * @param parentPath 父目录路径，不能为空
     */
    private void deleteChildrenRecursively(Long projectId, String parentPath) {
        // 查找直接子节点
        List<ProjectFileTree> children = projectFileTreeRepository
            .findByProjectIdAndParentPath(projectId, parentPath);

        for (ProjectFileTree child : children) {
            if (child.getType() == FileTreeType.DIRECTORY) {
                // 如果是目录，先递归删除其子节点
                deleteChildrenRecursively(projectId, child.getFilePath());
            }
            // 删除当前子节点
            projectFileTreeRepository.delete(child);
        }
    }

    /**
     * 在目录移动后批量更新子节点路径
     * 
     * 当一个目录被移动到新位置时，需要递归更新其所有子节点的路径信息。
     * 这个方法与重命名时的路径更新类似，但处理的是移动场景。
     * 
     * @param projectId 项目ID，不能为空
     * @param oldParentPath 原目录路径，不能为空
     * @param newParentPath 新目录路径，不能为空
     */
    private void updateChildrenPathsAfterMove(Long projectId, String oldParentPath, String newParentPath) {
        // 复用重命名时的路径更新逻辑
        updateChildrenPathsAfterRename(projectId, oldParentPath, newParentPath);
    }

    // ================================ CRUD操作方法（待实现） ================================

    @Override
    @Transactional
    public ProjectFileTree createFileTreeNode(Long projectId, String filePath, String fileName, 
                                            String parentPath, FileTreeType type, Long userId) {
        // 1. 参数验证
        if (projectId == null) {
            throw new IllegalArgumentException("项目ID不能为空");
        }
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (type == null) {
            throw new IllegalArgumentException("节点类型不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 2. 权限验证 - 必须有编辑权限
        if (!hasEditPermission(projectId, userId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_ACCESS_DENIED, 
                "没有项目编辑权限");
        }

        // 3. 路径格式验证
        if (!filePath.startsWith("/")) {
            throw new BaseBusinessException(ErrorCode.INVALID_PARAMETER, 
                "文件路径必须以/开头");
        }
        
        // 4. 检查路径冲突 - 确保路径不存在
        boolean pathExists = projectFileTreeRepository
            .existsByProjectIdAndFilePath(projectId, filePath);
        if (pathExists) {
            throw new BaseBusinessException(ErrorCode.INVALID_PARAMETER, 
                "文件路径已存在: " + filePath);
        }

        // 5. 验证父路径存在性（如果不是根目录节点）
        if (StringUtils.hasText(parentPath)) {
            boolean parentExists = projectFileTreeRepository
                .existsByProjectIdAndFilePath(projectId, parentPath);
            if (!parentExists) {
                // 自动创建不存在的中间目录路径
                createMissingParentDirectories(projectId, parentPath, userId);
            }
        }

        // 6. 验证父子路径关系
        if (StringUtils.hasText(parentPath)) {
            if (!filePath.startsWith(parentPath + "/")) {
                throw new BaseBusinessException(ErrorCode.INVALID_PARAMETER, 
                    "文件路径与父路径不匹配");
            }
        }

        // 7. 创建文件树节点
        ProjectFileTree newNode = new ProjectFileTree();
        newNode.setProjectId(projectId);
        newNode.setFilePath(filePath);
        newNode.setFileName(fileName);
        newNode.setParentPath(parentPath);
        newNode.setType(type);
        newNode.setLastEditorId(userId);
        
        LocalDateTime now = LocalDateTime.now();
        newNode.setLastEditedAt(now);
        
        // 8. 保存到数据库
        ProjectFileTree savedNode = projectFileTreeRepository.save(newNode);

        // 9. 更新项目统计信息
        syncProjectFileStatistics(projectId);

        // TODO: 10. 发送消息队列（如果是文件类型）
        // 暂时跳过消息队列集成，后续添加
        // if (type == FileTreeType.FILE) {
        //     fileOperationProducer.sendCreateFileMessage(savedNode);
        // }

        return savedNode;
    }

    @Override
    @Transactional
    public ProjectFileTree renameFileTreeNode(Long projectId, String oldFilePath, 
                                            String newFileName, Long userId) {
        // 1. 参数验证
        if (projectId == null) {
            throw new IllegalArgumentException("项目ID不能为空");
        }
        if (!StringUtils.hasText(oldFilePath)) {
            throw new IllegalArgumentException("原文件路径不能为空");
        }
        if (!StringUtils.hasText(newFileName)) {
            throw new IllegalArgumentException("新文件名不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 2. 权限验证 - 必须有编辑权限
        if (!hasEditPermission(projectId, userId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_ACCESS_DENIED, 
                "没有项目编辑权限");
        }

        // 3. 查找要重命名的节点
        Optional<ProjectFileTree> nodeOpt = projectFileTreeRepository
            .findByProjectIdAndFilePath(projectId, oldFilePath);
        if (nodeOpt.isEmpty()) {
            throw new BaseBusinessException(ErrorCode.FILE_NODE_NOT_FOUND, 
                "文件节点不存在: " + oldFilePath);
        }

        ProjectFileTree node = nodeOpt.get();
        String oldFileName = node.getFileName();
        
        // 4. 检查文件名是否真的改变了
        if (oldFileName.equals(newFileName)) {
            return node;  // 文件名没有变化，直接返回
        }

        // 5. 构建新的完整路径
        String parentPath = node.getParentPath();
        String newFilePath;
        if (StringUtils.hasText(parentPath)) {
            newFilePath = parentPath + "/" + newFileName;
        } else {
            newFilePath = "/" + newFileName;
        }

        // 6. 检查新路径是否冲突
        boolean newPathExists = projectFileTreeRepository
            .existsByProjectIdAndFilePath(projectId, newFilePath);
        if (newPathExists) {
            throw new BaseBusinessException(ErrorCode.INVALID_PARAMETER, 
                "目标文件路径已存在: " + newFilePath);
        }

        // 7. 更新当前节点信息
        node.setFileName(newFileName);
        node.setFilePath(newFilePath);
        node.setLastEditorId(userId);
        node.setLastEditedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());

        // 8. 如果是目录，需要批量更新所有子节点的路径
        if (node.getType() == FileTreeType.DIRECTORY) {
            updateChildrenPathsAfterRename(projectId, oldFilePath, newFilePath);
        }

        // 9. 保存更新
        ProjectFileTree savedNode = projectFileTreeRepository.save(node);

        // 10. 更新项目统计信息
        syncProjectFileStatistics(projectId);

        return savedNode;
    }

    @Override
    @Transactional
    public void deleteFileTreeNode(Long projectId, String filePath, Long userId) {
        // 1. 参数验证
        if (projectId == null) {
            throw new IllegalArgumentException("项目ID不能为空");
        }
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 2. 权限验证 - 必须有编辑权限
        if (!hasEditPermission(projectId, userId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_ACCESS_DENIED, 
                "没有项目编辑权限");
        }

        // 3. 查找要删除的节点
        Optional<ProjectFileTree> nodeOpt = projectFileTreeRepository
            .findByProjectIdAndFilePath(projectId, filePath);
        if (nodeOpt.isEmpty()) {
            throw new BaseBusinessException(ErrorCode.FILE_NODE_NOT_FOUND, 
                "文件节点不存在: " + filePath);
        }

        ProjectFileTree node = nodeOpt.get();

        // 4. 如果是目录，检查是否有子节点
        if (node.getType() == FileTreeType.DIRECTORY) {
            List<ProjectFileTree> children = projectFileTreeRepository
                .findByProjectIdAndParentPath(projectId, filePath);
            
            if (!children.isEmpty()) {
                // 递归删除所有子节点
                deleteChildrenRecursively(projectId, filePath);
            }
        }

        // 5. 删除当前节点
        projectFileTreeRepository.delete(node);

        // 6. 更新项目统计信息
        syncProjectFileStatistics(projectId);

        // TODO: 7. 发送消息队列异步删除物理文件
        // 暂时跳过消息队列集成，后续添加
        // if (node.getType() == FileTreeType.FILE && StringUtils.hasText(node.getFileId())) {
        //     fileOperationProducer.sendDeleteFileMessage(node.getFileId());
        // }
    }

    @Override
    @Transactional
    public ProjectFileTree moveFileTreeNode(Long projectId, String sourcePath, 
                                          String targetParentPath, Long userId) {
        // 1. 参数验证
        if (projectId == null) {
            throw new IllegalArgumentException("项目ID不能为空");
        }
        if (!StringUtils.hasText(sourcePath)) {
            throw new IllegalArgumentException("源路径不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 2. 权限验证 - 必须有编辑权限
        if (!hasEditPermission(projectId, userId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_ACCESS_DENIED, 
                "没有项目编辑权限");
        }

        // 3. 查找要移动的源节点
        Optional<ProjectFileTree> sourceNodeOpt = projectFileTreeRepository
            .findByProjectIdAndFilePath(projectId, sourcePath);
        if (sourceNodeOpt.isEmpty()) {
            throw new BaseBusinessException(ErrorCode.FILE_NODE_NOT_FOUND, 
                "源文件节点不存在: " + sourcePath);
        }

        ProjectFileTree sourceNode = sourceNodeOpt.get();
        
        // 4. 验证目标父路径
        if (StringUtils.hasText(targetParentPath)) {
            // 验证目标父路径存在且为目录
            Optional<ProjectFileTree> targetParentOpt = projectFileTreeRepository
                .findByProjectIdAndFilePath(projectId, targetParentPath);
            if (targetParentOpt.isEmpty()) {
                throw new BaseBusinessException(ErrorCode.PARENT_PATH_NOT_FOUND, 
                    "目标父路径不存在: " + targetParentPath);
            }
            if (targetParentOpt.get().getType() != FileTreeType.DIRECTORY) {
                throw new BaseBusinessException(ErrorCode.INVALID_PARAMETER, 
                    "目标父路径必须是目录");
            }

            // 防止循环引用 - 不能移动到自己的子目录
            if (sourceNode.getType() == FileTreeType.DIRECTORY) {
                if (targetParentPath.startsWith(sourcePath + "/") || targetParentPath.equals(sourcePath)) {
                    throw new BaseBusinessException(ErrorCode.INVALID_PARAMETER, 
                        "不能移动目录到自己的子目录中");
                }
            }
        }

        // 5. 构建新的目标路径
        String fileName = sourceNode.getFileName();
        String newFilePath;
        if (StringUtils.hasText(targetParentPath)) {
            newFilePath = targetParentPath + "/" + fileName;
        } else {
            newFilePath = "/" + fileName;
        }

        // 6. 检查目标路径是否冲突
        if (!newFilePath.equals(sourcePath)) {  // 如果路径确实改变了
            boolean targetExists = projectFileTreeRepository
                .existsByProjectIdAndFilePath(projectId, newFilePath);
            if (targetExists) {
                throw new BaseBusinessException(ErrorCode.INVALID_PARAMETER, 
                    "目标路径已存在: " + newFilePath);
            }
        }

        // 7. 检查是否真的需要移动
        if (Objects.equals(sourceNode.getParentPath(), targetParentPath)) {
            return sourceNode;  // 父路径没有变化，直接返回
        }

        // 8. 更新源节点信息
        String oldFilePath = sourceNode.getFilePath();
        sourceNode.setFilePath(newFilePath);
        sourceNode.setParentPath(targetParentPath);
        sourceNode.setLastEditorId(userId);
        sourceNode.setLastEditedAt(LocalDateTime.now());
        sourceNode.setUpdatedAt(LocalDateTime.now());

        // 9. 如果是目录，需要批量更新所有子节点的路径
        if (sourceNode.getType() == FileTreeType.DIRECTORY) {
            updateChildrenPathsAfterMove(projectId, oldFilePath, newFilePath);
        }

        // 10. 保存更新
        ProjectFileTree savedNode = projectFileTreeRepository.save(sourceNode);

        // 11. 更新项目统计信息
        syncProjectFileStatistics(projectId);

        return savedNode;
    }

    @Override
    @Transactional
    public void initializeProjectFileTree(Long projectId, String language, Long userId) {
        // TODO: 实现项目文件树初始化逻辑
        throw new UnsupportedOperationException("方法待实现");
    }

    // ================================ 消息队列集成方法（待实现） ================================

    @Override
    @Transactional
    public void handleFileOperationSuccess(Long fileTreeNodeId, String fileId) {
        // TODO: 实现文件操作成功回调逻辑
        throw new UnsupportedOperationException("方法待实现");
    }

    @Override
    @Transactional
    public void handleFileOperationFailure(Long fileTreeNodeId, String errorMessage) {
        // TODO: 实现文件操作失败回调逻辑
        throw new UnsupportedOperationException("方法待实现");
    }
}