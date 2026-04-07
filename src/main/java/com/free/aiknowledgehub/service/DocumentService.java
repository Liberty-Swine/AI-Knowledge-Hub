package com.free.aiknowledgehub.service;

import com.free.aiknowledgehub.entity.FileInfoEntity;
import com.free.aiknowledgehub.utils.MinioUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;


/**
 * @Description
 * @Author: Liberty-Swine
 * @Date 2026/4/6 16:16
 */
@Service
@Slf4j
public class DocumentService {

    private final MinioUtils minioUtils;

    private final RagAsyncService ragAsyncService;

    private final FileInfoService fileInfoService;

    public DocumentService(MinioUtils minioUtils, RagAsyncService ragAsyncService, FileInfoService fileInfoService) {
        this.minioUtils = minioUtils;
        this.ragAsyncService = ragAsyncService;
        this.fileInfoService = fileInfoService;
    }

    /**
     * 文件上传
     * @param file
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void uploadDocument(MultipartFile file){
        //生成唯一id
        String documentId = UUID.randomUUID().toString().replaceAll("-","");
        //上传文件到MinIO
        String objectName = minioUtils.uploadFile(file, "tech-manual/",documentId);
        //保存文件数据到数据库，todo 后续还需要加入当前操作人员信息
        String originalFilename = file.getOriginalFilename();
        FileInfoEntity saveFileInfo=new FileInfoEntity();
        saveFileInfo.setId(documentId);
        saveFileInfo.setFileName(originalFilename);
        saveFileInfo.setFilePath(objectName);
        saveFileInfo.setFileSize(file.getSize());
        saveFileInfo.setFileSuffix(FilenameUtils.getExtension(originalFilename));
        saveFileInfo.setCreateTime(new Date());
        fileInfoService.save(saveFileInfo);
        //异步获取文件内容+文本切片+向量化
        ragAsyncService.asyncParseAndVectorize(file,documentId);
    }
}
