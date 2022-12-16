package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    @Transactional
    public void saveWithFlavors(DishDto dishDto) {
        this.save(dishDto);
        Long dishId=dishDto.getId();
        List<DishFlavor> flavorList=dishDto.getFlavors();
        flavorList=flavorList.stream().map((item)->
        {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavorList);
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {//根据菜品查询菜品信息和口味信息
        DishDto dishDto=new DishDto();
        Dish dish=this.getById(id);//使用this就相当于与这个的service调用方法
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> dishFlavorList=dishFlavorService.list(lambdaQueryWrapper);
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(dishFlavorList);
        return dishDto;

    }

    /**
     * 更新菜品信息，同时更新口味信息
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavors(DishDto dishDto) {//注意，如果是新加入的，需要重新设定dishId
        //更新dish表
        this.updateById(dishDto);
        //删除原有dishflavor表
        Long dishId= dishDto.getId();
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
        dishFlavorService.remove(lambdaQueryWrapper);
        //加入更新的dishflavor表
        List<DishFlavor> dishFlavorList=dishDto.getFlavors();
        dishFlavorList=dishFlavorList.stream().map(item->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(dishFlavorList);
    }




}
