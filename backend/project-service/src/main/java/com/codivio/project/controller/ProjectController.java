package com.codivio.project.controller;

import com.codivio.project.dto.ProjectCreateDTO;
import com.codivio.project.dto.ProjectResponseDTO;
import com.codivio.project.dto.ProjectUpdateDTO;
import com.codivio.project.dto.ResultVO;
import com.codivio.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
}
