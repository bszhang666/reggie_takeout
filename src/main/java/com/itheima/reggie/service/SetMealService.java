package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import org.springframework.stereotype.Service;

@Service
public interface SetMealService extends IService<Setmeal> {
    public void saveWithDish(SetmealDto setMealDto);//保存套餐信息和菜品的关联信息
    public void removeWithDish(Long [] ids);
    public SetmealDto getByIdWithDishes(Long id);
    public void updateWithDishes(SetmealDto setmealDto);
}
