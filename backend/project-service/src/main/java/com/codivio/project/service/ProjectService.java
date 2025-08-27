package com.codivio.project.service;

import com.codivio.project.dto.ProjectCreateDTO;
import com.codivio.project.dto.ProjectResponseDTO;
import com.codivio.project.dto.ProjectUpdateDTO;

import java.util.List;

/**
 * 项目服务接口
 * 定义项目相关的业务操作
 */
public interface ProjectService {
    
    /**
     * 创建项目
     * 
     * 业务逻辑：
     * 1. 验证项目名称是否已存在（同一用户下）
     * 2. 设置项目默认状态为进行中（1）
     * 3. 设置创建时间和更新时间
     * 4. 保存项目信息
     * 5. 自动添加创建者为项目所有者成员
     * 
     * @param createDTO 项目创建数据（已通过DTO格式验证）
     * @param ownerId 项目所有者ID（从认证信息获取）
     * @return 创建成功的项目信息
     * @throws RuntimeException 当业务规则验证失败时抛出异常
     */
    ProjectResponseDTO createProject(ProjectCreateDTO createDTO, Long ownerId);
    
    /**
     * 根据项目ID获取项目信息
     * 
     * 业务逻辑：
     * 1. 根据ID查找项目
     * 2. 检查项目是否存在
     * 3. 验证用户是否有访问权限（是项目成员）
     * 4. 返回项目详细信息
     * 
     * @param projectId 项目ID
     * @param userId 当前用户ID（用于权限检查）
     * @return 项目详细信息
     * @throws RuntimeException 当项目不存在或无访问权限时抛出异常
     */
    ProjectResponseDTO getProjectById(Long projectId, Long userId);
    
    /**
     * 获取用户的项目列表
     * 
     * 业务逻辑：
     * 1. 查找用户拥有的所有项目
     * 2. 按创建时间倒序排列
     * 3. 转换为响应DTO格式
     * 
     * @param userId 项目所有者ID
     * @return 用户的项目列表
     */
    List<ProjectResponseDTO> getProjectsByOwner(Long userId);
    
    /**
     * 更新项目信息
     * 
     * 业务逻辑：
     * 1. 验证项目是否存在
     * 2. 验证用户是否为项目所有者
     * 3. 验证项目名称是否重复（如果修改了名称）
     * 4. 更新项目信息和更新时间
     * 5. 保存更改
     * 
     * @param projectId 项目ID
     * @param updateDTO 更新数据（已通过DTO格式验证）
     * @param userId 当前用户ID（必须是项目所有者）
     * @return 更新后的项目信息
     * @throws RuntimeException 当项目不存在、无权限或名称重复时抛出异常
     */
    ProjectResponseDTO updateProject(Long projectId, ProjectUpdateDTO updateDTO, Long userId);
    
    /**
     * 删除项目
     * 
     * 业务逻辑：
     * 1. 验证项目是否存在
     * 2. 验证用户是否为项目所有者
     * 3. 删除所有项目成员记录
     * 4. 删除项目记录
     * 
     * @param projectId 项目ID
     * @param userId 当前用户ID（必须是项目所有者）
     * @throws RuntimeException 当项目不存在或无权限时抛出异常
     */
    void deleteProject(Long projectId, Long userId);
    
    /**
     * 检查用户是否为项目成员
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return true-是成员，false-不是成员
     */
    boolean isProjectMember(Long projectId, Long userId);
    
    /**
     * 检查用户是否为项目所有者
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return true-是所有者，false-不是所有者
     */
    boolean isProjectOwner(Long projectId, Long userId);
}
