package com.free.aiknowledgehub.utils;

import com.free.aiknowledgehub.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * @Description MinIO 工具类：封装通用文件操作
 * @Author: Liberty-Swine
 * @Date 2026/4/6 16:29
 */
@Component
@Slf4j
public class MinioUtils {

    @Resource
    private MinioClient minioClient;
    @Resource
    private MinioConfig minioConfig;

    // 新增：文件大小限制（500MB，可配置）
    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024;

    /**
     * 项目启动时自动检查并创建存储桶（新增）
     */
    @PostConstruct
    public void initBucket() {
        checkAndCreateBucket();
        log.info("MinIO初始化完成，存储桶：{}", minioConfig.getBucketName());
    }

    // ========== 基础操作：存储桶管理 ==========
    /**
     * 检查存储桶是否存在，不存在则创建
     */
    public void checkAndCreateBucket() {
        try {
            String bucketName = minioConfig.getBucketName();
            BucketExistsArgs existsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
            if (!minioClient.bucketExists(existsArgs)) {
                MakeBucketArgs makeArgs = MakeBucketArgs.builder().bucket(bucketName).build();
                minioClient.makeBucket(makeArgs);
                log.info("MinIO存储桶【{}】创建成功", bucketName);
            }
        } catch (Exception e) {
            log.error("MinIO存储桶检查/创建失败", e);
            throw new RuntimeException("MinIO存储桶操作失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有存储桶列表
     */
    public List<Bucket> listBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            log.error("MinIO获取存储桶列表失败", e);
            throw new RuntimeException("获取存储桶列表失败：" + e.getMessage());
        }
    }

    // ========== 核心操作：文件上传 ==========
    /**
     * 上传文件到MinIO（MultipartFile方式，适配前端上传）
     * @param file 上传文件（支持doc/pdf/excel/ppt等）
     * @param dir  存储目录（如tech-manual/、product-doc/）
     * @return 文件在MinIO的存储路径（用于溯源/下载）
     */
    public String uploadFile(MultipartFile file, String dir) {
        // 1. 前置校验
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小超过限制（最大500MB）");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        try {
            // 2. 生成唯一文件名（避免覆盖，格式：UUID.后缀）
            String ext = FilenameUtils.getExtension(originalFilename);
            String fileName = UUID.randomUUID().toString() + "." + ext;
            String objectName = dir.endsWith("/") ? dir + fileName : dir + "/" + fileName; // 兼容目录结尾无/

            // 3. 上传文件（指定ContentType，适配预览）
            PutObjectArgs putArgs = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), MAX_FILE_SIZE) // 指定分片大小
                    .contentType(file.getContentType())
                    .build();
            minioClient.putObject(putArgs);

            log.info("文件【{}】上传成功，存储路径：{}", originalFilename, objectName);
            return objectName;
        } catch (Exception e) {
            log.error("MinIO文件上传失败，文件名：{}", originalFilename, e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传文件（InputStream方式，适配本地/网络文件）
     * @param inputStream 文件流
     * @param fileName    文件名（含后缀）
     * @param dir         存储目录
     * @return 存储路径
     */
    public String uploadFile(InputStream inputStream, String fileName, String dir) {
        if (inputStream == null) {
            throw new IllegalArgumentException("文件流不能为空");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        try {
            String objectName = dir.endsWith("/") ? dir + fileName : dir + "/" + fileName;
            PutObjectArgs putArgs = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(inputStream, inputStream.available(), MAX_FILE_SIZE)
                    .build();
            minioClient.putObject(putArgs);

            log.info("文件流上传成功，存储路径：{}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("MinIO文件流上传失败，文件名：{}", fileName, e);
            throw new RuntimeException("文件流上传失败：" + e.getMessage());
        }
    }

    // ========== 核心操作：文件下载 ==========
    /**
     * 下载文件（返回InputStream，适配任意下载场景）
     * @param objectName 文件存储路径
     * @return 文件输入流（使用后需关闭）
     */
    public InputStream downloadFile(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            throw new IllegalArgumentException("文件存储路径不能为空");
        }

        try {
            GetObjectArgs getArgs = GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build();
            return minioClient.getObject(getArgs);
        } catch (Exception e) {
            log.error("MinIO文件下载失败，路径：{}", objectName, e);
            throw new RuntimeException("文件下载失败：" + e.getMessage());
        }
    }

    // ========== 核心操作：获取文件预览URL（知识库溯源核心） ==========
    /**
     * 获取文件预览URL（带过期时间，直接访问）
     * @param objectName 文件存储路径
     * @return 预览URL
     */
    public String getFilePreviewUrl(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            throw new IllegalArgumentException("文件存储路径不能为空");
        }

        try {
            GetPresignedObjectUrlArgs urlArgs = GetPresignedObjectUrlArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(minioConfig.getFileUrlExpire())
                    .build();
            return minioClient.getPresignedObjectUrl(urlArgs);
        } catch (Exception e) {
            log.error("MinIO获取预览URL失败，路径：{}", objectName, e);
            throw new RuntimeException("获取预览URL失败：" + e.getMessage());
        }
    }

    // ========== 核心操作：文件删除 ==========
    /**
     * 删除MinIO中的文件（同步删除知识库关联数据）
     * @param objectName 文件存储路径
     */
    public void deleteFile(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            throw new IllegalArgumentException("文件存储路径不能为空");
        }

        try {
            RemoveObjectArgs removeArgs = RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build();
            minioClient.removeObject(removeArgs);

            log.info("MinIO文件删除成功，路径：{}", objectName);
        } catch (Exception e) {
            log.error("MinIO文件删除失败，路径：{}", objectName, e);
            throw new RuntimeException("文件删除失败：" + e.getMessage());
        }
    }

}
