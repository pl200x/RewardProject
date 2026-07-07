package com.example.Music_management.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    /**
     * 浏览器可达的公网地址。容器化部署时,服务在内网用 minio:9000 读写,
     * 而预签名 URL 是给浏览器用的且签名包含主机名,必须用公网地址签发。
     * 本地开发不配置,回落到 endpoint(此时内外网是同一个地址)。
     */
    @Value("${minio.public-endpoint:}")
    private String publicEndpoint;

    /** 服务端读写用(statObject / putObject):内网地址 */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 预签名专用:显式固定 region 后签名是纯本地计算。
     * 不固定的话 SDK 会先向 endpoint 发 bucket region 查询——而公网地址在容器内不可达。
     */
    @Bean
    public MinioClient minioPresignClient() {
        String ep = publicEndpoint == null || publicEndpoint.isEmpty() ? endpoint : publicEndpoint;
        return MinioClient.builder()
                .endpoint(ep)
                .region("us-east-1")
                .credentials(accessKey, secretKey)
                .build();
    }
}
