package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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

    @Autowired
    RedisTemplate redisTemplate;

    final String CACHE_DISH_LIST = "dish_";

    /**
     * 添加菜品
     *
     * @param dto
     * @return
     */
    @PostMapping
    @ApiOperation("添加菜品")
    public Result<String> save(@RequestBody DishDTO dto) {
        log.info("添加菜品：{}", dto);
        dishService.saveWithFlavor(dto);
        //清理redis中的缓存数据
        clearCache(CACHE_DISH_LIST + dto.getCategoryId());
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> pageQuery(DishPageQueryDTO dto) {
        log.info("菜品分页查询：{}", dto);
        PageResult result = dishService.queryPage(dto);
        return Result.success(result);
    }

    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("菜品批量删除：{}", ids);
        dishService.deleteBatch(ids);
        //清理redis中的缓存数据
        clearCache(CACHE_DISH_LIST + "*");
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据ID查菜品：{}", id);
        return Result.success(dishService.getByIdWithFlavor(id));
    }


    @PutMapping
    @ApiOperation("更新菜品")
    public Result update(@RequestBody DishDTO dto) {
        log.info("更新菜品：{}", dto);
        dishService.updateWithFlavor(dto);
        //清理redis中的缓存数据
        clearCache(CACHE_DISH_LIST + "*");
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("更新菜品状态")
    public Result updateDishSatus(@PathVariable Integer status,Long id) {
        log.info("更新菜品：{}", id);
        dishService.updateStatus(id,status);
        //清理redis中的缓存数据
        clearCache(CACHE_DISH_LIST + "*");
        return Result.success();
    }


    /**
     * 更新redis 中的缓存数据与后台一致
     * @param pattern
     */
    private void clearCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId) {
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

}
