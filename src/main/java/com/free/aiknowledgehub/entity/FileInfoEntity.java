package com.free.aiknowledgehub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Description
 * @Author: Liberty-Swine
 * @Date 2026/4/7 16:02
 */
@Data
@TableName("file_info")
public class FileInfoEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    /**
     * 文档ID（UUID），同时作为向量分片的 documentId 元数据
     */
    private String id;
    /**
     * 关联知识库ID（kbId）
     */
    private String kbId;
    /**
     * 原始文件名
     */
    private String fileName;
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    /**
     * 文件后缀（pdf/docx/txt/md）
     */
    private String fileSuffix;
    /**
     * MinIO objectName（用于下载/预览/删除）
     */
    private String filePath;
    /**
     * MIME 类型（如 application/pdf）
     */
    private String contentType;
    /**
     * 解析/向量化失败原因（如有）
     */
    private String errorMessage;
    /**
     * 分片数量
     */
    private Integer chunkCount;
    /**
     * 状态：0=uploaded,1=indexing,2=ready,3=failed,4=deleted
     */
    private Integer status;
    /**
     * 软删标记：0=未删除，1=已删除
     */
    private Boolean deleted;
    /**
     * 删除时间（软删）
     */
    private Date deletedTime;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
}
