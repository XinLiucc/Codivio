package com.codivio.file.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 项目服务客户端
 * 用于调用项目服务验证权限
 */
@FeignClient(name = "project-service", url = "http://localhost:8082")
public interface ProjectServiceClient {

    /**
     * 获取用户在项目中的角色
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param authorization JWT token
     * @return 用户角色信息
     */
    @GetMapping("/api/v1/projects/{projectId}/members/{userId}/role")
    ProjectMemberRoleResponse getUserRoleInProject(
            @PathVariable("projectId") String projectId,
            @PathVariable("userId") Long userId,
            @RequestHeader("Authorization") String authorization
    );

    /**
     * 检查用户是否是项目成员
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param authorization JWT token
     * @return 是否是项目成员
     */
    @GetMapping("/api/v1/projects/{projectId}/members/{userId}/exists")
    Boolean isProjectMember(
            @PathVariable("projectId") String projectId,
            @PathVariable("userId") Long userId,
            @RequestHeader("Authorization") String authorization
    );

    /**
     * 项目成员角色响应DTO
     */
    class ProjectMemberRoleResponse {
        private String role;  // OWNER, EDITOR, VIEWER
        private Boolean exists;

        public ProjectMemberRoleResponse() {}

        public ProjectMemberRoleResponse(String role, Boolean exists) {
            this.role = role;
            this.exists = exists;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Boolean getExists() {
            return exists;
        }

        public void setExists(Boolean exists) {
            this.exists = exists;
        }

        @Override
        public String toString() {
            return "ProjectMemberRoleResponse{" +
                    "role='" + role + '\'' +
                    ", exists=" + exists +
                    '}';
        }
    }
}