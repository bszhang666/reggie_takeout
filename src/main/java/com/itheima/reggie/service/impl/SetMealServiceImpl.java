package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomerException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetMealMapper;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetMealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetMealServiceImpl extends ServiceImpl<SetMealMapper, Setmeal> implements SetMealService {

    @Autowired
    private SetMealDishService setMealDishService;

    @Override
    @Transactional
    public void saveWithDish(SetmealDto setMealDto) {
        this.save(setMealDto);
        Long setMealId=setMealDto.getId();
        List<SetmealDish> setmealDisheList =setMealDto.getSetmealDishes();
        setmealDisheList = setmealDisheList.stream().map(item->{
            item.setSetmealId(setMealId);
            return item;
        }).collect(Collectors.toList());

        setMealDishService.saveBatch(setmealDisheList);
    }

    @Override
    public void removeWithDish(Long[] ids) {
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper1=new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.in(Setmeal::getId,ids);
        lambdaQueryWrapper1.eq(Setmeal::getStatus,1);
        int count=this.count(lambdaQueryWrapper1);
        if(count>0)
        {
            throw new CustomerException("正在售卖，无法删除");
        }

        for(Long id:ids)
        {
            Setmeal setmeal=this.getById(id);
            this.removeById(id);

            LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(SetmealDish::getDishId,id);
            setMealDishService.remove(lambdaQueryWrapper);
        }
    }

    @Override
    public SetmealDto getByIdWithDishes(Long id) {
        SetmealDto setmealDto=new SetmealDto();
        Setmeal setmeal=this.getById(id);
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> setmealDishList=setMealDishService.list(lambdaQueryWrapper);
        BeanUtils.copyProperties(setmeal,setmealDto);
        setmealDto.setSetmealDishes(setmealDishList);
        return setmealDto;

    }

    @Override
    public void updateWithDishes(SetmealDto setmealDto) {
        this.updateById(setmealDto);

        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper=new LambdaQueryWrapper<>();//删除掉
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setMealDishService.remove(setmealDishLambdaQueryWrapper);

        List<SetmealDish>  setmealDishList=setmealDto.getSetmealDishes();//再添加
        setmealDishList=setmealDishList.stream().map(item->{//给每一个setmealDIsh手动附上一个if
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setMealDishService.saveBatch(setmealDishList);

    }
}
