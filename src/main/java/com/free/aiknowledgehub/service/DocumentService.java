package com.free.aiknowledgehub.service;

import com.free.aiknowledgehub.utils.MinioUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


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

    @Resource
    private RagAsyncService ragAsyncService;

    /**
     * 文件上传
     * @param file
     * @throws Exception
     */
//    @Transactional
    public void uploadDocument(MultipartFile file){
        //生成唯一id
        String documentId = UUID.randomUUID().toString();
        //上传文件到MinIO
        String objectName = minioUtils.uploadFile(file, "tech-manual/");
        //todo 保存文件信息到数据库
        //异步获取文件内容+文本切片+向量化
        ragAsyncService.asyncParseAndVectorize(file,documentId);
    }
}
