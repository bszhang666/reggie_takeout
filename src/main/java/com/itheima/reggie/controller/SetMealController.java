package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetMealController {
    @Autowired
    private SetMealService setMealService;

    @Autowired
    private SetMealDishService setMealDishService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name)
    {
        Page<Setmeal> setmealPage=new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage=new Page<>();

        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(StringUtils.isNotEmpty(name),Setmeal::getName,name);
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setMealService.page(setmealPage,lambdaQueryWrapper);

        BeanUtils.copyProperties(setmealPage,setmealDtoPage);

        List <Setmeal> setmealList=setmealPage.getRecords();

        List<SetmealDto> setmealDtoList=setmealList.stream().map(item->{
            SetmealDto setmealDto=new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            Long categoryId=item.getCategoryId();
            Category category=categoryService.getById(categoryId);
            String categoryName=category.getName();
            setmealDto.setCategoryName(categoryName);
            return setmealDto;
        }).collect(Collectors.toList());
        setmealDtoPage.setRecords(setmealDtoList);

        return R.success(setmealDtoPage);

    }

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setMealDto)
    {
        setMealService.saveWithDish(setMealDto);
        return R.success("??????????????????");
    }

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status,Long [] ids)
    {
        for(Long id:ids)
        {
            Setmeal setmeal=setMealService.getById(id);
            setmeal.setStatus(status);
            setMealService.updateById(setmeal);
        }
        return R.success("????????????");
    }

    @DeleteMapping
    public R<String> delete(Long [] ids)
    {
        setMealService.removeWithDish(ids);

        return R.success("????????????");
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id)
    {
        SetmealDto setmealDto=setMealService.getByIdWithDishes(id);
        return R.success(setmealDto);

    }

    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto)//??????????????????setmealdto  ????????????
    {
        setMealService.updateWithDishes(setmealDto);
        return R.success("????????????");
    }

    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setMealService.list(queryWrapper);
        return R.success(list);
    }







}
