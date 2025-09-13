package com.codivio.project.client;

import com.codivio.project.dto.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 文件服务Feign客户端
 * 用于项目服务调用文件服务的接口
 */
@FeignClient(name = "file-service", url = "${feign.client.config.file-service.url:http://localhost:8083}")
public interface FileServiceClient {
    
    /**
     * 获取文件内容
     * 用于文件树节点的内容查看功能
     * 
     * @param fileId 文件服务中的文件ID
     * @return 文件内容响应
     */
    @GetMapping("/api/v1/files/{fileId}/content")
    ResultVO<Map<String, Object>> getFileContent(@PathVariable("fileId") String fileId);
    
    /**
     * 更新文件内容
     * 用于文件编辑保存功能
     * 
     * @param fileId 文件服务中的文件ID
     * @param request 文件内容更新请求
     * @return 更新结果
     */
    @PutMapping("/api/v1/files/{fileId}/content")
    ResultVO<Void> updateFileContent(
        @PathVariable("fileId") String fileId, 
        @RequestBody Map<String, Object> request);
    
    /**
     * 验证文件是否存在
     * 用于文件树同步验证
     * 
     * @param fileId 文件服务中的文件ID
     * @return 验证结果
     */
    @GetMapping("/api/v1/files/{fileId}/exists")
    ResultVO<Boolean> checkFileExists(@PathVariable("fileId") String fileId);
    
    /**
     * 获取文件元数据
     * 用于文件大小、修改时间等信息同步
     * 
     * @param fileId 文件服务中的文件ID
     * @return 文件元数据
     */
    @GetMapping("/api/v1/files/{fileId}/metadata")
    ResultVO<Map<String, Object>> getFileMetadata(@PathVariable("fileId") String fileId);
    
    /**
     * 批量获取文件状态
     * 用于文件树状态同步
     * 
     * @param fileIds 文件ID列表
     * @return 文件状态映射
     */
    @PostMapping("/api/v1/files/batch/status")
    ResultVO<Map<String, Object>> getBatchFileStatus(@RequestBody Map<String, Object> fileIds);
}