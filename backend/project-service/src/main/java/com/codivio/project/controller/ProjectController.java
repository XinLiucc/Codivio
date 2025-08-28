package com.codivio.project.controller;

import com.codivio.project.dto.*;
import com.codivio.project.exception.BaseBusinessException;
import com.codivio.project.exception.ErrorCode;
import com.codivio.project.service.ProjectService;
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
    
    /**
     * 创建项目
     * POST /api/v1/projects
     * 
     * @param createDTO 项目创建数据
     * @param ownerId 项目所有者ID（临时参数，后续从JWT获取）
     * @return 创建成功的项目信息
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // 201状态码
    public ResultVO<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectCreateDTO createDTO,
            @RequestParam("ownerId") Long ownerId) {
        
        ProjectResponseDTO responseDTO = projectService.createProject(createDTO, ownerId);
        return ResultVO.success(responseDTO);
    }
    
    /**
     * 获取项目列表
     * GET /api/v1/projects
     * 
     * @param ownerId 项目所有者ID（临时参数，后续从JWT获取）
     * @return 用户的项目列表
     */
    @GetMapping
    public ResultVO<List<ProjectResponseDTO>> getProjects(
            @RequestParam("ownerId") Long ownerId) {
        
        List<ProjectResponseDTO> projects = projectService.getProjectsByOwner(ownerId);
        return ResultVO.success(projects);
    }
    
    /**
     * 获取项目详情
     * GET /api/v1/projects/{projectId}
     * 
     * @param projectId 项目ID
     * @param userId 当前用户ID（用于权限验证，临时参数）
     * @return 项目详细信息
     */
    @GetMapping("/{projectId}")
    public ResultVO<ProjectResponseDTO> getProject(
            @PathVariable("projectId") Long projectId,
            @RequestParam("userId") Long userId) {
        
        ProjectResponseDTO responseDTO = projectService.getProjectById(projectId, userId);
        return ResultVO.success(responseDTO);
    }
    
    /**
     * 更新项目信息
     * PUT /api/v1/projects/{projectId}
     * 
     * @param projectId 项目ID
     * @param updateDTO 项目更新数据
     * @param userId 当前用户ID（临时参数，后续从JWT获取）
     * @return 更新后的项目信息
     */
    @PutMapping("/{projectId}")
    public ResultVO<ProjectResponseDTO> updateProject(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody ProjectUpdateDTO updateDTO,
            @RequestParam("userId") Long userId) {
        
        ProjectResponseDTO responseDTO = projectService.updateProject(projectId, updateDTO, userId);
        return ResultVO.success(responseDTO);
    }
    
    /**
     * 删除项目
     * DELETE /api/v1/projects/{projectId}
     * 
     * @param projectId 项目ID
     * @param userId 当前用户ID（临时参数，后续从JWT获取）
     * @return 删除成功响应
     */
    @DeleteMapping("/{projectId}")
    public ResultVO<Void> deleteProject(
            @PathVariable("projectId") Long projectId,
            @RequestParam("userId") Long userId) {
        
        projectService.deleteProject(projectId, userId);
        return ResultVO.success(null);
    }

    /**
     * 获取项目成员列表
     * GET /api/v1/projects/{projectId}/members
     * 
     * @param projectId 项目ID
     * @param operatorId 当前用户ID（用于权限验证，临时参数）
     * @return 项目成员列表
     */
    @GetMapping("/{projectId}/members")
    public ResultVO<List<ProjectMemberDTO>> getProjectMembers(
            @PathVariable Long projectId,
            @RequestParam Long operatorId
    ) {
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
     * @param operatorId 当前用户ID（用于权限验证，临时参数）
     * @param addMemberDTO 添加成员请求数据
     * @return 添加成功响应
     */
    @PostMapping("/{projectId}/members")
    public ResultVO<Void> addMember(
            @PathVariable Long projectId,
            @RequestParam Long operatorId,
            @Valid @RequestBody AddMemberDTO addMemberDTO
    ) {
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
     * @param operatorId 当前用户ID（用于权限验证，临时参数）
     * @param addMemberDTO 更新角色数据
     * @return 更新成功响应
     */
    @PutMapping("/{projectId}/members/{userId}")
    public ResultVO<Void> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestParam Long operatorId,
            @Valid @RequestBody AddMemberDTO addMemberDTO
    ) {
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
     * @param operatorId 当前用户ID（用于权限验证，临时参数）
     * @return 移除成功响应
     */
    @DeleteMapping("/{projectId}/members/{userId}")
    public ResultVO<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestParam Long operatorId
    ) {
        if(!projectService.isProjectOwner(projectId, operatorId)) {
            throw new BaseBusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
        projectService.removeMember(projectId, userId);
        return ResultVO.success(null);
    }

}
