package com.codivio.project.repository;

import com.codivio.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 项目数据访问接口
 * 基于Spring Data JPA
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * 根据所有者ID查找项目列表
     * @param ownerId 项目所有者ID
     * @return 项目列表
     */
    List<Project> findByOwnerId(Long ownerId);

    /**
     * 根据所有者ID查找项目列表（分页）
     * @param ownerId 项目所有者ID
     * @param pageable 分页参数
     * @return 分页的项目列表
     */
    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * 根据状态查找项目列表
     * @param status 项目状态
     * @return 项目列表
     */
    List<Project> findByStatus(Integer status);

    /**
     * 根据所有者ID和状态查找项目列表
     * @param ownerId 项目所有者ID
     * @param status 项目状态
     * @return 项目列表
     */
    List<Project> findByOwnerIdAndStatus(Long ownerId, Integer status);

    /**
     * 按项目名称模糊查询
     * @param name 项目名称关键字
     * @return 匹配的项目列表
     */
    List<Project> findByNameContaining(String name);

    /**
     * 检查用户是否已有同名项目
     * @param ownerId 项目所有者ID
     * @param name 项目名称
     * @return true-存在，false-不存在
     */
    boolean existsByOwnerIdAndName(Long ownerId, String name);

    /**
     * 根据项目ID和所有者ID查找项目（用于权限验证）
     * @param id 项目ID
     * @param ownerId 所有者ID
     * @return 项目信息（可能为空）
     */
    @Query("SELECT p FROM Project p WHERE p.id = :id AND p.ownerId = :ownerId")
    Project findByIdAndOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);

    Project getProjectById(Long id);
    
    /**
     * 检查项目名称重复（排除指定项目ID）
     * @param ownerId 所有者ID
     * @param name 项目名称
     * @param excludeId 排除的项目ID
     * @return true-存在重复，false-不重复
     */
    boolean existsByOwnerIdAndNameAndIdNot(Long ownerId, String name, Long excludeId);
}
