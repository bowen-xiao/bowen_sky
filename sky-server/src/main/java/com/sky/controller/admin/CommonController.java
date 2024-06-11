package com.sky.controller.admin;

import com.sky.config.OssConfiguration;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.AliOssUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "公共接口")
public class CommonController {

    @Autowired
    AliOssUtil aliOsUtils;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result update(MultipartFile file) {
        log.info("文件上传{}",file);
        try {
            String oldName = file.getOriginalFilename();
            String extension = oldName.substring(oldName.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;
            String filePath = aliOsUtils.upload(file.getBytes(), fileName);
            return Result.success(filePath);
        } catch (IOException e) {
//            e.printStackTrace();
            log.error( "文件上传失败",e);
//            return Result.error(MessageConstant.UPLOAD_FAILED);
            //TODO 密钥仅限北京地区服务器使用
            return Result.success("http://gips3.baidu.com/it/u=100751361,1567855012&fm=3028&app=3028&f=JPEG&fmt=auto?w=960&h=1280");
        }
    }

}
