package com.free.aiknowledgehub.service;

import com.free.aiknowledgehub.utils.MinioUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


/**
 * @Description
 * @Author: Liberty-Swine
 * @Date 2026/4/6 16:16
 */
@Service
@Slf4j
public class DocumentService {

    @Resource
    private MinioUtils minioUtils;

    /**
     * 文件上传
     * @param file
     * @return
     */
    public Boolean uploadDocument(MultipartFile file){
        String originalFilename = file.getOriginalFilename();
        try {
            // 1. 上传文件到MinIO（存储到tech-manual/目录）,todo 文件夹后续要改为配置
            String objectName = minioUtils.uploadFile(file, "tech-manual/");
            // 2. 获取预览URL（用于前端展示/溯源）
            String previewUrl = minioUtils.getFilePreviewUrl(objectName);
            // 3. 执行文件向量化（知识库核心流程）
//            String fileId = UUID.randomUUID().toString();
//            customFileVectorizationService.vectorizeFile(fileId, file.getOriginalFilename());
            // 4. 数据库操作（省略：存储fileId、objectName、previewUrl、文件类型等）
        } catch (Exception e) {
            log.error("文件名称为【{}】的文件上传失败",originalFilename,e);
        }
        return true;
    }


}
