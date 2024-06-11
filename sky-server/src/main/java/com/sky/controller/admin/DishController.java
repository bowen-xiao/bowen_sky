package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 添加菜品
     * @param dto
     * @return
     */
    @PostMapping
    @ApiOperation("添加菜品")
    public Result<String> save(@RequestBody DishDTO dto){
        log.info("添加菜品：{}", dto);
        dishService.saveWithFlavor(dto);
        return Result.success();
    }

}
