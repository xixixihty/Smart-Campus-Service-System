package com.hxq.smart_campus.controller.admin;

import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.utils.AliOSSUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@Slf4j
@Tag(name = "文件管理")
@RequiredArgsConstructor
public class FileController {
    private final AliOSSUtils aliOSSUtils;

    /**
     * 上传图片到阿里云OSS
     * @param file 上传的文件
     * @return 图片访问URL
     */
    @PostMapping("/upload")
    @Operation(summary = "上传图片")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("上传图片，文件名：{}，文件大小：{}", file.getOriginalFilename(), file.getSize());
        
        String url = aliOSSUtils.upload(file);
        log.info("图片上传成功，URL：{}", url);
        
        return Result.success(url);
    }
}
