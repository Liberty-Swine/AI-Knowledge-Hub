package com.free.aiknowledgehub.service;

import com.free.aiknowledgehub.entity.FileInfoEntity;
import com.free.aiknowledgehub.utils.DocumentParserUtils;
import com.free.aiknowledgehub.utils.TextSplitterUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

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
     * 异步解析文件
     * @param file
     * @param documentId
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
            log.info("文件id为【{}】，文本分片成功，一共【{}】个",documentId,chunks.size());
            //向量+存入es
            vectorStoreService.saveDocumentByBatch(chunks);
            log.info("文件id为【{}】，异步解析文件成功",documentId);
            updateFileInfo.setStatus(1);
            updateFileInfo.setChunkCount(chunks.size());
        }catch (Exception e){
            updateFileInfo.setStatus(2);
            log.error("文件id为【{}】，异步解析文件失败",documentId,e);
        }
        updateFileInfo.setUpdateTime(new Date());
        fileInfoService.updateById(updateFileInfo);
        log.info("文件id为【{}】，更新数据库成功",documentId);
    }

}
