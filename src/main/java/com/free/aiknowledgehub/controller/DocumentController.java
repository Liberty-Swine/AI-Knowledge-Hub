package com.free.aiknowledgehub.controller;

import com.free.aiknowledgehub.common.result.Result;
import com.free.aiknowledgehub.service.DocumentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    Result<Boolean> uploadDocument(@RequestParam("file") MultipartFile file){
        documentService.uploadDocument(file);
        return Result.success();
    }
}
