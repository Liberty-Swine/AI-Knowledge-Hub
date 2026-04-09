package com.free.aiknowledgehub.service;

import com.free.aiknowledgehub.entity.FileInfoEntity;
import com.free.aiknowledgehub.utils.MinioUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * @Description 文档服务：上传、查询、预览、删除、重建索引
 * @Author: Liberty-Swine
 * @Date 2026/4/6 16:16
 */
@Service
@Slf4j
public class DocumentService {

    /**
     * 文档状态：已上传（未开始索引）
     */
    public static final int STATUS_UPLOADED = 0;
    /**
     * 文档状态：索引中（解析/分片/向量化）
     */
    public static final int STATUS_INDEXING = 1;
    /**
     * 文档状态：就绪（可检索问答）
     */
    public static final int STATUS_READY = 2;
    /**
     * 文档状态：失败（可重建索引）
     */
    public static final int STATUS_FAILED = 3;
    /**
     * 文档状态：已删除（软删）
     */
    public static final int STATUS_DELETED = 4;

    private final MinioUtils minioUtils;

    private final RagAsyncService ragAsyncService;

    private final FileInfoService fileInfoService;

    public DocumentService(MinioUtils minioUtils, RagAsyncService ragAsyncService, FileInfoService fileInfoService) {
        this.minioUtils = minioUtils;
        this.ragAsyncService = ragAsyncService;
        this.fileInfoService = fileInfoService;
    }

    /**
     * 文件上传（绑定知识库）
     * @param kbId 知识库ID
     * @param file 文件
     */
    @Transactional(rollbackFor = Exception.class)
    public void uploadDocument(String kbId, MultipartFile file){
        //生成唯一id
        String documentId = UUID.randomUUID().toString().replaceAll("-","");
        //上传文件到MinIO
        String objectName = minioUtils.uploadFile(file, "tech-manual/",documentId);
        //保存文件数据到数据库，todo 后续还需要加入当前操作人员信息
        String originalFilename = file.getOriginalFilename();
        FileInfoEntity saveFileInfo=new FileInfoEntity();
        saveFileInfo.setId(documentId);
        saveFileInfo.setKbId(kbId);
        saveFileInfo.setFileName(originalFilename);
        saveFileInfo.setFilePath(objectName);
        saveFileInfo.setFileSize(file.getSize());
        saveFileInfo.setFileSuffix(FilenameUtils.getExtension(originalFilename));
        saveFileInfo.setContentType(file.getContentType());
        saveFileInfo.setStatus(STATUS_INDEXING);
        saveFileInfo.setDeleted(false);
        saveFileInfo.setCreateTime(new Date());
        fileInfoService.save(saveFileInfo);
        //异步获取文件内容+文本切片+向量化
        ragAsyncService.asyncParseAndVectorize(file, documentId);
    }

    /**
     * 按知识库查询文档列表
     * @param kbId 知识库ID
     * @return 文档列表
     */
    public List<FileInfoEntity> listByKbId(String kbId) {
        return fileInfoService.lambdaQuery()
                .eq(FileInfoEntity::getKbId, kbId)
                .eq(FileInfoEntity::getDeleted, false)
                .orderByDesc(FileInfoEntity::getCreateTime)
                .list();
    }

    /**
     * 查询文档详情
     * @param documentId 文档ID
     * @return 文档信息
     */
    public FileInfoEntity getById(String documentId) {
        return fileInfoService.getById(documentId);
    }

    /**
     * 获取文档预览URL
     * @param documentId 文档ID
     * @return MinIO 预签名URL
     */
    public String getPreviewUrl(String documentId) {
        FileInfoEntity file = fileInfoService.getById(documentId);
        if (file == null) {
            return null;
        }
        return minioUtils.getFilePreviewUrl(file.getFilePath());
    }

    /**
     * 删除文档：软删数据库记录 + 删除 MinIO 文件
     * @param documentId 文档ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(String documentId) {
        FileInfoEntity file = fileInfoService.getById(documentId);
        if (file == null) {
            return;
        }
        // 1) 删除 MinIO 文件（若已不存在则抛错时可调整为忽略）
        minioUtils.deleteFile(file.getFilePath());

        // 2) 软删数据库记录
        FileInfoEntity upd = new FileInfoEntity();
        upd.setId(documentId);
        upd.setDeleted(true);
        upd.setDeletedTime(new Date());
        upd.setStatus(STATUS_DELETED);
        upd.setUpdateTime(new Date());
        fileInfoService.updateById(upd);
    }

    /**
     * 重新索引：从 MinIO 重新下载并触发异步解析/分片/向量化
     * @param documentId 文档ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void reindex(String documentId) {
        FileInfoEntity file = fileInfoService.getById(documentId);
        if (file == null || Boolean.TRUE.equals(file.getDeleted())) {
            return;
        }
        // 更新状态为 indexing
        FileInfoEntity upd = new FileInfoEntity();
        upd.setId(documentId);
        upd.setStatus(STATUS_INDEXING);
        upd.setErrorMessage(null);
        upd.setUpdateTime(new Date());
        fileInfoService.updateById(upd);

        // 从 MinIO 下载文件流再解析（更稳健，也支持重建索引）
        try (InputStream in = minioUtils.downloadFile(file.getFilePath())) {
            ragAsyncService.asyncParseAndVectorize(in, file.getContentType(), documentId);
        } catch (Exception e) {
            FileInfoEntity fail = new FileInfoEntity();
            fail.setId(documentId);
            fail.setStatus(STATUS_FAILED);
            fail.setErrorMessage(e.getMessage());
            fail.setUpdateTime(new Date());
            fileInfoService.updateById(fail);
        }
    }
}
