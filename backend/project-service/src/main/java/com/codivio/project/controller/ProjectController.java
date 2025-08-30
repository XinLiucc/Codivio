package com.codivio.project.controller;

import com.codivio.project.dto.*;
import com.codivio.project.exception.BaseBusinessException;
import com.codivio.project.exception.ErrorCode;
import com.codivio.project.service.ProjectService;
import com.codivio.project.util.GatewayUserUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目控制器
 * 处理项目相关的HTTP请求
 */
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private GatewayUserUtil gatewayUserUtil;
    
    /**
     * 创建项目
     * POST /api/v1/projects
     * 
     * @param createDTO 项目创建数据
     * @return 创建成功的项目信息
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // 201状态码
    public ResultVO<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectCreateDTO createDTO) {
        
        Long ownerId = gatewayUserUtil.getCurrentUserId();
        if (ownerId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        ProjectResponseDTO responseDTO = projectService.createProject(createDTO, ownerId);
        return ResultVO.success(responseDTO);
    }
    
    /**
     * 获取项目列表
     * GET /api/v1/projects
     * 
     * @return 用户的项目列表
     */
    @GetMapping
    public ResultVO<List<ProjectResponseDTO>> getProjects() {
        
        Long ownerId = gatewayUserUtil.getCurrentUserId();
        if (ownerId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        List<ProjectResponseDTO> projects = projectService.getProjectsByOwner(ownerId);
        return ResultVO.success(projects);
    }
    
    /**
     * 获取项目详情
     * GET /api/v1/projects/{projectId}
     * 
     * @param projectId 项目ID
     * @return 项目详细信息
     */
    @GetMapping("/{projectId}")
    public ResultVO<ProjectResponseDTO> getProject(
            @PathVariable("projectId") Long projectId) {
        
        Long userId = gatewayUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        ProjectResponseDTO responseDTO = projectService.getProjectById(projectId, userId);
        return ResultVO.success(responseDTO);
    }
    
    /**
     * 更新项目信息
     * PUT /api/v1/projects/{projectId}
     * 
     * @param projectId 项目ID
     * @param updateDTO 项目更新数据
     * @return 更新后的项目信息
     */
    @PutMapping("/{projectId}")
    public ResultVO<ProjectResponseDTO> updateProject(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody ProjectUpdateDTO updateDTO) {
        
        Long userId = gatewayUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        ProjectResponseDTO responseDTO = projectService.updateProject(projectId, updateDTO, userId);
        return ResultVO.success(responseDTO);
    }
    
    /**
     * 删除项目
     * DELETE /api/v1/projects/{projectId}
     * 
     * @param projectId 项目ID
     * @return 删除成功响应
     */
    @DeleteMapping("/{projectId}")
    public ResultVO<Void> deleteProject(
            @PathVariable("projectId") Long projectId) {
        
        Long userId = gatewayUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        projectService.deleteProject(projectId, userId);
        return ResultVO.success(null);
    }

    /**
     * 获取项目成员列表
     * GET /api/v1/projects/{projectId}/members
     * 
     * @param projectId 项目ID
     * @return 项目成员列表
     */
    @GetMapping("/{projectId}/members")
    public ResultVO<List<ProjectMemberDTO>> getProjectMembers(
            @PathVariable("projectId") Long projectId
    ) {
        Long operatorId = gatewayUserUtil.getCurrentUserId();
        if (operatorId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        // 1. 权限验证：检查operatorId是否为项目owner
        if(!projectService.isProjectOwner(projectId, operatorId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }

        // 2. 调用service获取成员列表
        List<ProjectMemberDTO> projectMemberDTOS = projectService.getProjectMembers(projectId);

        // 3. 返回结果
        return ResultVO.success(projectMemberDTOS);
    }
    
    /**
     * 添加项目成员
     * POST /api/v1/projects/{projectId}/members
     * 
     * @param projectId 项目ID
     * @param addMemberDTO 添加成员请求数据
     * @return 添加成功响应
     */
    @PostMapping("/{projectId}/members")
    public ResultVO<Void> addMember(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody AddMemberDTO addMemberDTO
    ) {
        Long operatorId = gatewayUserUtil.getCurrentUserId();
        if (operatorId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        if(!projectService.isProjectOwner(projectId, operatorId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
        projectService.addMember(projectId, addMemberDTO);
        return ResultVO.success(null);
    }
    
    /**
     * 更新成员角色
     * PUT /api/v1/projects/{projectId}/members/{userId}
     * 
     * @param projectId 项目ID
     * @param userId 成员用户ID
     * @param addMemberDTO 更新角色数据
     * @return 更新成功响应
     */
    @PutMapping("/{projectId}/members/{userId}")
    public ResultVO<Void> updateMemberRole(
            @PathVariable("projectId") Long projectId,
            @PathVariable("userId") Long userId,
            @Valid @RequestBody AddMemberDTO addMemberDTO
    ) {
        Long operatorId = gatewayUserUtil.getCurrentUserId();
        if (operatorId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        if(!projectService.isProjectOwner(projectId, operatorId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
        projectService.updateMemberRole(projectId, userId, addMemberDTO.getRole());
        return ResultVO.success(null);
    }
    
    /**
     * 移除项目成员
     * DELETE /api/v1/projects/{projectId}/members/{userId}
     * 
     * @param projectId 项目ID
     * @param userId 要移除的成员用户ID
     * @return 移除成功响应
     */
    @DeleteMapping("/{projectId}/members/{userId}")
    public ResultVO<Void> removeMember(
            @PathVariable("projectId") Long projectId,
            @PathVariable("userId") Long userId
    ) {
        Long operatorId = gatewayUserUtil.getCurrentUserId();
        if (operatorId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        if(!projectService.isProjectOwner(projectId, operatorId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
        projectService.removeMember(projectId, userId);
        return ResultVO.success(null);
    }

}
