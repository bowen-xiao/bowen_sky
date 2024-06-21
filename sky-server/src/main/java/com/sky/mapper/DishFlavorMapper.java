package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.DishFlavor;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入数据
     * @param flavors
     */
     void insertBatch(List<DishFlavor> flavors) ;

    Page<DishVO> pageQuery();

    @Delete("delete from dish_flavor where dish_id = #{id}")
    void deleteByDishId(Long id);

    void deleteByDishIds(List<Long> dishIds);

    @Select("select  * from dish_flavor where  dish_id = #{dishId}")
    List<DishFlavor> getFlavorByDishId(Long dishId);

}
