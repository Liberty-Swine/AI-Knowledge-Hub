package com.free.aiknowledgehub.controller;

import com.free.aiknowledgehub.common.result.Result;
import com.free.aiknowledgehub.entity.KnowledgeBaseEntity;
import com.free.aiknowledgehub.service.KnowledgeBaseService;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @Description 知识库管理接口：创建/列表/详情/删除
 * @Author: Liberty-Swine
 * @Date 2026/4/9
 */
@RestController
@RequestMapping("/api/knowledge-bases")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /**
     * 创建知识库
     * @param req 请求体（name/description）
     * @return 创建后的知识库
     */
    @PostMapping
    public Result<KnowledgeBaseEntity> create(@RequestBody KnowledgeBaseEntity req) {
        KnowledgeBaseEntity kb = new KnowledgeBaseEntity();
        kb.setName(req.getName());
        kb.setDescription(req.getDescription());
        Date now = new Date();
        kb.setCreateTime(now);
        kb.setUpdateTime(now);
        knowledgeBaseService.save(kb);
        return Result.success(kb);
    }

    /**
     * 查询知识库列表
     * @return 知识库列表
     */
    @GetMapping
    public Result<List<KnowledgeBaseEntity>> list() {
        return Result.success(knowledgeBaseService.list());
    }

    /**
     * 查询知识库详情
     * @param id 知识库ID
     * @return 知识库详情（不存在时 data 为空）
     */
    @GetMapping("/{id}")
    public Result<KnowledgeBaseEntity> get(@PathVariable("id") String id) {
        return Result.success(knowledgeBaseService.getById(id));
    }

    /**
     * 删除知识库（当前为硬删；如需软删可扩展 deleted 字段）
     * @param id 知识库ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable("id") String id) {
        return Result.success(knowledgeBaseService.removeById(id));
    }
}

