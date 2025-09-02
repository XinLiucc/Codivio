package com.codivio.file.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 文件类型工具类
 * 用于判断文件类型、验证文件扩展名等
 */
public class FileTypeUtil {

    /**
     * 支持的文本文件扩展名（存储到数据库content字段）
     */
    private static final Set<String> TEXT_FILE_EXTENSIONS = Set.of(
            // 代码文件
            ".js", ".jsx", ".ts", ".tsx",
            ".java", ".py", ".go", ".rs", ".cpp", ".c", ".h",
            ".php", ".rb", ".swift", ".kt",
            
            // 标记语言
            ".html", ".htm", ".xml", ".json", ".yaml", ".yml",
            
            // 样式文件
            ".css", ".scss", ".sass", ".less",
            
            // 配置文件
            ".properties", ".ini", ".env", ".toml",
            ".gitignore", ".gitattributes",
            
            // 文档文件
            ".md", ".txt", ".log",
            
            // 脚本文件
            ".sh", ".bat", ".ps1",
            
            // Web相关
            ".vue", ".svelte"
    );

    /**
     * MIME类型映射
     */
    private static final Map<String, String> MIME_TYPE_MAP = new HashMap<>();
    
    static {
        // 代码文件
        MIME_TYPE_MAP.put(".js", "application/javascript");
        MIME_TYPE_MAP.put(".jsx", "application/javascript");
        MIME_TYPE_MAP.put(".ts", "application/typescript");
        MIME_TYPE_MAP.put(".tsx", "application/typescript");
        MIME_TYPE_MAP.put(".java", "text/x-java-source");
        MIME_TYPE_MAP.put(".py", "text/x-python");
        MIME_TYPE_MAP.put(".go", "text/x-go");
        MIME_TYPE_MAP.put(".cpp", "text/x-c++src");
        MIME_TYPE_MAP.put(".c", "text/x-csrc");
        
        // Web文件
        MIME_TYPE_MAP.put(".html", "text/html");
        MIME_TYPE_MAP.put(".htm", "text/html");
        MIME_TYPE_MAP.put(".css", "text/css");
        MIME_TYPE_MAP.put(".scss", "text/x-scss");
        MIME_TYPE_MAP.put(".vue", "text/x-vue");
        
        // 数据文件
        MIME_TYPE_MAP.put(".json", "application/json");
        MIME_TYPE_MAP.put(".xml", "application/xml");
        MIME_TYPE_MAP.put(".yaml", "application/x-yaml");
        MIME_TYPE_MAP.put(".yml", "application/x-yaml");
        
        // 文档文件
        MIME_TYPE_MAP.put(".md", "text/markdown");
        MIME_TYPE_MAP.put(".txt", "text/plain");
        MIME_TYPE_MAP.put(".log", "text/plain");
        
        // 配置文件
        MIME_TYPE_MAP.put(".properties", "text/x-java-properties");
        MIME_TYPE_MAP.put(".ini", "text/plain");
        MIME_TYPE_MAP.put(".env", "text/plain");
    }

    /**
     * 判断文件是否为支持的文本文件类型
     * @param fileName 文件名
     * @return true-支持的文本文件，false-不支持
     */
    public static boolean isSupportedTextFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        
        String extension = getFileExtension(fileName);
        return TEXT_FILE_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * 获取文件扩展名
     * @param fileName 文件名
     * @return 扩展名（包含点号，如：.js）
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex);
    }

    /**
     * 根据文件扩展名获取MIME类型
     * @param fileName 文件名
     * @return MIME类型
     */
    public static String getMimeType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return MIME_TYPE_MAP.getOrDefault(extension, "application/octet-stream");
    }

    /**
     * 验证文件扩展名是否允许上传
     * @param fileName 文件名
     * @return true-允许，false-不允许
     */
    public static boolean isAllowedFileType(String fileName) {
        // MVP阶段只允许文本文件
        return isSupportedTextFile(fileName);
    }

    /**
     * 获取文件类型描述
     * @param fileName 文件名
     * @return 文件类型描述
     */
    public static String getFileTypeDescription(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        
        return switch (extension) {
            case ".js", ".jsx" -> "JavaScript";
            case ".ts", ".tsx" -> "TypeScript";
            case ".java" -> "Java";
            case ".py" -> "Python";
            case ".go" -> "Go";
            case ".html", ".htm" -> "HTML";
            case ".css" -> "CSS";
            case ".vue" -> "Vue";
            case ".json" -> "JSON";
            case ".md" -> "Markdown";
            case ".txt" -> "Text";
            default -> "Unknown";
        };
    }

    /**
     * 检查文件大小是否超限
     * @param fileSize 文件大小（字节）
     * @param maxSize 最大允许大小（字节）
     * @return true-超限，false-未超限
     */
    public static boolean isFileSizeExceeded(long fileSize, long maxSize) {
        return fileSize > maxSize;
    }

    /**
     * 格式化文件大小显示
     * @param bytes 文件大小（字节）
     * @return 格式化后的大小字符串
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}