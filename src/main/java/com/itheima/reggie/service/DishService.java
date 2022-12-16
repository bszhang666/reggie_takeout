package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import org.springframework.stereotype.Service;

@Service
public interface DishService extends IService<Dish> {
    public void saveWithFlavors(DishDto dishDto);
    public DishDto getByIdWithFlavor(Long id);
    public void updateWithFlavors(DishDto dishDto);
}
