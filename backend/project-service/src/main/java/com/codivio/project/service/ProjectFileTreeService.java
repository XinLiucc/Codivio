package com.codivio.project.service;

import com.codivio.project.entity.ProjectFileTree;
import com.codivio.project.entity.ProjectFileTree.FileTreeType;

import java.util.List;
import java.util.Map;

/**
 * 项目文件树服务接口
 * 
 * 负责管理项目的层级化文件目录结构，支持文件和目录的CRUD操作，
 * 以及与文件服务的协调配合，确保数据一致性和权限控制。
 * 
 * 核心功能：
 * 1. 文件树查询和构建 - 构建层级化的文件树数据结构
 * 2. 文件/目录节点操作 - 创建、重命名、删除、移动操作
 * 3. 权限验证集成 - 与项目成员权限系统集成
 * 4. 文件服务协调 - 通过消息队列与文件服务异步通信
 * 5. 数据一致性保证 - 确保文件树与物理文件的一致性
 * 
 * @author Codivio Development Team
 * @since 1.0.0
 */
public interface ProjectFileTreeService {

    // ================================ 查询相关方法 ================================

    /**
     * 获取项目的完整层级化文件树结构
     * 
     * 业务流程：
     * 1. 验证用户对项目的访问权限（至少是项目成员）
     * 2. 从数据库查询项目的所有文件树节点
     * 3. 在内存中构建层级化的树形数据结构
     * 4. 返回包含展开状态和元数据的完整文件树
     * 
     * 权限要求：用户必须是项目成员（OWNER/EDITOR/VIEWER）
     * 
     * @param projectId 项目ID，不能为空
     * @param userId 当前用户ID，用于权限验证，不能为空
     * @return 层级化的文件树结构，包含以下信息：
     *         - tree: 树形节点数据
     *         - totalFiles: 文件总数
     *         - totalDirectories: 目录总数
     *         - lastModified: 最后修改时间
     * @throws IllegalArgumentException 当参数为空或无效时
     * @throws com.codivio.project.exception.BusinessException 当项目不存在或用户无权限时
     */
    Map<String, Object> getProjectFileTree(String projectId, Long userId);

    /**
     * 获取指定路径下的直接子节点
     * 
     * 业务流程：
     * 1. 验证用户对项目的访问权限
     * 2. 验证父路径的有效性（如果不是根目录）
     * 3. 查询指定父路径下的直接子节点
     * 4. 按类型（目录优先）和名称排序返回
     * 
     * 用途：支持文件树的懒加载和按需展开
     * 权限要求：用户必须是项目成员
     * 
     * @param projectId 项目ID，不能为空
     * @param parentPath 父级路径，根目录传null
     * @param userId 当前用户ID，用于权限验证，不能为空
     * @return 直接子节点列表，按目录优先、名称升序排列
     * @throws IllegalArgumentException 当参数无效时
     * @throws com.codivio.project.exception.BusinessException 当权限不足或路径不存在时
     */
    List<ProjectFileTree> getChildrenByParentPath(String projectId, String parentPath, Long userId);

    /**
     * 根据文件服务ID查找对应的文件树节点
     * 
     * 业务流程：
     * 1. 根据文件服务ID查询对应的文件树记录
     * 2. 用于文件服务回调时定位对应的文件树节点
     * 
     * 用途：文件服务操作完成后的回调处理
     * 权限要求：内部服务调用，无权限限制
     * 
     * @param fileId 文件服务ID，不能为空
     * @return 对应的文件树节点，如果不存在返回null
     * @throws IllegalArgumentException 当fileId为空时
     */
    ProjectFileTree findByFileId(String fileId);

    // ================================ 操作相关方法 ================================

    /**
     * 创建文件或目录节点
     * 
     * 业务流程：
     * 1. 验证用户对项目的编辑权限（OWNER或EDITOR）
     * 2. 验证父路径存在性（如果不是根目录）
     * 3. 检查路径冲突（同一路径不能重复）
     * 4. 创建数据库文件树节点记录
     * 5. 如果是文件类型，发送消息队列异步创建物理文件
     * 6. 更新项目统计信息（文件数量、最后活动时间）
     * 
     * 特殊处理：
     * - 自动创建不存在的中间目录路径
     * - 设置节点状态为CREATING，等待异步操作完成
     * 
     * 权限要求：用户必须具有项目编辑权限（OWNER/EDITOR）
     * 
     * @param projectId 项目ID，不能为空
     * @param filePath 文件完整路径，如 "/src/main/App.java"，不能为空
     * @param fileName 文件名，如 "App.java"，不能为空
     * @param parentPath 父级路径，如 "/src/main"，根目录传null
     * @param type 节点类型（FILE/DIRECTORY），不能为空
     * @param userId 当前用户ID，用于权限验证和记录操作者，不能为空
     * @return 创建的文件树节点，状态为CREATING
     * @throws IllegalArgumentException 当参数无效时
     * @throws com.codivio.project.exception.BusinessException 当权限不足、路径冲突或创建失败时
     */
    ProjectFileTree createFileTreeNode(String projectId, String filePath, String fileName,
                                     String parentPath, FileTreeType type, Long userId);

    /**
     * 重命名文件或目录节点
     * 
     * 业务流程：
     * 1. 验证用户对项目的编辑权限（OWNER或EDITOR）
     * 2. 查找要重命名的节点，验证其存在性
     * 3. 检查新名称是否与同级其他节点冲突
     * 4. 更新节点的文件名和路径信息
     * 5. 如果是目录，批量更新所有子节点的路径
     * 6. 如果是文件，发送消息队列通知文件服务更新
     * 7. 记录操作日志和更新统计信息
     * 
     * 影响范围：
     * - 目录重命名会影响所有子节点的路径
     * - 文件重命名只影响单个节点
     * 
     * 权限要求：用户必须具有项目编辑权限（OWNER/EDITOR）
     * 
     * @param projectId 项目ID，不能为空
     * @param oldFilePath 原文件路径，不能为空
     * @param newFileName 新文件名（不包含路径），不能为空
     * @param userId 当前用户ID，用于权限验证和记录操作者，不能为空
     * @return 更新后的文件树节点
     * @throws IllegalArgumentException 当参数无效时
     * @throws com.codivio.project.exception.BusinessException 当权限不足、节点不存在或名称冲突时
     */
    ProjectFileTree renameFileTreeNode(String projectId, String oldFilePath, String newFileName, Long userId);

    /**
     * 删除文件或目录节点
     * 
     * 业务流程：
     * 1. 验证用户对项目的编辑权限（OWNER或EDITOR）
     * 2. 查找要删除的节点，验证其存在性
     * 3. 如果是目录，递归删除所有子节点
     * 4. 收集所有需要删除的文件ID（用于通知文件服务）
     * 5. 从数据库批量删除文件树记录
     * 6. 发送消息队列异步删除物理文件
     * 7. 更新项目统计信息
     * 
     * 删除策略：
     * - 物理删除：立即从数据库删除记录
     * - 异步清理：通过消息队列异步删除物理文件
     * - 错误恢复：如果物理文件删除失败，记录日志但不回滚数据库
     * 
     * 权限要求：用户必须具有项目编辑权限（OWNER/EDITOR）
     * 
     * @param projectId 项目ID，不能为空
     * @param filePath 要删除的文件路径，不能为空
     * @param userId 当前用户ID，用于权限验证和记录操作者，不能为空
     * @throws IllegalArgumentException 当参数无效时
     * @throws com.codivio.project.exception.BusinessException 当权限不足、节点不存在或删除失败时
     */
    void deleteFileTreeNode(String projectId, String filePath, Long userId);

    /**
     * 移动文件或目录节点到新位置
     * 
     * 业务流程：
     * 1. 验证用户对项目的编辑权限（OWNER/EDITOR）
     * 2. 验证源路径和目标父路径的有效性
     * 3. 检查目标位置是否会产生路径冲突
     * 4. 防止目录移动到自己的子目录（避免循环引用）
     * 5. 更新节点的路径和父路径信息
     * 6. 如果是目录，批量更新所有子节点的路径
     * 7. 通知文件服务更新物理文件路径（如果需要）
     * 
     * 特殊处理：
     * - 移动目录时需要重新计算所有子节点的完整路径
     * - 确保移动操作的原子性，失败时回滚所有更改
     * 
     * 权限要求：用户必须具有项目编辑权限（OWNER/EDITOR）
     * 
     * @param projectId 项目ID，不能为空
     * @param sourcePath 源文件路径，不能为空
     * @param targetParentPath 目标父路径，根目录传null
     * @param userId 当前用户ID，用于权限验证和记录操作者，不能为空
     * @return 移动后的文件树节点
     * @throws IllegalArgumentException 当参数无效时
     * @throws com.codivio.project.exception.BusinessException 当权限不足、路径无效或移动失败时
     */
    ProjectFileTree moveFileTreeNode(String projectId, String sourcePath, String targetParentPath, Long userId);

    // ================================ 权限验证方法 ================================

    /**
     * 验证用户对项目文件树的访问权限
     * 
     * 验证逻辑：
     * 1. 检查用户是否为项目成员
     * 2. 任何角色的项目成员都有访问权限（OWNER/EDITOR/VIEWER）
     * 
     * @param projectId 项目ID，不能为空
     * @param userId 用户ID，不能为空
     * @return true-有访问权限，false-无访问权限
     * @throws IllegalArgumentException 当参数为空时
     */
    boolean hasAccessPermission(String projectId, Long userId);

    /**
     * 验证用户对项目文件树的编辑权限
     * 
     * 验证逻辑：
     * 1. 检查用户是否为项目成员
     * 2. 只有OWNER和EDITOR角色具有编辑权限
     * 3. VIEWER角色只能查看，不能编辑
     * 
     * @param projectId 项目ID，不能为空
     * @param userId 用户ID，不能为空
     * @return true-有编辑权限，false-无编辑权限
     * @throws IllegalArgumentException 当参数为空时
     */
    boolean hasEditPermission(String projectId, Long userId);

    // ================================ 工具方法 ================================

    /**
     * 构建层级化文件树数据结构
     * 
     * 算法流程：
     * 1. 将平铺的文件树节点列表按路径排序
     * 2. 构建路径到节点的映射关系
     * 3. 递归构建父子关系树形结构
     * 4. 添加展开状态和子节点计数等元数据
     * 
     * 返回结构：
     * ```json
     * {
     *   "id": "节点ID",
     *   "name": "文件/目录名",
     *   "type": "file|directory", 
     *   "path": "完整路径",
     *   "size": "文件大小(仅文件类型)",
     *   "lastModified": "最后修改时间",
     *   "children": [子节点数组],
     *   "expanded": "是否展开(默认false)",
     *   "loading": "是否加载中"
     * }
     * ```
     * 
     * @param fileTreeNodes 平铺的文件树节点列表，不能为空
     * @return 层级化的树形数据结构Map，包含root节点和统计信息
     * @throws IllegalArgumentException 当参数为空时
     */
    Map<String, Object> buildHierarchicalTree(List<ProjectFileTree> fileTreeNodes);

    /**
     * 更新文件节点的编辑信息
     * 
     * 业务场景：
     * 1. 文件内容被用户编辑保存时调用
     * 2. 文件服务通过回调通知文件更新时调用
     * 
     * 更新内容：
     * - 最后编辑者ID (last_editor_id)
     * - 最后编辑时间 (last_edited_at) 
     * - 节点更新时间 (updated_at)
     * 
     * @param projectId 项目ID，不能为空
     * @param filePath 文件路径，不能为空
     * @param userId 编辑者用户ID，不能为空
     * @throws IllegalArgumentException 当参数为空或文件节点不存在时
     */
    void updateFileEditInfo(String projectId, String filePath, Long userId);

    /**
     * 同步更新项目文件统计信息
     * 
     * 更新内容：
     * 1. 统计项目中的文件数量（不包括目录）
     * 2. 更新projects表中的file_count字段
     * 3. 更新projects表中的last_activity_at时间戳
     * 
     * 调用时机：
     * - 创建/删除文件节点后
     * - 批量操作完成后
     * - 定时任务修正数据不一致时
     * 
     * @param projectId 项目ID，不能为空
     * @throws IllegalArgumentException 当项目ID为空或项目不存在时
     */
    void syncProjectFileStatistics(String projectId);

    /**
     * 初始化新项目的文件树结构
     * 
     * 业务场景：新项目创建时自动调用，建立基础目录结构
     * 
     * 根据编程语言创建相应的目录结构：
     * - Java: src/main/java, src/main/resources, src/test/java
     * - JavaScript: src, public, tests  
     * - Python: src, tests, docs
     * - 通用: docs, README.md
     * 
     * 创建的文件/目录特点：
     * - 所有节点的创建者都是项目创建者
     * - 目录节点无需创建物理文件
     * - 自动创建项目说明文件(README.md)
     * 
     * @param projectId 项目ID，不能为空
     * @param language 项目主要编程语言，如 "java", "javascript", "python"等
     * @param userId 项目创建者用户ID，不能为空
     * @throws IllegalArgumentException 当参数为空或无效时
     * @throws com.codivio.project.exception.BusinessException 当初始化失败时
     */
    void initializeProjectFileTree(String projectId, String language, Long userId);

    // ================================ 消息队列集成方法 ================================

    /**
     * 处理文件操作成功的回调
     * 
     * 业务场景：文件服务通过消息队列通知操作结果
     * 
     * 处理流程：
     * 1. 根据文件树节点ID查找对应记录
     * 2. 更新节点状态为NORMAL
     * 3. 设置文件服务返回的fileId
     * 4. 更新最后修改时间
     * 
     * @param fileTreeNodeId 文件树节点ID，不能为空
     * @param fileId 文件服务返回的文件ID，不能为空
     * @throws IllegalArgumentException 当参数为空或节点不存在时
     */
    void handleFileOperationSuccess(Long fileTreeNodeId, String fileId);

    /**
     * 处理文件操作失败的回调
     * 
     * 业务场景：文件服务操作失败，需要进行补偿处理
     * 
     * 补偿策略：
     * 1. 如果是创建操作失败：删除数据库中的文件树记录
     * 2. 如果是删除操作失败：标记节点为失败状态，记录错误信息
     * 3. 发送前端通知，告知用户操作失败
     * 
     * @param fileTreeNodeId 文件树节点ID，不能为空
     * @param errorMessage 失败原因，不能为空
     * @throws IllegalArgumentException 当参数为空时
     */
    void handleFileOperationFailure(Long fileTreeNodeId, String errorMessage);
}