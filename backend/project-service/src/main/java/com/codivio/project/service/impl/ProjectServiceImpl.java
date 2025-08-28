package com.codivio.project.service.impl;

import com.codivio.project.dto.AddMemberDTO;
import com.codivio.project.dto.ProjectCreateDTO;
import com.codivio.project.dto.ProjectMemberDTO;
import com.codivio.project.dto.ProjectResponseDTO;
import com.codivio.project.dto.ProjectUpdateDTO;
import com.codivio.project.entity.Project;
import com.codivio.project.entity.ProjectMember;
import com.codivio.project.entity.ProjectRole;
import com.codivio.project.exception.BaseBusinessException;
import com.codivio.project.exception.ErrorCode;
import com.codivio.project.repository.ProjectRepository;
import com.codivio.project.repository.ProjectMemberRepository;
import com.codivio.project.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 项目服务实现类
 */
@Service
public class ProjectServiceImpl implements ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional
    public ProjectResponseDTO createProject(ProjectCreateDTO createDTO, Long ownerId) {
        // 1. 验证项目名称是否已存在（同一用户下）
        if (projectRepository.existsByOwnerIdAndName(ownerId, createDTO.getName())) {
            throw new BaseBusinessException(ErrorCode.PROJECT_NAME_ALREADY_EXISTS);
        }
        
        // 2. 创建项目实体并设置属性
        Project project = new Project();
        project.setName(createDTO.getName());
        project.setDescription(createDTO.getDescription());
        project.setLanguage(createDTO.getLanguage());
        project.setStatus(1); // 默认状态：进行中
        project.setOwnerId(ownerId);
        // 时间字段会通过 @PrePersist 自动设置
        
        // 3. 保存项目信息
        Project savedProject = projectRepository.save(project);
        
        // 4. 自动添加创建者为项目所有者成员
        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProjectId(savedProject.getId());
        ownerMember.setUserId(ownerId);
        ownerMember.setRole(ProjectRole.OWNER);
        ownerMember.setJoinedAt(LocalDateTime.now());
        projectMemberRepository.save(ownerMember);
        
        // 5. 转换为响应DTO并返回
        return convertToResponseDTO(savedProject);
    }
    
    /**
     * 将 Project 实体转换为 ProjectResponseDTO
     */
    private ProjectResponseDTO convertToResponseDTO(Project project) {
        ProjectResponseDTO responseDTO = new ProjectResponseDTO();
        responseDTO.setId(project.getId());
        responseDTO.setName(project.getName());
        responseDTO.setDescription(project.getDescription());
        responseDTO.setLanguage(project.getLanguage());
        responseDTO.setStatus(project.getStatus());
        responseDTO.setOwnerId(project.getOwnerId());
        responseDTO.setCreatedAt(project.getCreatedAt());
        responseDTO.setUpdatedAt(project.getUpdatedAt());
        return responseDTO;
    }

    @Override
    public ProjectResponseDTO getProjectById(Long projectId, Long userId) {
        // 1. 根据ID查找项目
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.PROJECT_NOT_FOUND));
        
        // 2. 验证用户是否有访问权限（是项目成员）
        if (!isProjectMember(projectId, userId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
        
        // 3. 返回项目详细信息
        return convertToResponseDTO(project);
    }

    @Override
    public List<ProjectResponseDTO> getProjectsByOwner(Long userId) {
        // 1. 查找用户拥有的所有项目
        List<Project> projects = projectRepository.findByOwnerId(userId);
        
        // 2. 转换为响应DTO格式
        List<ProjectResponseDTO> responseDTOs = new ArrayList<>();
        for(Project project : projects) {
            ProjectResponseDTO responseDTO = convertToResponseDTO(project);
            responseDTOs.add(responseDTO);
        }
        
        // 3. 返回项目列表（如果用户没有项目，返回空列表是正常的）
        return responseDTOs;
    }

    @Override
    @Transactional
    public ProjectResponseDTO updateProject(Long projectId, ProjectUpdateDTO updateDTO, Long userId) {
        // 1. 检查项目是否存在
        if (!projectRepository.existsById(projectId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        
        // 2. 检查用户是否有更新权限（OWNER 或 EDITOR）
        if (!canUserEditProject(projectId, userId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
        
        // 3. 获取项目信息
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.PROJECT_NOT_FOUND));
        
        // 4. 检查是否有更新内容
        if (!updateDTO.hasUpdates()) {
            return convertToResponseDTO(project); // 没有更新内容，直接返回
        }
        
        // 5. 部分更新字段（只更新非空字段）
        if (updateDTO.getName() != null) {
            // 检查项目名称重复（同一owner下）
            if (projectRepository.existsByOwnerIdAndNameAndIdNot(
                    project.getOwnerId(), updateDTO.getName(), projectId)) {
                throw new BaseBusinessException(ErrorCode.PROJECT_NAME_ALREADY_EXISTS);
            }
            project.setName(updateDTO.getName());
        }
        
        if (updateDTO.getDescription() != null) {
            project.setDescription(updateDTO.getDescription());
        }
        
        if (updateDTO.getLanguage() != null) {
            project.setLanguage(updateDTO.getLanguage());
        }
        
        // 6. 保存更新（updatedAt会通过@PreUpdate自动更新）
        Project updatedProject = projectRepository.save(project);
        
        return convertToResponseDTO(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        // 1. 检查项目是否存在
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseBusinessException(ErrorCode.PROJECT_NOT_FOUND));
        
        // 2. 检查用户是否是项目所有者（只有OWNER可以删除项目）
        if (!isProjectOwner(projectId, userId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
        
        // 3. 删除项目（由于数据库配置了CASCADE，会自动删除相关的project_members记录）
        projectRepository.delete(project);
        
        // 注意：实际的企业级应用可能需要考虑：
        // - 软删除而不是硬删除
        // - 删除前检查是否有关联的文件需要清理
        // - 发送删除事件通知其他服务
        // 但在MVP阶段，简单的硬删除就足够了
    }

    @Override
    public boolean isProjectMember(Long projectId, Long userId) {
        return projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    @Override
    public boolean isProjectOwner(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        return project != null && project.getOwnerId().equals(userId);
    }
    
    /**
     * 检查用户是否可以编辑项目（OWNER 或 EDITOR）
     */
    private boolean canUserEditProject(Long projectId, Long userId) {
        // 1. 检查是否是项目所有者
        if (isProjectOwner(projectId, userId)) {
            return true;
        }
        
        // 2. 检查是否是项目成员且角色为EDITOR或OWNER
        return projectMemberRepository.existsByProjectIdAndUserIdAndRoleIn(
                projectId, userId, Arrays.asList(ProjectRole.OWNER, ProjectRole.EDITOR));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberDTO> getProjectMembers(Long projectId) {
        // 1. 检查项目是否存在
        if(!projectRepository.existsById(projectId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        
        // 2. 查询项目的所有成员
        List<ProjectMember> projectMembers = projectMemberRepository.findByProjectId(projectId);
        
        // 3. 转换为DTO列表
        List<ProjectMemberDTO> projectMemberDTOS = new ArrayList<>();
        for(ProjectMember projectMember : projectMembers) {
            ProjectMemberDTO projectMemberDTO =  new ProjectMemberDTO();
            projectMemberDTO.setId(projectMember.getId());
            projectMemberDTO.setProjectId(projectMember.getProjectId());
            projectMemberDTO.setUserId(projectMember.getUserId());
            projectMemberDTO.setRole(projectMember.getRole());
            projectMemberDTO.setJoinedAt(projectMember.getJoinedAt());
            projectMemberDTOS.add(projectMemberDTO);
        }
        
        return projectMemberDTOS;
    }
    
    @Override
    @Transactional
    public void addMember(Long projectId, AddMemberDTO addMemberDTO) {
        // 1. 检查项目是否存在
        if(!projectRepository.existsById(projectId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        
        // 2. 检查用户是否已经是成员
        if(projectMemberRepository.existsByProjectIdAndUserId(projectId, addMemberDTO.getUserId())) {
            throw new BaseBusinessException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }
        
        // 3. 验证角色（不能添加OWNER）
        if(addMemberDTO.getRole().equals(ProjectRole.OWNER)) {
            throw new BaseBusinessException(ErrorCode.CANNOT_ADD_OWNER_ROLE);
        }
        
        // 4. 创建成员记录并保存
        ProjectMember projectMember = new ProjectMember();
        projectMember.setProjectId(projectId);
        projectMember.setUserId(addMemberDTO.getUserId());
        projectMember.setRole(addMemberDTO.getRole());
        projectMember.setJoinedAt(LocalDateTime.now());
        projectMemberRepository.save(projectMember);
    }
    
    @Override
    @Transactional
    public void updateMemberRole(Long projectId, Long userId, ProjectRole newRole) {
        // 1. 检查项目是否存在
        if(!projectRepository.existsById(projectId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        
        // 2. 查询现有成员记录
        ProjectMember projectMember = projectMemberRepository
            .findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new BaseBusinessException(ErrorCode.PROJECT_MEMBER_NOT_FOUND));
        
        // 3. 验证角色变更规则
        // 3.1 不能变更为OWNER角色
        if(newRole.equals(ProjectRole.OWNER)) {
            throw new BaseBusinessException(ErrorCode.CANNOT_ADD_OWNER_ROLE);
        }
        
        // 3.2 不能修改OWNER的角色
        if(projectMember.getRole().equals(ProjectRole.OWNER)) {
            throw new BaseBusinessException(ErrorCode.CANNOT_MODIFY_OWNER_ROLE);
        }
        
        // 4. 更新角色并保存
        projectMember.setRole(newRole);
        projectMemberRepository.save(projectMember);
    }
    
    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        // 1. 检查项目是否存在
        if(!projectRepository.existsById(projectId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        
        // 2. 查询成员记录（同时验证成员存在性）
        ProjectMember projectMember = projectMemberRepository
            .findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new BaseBusinessException(ErrorCode.PROJECT_MEMBER_NOT_FOUND));
        
        // 3. 验证不能删除OWNER
        if(projectMember.getRole().equals(ProjectRole.OWNER)) {
            throw new BaseBusinessException(ErrorCode.CANNOT_REMOVE_OWNER);
        }
        
        // 4. 删除成员记录
        projectMemberRepository.delete(projectMember);
    }
}
