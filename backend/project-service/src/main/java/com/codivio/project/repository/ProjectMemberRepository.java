package com.codivio.project.repository;

import com.codivio.project.entity.ProjectMember;
import com.codivio.project.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 项目成员数据访问接口
 * 基于Spring Data JPA
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    /**
     * 根据项目ID查找所有成员
     * @param projectId 项目ID
     * @return 项目成员列表
     */
    List<ProjectMember> findByProjectId(Long projectId);

    /**
     * 根据用户ID查找参与的所有项目
     * @param userId 用户ID
     * @return 用户参与的项目成员记录列表
     */
    List<ProjectMember> findByUserId(Long userId);

    /**
     * 根据项目ID和用户ID查找成员记录（权限检查用）
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 成员记录（可能为空）
     */
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    /**
     * 根据项目ID和角色查找成员
     * @param projectId 项目ID
     * @param role 成员角色
     * @return 指定角色的成员列表
     */
    List<ProjectMember> findByProjectIdAndRole(Long projectId, ProjectRole role);

    /**
     * 统计项目成员数量
     * @param projectId 项目ID
     * @return 成员数量
     */
    long countByProjectId(Long projectId);

    /**
     * 检查用户是否是项目成员
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return true-是成员，false-不是成员
     */
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    /**
     * 查找项目的所有者
     * @param projectId 项目ID
     * @return 项目所有者记录（可能为空）
     */
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.projectId = :projectId AND pm.role = 'OWNER'")
    Optional<ProjectMember> findOwnerByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据用户ID和角色查找项目列表
     * @param userId 用户ID
     * @param role 用户角色
     * @return 用户以指定角色参与的项目成员记录列表
     */
    List<ProjectMember> findByUserIdAndRole(Long userId, ProjectRole role);
    
    /**
     * 检查用户是否具有指定角色中的任一角色
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param roles 角色列表
     * @return true-用户具有指定角色之一，false-不具有
     */
    boolean existsByProjectIdAndUserIdAndRoleIn(Long projectId, Long userId, List<ProjectRole> roles);
}
