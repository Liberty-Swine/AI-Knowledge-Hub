package com.free.aiknowledgehub.config;

import io.minio.MinioClient;
import lombok.Data;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @Description MinIO 配置类：自动装配客户端，绑定配置文件参数
 * @Author: Liberty-Swine
 * @Date 2026/4/6 16:34
 */
@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioConfig {
    // 核心连接参数
    private String endpoint;
    private String accessKey;
    private String secretKey;
    // 存储桶参数
    private String bucketName;
    private Integer fileUrlExpire;
    // 超时/重试参数（新增）
    private Integer connectTimeout;
    private Integer writeTimeout;
    private Integer readTimeout;
    private Integer retryCount;

    /**
     * 自定义 OkHttpClient 配置
     */
    public OkHttpClient okHttpClientInit() {
        // 创建自定义 OkHttpClient
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)      // 连接超时
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)       // 写入超时（上传）
                .readTimeout(readTimeout, TimeUnit.SECONDS)        // 读取超时（下载）
                .callTimeout(120, TimeUnit.SECONDS)       // 整个请求超时（可选）
                .retryOnConnectionFailure(true)           // 启用连接失败自动重试（OkHttp 默认行为）
                .build();
        return httpClient;
    }

    /**
     * 装配 MinIO 客户端
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .httpClient(okHttpClientInit())
                .build();
    }

    //集群配置
//    @Bean
//    public MinioClient minioClient() {
//        return MinioClient.builder()
//                .endpoint("http://minio-node1:9000", "http://minio-node2:9000", "http://minio-node3:9000")
//                .credentials(accessKey, secretKey)
//                .build();
//    }
}
