package com.free.aiknowledgehub.service;

import com.free.aiknowledgehub.entity.FileInfoEntity;
import com.free.aiknowledgehub.utils.DocumentParserUtils;
import com.free.aiknowledgehub.utils.TextSplitterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description 异步处理文本分片+向量化
 * @Author: Liberty-Swine
 * @Date 2026/4/6 19:16
 */
@Service
@Slf4j
public class RagAsyncService {

    private final DocumentParserUtils documentParserUtils;

    private final TextSplitterUtils textSplitterUtil;

    private final VectorStoreService vectorStoreService;

    private final FileInfoService fileInfoService;

    public RagAsyncService(DocumentParserUtils documentParserUtils, TextSplitterUtils textSplitterUtil, VectorStoreService vectorStoreService, FileInfoService fileInfoService) {
        this.documentParserUtils = documentParserUtils;
        this.textSplitterUtil = textSplitterUtil;
        this.vectorStoreService = vectorStoreService;
        this.fileInfoService = fileInfoService;
    }

    /**
     * 异步解析文件（上传后直接使用 MultipartFile 流）
     * @param file 文件
     * @param documentId 文档ID
     */
    @Async("ragAsyncExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void asyncParseAndVectorize(MultipartFile file,String documentId){
        FileInfoEntity updateFileInfo=new FileInfoEntity();
        updateFileInfo.setId(documentId);
        try {
            log.info("文件id为【{}】，开始异步解析文件",documentId);
            InputStream inputStream = file.getInputStream();
            //获取文件内容
            String content = documentParserUtils.parse(inputStream, file.getContentType());
            log.info("文件id为【{}】，解析文件流成功，获取到文件内容【{}】",documentId,content);
            //文本分片
            List<Document> chunks = textSplitterUtil.split(content,documentId);
            // 补齐分片元数据：kbId/fileName/sourcePath/chunkIndex（用于检索过滤/溯源/删除）
            fillChunkMetadata(documentId, chunks);
            log.info("文件id为【{}】，文本分片成功，一共【{}】个",documentId,chunks.size());
            //向量+存入es
            vectorStoreService.saveDocumentByBatch(chunks);
            log.info("文件id为【{}】，异步解析文件成功",documentId);
            updateFileInfo.setStatus(DocumentService.STATUS_READY);
            updateFileInfo.setChunkCount(chunks.size());
            updateFileInfo.setErrorMessage(null);
        }catch (Exception e){
            updateFileInfo.setStatus(DocumentService.STATUS_FAILED);
            updateFileInfo.setErrorMessage(e.getMessage());
            log.error("文件id为【{}】，异步解析文件失败",documentId,e);
        }
        updateFileInfo.setUpdateTime(new Date());
        fileInfoService.updateById(updateFileInfo);
        log.info("文件id为【{}】，更新数据库成功",documentId);
    }

    /**
     * 异步解析文件（从 MinIO 下载后传入 InputStream，适用于重建索引/避免请求流失效）
     * @param inputStream 文件输入流
     * @param contentType MIME 类型
     * @param documentId 文档ID
     */
    @Async("ragAsyncExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void asyncParseAndVectorize(InputStream inputStream, String contentType, String documentId) {
        FileInfoEntity updateFileInfo = new FileInfoEntity();
        updateFileInfo.setId(documentId);
        try {
            log.info("文件id为【{}】，开始异步解析文件（InputStream）", documentId);
            String content = documentParserUtils.parse(inputStream, contentType);
            List<Document> chunks = textSplitterUtil.split(content, documentId);
            fillChunkMetadata(documentId, chunks);
            vectorStoreService.saveDocumentByBatch(chunks);

            updateFileInfo.setStatus(DocumentService.STATUS_READY);
            updateFileInfo.setChunkCount(chunks.size());
            updateFileInfo.setErrorMessage(null);
            log.info("文件id为【{}】，异步解析文件成功（InputStream），分片数={}", documentId, chunks.size());
        } catch (Exception e) {
            updateFileInfo.setStatus(DocumentService.STATUS_FAILED);
            updateFileInfo.setErrorMessage(e.getMessage());
            log.error("文件id为【{}】，异步解析文件失败（InputStream）", documentId, e);
        }
        updateFileInfo.setUpdateTime(new Date());
        fileInfoService.updateById(updateFileInfo);
    }

    /**
     * 为每个分片补齐统一元数据（检索过滤/溯源/删除向量依赖）
     * @param documentId 文档ID
     * @param chunks 切片结果
     */
    private void fillChunkMetadata(String documentId, List<Document> chunks) {
        FileInfoEntity file = fileInfoService.getById(documentId);
        if (file == null || chunks == null || chunks.isEmpty()) {
            return;
        }
        for (int i = 0; i < chunks.size(); i++) {
            Document d = chunks.get(i);
            if (d == null) {
                continue;
            }
            Map<String, Object> md = d.getMetadata() == null ? new HashMap<>() : new HashMap<>(d.getMetadata());
            md.put("kbId", file.getKbId());
            md.put("documentId", documentId);
            md.put("fileName", file.getFileName());
            md.put("sourcePath", file.getFilePath());
            md.put("chunkIndex", i);
            // Document metadata is mutable; putAll is enough to enrich it.
            d.getMetadata().putAll(md);
        }
    }

}
