package com.hxq.smart_campus.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.ClientBuilderConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


@Component
@Slf4j
public class AliOSSUtils {
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;
    
    private OSS ossClient;

    /**
     * 初始化OSS客户端
     */
    @PostConstruct
    public void init() {
        try {
            ClientBuilderConfiguration config = new ClientBuilderConfiguration();
            config.setConnectionTimeout(30000); // 连接超时时间，单位毫秒
            config.setSocketTimeout(30000); // socket超时时间，单位毫秒
            config.setMaxErrorRetry(3); // 最大错误重试次数
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret, config);
            log.info("OSS客户端初始化成功");
        } catch (Exception e) {
            log.error("OSS客户端初始化失败", e);
        }
    }

    /**
     * 销毁OSS客户端
     */
    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            try {
                ossClient.shutdown();
                log.info("OSS客户端销毁成功");
            } catch (Exception e) {
                log.error("OSS客户端销毁失败", e);
            }
        }
    }

    /**
     * 实现上传图片到OSS
     */
    public String upload(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        
        if (ossClient == null) {
            throw new RuntimeException("OSS客户端未初始化");
        }
        
        InputStream inputStream = null;
        try {
            // 获取上传的文件的输入流
            inputStream = multipartFile.getInputStream();

            // 避免文件覆盖
            String originalFilename = multipartFile.getOriginalFilename();
            if (originalFilename == null || originalFilename.lastIndexOf(".") == -1) {
                throw new IllegalArgumentException("文件名格式不正确");
            }
            String fileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));

            //上传文件到 OSS
            ossClient.putObject(bucketName, fileName, inputStream);

            //文件访问路径
            String url = endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + fileName;

            log.info("文件上传成功，URL: {}", url);
            return url;// 把上传到oss的路径返回
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        } finally {
            // 关闭输入流
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭输入流失败", e);
                }
            }
        }
    }
    
    /**
     * 实现从OSS下载文件
     */
    public InputStream download(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        if (ossClient == null) {
            throw new RuntimeException("OSS客户端未初始化");
        }
        
        try {
            // 获取文件输入流
            InputStream inputStream = ossClient.getObject(bucketName, fileName).getObjectContent();
            log.info("文件下载成功，文件名: {}", fileName);
            return inputStream;
        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw new RuntimeException("文件下载失败：" + e.getMessage());
        }
    }
    
    /**
     * 实现从OSS删除文件
     */
    public boolean delete(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        if (ossClient == null) {
            throw new RuntimeException("OSS客户端未初始化");
        }
        
        try {
            ossClient.deleteObject(bucketName, fileName);
            log.info("文件删除成功，文件名: {}", fileName);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new RuntimeException("文件删除失败：" + e.getMessage());
        }
    }
}
