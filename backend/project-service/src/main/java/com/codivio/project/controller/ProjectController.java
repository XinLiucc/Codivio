package com.codivio.project.controller;

import com.codivio.project.dto.*;
import com.codivio.project.exception.BaseBusinessException;
import com.codivio.project.exception.ErrorCode;
import com.codivio.project.entity.ProjectRole;
import com.codivio.project.repository.ProjectFileTreeRepository;
import com.codivio.project.repository.ProjectMemberRepository;
import com.codivio.project.repository.ProjectRepository;
import com.codivio.project.service.ProjectService;
import com.codivio.project.util.GatewayUserUtil;
import com.codivio.project.entity.ProjectFileTree;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectFileTreeRepository projectFileTreeRepository;

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
    @GetMapping("/stats")
    public ResultVO<Map<String, Long>> getDashboardStats() {
        Long userId = gatewayUserUtil.getCurrentUserId();
        if (userId == null) throw new BaseBusinessException(ErrorCode.UNAUTHORIZED);

        // 我的项目数（作为 OWNER）
        long projectCount = projectRepository.findByOwnerId(userId).size();

        // 协作项目数（作为成员，角色不是 OWNER）
        long collaborationCount = projectMemberRepository
                .findByUserIdAndRole(userId, ProjectRole.EDITOR).size()
                + projectMemberRepository
                .findByUserIdAndRole(userId, ProjectRole.VIEWER).size();

        // 文件总数：统计所有有权访问的项目中 FILE 类型节点
        // 我的项目 + 协作项目
        List<String> allProjectIds = new ArrayList<>();
        projectRepository.findByOwnerId(userId)
                .forEach(p -> allProjectIds.add(p.getId()));
        projectMemberRepository.findByUserId(userId).stream()
                .filter(m -> m.getRole() != ProjectRole.OWNER)
                .forEach(m -> allProjectIds.add(m.getProjectId()));

        long fileCount = allProjectIds.stream()
                .mapToLong(pid -> projectFileTreeRepository
                        .countByProjectIdAndType(pid, ProjectFileTree.FileTreeType.FILE))
                .sum();

        Map<String, Long> stats = new HashMap<>();
        stats.put("projectCount", projectCount);
        stats.put("collaborationCount", collaborationCount);
        stats.put("fileCount", fileCount);
        return ResultVO.success(stats);
    }

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
            @PathVariable("projectId") String projectId) {
        
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
            @PathVariable("projectId") String projectId,
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
            @PathVariable("projectId") String projectId) {
        
        Long userId = gatewayUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        projectService.deleteProject(projectId, userId);
        return ResultVO.success(null);
    }

    /**
     * 获取当前用户在项目中的角色
     * GET /api/v1/projects/{projectId}/my-role
     */
    @GetMapping("/{projectId}/my-role")
    public ResultVO<Map<String, String>> getMyRole(
            @PathVariable("projectId") String projectId
    ) {
        Long userId = gatewayUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED);
        }

        var memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        if (memberOpt.isEmpty()) {
            throw new BaseBusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }

        Map<String, String> result = new HashMap<>();
        result.put("role", memberOpt.get().getRole().name());
        return ResultVO.success(result);
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
            @PathVariable("projectId") String projectId
    ) {
        Long operatorId = gatewayUserUtil.getCurrentUserId();
        if (operatorId == null) {
            throw new BaseBusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        // 1. 权限验证：是项目成员即可查看
        if(!projectMemberRepository.existsByProjectIdAndUserId(projectId, operatorId)) {
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
            @PathVariable("projectId") String projectId,
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
            @PathVariable("projectId") String projectId,
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
            @PathVariable("projectId") String projectId,
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
