package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.controller.user.DishController;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealMapper setmealMapper;


    /**
     * 新增菜品
     * @param dto
     */
    @Transient
    public void saveWithFlavor(DishDTO dto) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dto,dish);
        dishMapper.insert(dish);
        Long id = dish.getId();
        List<DishFlavor> flavors = dto.getFlavors();
        //如果没有口味数据直接返回
        if(flavors == null || flavors.size() == 0){return;}
        flavors.forEach(dishFlavor -> dishFlavor.setDishId(id));
        dishFlavorMapper.insertBatch(flavors);

    }

    @Override
    public PageResult queryPage(DishPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(),dto.getPageSize());
        Page<DishVO> page = dishFlavorMapper.pageQuery();
        return new PageResult(page.getTotal(),page.getResult());
    }

    //批量删除数据，需要事务的支持
    @Override
    @Transient
    public void deleteBatch(List<Long> ids) {
        //如果是启销售中不能删除
        ids.forEach(id->
            {
                Dish  dish = dishMapper.getById(id);
                //如果是启销售中的不能删除
                if(dish.getStatus() == StatusConstant.ENABLE){
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
                }
            });
        //如果是关联了套餐不能删除
        List<Long> stemealIds = setmealMapper.getSetmealIdsByDishIds(ids);
        if(stemealIds != null && stemealIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
        }
        //可以删除
        //代码优化，这样多次操作数据库，性能低下，改成批量删除
        /*ids.forEach(id->
            {
                dishMapper.deleteById(id);
                //再删除可味关联表中的数据
                dishFlavorMapper.deleteByDishId(id);
            });*/
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);

    }


    @Override
    public DishVO getByIdWithFlavor(Long id) {
        DishVO dishVO = new DishVO();
        Dish dish = dishMapper.getById(id);
        BeanUtils.copyProperties(dish,dishVO);
        List<DishFlavor> flavors = dishFlavorMapper.getFlavorByDishId(id);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    @Transient
    @Override
    public void updateWithFlavor(DishDTO dto) {
        //更新菜品基本数据表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dto,dish);
        dishMapper.update(dish);
        //口味表需要先进行删除再进行更新
        dishFlavorMapper.deleteByDishId(dto.getId());
        //更新口味数据
        List<DishFlavor> flavors = dto.getFlavors();
        if(flavors == null || flavors.size() == 0){return;}
        flavors.forEach(dishFlavor -> dishFlavor.setDishId(dto.getId()));
        dishFlavorMapper.insertBatch(flavors);

    }


    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getFlavorByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        dishMapper.updateStatus(id,status);
    }

}
