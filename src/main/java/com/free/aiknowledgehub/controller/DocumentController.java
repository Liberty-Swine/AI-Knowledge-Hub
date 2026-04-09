package com.free.aiknowledgehub.controller;

import com.free.aiknowledgehub.common.result.Result;
import com.free.aiknowledgehub.service.DocumentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Description 文档上传/管理
 * @Author: Liberty-Swine
 * @Date 2026/4/6 16:13
 */
@RestController
@RequestMapping("/api/document")
public class DocumentController {

    /**
     * 文档上传service
     */
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * 文件上传（绑定知识库）
     * @param kbId 知识库ID
     * @param file 文件
     * @return 是否成功
     */
    @PostMapping("/upload")
    Result<Boolean> uploadDocument(@RequestParam("kbId") String kbId,
                                   @RequestParam("file") MultipartFile file){
        documentService.uploadDocument(kbId, file);
        return Result.success();
    }

    /**
     * 文档列表（按知识库）
     * @param kbId 知识库ID
     * @return 文档列表
     */
    @GetMapping
    Result<List<com.free.aiknowledgehub.entity.FileInfoEntity>> list(@RequestParam("kbId") String kbId) {
        return Result.success(documentService.listByKbId(kbId));
    }

    /**
     * 文档详情
     * @param id 文档ID
     * @return 文档信息
     */
    @GetMapping("/{id}")
    Result<com.free.aiknowledgehub.entity.FileInfoEntity> detail(@PathVariable("id") String id) {
        return Result.success(documentService.getById(id));
    }

    /**
     * 获取文档预览URL（MinIO 预签名）
     * @param id 文档ID
     * @return 预览URL
     */
    @GetMapping("/{id}/preview-url")
    Result<String> previewUrl(@PathVariable("id") String id) {
        return Result.success(documentService.getPreviewUrl(id));
    }

    /**
     * 删除文档（软删 file_info + 删除 MinIO 文件；向量删除后续由向量模块完成）
     * @param id 文档ID
     * @return 是否成功
     */
    @PostMapping("/{id}/delete")
    Result<Boolean> delete(@PathVariable("id") String id) {
        documentService.deleteDocument(id);
        return Result.success(true);
    }

    /**
     * 重新索引：从 MinIO 重新解析→分片→向量化（用于修复失败或重建索引）
     * @param id 文档ID
     * @return 是否成功提交
     */
    @PostMapping("/{id}/reindex")
    Result<Boolean> reindex(@PathVariable("id") String id) {
        documentService.reindex(id);
        return Result.success(true);
    }
}
