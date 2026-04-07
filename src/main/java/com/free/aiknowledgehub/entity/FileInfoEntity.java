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
    private String id;
    private String fileName;
    private Long fileSize;
    private String fileSuffix;
    private String filePath;
    private Integer chunkCount;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
