package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto)
    {
        dishService.saveWithFlavors(dishDto);
        return R.success("成功加入");
    }

    /**
     * 菜品信息分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */


    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name)
    {
        Page<Dish> dishPage=new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage=new Page<>();

        LambdaQueryWrapper<Dish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name);
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        dishService.page(dishPage,lambdaQueryWrapper);

        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");

        List<Dish> records=dishPage.getRecords();

        List<DishDto> DTOrecords=records.stream().map(item->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId= item.getCategoryId();
            Category category=categoryService.getById(categoryId);
            String categoryName=category.getName();
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(DTOrecords);
        return R.success(dishDtoPage);
    }






    @GetMapping("/{id}")
    public R<DishDto> update(@PathVariable Long id)
    {

        DishDto dishDto=dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto)
    {
        dishService.updateWithFlavors(dishDto);

        return R.success("成功加入");
    }

    @PostMapping("/status/{status}")
    public R<String> cancel(@PathVariable int  status,Long [] ids)
    {
        for (Long id:ids)
        {
            Dish dish=dishService.getById(id);
            dish.setStatus(status);
            dishService.updateById(dish);

        }
        return R.success("停售成功");
    }

    @DeleteMapping
    public R<String> delete(Long [] ids)
    {
        for(Long id:ids)
        {
            dishService.removeById(id);
        }
        return R.success("删除成功");

    }

//    @GetMapping("/list")
//    public R<List<Dish>> listDish(Dish dish)
//    {
//        LambdaQueryWrapper<Dish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        lambdaQueryWrapper.eq(Dish::getStatus,1);
//        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> dishList=dishService.list(lambdaQueryWrapper);
//        return R.success(dishList);
//    }

    @GetMapping("/list")
    public R<List<DishDto>> listDish(Dish dish)
    {
        LambdaQueryWrapper<Dish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        lambdaQueryWrapper.eq(Dish::getStatus,1);
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList=dishService.list(lambdaQueryWrapper);

        List<DishDto> dishDtoList=dishList.stream().map(item-> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            Long dishID = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            lambdaQueryWrapper1.eq(DishFlavor::getDishId, dishID);
            List<DishFlavor> dishFlavorList=dishFlavorService.list(lambdaQueryWrapper1);
            dishDto.setFlavors(dishFlavorList);

            Long categoryId=item.getCategoryId();
            Category category=categoryService.getById(categoryId);
            if(category!=null)
            {
                String categoryName=category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }
//@GetMapping("/list")
//public R<List<DishDto>> list(Dish dish) {
//
//    //构造查询条件
//    LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//    //添加条件，查询状态为1的（起售状态）
//    lambdaQueryWrapper.eq(Dish::getStatus, 1);
//    lambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
//    //条件排序条件
//    lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//    List<Dish> list = dishService.list(lambdaQueryWrapper);
//
//    List<DishDto> dishDtoList = list.stream().map((item) -> {
//        DishDto dishDto = new DishDto();
//
//        BeanUtils.copyProperties(item, dishDto);
//        Long categoryId = item.getCategoryId();
//        //根据id查分类对象
//        Category category = categoryService.getById(categoryId);
//        if (category != null) {
//            String categoryName = category.getName();
//            dishDto.setCategoryName(categoryName);
//        }
//
//        //当前菜品id
//        Long dishId = item.getId();
//        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(DishFlavor::getDishId, dishId);
//        //SQL: select* from dishflavor where dish_id=?;
//        List<DishFlavor> dishFlavorlist = dishFlavorService.list(queryWrapper);
//        dishDto.setFlavors(dishFlavorlist);
//        return dishDto;
//    }).collect(Collectors.toList());
//
//    return R.success(dishDtoList);
//}




}
